package com.touchgrass.app.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.touchgrass.app.data.local.PreferencesRepository
import com.touchgrass.app.data.repository.BlockEventRepository
import com.touchgrass.app.domain.PauseManager
import com.touchgrass.app.domain.QuickPeekManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * The engine. The OS calls [onAccessibilityEvent] for every UI event in any app on our
 * `packageNames` allowlist (see `res/xml/accessibility_service_config.xml`). For each event we
 *
 *  1. record a heartbeat (so the watchdog knows we're alive — spec §4.6),
 *  2. route to the [Detector] registered for that package,
 *  3. if the detector says "short-form feed", debounce, then apply the [BlockingStrategy].
 *
 * Note for .NET devs: a Service ≈ a Windows Service. AccessibilityService is constructed by the
 * OS, not by Hilt — `@AndroidEntryPoint` does field injection *after* `onCreate()` so all
 * `lateinit` injected fields are usable from `onServiceConnected` onward.
 */
@AndroidEntryPoint
class TouchgrassAccessibilityService : AccessibilityService() {
    @Inject lateinit var detectors: Set<@JvmSuppressWildcards Detector>

    @Inject lateinit var blockingStrategy: BlockingStrategy

    @Inject lateinit var debouncer: EventDebouncer

    @Inject lateinit var heartbeat: Heartbeat

    @Inject lateinit var blockEventRepository: BlockEventRepository

    @Inject lateinit var pauseManager: PauseManager

    @Inject lateinit var quickPeekManager: QuickPeekManager

    @Inject lateinit var preferencesRepository: PreferencesRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Snapshots of the user's enabled state, kept fresh by collectors started in
    // [onServiceConnected]. Volatile so the OS-thread read in [onAccessibilityEvent]
    // observes the latest write from the IO-thread collector without a lock.
    @Volatile private var touchgrassEnabled: Boolean = true

    @Volatile private var enabledPackages: Set<String> = emptySet()

    private val detectorsByPackage: Map<String, Detector> by lazy {
        buildMap {
            for (detector in detectors) {
                for (pkg in detector.packageNames) {
                    val previous = put(pkg, detector)
                    if (previous != null) {
                        Timber.w(
                            "package %s claimed by both %s and %s — last writer wins",
                            pkg,
                            previous::class.simpleName,
                            detector::class.simpleName,
                        )
                    }
                }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Timber.i("TouchgrassAccessibilityService connected with %d detectors", detectorsByPackage.size)
        scope.launch {
            preferencesRepository.touchgrassEnabled.collectLatest { touchgrassEnabled = it }
        }
        scope.launch {
            preferencesRepository.enabledPackages.collectLatest { enabledPackages = it }
        }
    }

    // Guard-clause dispatch (many early returns) plus a deliberate catch-all below: one
    // detector throwing must never crash the service and disable blocking for every app.
    @Suppress("ReturnCount", "TooGenericExceptionCaught")
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val packageName = event.packageName?.toString() ?: return

        scope.launch {
            runCatching { heartbeat.beat() }
                .onFailure { Timber.w(it, "heartbeat write failed") }
        }

        // Hot-path short-circuits. Order matters: cheapest reads first. Each is a single
        // volatile read or a Set membership check — no IO, no allocations on the event path.
        // Heartbeat above still fires so the watchdog can distinguish "user turned us off"
        // from "service is dead".
        if (!touchgrassEnabled) return
        if (packageName !in enabledPackages) return
        if (pauseManager.isPausedNow()) return

        val detector = detectorsByPackage[packageName] ?: return

        val detection =
            try {
                detector.detect(event, rootInActiveWindow)
            } catch (t: Throwable) {
                // Detector implementations are isolated; a bug in one must not crash the whole service.
                // Without this guard, a single broken detector could disable blocking for every app.
                Timber.e(t, "detector %s threw on event from %s", detector::class.simpleName, packageName)
                return
            }

        if (detection is Detection.ShortFormFeed) {
            val debounceKey = "$packageName:${detection.surface}"
            if (!debouncer.shouldBlock(debounceKey)) return

            // Quick Peek (spec §3.1.E): grant one free pass per package per session. The peek is
            // silent — not recorded in stats and not counted toward the daily pause budget. It's
            // an act of agency, not a doomscroll.
            if (quickPeekManager.checkAndConsumeQuickPeek(packageName)) {
                Timber.i("quick peek allowed for pkg=%s surface=%s", packageName, detection.surface)
                return
            }

            scope.launch {
                Timber.i("blocking surface=%s pkg=%s", detection.surface, packageName)
                // Record FIRST so we don't lose the attempt if the strategy throws.
                runCatching { blockEventRepository.record(packageName, detection.surface) }
                    .onFailure { Timber.e(it, "stats record failed for surface=%s", detection.surface) }
                runCatching { blockingStrategy.apply(this@TouchgrassAccessibilityService, detection.surface) }
                    .onFailure { Timber.e(it, "blocking strategy failed for surface=%s", detection.surface) }
            }
        }
    }

    override fun onInterrupt() {
        Timber.w("TouchgrassAccessibilityService interrupted by system")
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}

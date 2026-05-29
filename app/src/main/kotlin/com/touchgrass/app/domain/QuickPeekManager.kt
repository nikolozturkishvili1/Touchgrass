package com.touchgrass.app.domain

import com.touchgrass.app.data.local.PauseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Quick Peek (spec §3.1.E, §6.2): when enabled, the user is allowed to watch **one** reel/short
 * per package per session before regular blocking resumes. A "session" ends when the user
 * switches to a different package (the foreground app changes from inside the
 * AccessibilityService's allowlist).
 *
 * This V1 heuristic doesn't try to distinguish "opened from DM" vs "tapped the Reels tab" —
 * either gets one free pass. The spec describes the DM case as the motivating scenario, but the
 * resulting behavior is the same from the user's standpoint ("I get one peek").
 *
 * Read path is the AccessibilityService hot path — must stay non-suspending. `quickPeekEnabled`
 * is hot-cached as a volatile field; per-package consumption is a [ConcurrentHashMap].
 *
 * State is in-memory and lost on process death by design — that's the whole point of "one peek
 * per session".
 */
@Singleton
class QuickPeekManager @Inject constructor(
    pauseRepository: PauseRepository,
    @ApplicationScope appScope: CoroutineScope,
) {
    @Volatile
    private var quickPeekEnabled: Boolean = false

    private val peekConsumed = ConcurrentHashMap<String, Boolean>()

    @Volatile
    private var lastObservedPackage: String? = null

    init {
        appScope.launch {
            pauseRepository.quickPeekEnabledFlow.collect { enabled ->
                quickPeekEnabled = enabled
                // Turning Quick Peek off wipes any pending session — turning it back on later
                // shouldn't accidentally honor a stale "I already used my peek" flag.
                if (!enabled) peekConsumed.clear()
            }
        }
    }

    /**
     * If Quick Peek is enabled and the user hasn't yet consumed a peek for [packageName] this
     * session, grant the peek and return `true` (caller skips the block). Otherwise return `false`.
     *
     * Also performs the session-reset bookkeeping: when the observed package changes, peek
     * consumption for the prior package is cleared.
     */
    fun checkAndConsumeQuickPeek(packageName: String): Boolean {
        val previous = lastObservedPackage
        if (previous != null && previous != packageName) {
            peekConsumed.remove(previous)
        }
        lastObservedPackage = packageName

        if (!quickPeekEnabled) return false
        if (peekConsumed[packageName] == true) return false

        peekConsumed[packageName] = true
        return true
    }
}

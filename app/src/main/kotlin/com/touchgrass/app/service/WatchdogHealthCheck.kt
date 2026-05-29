package com.touchgrass.app.service

import androidx.annotation.VisibleForTesting
import com.touchgrass.app.accessibility.Heartbeat
import com.touchgrass.app.util.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure health-check logic for the watchdog (spec §4.6). Extracted from [WatchdogWorker] so it
 * can be unit-tested without WorkManager test infrastructure.
 *
 * Decision tree:
 *  1. If the user has not enabled the AccessibilityService: [WatchdogHealth.AccessibilityNotEnabled].
 *  2. Else if the service has never written a heartbeat: [WatchdogHealth.NeverBeaten].
 *  3. Else if the heartbeat is older than [stalenessThresholdMs]: [WatchdogHealth.Stale].
 *  4. Else: [WatchdogHealth.Healthy].
 */
@Singleton
class WatchdogHealthCheck @Inject constructor(
    private val heartbeat: Heartbeat,
    private val accessibilityEnablementCheck: AccessibilityEnablementCheck,
    private val clock: Clock,
) {
    /**
     * Six hours. During active use Touchgrass beats hundreds of times per day; a six-hour gap
     * during waking hours means the service is genuinely dead. Long enough to avoid false
     * positives during the user's sleep cycle.
     */
    @VisibleForTesting
    internal var stalenessThresholdMs: Long = DEFAULT_STALENESS_MS

    suspend fun check(): WatchdogHealth {
        if (!accessibilityEnablementCheck.isEnabled()) return WatchdogHealth.AccessibilityNotEnabled
        val lastBeat = heartbeat.lastBeatElapsedMillis() ?: return WatchdogHealth.NeverBeaten
        val gap = clock.elapsedMillis() - lastBeat
        return if (gap > stalenessThresholdMs) WatchdogHealth.Stale(gap) else WatchdogHealth.Healthy
    }

    companion object {
        const val DEFAULT_STALENESS_MS: Long = 6L * 60L * 60L * 1_000L
    }
}

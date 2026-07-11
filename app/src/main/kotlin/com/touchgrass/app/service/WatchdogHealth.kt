package com.touchgrass.app.service

/**
 * Result of one [WatchdogHealthCheck] pass. Sealed for exhaustive `when` matching.
 *
 * Note what is deliberately ABSENT: a "heartbeat too old" state. Heartbeats only advance when
 * the service receives events, and events only come from the blocked apps — so a stale
 * heartbeat usually just means the user hasn't opened a reel in hours. That's success, not
 * failure. Liveness comes from the binding flag instead ([AccessibilityServiceLiveness]).
 */
sealed interface WatchdogHealth {
    data object Healthy : WatchdogHealth

    /** The user (or the OEM's battery manager) turned the service off in system Settings. */
    data object AccessibilityNotEnabled : WatchdogHealth

    /**
     * Enabled in Settings, but the OS does not currently hold a binding to our service —
     * the process was killed (OEM battery manager, force stop, crash) and not rebound.
     *
     * @property sinceLastBeatMs diagnostic only: elapsed ms since the service last processed
     *   an event, or `null` if unknown (never beaten this boot, or heartbeat predates boot).
     */
    data class ServiceNotConnected(
        val sinceLastBeatMs: Long?,
    ) : WatchdogHealth
}

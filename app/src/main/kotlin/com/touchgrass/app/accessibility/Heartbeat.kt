package com.touchgrass.app.accessibility

/**
 * Diagnostic activity signal from the [TouchgrassAccessibilityService] (spec §4.6).
 *
 * The service calls [beat] on every event it processes. NOTE: this is NOT a liveness signal —
 * events only arrive from the blocked apps, so hours without a beat usually just means the
 * user didn't open a reel (which is the goal!). Liveness is the service's binding flag
 * ([com.touchgrass.app.service.AccessibilityServiceLiveness]); the watchdog only uses the
 * heartbeat age as diagnostic context when the service is already known to be dead.
 *
 * Implementation is DataStore-backed so it survives process death.
 */
interface Heartbeat {
    /** Record that the service just handled an event. Cheap; safe to call on every event. */
    suspend fun beat()

    /**
     * Read the last heartbeat as a monotonic elapsed-since-boot timestamp.
     *
     * @return `null` if the service has never beaten. Values persist across reboots while the
     *   elapsed clock resets, so callers must treat "stored > now" as unknown, not negative.
     */
    suspend fun lastBeatElapsedMillis(): Long?
}

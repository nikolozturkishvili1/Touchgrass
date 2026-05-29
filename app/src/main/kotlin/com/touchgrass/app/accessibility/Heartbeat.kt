package com.touchgrass.app.accessibility

/**
 * Liveness signal from the [TouchgrassAccessibilityService] (spec §4.6).
 *
 * The service calls [beat] on every event it processes. The watchdog (a WorkManager job — coming
 * in Week 4) reads [lastBeatElapsedMillis] and, if too old during waking hours, fires the
 * "Touchgrass stopped working — tap to fix" notification.
 *
 * Implementation is DataStore-backed so it survives process death.
 */
interface Heartbeat {
    /** Record that the service just handled an event. Cheap; safe to call on every event. */
    suspend fun beat()

    /**
     * Read the last heartbeat as a monotonic elapsed-since-boot timestamp.
     *
     * @return `null` if the service has never beaten on this boot (possibly killed, possibly
     *   never started). The watchdog should treat `null` as "needs attention" only if the user
     *   has finished onboarding.
     */
    suspend fun lastBeatElapsedMillis(): Long?
}

package com.touchgrass.app.service

/**
 * Result of one [WatchdogHealthCheck] pass. Sealed for exhaustive `when` matching.
 *
 * The non-[Healthy] variants all trigger the same user-facing alert today, but we keep them
 * distinct so future copy can be tailored ("Accessibility was turned off" vs "Touchgrass crashed").
 */
sealed interface WatchdogHealth {
    data object Healthy : WatchdogHealth

    data object AccessibilityNotEnabled : WatchdogHealth

    data object NeverBeaten : WatchdogHealth

    data class Stale(
        val sinceLastBeatMs: Long,
    ) : WatchdogHealth
}

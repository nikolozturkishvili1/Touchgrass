package com.touchgrass.app.accessibility

import android.accessibilityservice.AccessibilityService

/**
 * How Touchgrass kicks the user out of a detected feed (spec §4.4).
 *
 * V1 ships [BackPressBlockingStrategy] (Strategy 1 in the spec). [OverlayBlockingStrategy] and
 * [QuickPeekBlockingStrategy] are V1 stretch / Plus-tier features.
 *
 * Implementations must be safe to call from the AccessibilityService thread.
 */
interface BlockingStrategy {
    /**
     * Apply the block.
     *
     * @param service the live AccessibilityService — used for [AccessibilityService.performGlobalAction]
     *   and (for overlays) for the [android.view.WindowManager] obtained from it.
     * @param surface the detected surface identifier (e.g. `"youtube-shorts"`), so strategies can
     *   tailor copy or telemetry locally.
     */
    suspend fun apply(
        service: AccessibilityService,
        surface: String,
    )
}

package com.touchgrass.app.accessibility

import android.accessibilityservice.AccessibilityService
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Default V1 blocking strategy: simulate the system Back gesture (spec §4.4, Strategy 1).
 *
 * This is the most reliable and least intrusive option — it works across all target apps without
 * requiring overlay permissions, and the user remains in control. The downside, addressed by the
 * [EventDebouncer], is that an aggressive user can spam-tap back into the feed; the debouncer
 * absorbs that.
 */
@Singleton
class BackPressBlockingStrategy @Inject constructor() : BlockingStrategy {
    override suspend fun apply(service: AccessibilityService, surface: String) {
        val performed = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        Timber.i("BackPress block applied for surface=%s success=%s", surface, performed)
    }
}

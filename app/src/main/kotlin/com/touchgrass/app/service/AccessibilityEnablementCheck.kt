package com.touchgrass.app.service

import android.content.Context
import android.provider.Settings
import com.touchgrass.app.accessibility.TouchgrassAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Checks whether the user has actually enabled the Touchgrass AccessibilityService in
 * system Settings (spec §4.6). This is the first thing the watchdog asks — if the answer is
 * "no" then a stale heartbeat is expected, not a failure.
 *
 * Implementation reads `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES`, which is a
 * colon-separated list of `ComponentName` strings of currently-enabled services.
 */
@Singleton
class AccessibilityEnablementCheck
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun isEnabled(): Boolean {
            val enabled =
                Settings.Secure
                    .getString(
                        context.contentResolver,
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    ).orEmpty()
            if (enabled.isEmpty()) return false

            val ourComponentSuffix = "/${TouchgrassAccessibilityService::class.java.name}"
            return enabled
                .split(':')
                .any {
                    it.endsWith(ourComponentSuffix) ||
                        it.contains(context.packageName) &&
                        it.endsWith(ourComponentSuffix)
                }
        }
    }

package com.touchgrass.app.service

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.touchgrass.app.accessibility.TouchgrassAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Checks whether the user has actually enabled the Touchgrass AccessibilityService in
 * system Settings (spec §4.6). This is the first thing the watchdog asks — if the answer is
 * "no" then a dead service is expected, not a failure — and it gates the home screen's
 * "Touchgrass needs the keys" state.
 *
 * Two sources, ComponentName-normalized:
 *
 *  1. [AccessibilityManager.getEnabledAccessibilityServiceList] — the framework's own view.
 *  2. `Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES` — a colon-separated list of flattened
 *     `ComponentName` strings.
 *
 * Entries in (2) may be written in SHORT form (`com.touchgrass.app/.accessibility.Foo`) or
 * FULL form (`com.touchgrass.app/com.touchgrass.app.accessibility.Foo`) depending on OEM and
 * OS version. Naive suffix/contains string matching mistakes one form for "not enabled",
 * which made the app wrongly re-ask for the permission. [ComponentName.unflattenFromString]
 * normalizes both forms, so we compare ComponentName equality instead of strings.
 */
@Singleton
class AccessibilityEnablementCheck
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun isEnabled(): Boolean {
            val expected = ComponentName(context, TouchgrassAccessibilityService::class.java)
            return enabledViaManager(expected) || enabledViaSecureSetting(expected)
        }

        private fun enabledViaManager(expected: ComponentName): Boolean {
            val manager =
                context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
                    ?: return false
            return manager
                .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                .any { ComponentName.unflattenFromString(it.id) == expected }
        }

        private fun enabledViaSecureSetting(expected: ComponentName): Boolean {
            val enabled =
                Settings.Secure
                    .getString(
                        context.contentResolver,
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                    ).orEmpty()
            if (enabled.isEmpty()) return false

            return enabled
                .split(':')
                .any { entry -> ComponentName.unflattenFromString(entry.trim()) == expected }
        }
    }

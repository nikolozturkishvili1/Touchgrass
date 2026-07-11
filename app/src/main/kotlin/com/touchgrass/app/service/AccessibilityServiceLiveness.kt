package com.touchgrass.app.service

import com.touchgrass.app.accessibility.TouchgrassAccessibilityService
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Injectable view of the AccessibilityService's live binding state (spec §4.6).
 *
 * Exists so [WatchdogHealthCheck] can be unit-tested — the companion-object flag on
 * [TouchgrassAccessibilityService] is process-global static state, which mocks poorly.
 *
 * `isConnected() == true` means the OS currently holds an active binding to our service in
 * THIS process. It is authoritative for "alive right now": the flag dies with the process,
 * so a One UI deep-sleep kill or crash makes it false, while a user who simply hasn't opened
 * Instagram all day leaves it true.
 */
@Singleton
class AccessibilityServiceLiveness
    @Inject
    constructor() {
        fun isConnected(): Boolean = TouchgrassAccessibilityService.isConnected
    }

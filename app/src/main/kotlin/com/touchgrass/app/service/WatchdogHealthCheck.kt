package com.touchgrass.app.service

import com.touchgrass.app.accessibility.Heartbeat
import com.touchgrass.app.util.Clock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pure health-check logic for the watchdog (spec §4.6). Extracted from [WatchdogWorker] so it
 * can be unit-tested without WorkManager test infrastructure.
 *
 * Decision tree:
 *  1. If the service is not enabled in system Settings: [WatchdogHealth.AccessibilityNotEnabled].
 *  2. Else if the OS currently holds a binding to the service: [WatchdogHealth.Healthy] —
 *     regardless of heartbeat age. No events for hours just means the user didn't open a
 *     blocked app; alarming on that punished users for the exact behavior the app exists to
 *     encourage.
 *  3. Else: [WatchdogHealth.ServiceNotConnected] — enabled but not bound, i.e. killed and not
 *     rebound. Heartbeat age goes along as a diagnostic.
 */
@Singleton
class WatchdogHealthCheck
    @Inject
    constructor(
        private val heartbeat: Heartbeat,
        private val accessibilityEnablementCheck: AccessibilityEnablementCheck,
        private val serviceLiveness: AccessibilityServiceLiveness,
        private val clock: Clock,
    ) {
        suspend fun check(): WatchdogHealth {
            if (!accessibilityEnablementCheck.isEnabled()) return WatchdogHealth.AccessibilityNotEnabled
            if (serviceLiveness.isConnected()) return WatchdogHealth.Healthy

            // Diagnostic payload. `elapsedMillis` resets on reboot while DataStore persists,
            // so a negative gap means "heartbeat from a previous boot" — report as unknown.
            val sinceLastBeat =
                heartbeat.lastBeatElapsedMillis()?.let { last ->
                    (clock.elapsedMillis() - last).takeIf { it >= 0 }
                }
            return WatchdogHealth.ServiceNotConnected(sinceLastBeat)
        }
    }

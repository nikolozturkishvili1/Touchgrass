package com.touchgrass.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.content.getSystemService
import com.touchgrass.app.R

/**
 * Centralized notification-channel IDs and one-time registration.
 *
 * Channels are required on Android 8+ (API 26+); our min SDK is 29 so we don't need any
 * version guards. Channels must exist before any notification is posted, so [ensureRegistered]
 * runs from [com.touchgrass.app.TouchgrassApplication.onCreate].
 *
 * Two channels:
 *  - [FOREGROUND_ID]: LOW importance. The persistent "Touchgrass is on" notification. Users
 *    can mute it without losing the watchdog alerts because the watchdog uses its own channel.
 *  - [WATCHDOG_ID]: HIGH importance. Fires when the service has died — we want this to be
 *    impossible to miss because that's our entire wedge against the competitor.
 */
object NotificationChannels {
    const val FOREGROUND_ID: String = "touchgrass.foreground"
    const val WATCHDOG_ID: String = "touchgrass.watchdog"

    fun ensureRegistered(context: Context) {
        val nm = context.getSystemService<NotificationManager>() ?: return

        nm.createNotificationChannel(
            NotificationChannel(
                FOREGROUND_ID,
                context.getString(R.string.foreground_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = context.getString(R.string.foreground_notification_channel_description)
                setShowBadge(false)
                enableVibration(false)
                setSound(null, null)
            },
        )

        nm.createNotificationChannel(
            NotificationChannel(
                WATCHDOG_ID,
                context.getString(R.string.watchdog_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = context.getString(R.string.watchdog_notification_channel_description)
                setShowBadge(true)
                enableVibration(true)
            },
        )
    }
}

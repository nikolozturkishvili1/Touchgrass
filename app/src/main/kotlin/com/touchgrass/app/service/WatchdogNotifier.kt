package com.touchgrass.app.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.touchgrass.app.MainActivity
import com.touchgrass.app.R
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Posts the high-priority "Touchgrass stopped working — tap to fix" notification (spec §4.6).
 *
 * On Android 13+ the `POST_NOTIFICATIONS` runtime permission must be granted for the
 * notification to actually appear. If it isn't, `notify()` silently no-ops; the onboarding
 * flow is responsible for requesting it.
 */
@Singleton
class WatchdogNotifier
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        // `reason` is accepted for API symmetry and future per-reason copy; not surfaced yet.
        @Suppress("UnusedParameter")
        fun notifyUnhealthy(reason: WatchdogHealth) {
            val openAppIntent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    REQUEST_OPEN_APP,
                    openAppIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )

            val notification =
                NotificationCompat
                    .Builder(context, NotificationChannels.WATCHDOG_ID)
                    .setSmallIcon(R.drawable.ic_notification_touchgrass)
                    .setContentTitle(context.getString(R.string.watchdog_notification_title))
                    .setContentText(context.getString(R.string.watchdog_notification_text))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setCategory(NotificationCompat.CATEGORY_ERROR)
                    .build()

            runCatching {
                NotificationManagerCompat.from(context).notify(WATCHDOG_NOTIFICATION_ID, notification)
            }.onFailure {
                Timber.w(it, "watchdog notify failed (likely POST_NOTIFICATIONS not granted)")
            }
        }

        fun clear() {
            NotificationManagerCompat.from(context).cancel(WATCHDOG_NOTIFICATION_ID)
        }

        companion object {
            const val WATCHDOG_NOTIFICATION_ID: Int = 2001
            private const val REQUEST_OPEN_APP: Int = 200
        }
    }

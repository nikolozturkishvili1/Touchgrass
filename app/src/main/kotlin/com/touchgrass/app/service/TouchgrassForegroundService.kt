package com.touchgrass.app.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.touchgrass.app.MainActivity
import com.touchgrass.app.R
import com.touchgrass.app.domain.PauseManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Persistent foreground service whose primary job is to keep the process alive (spec §4.6).
 * Secondarily, it now also reflects pause state in its notification so users see "Touchgrass is
 * paused — 14:32 left" instead of the static "Touchgrass is on" copy.
 *
 * The countdown is driven by Android's built-in chronometer
 * (`setUsesChronometer + setChronometerCountDown + setWhen(endTimeMs)`) — the OS ticks the
 * notification text for us once per second with zero ongoing CPU on our side.
 *
 * Android 14+ requires the FGS type to be declared at `startForeground()` time. We use
 * `specialUse` because no other built-in type fits (we're not a media player, downloader,
 * health tracker, location reporter, etc.). The `PROPERTY_SPECIAL_USE_FGS_SUBTYPE` value in
 * AndroidManifest.xml documents the subtype for Play Store review.
 */
@AndroidEntryPoint
class TouchgrassForegroundService : Service() {
    @Inject lateinit var pauseManager: PauseManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // Initial notification reflects current pause state (zero if not paused).
        val initialNotification = buildNotification(pauseEndsAtMs = pauseManager.pausedUntilMs.value)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                initialNotification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
            )
        } else {
            startForeground(NOTIFICATION_ID, initialNotification)
        }
        Timber.i("TouchgrassForegroundService started")

        // Re-render the notification whenever pause state changes. The chronometer ticks the
        // displayed time without our involvement; we just need to rebuild when the *end time*
        // changes (i.e. start / cancel / expire).
        serviceScope.launch {
            pauseManager.pausedUntilMs.collect { pauseEndsAtMs ->
                val updated = buildNotification(pauseEndsAtMs)
                runCatching {
                    NotificationManagerCompat
                        .from(this@TouchgrassForegroundService)
                        .notify(NOTIFICATION_ID, updated)
                }.onFailure { Timber.w(it, "failed to update foreground notification") }
            }
        }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        // START_STICKY asks the OS to recreate us if it kills the process. Part of the
        // belt-and-braces reliability strategy.
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        Timber.i("TouchgrassForegroundService destroyed")
        super.onDestroy()
    }

    private fun buildNotification(pauseEndsAtMs: Long): Notification {
        val isPaused = pauseEndsAtMs > System.currentTimeMillis()

        val openAppIntent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                REQUEST_OPEN_APP,
                openAppIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )

        val builder =
            NotificationCompat
                .Builder(this, NotificationChannels.FOREGROUND_ID)
                .setSmallIcon(R.drawable.ic_notification_touchgrass)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)

        if (isPaused) {
            builder
                .setContentTitle(getString(R.string.foreground_notification_title_paused))
                .setContentText(getString(R.string.foreground_notification_text_paused))
                .setUsesChronometer(true)
                .setChronometerCountDown(true)
                .setWhen(pauseEndsAtMs)
                .setShowWhen(true)
        } else {
            builder
                .setContentTitle(getString(R.string.foreground_notification_title))
                .setContentText(getString(R.string.foreground_notification_text))
                .setUsesChronometer(false)
                .setShowWhen(false)
        }

        return builder.build()
    }

    companion object {
        const val NOTIFICATION_ID: Int = 1001
        private const val REQUEST_OPEN_APP: Int = 100
    }
}

package com.touchgrass.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.touchgrass.app.service.NotificationChannels
import com.touchgrass.app.service.WatchdogScheduler
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * Touchgrass `Application` entry point.
 *
 * C# analogy: this is the equivalent of `Program.Main` + composition root in a .NET app.
 * `@HiltAndroidApp` wires up Hilt's DI graph (akin to `services.AddSingleton<...>` lifecycle).
 *
 * Responsibilities:
 *  - Bootstrap logging (Timber).
 *  - Provide Hilt-aware [Configuration] so WorkManager-driven workers (e.g. the watchdog,
 *    spec §4.6) can be constructor-injected.
 */
@HiltAndroidApp
class TouchgrassApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Inject lateinit var watchdogScheduler: WatchdogScheduler

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        // Release builds intentionally have no log tree. Per the brand promise, no analytics SDK
        // is installed here in V1 (spec §11.3). If Sentry is later added, plant it in a release
        // tree gated on a runtime opt-in disclosed in the Trust Dashboard.

        // Notification channels must exist before any notification is posted. Safe to call on
        // every cold start — createNotificationChannel is idempotent.
        NotificationChannels.ensureRegistered(this)

        // Reliability layer (spec §4.6). KEEP policy means re-scheduling is a no-op if the work
        // is already pending, so calling this on every cold start is intentional.
        watchdogScheduler.scheduleIfNotScheduled()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(if (BuildConfig.DEBUG) android.util.Log.DEBUG else android.util.Log.ERROR)
            .build()
}

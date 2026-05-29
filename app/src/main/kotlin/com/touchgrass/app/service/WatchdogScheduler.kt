package com.touchgrass.app.service

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules the [WatchdogWorker] with WorkManager.
 *
 * `enqueueUniquePeriodicWork` with [ExistingPeriodicWorkPolicy.KEEP] means it's safe to call
 * [scheduleIfNotScheduled] every app launch — if it's already scheduled, WorkManager leaves
 * the existing one alone.
 *
 * Interval is the WorkManager minimum of 15 minutes. The watchdog is cheap enough that 15
 * minutes is fine.
 */
@Singleton
class WatchdogScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun scheduleIfNotScheduled() {
        val request = PeriodicWorkRequestBuilder<WatchdogWorker>(
            repeatInterval = WATCHDOG_INTERVAL_MINUTES,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
        Timber.i("watchdog scheduled every %d minutes", WATCHDOG_INTERVAL_MINUTES)
    }

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_NAME)
        Timber.i("watchdog cancelled")
    }

    companion object {
        const val UNIQUE_NAME: String = "touchgrass-watchdog"
        const val WATCHDOG_INTERVAL_MINUTES: Long = 15L
    }
}

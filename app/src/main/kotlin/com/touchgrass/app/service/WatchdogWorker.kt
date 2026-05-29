package com.touchgrass.app.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

/**
 * Periodic WorkManager job that decides whether the AccessibilityService is alive and fires the
 * "Touchgrass stopped working" notification if not (spec §4.6).
 *
 * Scheduled by [WatchdogScheduler] with a 15-minute repeat interval (the WorkManager minimum).
 *
 * `@HiltWorker` lets us constructor-inject dependencies via the [HiltWorkerFactory] wired up in
 * [com.touchgrass.app.TouchgrassApplication]. `@AssistedInject` is needed because the worker's
 * other two constructor params ([appContext] and [workerParams]) come from WorkManager itself,
 * not from the DI graph.
 */
@HiltWorker
class WatchdogWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
        private val healthCheck: WatchdogHealthCheck,
        private val notifier: WatchdogNotifier,
    ) : CoroutineWorker(appContext, workerParams) {
        override suspend fun doWork(): Result {
            val health =
                runCatching { healthCheck.check() }.getOrElse {
                    Timber.w(it, "watchdog health check failed; treating as success to avoid retry loop")
                    return Result.success()
                }

            when (health) {
                is WatchdogHealth.Healthy -> {
                    Timber.d("watchdog: healthy")
                    notifier.clear()
                }
                is WatchdogHealth.AccessibilityNotEnabled,
                is WatchdogHealth.NeverBeaten,
                is WatchdogHealth.Stale,
                -> {
                    Timber.i("watchdog: unhealthy (%s)", health)
                    notifier.notifyUnhealthy(health)
                }
            }

            return Result.success()
        }
    }

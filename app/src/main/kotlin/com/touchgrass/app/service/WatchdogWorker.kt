package com.touchgrass.app.service

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import timber.log.Timber

/**
 * Periodic WorkManager job that decides whether the AccessibilityService is alive and fires the
 * "Touchgrass stopped working" notification if not (spec §4.6).
 *
 * Scheduled by [WatchdogScheduler] with a 15-minute repeat interval (the WorkManager minimum).
 *
 * Rebind race: when the OS killed our process, this worker's own start is often what respawns
 * it — and the system re-binds the AccessibilityService a moment later. If we alerted on the
 * first "not connected" reading we'd cry wolf during that window, so we wait [REBIND_GRACE_MS]
 * and confirm before notifying.
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
            val health = checkSafely() ?: return Result.success()

            val confirmed =
                if (health is WatchdogHealth.ServiceNotConnected) {
                    delay(REBIND_GRACE_MS)
                    checkSafely() ?: return Result.success()
                } else {
                    health
                }

            when (confirmed) {
                is WatchdogHealth.Healthy -> {
                    Timber.d("watchdog: healthy")
                    notifier.clear()
                }
                is WatchdogHealth.AccessibilityNotEnabled,
                is WatchdogHealth.ServiceNotConnected,
                -> {
                    Timber.i("watchdog: unhealthy (%s)", confirmed)
                    notifier.notifyUnhealthy(confirmed)
                }
            }

            return Result.success()
        }

        private suspend fun checkSafely(): WatchdogHealth? =
            runCatching { healthCheck.check() }.getOrElse {
                Timber.w(it, "watchdog health check failed; treating as success to avoid retry loop")
                null
            }

        companion object {
            /** Long enough for the system to re-bind a freshly respawned service. */
            internal const val REBIND_GRACE_MS: Long = 15_000L
        }
    }

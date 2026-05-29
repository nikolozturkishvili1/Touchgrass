package com.touchgrass.app.domain

import com.touchgrass.app.data.local.PauseRepository
import com.touchgrass.app.util.Clock
import com.touchgrass.app.util.TimeBoundaries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Orchestrates pause state across [PauseRepository] (persistence), the AccessibilityService
 * ("should I skip blocking right now?"), and the UI (countdown rendering).
 *
 * Synchronous read path: [isPausedNow] is called by the AccessibilityService on every event,
 * so it must be cheap and non-suspending. We hot-cache the `pausedUntilMs` value via
 * [pausedUntilMs] (a `StateFlow` whose `.value` is `@Volatile`-equivalent). The cache is kept
 * in sync with disk by an init-time collector running on the injected [ApplicationScope].
 *
 * Budget accounting (spec §3.1.D): each successful `requestPause` charges its full duration
 * against today's budget *up front*, even if the user cancels early. This is deliberate —
 * canceling early shouldn't refund time, because the friction-vs-cancel decision already
 * happened.
 */
@Singleton
class PauseManager
    @Inject
    constructor(
        private val pauseRepository: PauseRepository,
        private val clock: Clock,
        @ApplicationScope appScope: CoroutineScope,
    ) {
        private val _pausedUntilMs = MutableStateFlow(0L)
        val pausedUntilMs: StateFlow<Long> = _pausedUntilMs.asStateFlow()

        init {
            // Hot-cache the disk value so isPausedNow() is non-suspending.
            appScope.launch {
                pauseRepository.pausedUntilMsFlow.collect { value ->
                    _pausedUntilMs.value = value
                }
            }
        }

        /** Cheap synchronous read for the AccessibilityService hot path. */
        fun isPausedNow(): Boolean = clock.nowMillis() < _pausedUntilMs.value

        /**
         * Try to start a pause of [durationMs] starting now.
         *
         * Pre-conditions checked in order:
         *  - No pause currently active. (`AlreadyPaused` if violated — cancel first.)
         *  - Today's remaining budget can cover [durationMs]. (`BudgetExceeded` if violated.)
         */
        suspend fun requestPause(durationMs: Long): PauseResult {
            if (durationMs <= 0L) return PauseResult.BudgetExceeded(remainingMsToday = remainingBudgetMsToday())
            if (isPausedNow()) return PauseResult.AlreadyPaused

            val remaining = remainingBudgetMsToday()
            if (durationMs > remaining) {
                return PauseResult.BudgetExceeded(remainingMsToday = remaining)
            }

            val now = clock.nowMillis()
            val endsAt = now + durationMs
            val todayStart = TimeBoundaries.startOfToday(now)

            pauseRepository.addBudgetConsumed(todayStart, durationMs)
            pauseRepository.setPausedUntilMs(endsAt)

            return PauseResult.Success(pauseEndsAtMs = endsAt)
        }

        /** Cancel any current pause. No budget refund — see class-level KDoc. */
        suspend fun cancelPause() {
            pauseRepository.setPausedUntilMs(0L)
        }

        suspend fun remainingBudgetMsToday(): Long {
            val today = TimeBoundaries.startOfToday(clock.nowMillis())
            val daily = pauseRepository.dailyBudgetMsFlow.first()
            val used = pauseRepository.budgetUsedTodayMs(today)
            return (daily - used).coerceAtLeast(0L)
        }
    }

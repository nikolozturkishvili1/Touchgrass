package com.touchgrass.app.domain

import com.touchgrass.app.data.local.PauseRepository
import com.touchgrass.app.util.Clock
import com.touchgrass.app.util.TimeBoundaries
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PauseManagerTest {

    private class FakeClock(var now: Long = 0L) : Clock {
        override fun nowMillis(): Long = now
        override fun elapsedMillis(): Long = now
    }

    private fun newManager(
        clock: FakeClock,
        pausedUntilMs: Long = 0L,
        dailyBudgetMs: Long = PauseRepository.DEFAULT_DAILY_BUDGET_MS,
        budgetUsedToday: Long = 0L,
    ): Pair<PauseManager, PauseRepository> {
        val repo = mockk<PauseRepository>(relaxUnitFun = true)
        every { repo.pausedUntilMsFlow } returns MutableStateFlow(pausedUntilMs)
        every { repo.dailyBudgetMsFlow } returns flowOf(dailyBudgetMs)
        coEvery { repo.budgetUsedTodayMs(any()) } returns budgetUsedToday
        val scope = CoroutineScope(UnconfinedTestDispatcher())
        val manager = PauseManager(repo, clock, scope)
        return manager to repo
    }

    @Test
    fun `isPausedNow is true when pausedUntilMs is in the future`() = runTest {
        val clock = FakeClock(now = 1_000L)
        val (manager, _) = newManager(clock, pausedUntilMs = 2_000L)

        assertTrue(manager.isPausedNow())
    }

    @Test
    fun `isPausedNow is false when pausedUntilMs equals now`() = runTest {
        val clock = FakeClock(now = 2_000L)
        val (manager, _) = newManager(clock, pausedUntilMs = 2_000L)

        assertEquals(false, manager.isPausedNow())
    }

    @Test
    fun `isPausedNow is false when pausedUntilMs is in the past`() = runTest {
        val clock = FakeClock(now = 5_000L)
        val (manager, _) = newManager(clock, pausedUntilMs = 2_000L)

        assertEquals(false, manager.isPausedNow())
    }

    @Test
    fun `requestPause succeeds and persists end time when budget allows`() = runTest {
        val clock = FakeClock(now = 10_000L)
        val (manager, repo) = newManager(clock, dailyBudgetMs = 60_000L, budgetUsedToday = 0L)

        val durationMs = 5_000L
        val result = manager.requestPause(durationMs)

        assertTrue(result is PauseResult.Success)
        assertEquals(15_000L, (result as PauseResult.Success).pauseEndsAtMs)

        coVerify { repo.addBudgetConsumed(TimeBoundaries.startOfToday(10_000L), durationMs) }
        coVerify { repo.setPausedUntilMs(15_000L) }
    }

    @Test
    fun `requestPause rejects when duration exceeds remaining budget`() = runTest {
        val clock = FakeClock(now = 10_000L)
        val (manager, _) = newManager(
            clock,
            dailyBudgetMs = 10_000L,
            budgetUsedToday = 8_000L, // only 2_000 left
        )

        val result = manager.requestPause(durationMs = 3_000L)

        assertTrue(result is PauseResult.BudgetExceeded)
        assertEquals(2_000L, (result as PauseResult.BudgetExceeded).remainingMsToday)
    }

    @Test
    fun `requestPause rejects when already paused`() = runTest {
        val clock = FakeClock(now = 1_000L)
        val (manager, _) = newManager(clock, pausedUntilMs = 5_000L, dailyBudgetMs = 60_000L)

        val result = manager.requestPause(durationMs = 1_000L)

        assertEquals(PauseResult.AlreadyPaused, result)
    }

    @Test
    fun `requestPause with zero duration is treated as a budget violation`() = runTest {
        val clock = FakeClock(now = 1_000L)
        val (manager, _) = newManager(clock, dailyBudgetMs = 60_000L)

        val result = manager.requestPause(durationMs = 0L)

        assertTrue(result is PauseResult.BudgetExceeded)
    }

    @Test
    fun `cancelPause writes zero to pausedUntilMs`() = runTest {
        val clock = FakeClock(now = 1_000L)
        val (manager, repo) = newManager(clock)

        manager.cancelPause()

        coVerify { repo.setPausedUntilMs(0L) }
    }

    @Test
    fun `remainingBudgetMsToday reflects daily budget minus used`() = runTest {
        val clock = FakeClock(now = 1_000L)
        val (manager, _) = newManager(
            clock,
            dailyBudgetMs = 20L * 60L * 1_000L,
            budgetUsedToday = 8L * 60L * 1_000L,
        )

        val remaining = manager.remainingBudgetMsToday()

        assertEquals(12L * 60L * 1_000L, remaining)
    }

    @Test
    fun `remainingBudgetMsToday clamps to zero when over budget`() = runTest {
        val clock = FakeClock(now = 1_000L)
        val (manager, _) = newManager(
            clock,
            dailyBudgetMs = 5_000L,
            budgetUsedToday = 9_000L,
        )

        val remaining = manager.remainingBudgetMsToday()

        assertEquals(0L, remaining)
    }
}

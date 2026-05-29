package com.touchgrass.app.service

import com.touchgrass.app.accessibility.Heartbeat
import com.touchgrass.app.util.Clock
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WatchdogHealthCheckTest {
    private class FakeClock(
        var elapsed: Long = 0L,
    ) : Clock {
        override fun nowMillis(): Long = elapsed

        override fun elapsedMillis(): Long = elapsed
    }

    private val heartbeat = mockk<Heartbeat>()
    private val enablement = mockk<AccessibilityEnablementCheck>()
    private val clock = FakeClock()

    private fun newCheck(stalenessMs: Long = WatchdogHealthCheck.DEFAULT_STALENESS_MS): WatchdogHealthCheck =
        WatchdogHealthCheck(heartbeat, enablement, clock).apply { stalenessThresholdMs = stalenessMs }

    @Test
    fun `accessibility disabled short-circuits to AccessibilityNotEnabled`() =
        runTest {
            every { enablement.isEnabled() } returns false

            val result = newCheck().check()

            assertEquals(WatchdogHealth.AccessibilityNotEnabled, result)
        }

    @Test
    fun `enabled but no heartbeat returns NeverBeaten`() =
        runTest {
            every { enablement.isEnabled() } returns true
            coEvery { heartbeat.lastBeatElapsedMillis() } returns null

            val result = newCheck().check()

            assertEquals(WatchdogHealth.NeverBeaten, result)
        }

    @Test
    fun `fresh heartbeat returns Healthy`() =
        runTest {
            every { enablement.isEnabled() } returns true
            coEvery { heartbeat.lastBeatElapsedMillis() } returns 1_000L
            clock.elapsed = 2_000L

            val result = newCheck(stalenessMs = 60_000L).check()

            assertEquals(WatchdogHealth.Healthy, result)
        }

    @Test
    fun `heartbeat exactly at the staleness threshold is still Healthy`() =
        runTest {
            every { enablement.isEnabled() } returns true
            coEvery { heartbeat.lastBeatElapsedMillis() } returns 0L
            clock.elapsed = 60_000L

            val result = newCheck(stalenessMs = 60_000L).check()

            assertEquals(WatchdogHealth.Healthy, result)
        }

    @Test
    fun `heartbeat one ms over the threshold returns Stale`() =
        runTest {
            every { enablement.isEnabled() } returns true
            coEvery { heartbeat.lastBeatElapsedMillis() } returns 0L
            clock.elapsed = 60_001L

            val result = newCheck(stalenessMs = 60_000L).check()

            assertTrue("expected Stale, got $result", result is WatchdogHealth.Stale)
            assertEquals(60_001L, (result as WatchdogHealth.Stale).sinceLastBeatMs)
        }

    @Test
    fun `default staleness threshold matches spec at 6 hours`() {
        assertEquals(6L * 60L * 60L * 1_000L, WatchdogHealthCheck.DEFAULT_STALENESS_MS)
    }
}

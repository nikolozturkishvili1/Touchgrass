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
    private val liveness = mockk<AccessibilityServiceLiveness>()
    private val clock = FakeClock()

    private fun newCheck(): WatchdogHealthCheck = WatchdogHealthCheck(heartbeat, enablement, liveness, clock)

    @Test
    fun `accessibility disabled short-circuits to AccessibilityNotEnabled`() =
        runTest {
            every { enablement.isEnabled() } returns false

            val result = newCheck().check()

            assertEquals(WatchdogHealth.AccessibilityNotEnabled, result)
        }

    @Test
    fun `enabled and connected is Healthy even with no heartbeat ever`() =
        runTest {
            every { enablement.isEnabled() } returns true
            every { liveness.isConnected() } returns true

            val result = newCheck().check()

            assertEquals(WatchdogHealth.Healthy, result)
        }

    @Test
    fun `enabled and connected is Healthy even with an ancient heartbeat`() =
        runTest {
            // The false-positive regression case: user simply hasn't opened a blocked app in
            // 8+ hours. Service is bound, so this must be Healthy — no "tap to fix" alarm.
            every { enablement.isEnabled() } returns true
            every { liveness.isConnected() } returns true
            coEvery { heartbeat.lastBeatElapsedMillis() } returns 0L
            clock.elapsed = 8L * 60L * 60L * 1_000L

            val result = newCheck().check()

            assertEquals(WatchdogHealth.Healthy, result)
        }

    @Test
    fun `enabled but not connected returns ServiceNotConnected with heartbeat age`() =
        runTest {
            every { enablement.isEnabled() } returns true
            every { liveness.isConnected() } returns false
            coEvery { heartbeat.lastBeatElapsedMillis() } returns 1_000L
            clock.elapsed = 61_000L

            val result = newCheck().check()

            assertTrue("expected ServiceNotConnected, got $result", result is WatchdogHealth.ServiceNotConnected)
            assertEquals(60_000L, (result as WatchdogHealth.ServiceNotConnected).sinceLastBeatMs)
        }

    @Test
    fun `not connected with no heartbeat ever reports null age`() =
        runTest {
            every { enablement.isEnabled() } returns true
            every { liveness.isConnected() } returns false
            coEvery { heartbeat.lastBeatElapsedMillis() } returns null

            val result = newCheck().check()

            assertEquals(WatchdogHealth.ServiceNotConnected(sinceLastBeatMs = null), result)
        }

    @Test
    fun `heartbeat from a previous boot reports null age instead of negative`() =
        runTest {
            // DataStore persists across reboots; elapsedRealtime resets. Stored beat can be
            // LARGER than current elapsed — must not surface a negative gap.
            every { enablement.isEnabled() } returns true
            every { liveness.isConnected() } returns false
            coEvery { heartbeat.lastBeatElapsedMillis() } returns 999_999L
            clock.elapsed = 10_000L

            val result = newCheck().check()

            assertEquals(WatchdogHealth.ServiceNotConnected(sinceLastBeatMs = null), result)
        }
}

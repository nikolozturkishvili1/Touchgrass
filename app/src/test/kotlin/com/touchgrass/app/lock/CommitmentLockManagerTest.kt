package com.touchgrass.app.lock

import com.touchgrass.app.util.Clock
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CommitmentLockManagerTest {
    private class FakeClock(
        var now: Long = 0L,
    ) : Clock {
        override fun nowMillis(): Long = now

        override fun elapsedMillis(): Long = now
    }

    private fun newManager(
        clock: FakeClock = FakeClock(),
        pending: LockRepository.PendingOtp = LockRepository.PendingOtp(null, null, null),
        savedEmail: String? = null,
        emailServiceResult: Result<Unit> = Result.success(Unit),
    ): Triple<CommitmentLockManager, LockRepository, EmailOtpService> {
        val repo = mockk<LockRepository>(relaxUnitFun = true)
        coEvery { repo.pendingOtp() } returns pending
        coEvery { repo.email() } returns savedEmail
        val service = mockk<EmailOtpService>()
        coEvery { service.sendOtp(any(), any()) } returns emailServiceResult
        return Triple(CommitmentLockManager(repo, service, clock), repo, service)
    }

    @Test
    fun `valid email regex accepts common addresses`() {
        listOf("a@b.co", "first.last+filter@sub.example.com", "x_y-z@example.io").forEach {
            assertTrue("expected $it valid", CommitmentLockManager.isValidEmail(it))
        }
    }

    @Test
    fun `valid email regex rejects malformed addresses`() {
        listOf("", "no-at-sign", "@no-local.com", "no-domain@", "a@b", "a b@example.com").forEach {
            assertFalse("expected $it invalid", CommitmentLockManager.isValidEmail(it))
        }
    }

    @Test
    fun `sendOtp rejects invalid email without hitting the service`() =
        runTest {
            val (manager, _, service) = newManager()

            val result = manager.sendOtp("not-an-email")

            assertEquals(SendOtpResult.InvalidEmail, result)
            coVerify(exactly = 0) { service.sendOtp(any(), any()) }
        }

    @Test
    fun `sendOtp enforces cooldown when last send was under 60 seconds ago`() =
        runTest {
            val clock = FakeClock(now = 100_000L)
            val (manager, _, service) =
                newManager(
                    clock = clock,
                    pending =
                        LockRepository.PendingOtp(
                            hash = "stale",
                            expiresAtMs = 200_000L,
                            lastSentAtMs = 50_000L, // 50s ago
                        ),
                )

            val result = manager.sendOtp("a@b.co")

            assertTrue(result is SendOtpResult.Cooldown)
            assertEquals(10_000L, (result as SendOtpResult.Cooldown).waitMs)
            coVerify(exactly = 0) { service.sendOtp(any(), any()) }
        }

    @Test
    fun `sendOtp allows send when cooldown has elapsed`() =
        runTest {
            val clock = FakeClock(now = 200_000L)
            val (manager, repo, service) =
                newManager(
                    clock = clock,
                    pending =
                        LockRepository.PendingOtp(
                            hash = "stale",
                            expiresAtMs = 100_000L,
                            lastSentAtMs = 100_000L, // 100s ago, > 60s cooldown
                        ),
                )

            val result = manager.sendOtp("a@b.co", Random(seed = 0))

            assertTrue(result is SendOtpResult.Sent)
            val expectedExpiry = 200_000L + CommitmentLockManager.OTP_TTL_MS
            assertEquals(expectedExpiry, (result as SendOtpResult.Sent).expiresAtMs)
            coVerify { repo.storePendingOtp(any(), expectedExpiry, 200_000L) }
            coVerify { service.sendOtp("a@b.co", any()) }
        }

    @Test
    fun `sendOtp returns DeliveryFailed but still stores the hash and cooldown`() =
        runTest {
            val clock = FakeClock(now = 1_000L)
            val (manager, repo, _) =
                newManager(
                    clock = clock,
                    emailServiceResult = Result.failure(java.io.IOException("boom")),
                )

            val result = manager.sendOtp("a@b.co")

            assertEquals(SendOtpResult.DeliveryFailed, result)
            // Important: we still persisted so the cooldown applies to retries.
            coVerify { repo.storePendingOtp(any(), any(), 1_000L) }
        }

    @Test
    fun `verifyOtp rejects non-numeric input`() =
        runTest {
            val (manager, _, _) = newManager()
            assertEquals(VerifyOtpResult.IncorrectCode, manager.verifyOtp("abc123"))
        }

    @Test
    fun `verifyOtp rejects wrong-length input`() =
        runTest {
            val (manager, _, _) = newManager()
            assertEquals(VerifyOtpResult.IncorrectCode, manager.verifyOtp("12345"))
            assertEquals(VerifyOtpResult.IncorrectCode, manager.verifyOtp("1234567"))
        }

    @Test
    fun `verifyOtp returns NoOtpPending when nothing was sent`() =
        runTest {
            val (manager, _, _) = newManager(pending = LockRepository.PendingOtp(null, null, null))
            assertEquals(VerifyOtpResult.NoOtpPending, manager.verifyOtp("123456"))
        }

    @Test
    fun `verifyOtp returns Expired and clears pending when expiry has passed`() =
        runTest {
            val clock = FakeClock(now = 1_000_000L)
            val (manager, repo, _) =
                newManager(
                    clock = clock,
                    pending =
                        LockRepository.PendingOtp(
                            hash = OtpHasher.hash("123456"),
                            expiresAtMs = 500_000L,
                            lastSentAtMs = 1L,
                        ),
                )

            assertEquals(VerifyOtpResult.Expired, manager.verifyOtp("123456"))
            coVerify { repo.clearPendingOtp() }
        }

    @Test
    fun `verifyOtp returns IncorrectCode when hash doesn't match`() =
        runTest {
            val clock = FakeClock(now = 100L)
            val (manager, _, _) =
                newManager(
                    clock = clock,
                    pending =
                        LockRepository.PendingOtp(
                            hash = OtpHasher.hash("123456"),
                            expiresAtMs = 1_000L,
                            lastSentAtMs = 0L,
                        ),
                )

            assertEquals(VerifyOtpResult.IncorrectCode, manager.verifyOtp("654321"))
        }

    @Test
    fun `verifyOtp returns Success and clears pending on matching hash`() =
        runTest {
            val clock = FakeClock(now = 100L)
            val (manager, repo, _) =
                newManager(
                    clock = clock,
                    pending =
                        LockRepository.PendingOtp(
                            hash = OtpHasher.hash("987654"),
                            expiresAtMs = 1_000L,
                            lastSentAtMs = 0L,
                        ),
                )

            assertEquals(VerifyOtpResult.Success, manager.verifyOtp("987654"))
            coVerify { repo.clearPendingOtp() }
        }

    @Test
    fun `enableLock persists email and flips the flag`() =
        runTest {
            val (manager, repo, _) = newManager()
            manager.enableLock("a@b.co")
            coVerify { repo.setEmail("a@b.co") }
            coVerify { repo.setLockEnabled(true) }
        }

    @Test
    fun `disableLock clears email by default`() =
        runTest {
            val (manager, repo, _) = newManager()
            manager.disableLock()
            coVerify { repo.setLockEnabled(false) }
            coVerify { repo.setEmail(null) }
        }

    @Test
    fun `disableLock keeps email when caller asks`() =
        runTest {
            val (manager, repo, _) = newManager()
            manager.disableLock(clearEmail = false)
            coVerify { repo.setLockEnabled(false) }
            coVerify(exactly = 0) { repo.setEmail(null) }
        }
}

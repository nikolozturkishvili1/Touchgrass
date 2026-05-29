package com.touchgrass.app.lock

import com.touchgrass.app.util.Clock
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Orchestrates the commitment-lock OTP gate (spec §3.1.D, §2.3 Paulo).
 *
 * Lifecycle of a single OTP:
 *  1. Caller invokes [sendOtp]. Generate, hash, persist, dispatch via [EmailOtpService].
 *  2. Caller (after user types code) invokes [verifyOtp].
 *  3. On success: clear pending OTP. Caller decides what to do next (enable lock / disable
 *     Touchgrass / disable lock / etc.).
 *
 * Two protections on [sendOtp] beyond plain delivery:
 *  - **Cooldown** ([SEND_COOLDOWN_MS]): 60s minimum between sends to prevent accidental or
 *    malicious flooding (Resend free tier has limits + this is the user's email inbox).
 *  - **Expiry** ([OTP_TTL_MS]): 5 min TTL on the stored hash; old OTPs cease to verify.
 *
 * [enableLock] / [disableLock] / [setEmail] are the high-level state mutators. They do NOT
 * gate themselves — callers verify OTP first, then call these.
 */
@Singleton
class CommitmentLockManager
    @Inject
    constructor(
        private val lockRepository: LockRepository,
        private val emailOtpService: EmailOtpService,
        private val clock: Clock,
    ) {
        val lockEnabled: Flow<Boolean> = lockRepository.lockEnabledFlow
        val email: Flow<String?> = lockRepository.emailFlow

        suspend fun lockEnabledNow(): Boolean = lockRepository.lockEnabled()

        suspend fun emailNow(): String? = lockRepository.email()

        /**
         * Generate a new OTP, persist its hash, dispatch via email.
         *
         * @param toEmail address to send to. Use the saved [email] for verification flows; use a
         *   freshly-typed address for enrollment.
         * @param random override only in tests.
         */
        suspend fun sendOtp(
            toEmail: String,
            random: Random = Random.Default,
        ): SendOtpResult {
            if (!isValidEmail(toEmail)) return SendOtpResult.InvalidEmail
            val now = clock.nowMillis()

            val pending = lockRepository.pendingOtp()
            val lastSent = pending.lastSentAtMs ?: 0L
            val sinceLastSent = now - lastSent
            if (lastSent != 0L && sinceLastSent < SEND_COOLDOWN_MS) {
                val waitMs = SEND_COOLDOWN_MS - sinceLastSent
                return SendOtpResult.Cooldown(waitMs)
            }

            val otp = OtpGenerator.next(random)
            val hash = OtpHasher.hash(otp)
            val expiresAt = now + OTP_TTL_MS

            // Persist BEFORE sending so a delivery failure still records the cooldown.
            lockRepository.storePendingOtp(hash = hash, expiresAtMs = expiresAt, sentAtMs = now)

            val sendResult = emailOtpService.sendOtp(toEmail, otp)
            return if (sendResult.isSuccess) {
                SendOtpResult.Sent(expiresAt)
            } else {
                Timber.w(sendResult.exceptionOrNull(), "OTP send failed for %s", toEmail)
                SendOtpResult.DeliveryFailed
            }
        }

        suspend fun verifyOtp(input: String): VerifyOtpResult {
            val normalized = input.trim()
            if (normalized.length != OtpGenerator.OTP_LENGTH || !normalized.all { it.isDigit() }) {
                return VerifyOtpResult.IncorrectCode
            }
            val pending = lockRepository.pendingOtp()
            val hash = pending.hash ?: return VerifyOtpResult.NoOtpPending
            val expiresAt = pending.expiresAtMs ?: return VerifyOtpResult.NoOtpPending
            if (clock.nowMillis() > expiresAt) {
                lockRepository.clearPendingOtp()
                return VerifyOtpResult.Expired
            }
            return if (OtpHasher.hash(normalized) == hash) {
                lockRepository.clearPendingOtp()
                VerifyOtpResult.Success
            } else {
                VerifyOtpResult.IncorrectCode
            }
        }

        /** Set the email and turn the lock on. Caller must have verified an OTP first. */
        suspend fun enableLock(email: String) {
            lockRepository.setEmail(email)
            lockRepository.setLockEnabled(true)
        }

        /** Turn the lock off. Caller must have verified an OTP first. */
        suspend fun disableLock(clearEmail: Boolean = true) {
            lockRepository.setLockEnabled(false)
            if (clearEmail) lockRepository.setEmail(null)
        }

        companion object {
            /** OTP validity window. 5 min matches industry norms for transactional codes. */
            const val OTP_TTL_MS: Long = 5L * 60L * 1_000L

            /** Minimum gap between consecutive [sendOtp] calls. Protects against flooding the inbox. */
            const val SEND_COOLDOWN_MS: Long = 60L * 1_000L

            private val EMAIL_REGEX =
                Regex(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
                )

            fun isValidEmail(email: String): Boolean = EMAIL_REGEX.matches(email.trim())
        }
    }

sealed interface SendOtpResult {
    data class Sent(
        val expiresAtMs: Long,
    ) : SendOtpResult

    data class Cooldown(
        val waitMs: Long,
    ) : SendOtpResult

    data object InvalidEmail : SendOtpResult

    data object DeliveryFailed : SendOtpResult
}

sealed interface VerifyOtpResult {
    data object Success : VerifyOtpResult

    data object IncorrectCode : VerifyOtpResult

    data object Expired : VerifyOtpResult

    data object NoOtpPending : VerifyOtpResult
}

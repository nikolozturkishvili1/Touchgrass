package com.touchgrass.app.lock

import kotlin.random.Random

/**
 * 6-digit numeric one-time code generator. Industry standard for transactional OTP; balances
 * security (~20 bits of entropy) against legibility on the device that receives the email.
 *
 * Security is provided by:
 *  - short expiry (5 min — [CommitmentLockManager.OTP_TTL_MS])
 *  - rate-limited resend (60s cooldown — [CommitmentLockManager.SEND_COOLDOWN_MS])
 *  - hashed-at-rest storage ([OtpHasher])
 *
 * NOT by entropy of the code itself. Don't extend to alphanumerics under the assumption that
 * adds meaningful security.
 */
object OtpGenerator {
    const val OTP_LENGTH: Int = 6
    private const val UPPER_BOUND = 1_000_000 // i.e. 10^OTP_LENGTH

    fun next(random: Random = Random.Default): String {
        val value = random.nextInt(0, UPPER_BOUND)
        return value.toString().padStart(OTP_LENGTH, '0')
    }
}

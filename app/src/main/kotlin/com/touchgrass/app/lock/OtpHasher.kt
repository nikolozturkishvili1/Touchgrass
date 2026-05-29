package com.touchgrass.app.lock

import java.security.MessageDigest

/**
 * SHA-256 hash of an OTP with a constant pepper. We hash so the OTP is never persisted in
 * plaintext on disk.
 *
 * Why pepper-only (no per-OTP salt)?
 *  - A 6-digit OTP has ~20 bits of entropy. Salting adds nothing — an attacker who can read
 *    DataStore can also brute-force 1M hashes in milliseconds. Salt protects passwords, which
 *    have higher entropy and longer lifetimes.
 *  - The real protection is the 5-min expiry and the 60s send cooldown. Hashing is just hygiene
 *    so the OTP doesn't sit in plaintext in `prefs.preferences_pb`.
 */
object OtpHasher {
    private const val PEPPER = "touchgrass.commitment-lock.v1"

    fun hash(otp: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest("$PEPPER:$otp".toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    fun matches(
        otp: String,
        hash: String,
    ): Boolean = hash(otp) == hash
}

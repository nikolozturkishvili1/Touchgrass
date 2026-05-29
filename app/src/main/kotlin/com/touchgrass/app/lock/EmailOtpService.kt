package com.touchgrass.app.lock

/**
 * Sends a one-time code to an email address. Two impls live alongside:
 *  - [ResendEmailOtpService] — production, hits the Resend API.
 *  - [FakeEmailOtpService] — dev/test, logs the OTP to Logcat.
 *
 * The Hilt provider picks between them based on whether `RESEND_API_KEY` is set in
 * `BuildConfig`. See [LockModule] and `app/build.gradle.kts`.
 */
interface EmailOtpService {
    suspend fun sendOtp(email: String, code: String): Result<Unit>
}

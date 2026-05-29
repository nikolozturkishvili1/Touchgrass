package com.touchgrass.app.lock

import timber.log.Timber

/**
 * Dev fallback when no `RESEND_API_KEY` is configured in `BuildConfig`. Logs the OTP to Logcat
 * so the developer can read it during local testing without setting up a Resend account.
 *
 * Never used in release builds with a real API key — see [LockModule].
 */
class FakeEmailOtpService : EmailOtpService {
    override suspend fun sendOtp(email: String, code: String): Result<Unit> {
        Timber.w(
            "[FakeEmailOtpService] would send OTP %s to %s — set RESEND_API_KEY in gradle.properties for real delivery",
            code,
            email,
        )
        return Result.success(Unit)
    }
}

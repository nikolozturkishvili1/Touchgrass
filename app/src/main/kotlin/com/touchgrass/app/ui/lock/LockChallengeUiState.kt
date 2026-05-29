package com.touchgrass.app.ui.lock

import com.touchgrass.app.lock.OtpGenerator

/**
 * State the [LockChallenge] Composable consumes. One Composable handles both enrollment
 * (collects a new email) and verification (uses the saved email) — the distinction is the
 * initial [LockChallengePhase].
 */
data class LockChallengeUiState(
    val phase: LockChallengePhase,
    val savedEmail: String? = null,
    val typedEmail: String = "",
    val typedOtp: String = "",
    val errorMessage: String? = null,
    val cooldownSecondsRemaining: Int = 0,
    val verified: Boolean = false,
    val sentToEmail: String? = null,
    val inFlight: Boolean = false,
) {
    val otpExpectedLength: Int = OtpGenerator.OTP_LENGTH
    val canVerify: Boolean = typedOtp.length == OtpGenerator.OTP_LENGTH
}

enum class LockChallengePhase {
    /** Enrollment-only: user is typing their email address. */
    NeedsEmail,

    /** OTP is sent (or about to be sent); show "We sent a code to ..." + 6-digit input. */
    AwaitingCode,

    /** Verification just succeeded; caller dismisses on the next render via LaunchedEffect. */
    Done,
}

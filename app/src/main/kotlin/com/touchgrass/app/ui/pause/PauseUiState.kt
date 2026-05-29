package com.touchgrass.app.ui.pause

import com.touchgrass.app.domain.FrictionMode

/**
 * The pause picker is a small state machine, not a screen graph — keeping it in one Composable
 * keeps nav simple and the back-stack clean (the user has only one back arrow to deal with).
 */
data class PauseUiState(
    val phase: PausePhase = PausePhase.PickDuration,
    val availableDurationsMs: List<Long> = DEFAULT_DURATIONS_MS,
    val selectedDurationMs: Long? = null,
    val frictionMode: FrictionMode = FrictionMode.WaitTimer,
    val remainingBudgetMsToday: Long = 0L,
    val lockEnabled: Boolean = false,
    val errorMessage: String? = null,
    val confirmed: Boolean = false,
) {
    companion object {
        /** 5 min / 15 min / 30 min — matches the V1 spec §3.1.D ("5min, 15min, 30min"). */
        val DEFAULT_DURATIONS_MS: List<Long> = listOf(
            5L * 60L * 1_000L,
            15L * 60L * 1_000L,
            30L * 60L * 1_000L,
        )
    }
}

enum class PausePhase {
    /** User is choosing how long to pause for. */
    PickDuration,

    /** OTP commitment-lock gate — only when [com.touchgrass.app.lock.CommitmentLockManager] says so. */
    LockGate,

    /** Friction gate (WaitTimer / Math / Code / Breathing) — applies if frictionMode != None. */
    Friction,

    /** Pause has started; show confirmation and bounce back to home. */
    Done,
}

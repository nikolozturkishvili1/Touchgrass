package com.touchgrass.app.ui.settings

import com.touchgrass.app.data.model.SupportedApp
import com.touchgrass.app.domain.FrictionMode

data class SettingsUiState(
    val pauseButtonVisible: Boolean = true,
    val dailyBudgetMinutes: Long = 20L,
    val frictionMode: FrictionMode = FrictionMode.WaitTimer,
    val quickPeekEnabled: Boolean = false,
    val lockEnabled: Boolean = false,
    val lockEmail: String? = null,
    val lockChallenge: LockChallengeMode = LockChallengeMode.None,
    /** How many of [SupportedApp.ALL] currently have *every* package variant in the enabled set. */
    val enabledAppsCount: Int = SupportedApp.ALL.size,
    val totalAppsCount: Int = SupportedApp.ALL.size,
) {
    companion object {
        /** Daily-budget presets surfaced as radio options. */
        val BUDGET_PRESETS_MINUTES: List<Long> = listOf(5L, 10L, 20L, 30L, 60L)
    }
}

/**
 * Whether — and why — Settings should overlay the [com.touchgrass.app.ui.lock.LockChallenge]
 * Composable right now.
 */
enum class LockChallengeMode {
    /** No overlay; show the regular Settings content. */
    None,

    /** Enrolling: user is turning the lock on. LockChallenge collects email + verifies. */
    Enrolling,

    /** Disenrolling: user is turning the lock off. LockChallenge verifies the saved email. */
    Disenrolling,
}

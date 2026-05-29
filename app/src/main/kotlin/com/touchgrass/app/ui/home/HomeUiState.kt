package com.touchgrass.app.ui.home

/**
 * Screen state the home Composable consumes (sealed for exhaustive `when`).
 *
 * Order of precedence (top-most wins):
 *  1. [Loading]: initial render before DataStore has emitted.
 *  2. [NeedsOnboarding]: user has not finished the first-run flow yet.
 *  3. [AccessibilityOff]: onboarded but the OS-level Accessibility toggle is off.
 *  4. [Off]: user toggled Touchgrass off at the app level.
 *  5. [On]: everything healthy; show today's stats.
 */
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object NeedsOnboarding : HomeUiState
    data object AccessibilityOff : HomeUiState
    data class On(
        val todaysBlockCount: Int,
        val pauseButtonVisible: Boolean,
    ) : HomeUiState

    /**
     * A pause is currently active. `pauseEndsAtMs` is wall-clock time; the Composable owns the
     * per-second countdown via a `LaunchedEffect` tick.
     */
    data class Paused(
        val pauseEndsAtMs: Long,
        val todaysBlockCount: Int,
    ) : HomeUiState

    data object Off : HomeUiState
}

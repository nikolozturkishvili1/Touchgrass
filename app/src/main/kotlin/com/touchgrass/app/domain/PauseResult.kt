package com.touchgrass.app.domain

/**
 * Outcome of a [PauseManager.requestPause] call. Sealed so callers' `when` arms are exhaustive.
 */
sealed interface PauseResult {
    /** Pause started successfully; expires at [pauseEndsAtMs] wall-clock time. */
    data class Success(val pauseEndsAtMs: Long) : PauseResult

    /**
     * Requested duration would exceed today's pause budget.
     * @param remainingMsToday how much pause time is still available today.
     */
    data class BudgetExceeded(val remainingMsToday: Long) : PauseResult

    /** A pause is already in progress; cancel it first. */
    data object AlreadyPaused : PauseResult
}

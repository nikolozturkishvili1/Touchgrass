package com.touchgrass.app.ui.pause

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchgrass.app.data.local.PauseRepository
import com.touchgrass.app.domain.FrictionMode
import com.touchgrass.app.domain.PauseManager
import com.touchgrass.app.domain.PauseResult
import com.touchgrass.app.lock.CommitmentLockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PauseViewModel
    @Inject
    constructor(
        private val pauseManager: PauseManager,
        private val pauseRepository: PauseRepository,
        private val commitmentLockManager: CommitmentLockManager,
    ) : ViewModel() {
        private val _state = MutableStateFlow(PauseUiState())
        val state: StateFlow<PauseUiState> = _state.asStateFlow()

        init {
            loadPreferences()
        }

        private fun loadPreferences() {
            viewModelScope.launch {
                _state.update {
                    it.copy(
                        frictionMode = pauseRepository.frictionModeFlow.first(),
                        remainingBudgetMsToday = pauseManager.remainingBudgetMsToday(),
                        lockEnabled = commitmentLockManager.lockEnabledNow(),
                    )
                }
            }
        }

        fun pickDuration(durationMs: Long) {
            _state.update { it.copy(selectedDurationMs = durationMs, errorMessage = null) }
            advanceFromPicker()
        }

        /**
         * Decide the next phase after a duration is picked. Order matters:
         *  1. If the commitment lock is on, require OTP first.
         *  2. Otherwise if a friction is configured, show it.
         *  3. Otherwise commit the pause directly.
         */
        private fun advanceFromPicker() {
            val s = _state.value
            when {
                s.lockEnabled -> _state.update { it.copy(phase = PausePhase.LockGate) }
                s.frictionMode == FrictionMode.None -> commitPause()
                else -> _state.update { it.copy(phase = PausePhase.Friction) }
            }
        }

        fun onLockChallengeVerified() {
            val s = _state.value
            if (s.frictionMode == FrictionMode.None) {
                commitPause()
            } else {
                _state.update { it.copy(phase = PausePhase.Friction) }
            }
        }

        fun onLockChallengeCancel() {
            _state.update { it.copy(phase = PausePhase.PickDuration, selectedDurationMs = null) }
        }

        fun onFrictionComplete() {
            commitPause()
        }

        fun onFrictionCancel() {
            _state.update { it.copy(phase = PausePhase.PickDuration, selectedDurationMs = null) }
        }

        private fun commitPause() {
            val duration = _state.value.selectedDurationMs ?: return
            viewModelScope.launch {
                when (val result = pauseManager.requestPause(duration)) {
                    is PauseResult.Success ->
                        _state.update { it.copy(phase = PausePhase.Done, confirmed = true) }

                    is PauseResult.BudgetExceeded -> {
                        val mins = result.remainingMsToday / 60_000L
                        _state.update {
                            it.copy(
                                phase = PausePhase.PickDuration,
                                errorMessage =
                                    if (mins <= 0) {
                                        "You've used your pause budget for today. Try again tomorrow."
                                    } else {
                                        "Only $mins min left in your daily pause budget."
                                    },
                                remainingBudgetMsToday = result.remainingMsToday,
                            )
                        }
                    }

                    PauseResult.AlreadyPaused -> {
                        Timber.w("requestPause returned AlreadyPaused — should not be reachable from picker")
                        _state.update {
                            it.copy(
                                phase = PausePhase.PickDuration,
                                errorMessage = "A pause is already in progress.",
                            )
                        }
                    }
                }
            }
        }
    }

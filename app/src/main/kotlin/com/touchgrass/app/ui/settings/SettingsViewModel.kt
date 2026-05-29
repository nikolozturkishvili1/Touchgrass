package com.touchgrass.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchgrass.app.data.local.PauseRepository
import com.touchgrass.app.data.local.PreferencesRepository
import com.touchgrass.app.data.model.SupportedApp
import com.touchgrass.app.domain.FrictionMode
import com.touchgrass.app.lock.CommitmentLockManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val pauseRepository: PauseRepository,
        private val commitmentLockManager: CommitmentLockManager,
        private val preferencesRepository: PreferencesRepository,
    ) : ViewModel() {
        private val challengeMode = MutableStateFlow(LockChallengeMode.None)

        private data class PauseInputs(
            val pauseVisible: Boolean,
            val dailyMs: Long,
            val frictionMode: FrictionMode,
            val quickPeekEnabled: Boolean,
        )

        private data class LockInputs(
            val lockEnabled: Boolean,
            val lockEmail: String?,
        )

        private val pause: Flow<PauseInputs> =
            combine(
                pauseRepository.pauseButtonVisibleFlow,
                pauseRepository.dailyBudgetMsFlow,
                pauseRepository.frictionModeFlow,
                pauseRepository.quickPeekEnabledFlow,
            ) { visible, dailyMs, mode, quickPeek ->
                PauseInputs(visible, dailyMs, mode, quickPeek)
            }

        private val lock: Flow<LockInputs> =
            combine(
                commitmentLockManager.lockEnabled,
                commitmentLockManager.email,
            ) { enabled, email -> LockInputs(enabled, email) }

        val uiState: StateFlow<SettingsUiState> =
            combine(
                pause,
                lock,
                challengeMode,
                preferencesRepository.enabledPackages,
            ) { p, l, mode, enabledPackages ->
                // A logical app counts as enabled only when every one of its package variants is in the
                // set — same semantics as the AppSelectionList row, so the "N of 7" count never lies.
                val enabledApps = SupportedApp.ALL.count { app -> enabledPackages.containsAll(app.packageNames) }
                SettingsUiState(
                    pauseButtonVisible = p.pauseVisible,
                    dailyBudgetMinutes = p.dailyMs / 60_000L,
                    frictionMode = p.frictionMode,
                    quickPeekEnabled = p.quickPeekEnabled,
                    lockEnabled = l.lockEnabled,
                    lockEmail = l.lockEmail,
                    lockChallenge = mode,
                    enabledAppsCount = enabledApps,
                    totalAppsCount = SupportedApp.ALL.size,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
                initialValue = SettingsUiState(),
            )

        fun setPauseButtonVisible(visible: Boolean) {
            viewModelScope.launch { pauseRepository.setPauseButtonVisible(visible) }
        }

        fun setDailyBudgetMinutes(minutes: Long) {
            viewModelScope.launch { pauseRepository.setDailyBudgetMs(minutes * 60_000L) }
        }

        fun setFrictionMode(mode: FrictionMode) {
            viewModelScope.launch { pauseRepository.setFrictionMode(mode) }
        }

        fun setQuickPeekEnabled(enabled: Boolean) {
            viewModelScope.launch { pauseRepository.setQuickPeekEnabled(enabled) }
        }

        fun beginLockEnrollment() {
            challengeMode.value = LockChallengeMode.Enrolling
        }

        fun beginLockDisenrollment() {
            if (!uiState.value.lockEnabled) return
            challengeMode.value = LockChallengeMode.Disenrolling
        }

        fun cancelLockChallenge() {
            challengeMode.value = LockChallengeMode.None
        }

        /**
         * Called by LockChallenge on a successful OTP verification. We then apply the side effect
         * (enable/disable lock) and dismiss the overlay.
         */
        fun onLockChallengeCompleted(email: String) {
            val mode = challengeMode.value
            viewModelScope.launch {
                when (mode) {
                    LockChallengeMode.Enrolling -> commitmentLockManager.enableLock(email)
                    LockChallengeMode.Disenrolling -> commitmentLockManager.disableLock(clearEmail = true)
                    LockChallengeMode.None -> Unit
                }
                challengeMode.value = LockChallengeMode.None
            }
        }

        private companion object {
            const val STATE_SHARING_TIMEOUT_MS = 5_000L
        }
    }

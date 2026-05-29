package com.touchgrass.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchgrass.app.data.local.PauseRepository
import com.touchgrass.app.data.local.PreferencesRepository
import com.touchgrass.app.data.repository.BlockEventRepository
import com.touchgrass.app.domain.PauseManager
import com.touchgrass.app.lock.CommitmentLockManager
import com.touchgrass.app.service.AccessibilityEnablementCheck
import com.touchgrass.app.service.ServiceLauncher
import com.touchgrass.app.util.Clock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Home-screen state.
 *
 * Composes:
 *  - persistent prefs ([PreferencesRepository]) for onboarding + user-level on/off
 *  - the live OS-level accessibility binding state ([AccessibilityEnablementCheck])
 *  - today's block count from [BlockEventRepository]
 *  - pause state ([PauseManager.pausedUntilMs] + [PauseRepository.pauseButtonVisibleFlow])
 *  - commitment-lock state ([CommitmentLockManager.lockEnabled])
 *
 * When the user taps "Turn off" while the commitment lock is enabled, [setEnabled] does NOT
 * immediately flip the preference. It surfaces a [pendingLockChallenge] flag instead; the
 * Composable overlays a [com.touchgrass.app.ui.lock.LockChallenge] which on verification calls
 * [confirmTurnOff].
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val accessibilityEnablementCheck: AccessibilityEnablementCheck,
    private val serviceLauncher: ServiceLauncher,
    private val pauseManager: PauseManager,
    private val commitmentLockManager: CommitmentLockManager,
    private val clock: Clock,
    blockEventRepository: BlockEventRepository,
    pauseRepository: PauseRepository,
) : ViewModel() {

    private val accessibilityEnabled = MutableStateFlow(false)
    private val _pendingLockChallenge = MutableStateFlow(false)
    val pendingLockChallenge: StateFlow<Boolean> = _pendingLockChallenge.asStateFlow()

    private data class StableInputs(
        val onboarded: Boolean,
        val enabledAtAppLevel: Boolean,
        val accessibilityOn: Boolean,
        val pauseButtonVisible: Boolean,
    )

    private val stable: Flow<StableInputs> = combine(
        preferencesRepository.onboardingComplete,
        preferencesRepository.touchgrassEnabled,
        accessibilityEnabled,
        pauseRepository.pauseButtonVisibleFlow,
    ) { onboarded, enabled, accessibilityOn, pauseVisible ->
        StableInputs(onboarded, enabled, accessibilityOn, pauseVisible)
    }

    val uiState: StateFlow<HomeUiState> = combine(
        stable,
        blockEventRepository.todayCountFlow(),
        pauseManager.pausedUntilMs,
    ) { s, todaysCount, pausedUntil ->
        when {
            !s.onboarded -> HomeUiState.NeedsOnboarding
            !s.accessibilityOn -> HomeUiState.AccessibilityOff
            !s.enabledAtAppLevel -> HomeUiState.Off
            clock.nowMillis() < pausedUntil -> HomeUiState.Paused(
                pauseEndsAtMs = pausedUntil,
                todaysBlockCount = todaysCount,
            )
            else -> HomeUiState.On(
                todaysBlockCount = todaysCount,
                pauseButtonVisible = s.pauseButtonVisible,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
        initialValue = HomeUiState.Loading,
    )

    init {
        refreshAccessibilityStatus()
    }

    /** Re-read the OS-level accessibility toggle. Cheap; call from `LifecycleResumeEffect`. */
    fun refreshAccessibilityStatus() {
        accessibilityEnabled.value = accessibilityEnablementCheck.isEnabled()
    }

    /**
     * Public entry point for the toggle.
     *
     * `enable=true` is always immediate. `enable=false` checks the commitment lock — if the
     * lock is on, we set [pendingLockChallenge] = true and let the Composable show the gate.
     * The actual turn-off happens via [confirmTurnOff] after a successful OTP verification.
     */
    fun setEnabled(enabled: Boolean) {
        if (enabled) {
            viewModelScope.launch {
                preferencesRepository.setTouchgrassEnabled(true)
                serviceLauncher.start()
            }
            return
        }
        viewModelScope.launch {
            if (commitmentLockManager.lockEnabledNow()) {
                _pendingLockChallenge.value = true
            } else {
                applyTurnOff()
            }
        }
    }

    fun cancelLockChallenge() {
        _pendingLockChallenge.value = false
    }

    fun confirmTurnOff() {
        viewModelScope.launch {
            applyTurnOff()
            _pendingLockChallenge.value = false
        }
    }

    private suspend fun applyTurnOff() {
        preferencesRepository.setTouchgrassEnabled(false)
        serviceLauncher.stop()
    }

    fun cancelPause() {
        viewModelScope.launch { pauseManager.cancelPause() }
    }

    private companion object {
        const val STATE_SHARING_TIMEOUT_MS = 5_000L
    }
}

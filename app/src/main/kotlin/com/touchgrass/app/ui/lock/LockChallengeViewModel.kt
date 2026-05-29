package com.touchgrass.app.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchgrass.app.lock.CommitmentLockManager
import com.touchgrass.app.lock.SendOtpResult
import com.touchgrass.app.lock.VerifyOtpResult
import com.touchgrass.app.util.Clock
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State + actions for the [LockChallenge] Composable.
 *
 * Re-used by both flows:
 *  - **Enrollment**: caller initialises with `requiresEmail = true`. Phase starts at `NeedsEmail`.
 *  - **Verification**: caller initialises with `requiresEmail = false`. Phase starts at
 *    `AwaitingCode`; we use the saved email and dispatch a send immediately.
 */
@HiltViewModel
class LockChallengeViewModel @Inject constructor(
    private val commitmentLockManager: CommitmentLockManager,
    private val clock: Clock,
) : ViewModel() {

    private val _state = MutableStateFlow(
        LockChallengeUiState(phase = LockChallengePhase.NeedsEmail),
    )
    val state: StateFlow<LockChallengeUiState> = _state.asStateFlow()

    /**
     * Configure the challenge. Call exactly once from a `LaunchedEffect(Unit)` in the host
     * Composable. Subsequent calls are ignored.
     */
    fun initialise(requiresEmail: Boolean) {
        viewModelScope.launch {
            val savedEmail = commitmentLockManager.emailNow()
            _state.update {
                it.copy(
                    phase = if (requiresEmail || savedEmail.isNullOrBlank()) LockChallengePhase.NeedsEmail
                    else LockChallengePhase.AwaitingCode,
                    savedEmail = savedEmail,
                )
            }
            if (!requiresEmail && !savedEmail.isNullOrBlank()) {
                // Auto-dispatch the OTP for the verification flow.
                sendOtpInternal(savedEmail)
            }
        }
    }

    fun setTypedEmail(email: String) {
        _state.update { it.copy(typedEmail = email, errorMessage = null) }
    }

    fun setTypedOtp(otp: String) {
        _state.update { it.copy(typedOtp = otp.filter { ch -> ch.isDigit() }.take(OTP_DISPLAY_LIMIT), errorMessage = null) }
    }

    fun sendCode() {
        val email = _state.value.typedEmail.takeIf { it.isNotBlank() }
            ?: _state.value.savedEmail
            ?: run {
                _state.update { it.copy(errorMessage = "Enter your email first.") }
                return
            }
        viewModelScope.launch { sendOtpInternal(email) }
    }

    fun verify() {
        if (_state.value.inFlight) return
        viewModelScope.launch {
            _state.update { it.copy(inFlight = true, errorMessage = null) }
            when (val result = commitmentLockManager.verifyOtp(_state.value.typedOtp)) {
                VerifyOtpResult.Success ->
                    _state.update { it.copy(phase = LockChallengePhase.Done, verified = true, inFlight = false) }

                VerifyOtpResult.IncorrectCode ->
                    _state.update { it.copy(inFlight = false, errorMessage = "That code doesn't match. Try again.") }

                VerifyOtpResult.Expired ->
                    _state.update {
                        it.copy(
                            inFlight = false,
                            errorMessage = "That code has expired. Tap Resend.",
                            typedOtp = "",
                        )
                    }

                VerifyOtpResult.NoOtpPending ->
                    _state.update {
                        it.copy(
                            inFlight = false,
                            errorMessage = "No code on file. Tap Send.",
                            typedOtp = "",
                        )
                    }

                else ->
                    _state.update { it.copy(inFlight = false, errorMessage = "Couldn't verify — try again.") }
            }
        }
    }

    private suspend fun sendOtpInternal(email: String) {
        _state.update { it.copy(inFlight = true, errorMessage = null) }
        when (val result = commitmentLockManager.sendOtp(email)) {
            is SendOtpResult.Sent -> {
                _state.update {
                    it.copy(
                        phase = LockChallengePhase.AwaitingCode,
                        sentToEmail = email,
                        inFlight = false,
                    )
                }
                startCooldownTicker()
            }

            is SendOtpResult.Cooldown -> {
                _state.update {
                    it.copy(
                        phase = LockChallengePhase.AwaitingCode,
                        sentToEmail = it.sentToEmail ?: email,
                        errorMessage = null,
                        cooldownSecondsRemaining = ((result.waitMs + 999L) / 1000L).toInt(),
                        inFlight = false,
                    )
                }
                startCooldownTicker()
            }

            SendOtpResult.InvalidEmail ->
                _state.update {
                    it.copy(
                        errorMessage = "That doesn't look like an email address.",
                        inFlight = false,
                    )
                }

            SendOtpResult.DeliveryFailed ->
                _state.update {
                    it.copy(
                        errorMessage = "Couldn't send right now. Check your connection and try again.",
                        inFlight = false,
                    )
                }
        }
    }

    private fun startCooldownTicker() {
        viewModelScope.launch {
            while (_state.value.cooldownSecondsRemaining > 0) {
                delay(1_000L)
                _state.update {
                    it.copy(cooldownSecondsRemaining = (it.cooldownSecondsRemaining - 1).coerceAtLeast(0))
                }
            }
        }
    }

    private companion object {
        // Display cap on typed OTP — guards against paste-bombs.
        const val OTP_DISPLAY_LIMIT = 6
    }
}

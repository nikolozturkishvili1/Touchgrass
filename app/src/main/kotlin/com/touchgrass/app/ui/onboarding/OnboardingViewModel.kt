package com.touchgrass.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchgrass.app.data.local.PreferencesRepository
import com.touchgrass.app.oem.OemDetector
import com.touchgrass.app.oem.OemRepository
import com.touchgrass.app.service.AccessibilityEnablementCheck
import com.touchgrass.app.service.ServiceLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * State machine for the first-run flow (spec §3.1.G, §6.1).
 *
 * Holds a single [OnboardingState] for the whole onboarding nav graph; each step Composable is
 * stateless and consumes this state. When the user reaches the final step and confirms, the
 * ViewModel writes [PreferencesRepository.setOnboardingComplete] and starts the foreground
 * service, then sets `finished = true` which the nav layer observes to navigate home.
 */
@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val preferencesRepository: PreferencesRepository,
        private val accessibilityEnablementCheck: AccessibilityEnablementCheck,
        private val oemDetector: OemDetector,
        private val oemRepository: OemRepository,
        private val serviceLauncher: ServiceLauncher,
    ) : ViewModel() {
        private val _state = MutableStateFlow(OnboardingState())
        val state: StateFlow<OnboardingState> = _state.asStateFlow()

        init {
            loadOemWalkthrough()
            refreshAccessibilityStatus()
        }

        private fun loadOemWalkthrough() {
            val oemId = oemDetector.detect()
            _state.update { it.copy(oemId = oemId) }
            viewModelScope.launch {
                val walkthrough = oemRepository.walkthroughFor(oemId)
                _state.update { it.copy(walkthrough = walkthrough) }
            }
        }

        /** Re-check the OS-level Accessibility toggle. Call from `LifecycleResumeEffect`. */
        fun refreshAccessibilityStatus() {
            _state.update { it.copy(accessibilityEnabled = accessibilityEnablementCheck.isEnabled()) }
        }

        fun setNotificationsPermissionGranted(granted: Boolean) {
            _state.update { it.copy(notificationsPermissionGranted = granted) }
        }

        fun advance() {
            _state.value.currentStep.next()?.let { next ->
                _state.update { it.copy(currentStep = next) }
            }
        }

        fun retreat() {
            _state.value.currentStep.previous()?.let { previous ->
                _state.update { it.copy(currentStep = previous) }
            }
        }

        fun togglePackages(packageNames: Set<String>) {
            if (packageNames.isEmpty()) return
            _state.update { current ->
                val toggled = current.selectedPackages.toMutableSet()
                if (toggled.containsAll(packageNames)) {
                    toggled.removeAll(packageNames)
                } else {
                    toggled.addAll(packageNames)
                }
                current.copy(selectedPackages = toggled)
            }
        }

        fun finish() {
            if (_state.value.finishing || _state.value.finished) return
            _state.update { it.copy(finishing = true) }
            viewModelScope.launch {
                runCatching {
                    preferencesRepository.setEnabledPackages(_state.value.selectedPackages)
                    preferencesRepository.setOnboardingComplete(true)
                    serviceLauncher.start()
                }.onFailure { Timber.e(it, "onboarding finish failed") }
                _state.update { it.copy(finished = true, finishing = false) }
            }
        }
    }

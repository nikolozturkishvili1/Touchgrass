package com.touchgrass.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.touchgrass.app.data.local.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State for the post-onboarding "Apps to block" sub-screen. The source of truth is
 * [PreferencesRepository.enabledPackages] — there is no local mutable selection set, so
 * the on-disk state and the UI cannot drift apart.
 */
data class SettingsAppPickerUiState(
    val selectedPackages: Set<String> = emptySet(),
)

@HiltViewModel
class SettingsAppPickerViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsAppPickerUiState> = preferencesRepository.enabledPackages
        .map { SettingsAppPickerUiState(selectedPackages = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STATE_SHARING_TIMEOUT_MS),
            initialValue = SettingsAppPickerUiState(),
        )

    /**
     * Toggle every [packageNames] variant for one logical app at once: if all are currently
     * selected, remove them all; otherwise add them all. Mirrors the onboarding semantics
     * in `OnboardingViewModel.togglePackages` so the two pickers stay consistent.
     *
     * Reads the latest persisted set before writing back to avoid clobbering a concurrent
     * change (unlikely on a single-user device but cheap to do right).
     */
    fun togglePackages(packageNames: Set<String>) {
        if (packageNames.isEmpty()) return
        viewModelScope.launch {
            val current = preferencesRepository.enabledPackages.first()
            val next = current.toMutableSet().apply {
                if (containsAll(packageNames)) removeAll(packageNames) else addAll(packageNames)
            }
            preferencesRepository.setEnabledPackages(next)
        }
    }

    private companion object {
        const val STATE_SHARING_TIMEOUT_MS = 5_000L
    }
}

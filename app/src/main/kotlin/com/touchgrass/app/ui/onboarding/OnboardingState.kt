package com.touchgrass.app.ui.onboarding

import com.touchgrass.app.data.local.PreferencesRepository
import com.touchgrass.app.oem.OemId
import com.touchgrass.app.oem.OemWalkthrough

/**
 * Step in the first-run flow (spec §3.1.G). Linear: Welcome → Accessibility → Battery → AppPicker.
 */
enum class OnboardingStep {
    Welcome,
    Accessibility,
    Battery,
    AppPicker,
    ;

    fun next(): OnboardingStep? = entries.getOrNull(ordinal + 1)

    fun previous(): OnboardingStep? = entries.getOrNull(ordinal - 1)

    val indexInFlow: Int get() = ordinal
    val totalSteps: Int get() = entries.size
}

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.Welcome,
    val oemId: OemId = OemId.Generic,
    val walkthrough: OemWalkthrough? = null,
    val accessibilityEnabled: Boolean = false,
    val notificationsPermissionGranted: Boolean = false,
    val selectedPackages: Set<String> = PreferencesRepository.DEFAULT_ENABLED_PACKAGES,
    val finishing: Boolean = false,
    val finished: Boolean = false,
)

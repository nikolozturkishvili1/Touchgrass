package com.touchgrass.app.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touchgrass.app.ui.onboarding.steps.AccessibilityStep
import com.touchgrass.app.ui.onboarding.steps.AppPickerStep
import com.touchgrass.app.ui.onboarding.steps.BatteryStep
import com.touchgrass.app.ui.onboarding.steps.WelcomeStep

@Composable
fun OnboardingRoute(
    onFinished: () -> Unit,
    onOpenTrustDashboard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refreshAccessibilityStatus()
        onPauseOrDispose { /* no-op */ }
    }

    LaunchedEffect(state.finished) {
        if (state.finished) onFinished()
    }

    BackHandler(enabled = state.currentStep != OnboardingStep.Welcome) {
        viewModel.retreat()
    }

    OnboardingFlow(
        state = state,
        onAdvance = viewModel::advance,
        onTogglePackages = viewModel::togglePackages,
        onFinish = viewModel::finish,
        onOpenTrustDashboard = onOpenTrustDashboard,
        onNotificationsPermissionGranted = viewModel::setNotificationsPermissionGranted,
        modifier = modifier,
    )
}

@Composable
fun OnboardingFlow(
    state: OnboardingState,
    onAdvance: () -> Unit,
    onTogglePackages: (Set<String>) -> Unit,
    onFinish: () -> Unit,
    onOpenTrustDashboard: () -> Unit,
    onNotificationsPermissionGranted: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
        ) {
            StepIndicator(state.currentStep)
            Spacer(Modifier.height(24.dp))

            when (state.currentStep) {
                OnboardingStep.Welcome -> WelcomeStep(onContinue = onAdvance)
                OnboardingStep.Accessibility ->
                    AccessibilityStep(
                        accessibilityEnabled = state.accessibilityEnabled,
                        onContinue = onAdvance,
                        onOpenTrustDashboard = onOpenTrustDashboard,
                        onNotificationsPermissionGranted = onNotificationsPermissionGranted,
                    )
                OnboardingStep.Battery ->
                    BatteryStep(
                        walkthrough = state.walkthrough,
                        oemId = state.oemId,
                        onContinue = onAdvance,
                    )
                OnboardingStep.AppPicker ->
                    AppPickerStep(
                        selectedPackages = state.selectedPackages,
                        onTogglePackages = onTogglePackages,
                        finishing = state.finishing,
                        onFinish = onFinish,
                    )
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: OnboardingStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OnboardingStep.entries.forEach { step ->
            val filled = step.ordinal <= currentStep.ordinal
            Surface(
                shape = RoundedCornerShape(4.dp),
                color =
                    if (filled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                modifier =
                    Modifier
                        .height(4.dp)
                        .weight(1f)
                        .clip(CircleShape),
            ) { Box(Modifier.fillMaxSize()) }
        }
        Spacer(Modifier.height(0.dp))
        Text(
            text = "${currentStep.indexInFlow + 1} / ${currentStep.totalSteps}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

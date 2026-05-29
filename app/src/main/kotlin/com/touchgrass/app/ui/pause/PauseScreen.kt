package com.touchgrass.app.ui.pause

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touchgrass.app.domain.FrictionMode
import com.touchgrass.app.ui.lock.LockChallenge
import com.touchgrass.app.ui.pause.friction.FrictionGate
import com.touchgrass.app.ui.theme.TouchgrassTheme

@Composable
fun PauseRoute(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: PauseViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.confirmed) {
        if (state.confirmed) onDismiss()
    }

    PauseScreen(
        state = state,
        onPickDuration = viewModel::pickDuration,
        onFrictionComplete = viewModel::onFrictionComplete,
        onFrictionCancel = viewModel::onFrictionCancel,
        onLockChallengeVerified = { viewModel.onLockChallengeVerified() },
        onLockChallengeCancel = viewModel::onLockChallengeCancel,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

@Composable
fun PauseScreen(
    state: PauseUiState,
    onPickDuration: (Long) -> Unit,
    onFrictionComplete: () -> Unit,
    onFrictionCancel: () -> Unit,
    onLockChallengeVerified: (String) -> Unit,
    onLockChallengeCancel: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Take a peek") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close")
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    ),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
        ) {
            when (state.phase) {
                PausePhase.PickDuration -> DurationPicker(state, onPickDuration)
                PausePhase.LockGate ->
                    LockChallenge(
                        purpose = "Take a peek",
                        requiresEmail = false,
                        onVerified = onLockChallengeVerified,
                        onCancel = onLockChallengeCancel,
                    )
                PausePhase.Friction ->
                    FrictionGate(
                        mode = state.frictionMode,
                        onComplete = onFrictionComplete,
                        onCancel = onFrictionCancel,
                    )
                PausePhase.Done -> Unit // route observer navigates away
            }
        }
    }
}

@Composable
private fun DurationPicker(
    state: PauseUiState,
    onPickDuration: (Long) -> Unit,
) {
    Spacer(Modifier.height(8.dp))
    Text(
        text = "How long?",
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text =
            "Pick a window. Touchgrass will look the other way for that long. " +
                "Daily cap: ${state.remainingBudgetMsToday / 60_000L} min left today.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )

    state.errorMessage?.let { message ->
        Spacer(Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )
    }

    Spacer(Modifier.height(24.dp))

    state.availableDurationsMs.forEach { durationMs ->
        val mins = durationMs / 60_000L
        val affordable = durationMs <= state.remainingBudgetMsToday
        DurationOption(
            label = "$mins minutes",
            sublabel = if (affordable) null else "Over your daily budget",
            enabled = affordable,
            onClick = { onPickDuration(durationMs) },
        )
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun DurationOption(
    label: String,
    sublabel: String?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors =
            CardDefaults.cardColors(
                containerColor =
                    if (enabled) {
                        MaterialTheme.colorScheme.surface
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    },
            ),
        onClick = { if (enabled) onClick() },
        enabled = enabled,
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            sublabel?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Pause — picker")
@Composable
private fun PausePickerPreview() {
    TouchgrassTheme(darkTheme = false) {
        PauseScreen(
            state =
                PauseUiState(
                    phase = PausePhase.PickDuration,
                    remainingBudgetMsToday = 18 * 60_000L,
                    frictionMode = FrictionMode.WaitTimer,
                ),
            onPickDuration = {},
            onFrictionComplete = {},
            onFrictionCancel = {},
            onLockChallengeVerified = {},
            onLockChallengeCancel = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "Pause — wait timer")
@Composable
private fun PauseWaitTimerPreview() {
    TouchgrassTheme(darkTheme = false) {
        PauseScreen(
            state =
                PauseUiState(
                    phase = PausePhase.Friction,
                    selectedDurationMs = 5 * 60_000L,
                    remainingBudgetMsToday = 18 * 60_000L,
                    frictionMode = FrictionMode.WaitTimer,
                ),
            onPickDuration = {},
            onFrictionComplete = {},
            onFrictionCancel = {},
            onLockChallengeVerified = {},
            onLockChallengeCancel = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "Pause — math problem")
@Composable
private fun PauseMathPreview() {
    TouchgrassTheme(darkTheme = false) {
        PauseScreen(
            state =
                PauseUiState(
                    phase = PausePhase.Friction,
                    selectedDurationMs = 5 * 60_000L,
                    remainingBudgetMsToday = 18 * 60_000L,
                    frictionMode = FrictionMode.MathProblem,
                ),
            onPickDuration = {},
            onFrictionComplete = {},
            onFrictionCancel = {},
            onLockChallengeVerified = {},
            onLockChallengeCancel = {},
            onDismiss = {},
        )
    }
}

@Preview(showBackground = true, name = "Pause — budget exceeded")
@Composable
private fun PauseBudgetExceededPreview() {
    TouchgrassTheme(darkTheme = false) {
        PauseScreen(
            state =
                PauseUiState(
                    phase = PausePhase.PickDuration,
                    remainingBudgetMsToday = 3 * 60_000L,
                    frictionMode = FrictionMode.WaitTimer,
                    errorMessage = "Only 3 min left in your daily pause budget.",
                ),
            onPickDuration = {},
            onFrictionComplete = {},
            onFrictionCancel = {},
            onLockChallengeVerified = {},
            onLockChallengeCancel = {},
            onDismiss = {},
        )
    }
}

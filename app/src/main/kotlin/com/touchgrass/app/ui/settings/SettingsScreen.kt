package com.touchgrass.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touchgrass.app.domain.FrictionMode
import com.touchgrass.app.ui.components.PrimaryButton
import com.touchgrass.app.ui.components.SecondaryButton
import com.touchgrass.app.ui.lock.LockChallenge
import com.touchgrass.app.ui.theme.TouchgrassTheme

@Composable
fun SettingsRoute(
    onNavigateBack: () -> Unit,
    onOpenTrustDashboard: () -> Unit,
    onOpenAppSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        state = state,
        onSetPauseButtonVisible = viewModel::setPauseButtonVisible,
        onSetDailyBudget = viewModel::setDailyBudgetMinutes,
        onSetFrictionMode = viewModel::setFrictionMode,
        onSetQuickPeekEnabled = viewModel::setQuickPeekEnabled,
        onBeginLockEnrollment = viewModel::beginLockEnrollment,
        onBeginLockDisenrollment = viewModel::beginLockDisenrollment,
        onLockChallengeCompleted = viewModel::onLockChallengeCompleted,
        onLockChallengeCancelled = viewModel::cancelLockChallenge,
        onNavigateBack = onNavigateBack,
        onOpenTrustDashboard = onOpenTrustDashboard,
        onOpenAppSelection = onOpenAppSelection,
        modifier = modifier,
    )
}

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onSetPauseButtonVisible: (Boolean) -> Unit,
    onSetDailyBudget: (Long) -> Unit,
    onSetFrictionMode: (FrictionMode) -> Unit,
    onSetQuickPeekEnabled: (Boolean) -> Unit,
    onBeginLockEnrollment: () -> Unit,
    onBeginLockDisenrollment: () -> Unit,
    onLockChallengeCompleted: (String) -> Unit,
    onLockChallengeCancelled: () -> Unit,
    onNavigateBack: () -> Unit,
    onOpenTrustDashboard: () -> Unit,
    onOpenAppSelection: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Settings") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
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
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(8.dp))
                LinkRow(
                    title = "Apps to block",
                    subtitle = "${state.enabledAppsCount} of ${state.totalAppsCount} enabled",
                    onClick = onOpenAppSelection,
                )

                Spacer(Modifier.height(32.dp))
                PauseSection(state, onSetPauseButtonVisible, onSetDailyBudget, onSetFrictionMode)

                Spacer(Modifier.height(32.dp))
                QuickPeekSection(
                    enabled = state.quickPeekEnabled,
                    onToggle = onSetQuickPeekEnabled,
                )

                Spacer(Modifier.height(32.dp))
                LockSection(
                    enabled = state.lockEnabled,
                    email = state.lockEmail,
                    onEnable = onBeginLockEnrollment,
                    onDisable = onBeginLockDisenrollment,
                )

                Spacer(Modifier.height(32.dp))
                SectionTitle("Privacy")
                Spacer(Modifier.height(8.dp))
                LinkRow(
                    title = "What can Touchgrass see?",
                    subtitle = "The Trust Dashboard.",
                    onClick = onOpenTrustDashboard,
                )

                Spacer(Modifier.height(32.dp))
            }
        }

        if (state.lockChallenge != LockChallengeMode.None) {
            // Fullscreen overlay covering the entire Settings screen while the OTP gate runs.
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxSize(),
            ) {
                LockChallenge(
                    purpose =
                        when (state.lockChallenge) {
                            LockChallengeMode.Enrolling -> "Enable commitment lock"
                            LockChallengeMode.Disenrolling -> "Disable commitment lock"
                            LockChallengeMode.None -> ""
                        },
                    requiresEmail = state.lockChallenge == LockChallengeMode.Enrolling,
                    onVerified = onLockChallengeCompleted,
                    onCancel = onLockChallengeCancelled,
                )
            }
        }
    }
}

@Composable
private fun PauseSection(
    state: SettingsUiState,
    onSetPauseButtonVisible: (Boolean) -> Unit,
    onSetDailyBudget: (Long) -> Unit,
    onSetFrictionMode: (FrictionMode) -> Unit,
) {
    SectionTitle("Pause")
    Spacer(Modifier.height(8.dp))
    ToggleRow(
        title = "Show pause button on home",
        subtitle = "When off, Touchgrass can't be paused from the home screen.",
        checked = state.pauseButtonVisible,
        onCheckedChange = onSetPauseButtonVisible,
    )
    Spacer(Modifier.height(16.dp))
    Subtitle("Daily pause budget")
    Spacer(Modifier.height(4.dp))
    Subdetail("Total time you can pause Touchgrass in a single day. Resets at midnight.")
    Spacer(Modifier.height(8.dp))
    BudgetPicker(selected = state.dailyBudgetMinutes, onSelect = onSetDailyBudget)
    Spacer(Modifier.height(24.dp))
    Subtitle("Friction")
    Spacer(Modifier.height(4.dp))
    Subdetail("What you have to do before a pause begins. The more friction, the harder to act on impulse.")
    Spacer(Modifier.height(8.dp))
    FrictionPicker(selected = state.frictionMode, onSelect = onSetFrictionMode)
}

@Composable
private fun QuickPeekSection(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    SectionTitle("Quick Peek")
    Spacer(Modifier.height(8.dp))
    Subdetail(
        "When on, the first reel you open in each app gets a pass — useful for the one a " +
            "friend sent you in a DM. The second is blocked.",
    )
    Spacer(Modifier.height(12.dp))
    ToggleRow(
        title = "Allow one peek per app, per session",
        subtitle = "Leaving the app and coming back gives you another peek.",
        checked = enabled,
        onCheckedChange = onToggle,
    )
}

@Composable
private fun LockSection(
    enabled: Boolean,
    email: String?,
    onEnable: () -> Unit,
    onDisable: () -> Unit,
) {
    SectionTitle("Commitment lock")
    Spacer(Modifier.height(8.dp))
    Subdetail(
        "When the lock is on, you can't turn Touchgrass off without entering a code we email you. " +
            "It's the dignified way to take your brain back.",
    )
    Spacer(Modifier.height(12.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (enabled) {
                Text(
                    text = "On — codes will be sent to:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = email.orEmpty(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(16.dp))
                SecondaryButton(text = "Disable commitment lock", onClick = onDisable)
            } else {
                Text(
                    text = "Off",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(16.dp))
                PrimaryButton(text = "Enable commitment lock", onClick = onEnable)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun Subtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
    )
}

@Composable
private fun Subdetail(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun BudgetPicker(
    selected: Long,
    onSelect: (Long) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            SettingsUiState.BUDGET_PRESETS_MINUTES.forEach { minutes ->
                RadioRow(
                    label = "$minutes minutes",
                    selected = minutes == selected,
                    onSelect = { onSelect(minutes) },
                )
            }
        }
    }
}

@Composable
private fun FrictionPicker(
    selected: FrictionMode,
    onSelect: (FrictionMode) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            FrictionMode.entries.forEach { mode ->
                RadioRow(
                    label = friendlyName(mode),
                    sublabel = friendlyDescription(mode),
                    selected = mode == selected,
                    onSelect = { onSelect(mode) },
                )
            }
        }
    }
}

private fun friendlyName(mode: FrictionMode): String =
    when (mode) {
        FrictionMode.None -> "None"
        FrictionMode.WaitTimer -> "Wait 5 seconds"
        FrictionMode.MathProblem -> "Solve a math problem"
        FrictionMode.RandomCode -> "Type a 30-character code"
        FrictionMode.Breathing -> "Breathe for 30 seconds"
    }

private fun friendlyDescription(mode: FrictionMode): String =
    when (mode) {
        FrictionMode.None -> "Pause starts immediately. Use sparingly."
        FrictionMode.WaitTimer -> "Just a small pause between impulse and action."
        FrictionMode.MathProblem -> "Engage the prefrontal cortex briefly."
        FrictionMode.RandomCode -> "The maximalist option."
        FrictionMode.Breathing -> "Calmer than the others."
    }

@Composable
private fun RadioRow(
    label: String,
    sublabel: String? = null,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(selected = selected, onClick = onSelect)
                .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onSelect)
        Spacer(Modifier.height(0.dp))
        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
            Text(text = label, style = MaterialTheme.typography.titleLarge)
            sublabel?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LinkRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(showBackground = true, name = "Settings — defaults")
@Composable
private fun SettingsDefaultsPreview() {
    TouchgrassTheme(darkTheme = false) {
        SettingsScreen(
            state = SettingsUiState(),
            onSetPauseButtonVisible = {},
            onSetDailyBudget = {},
            onSetFrictionMode = {},
            onSetQuickPeekEnabled = {},
            onBeginLockEnrollment = {},
            onBeginLockDisenrollment = {},
            onLockChallengeCompleted = {},
            onLockChallengeCancelled = {},
            onNavigateBack = {},
            onOpenTrustDashboard = {},
            onOpenAppSelection = {},
        )
    }
}

@Preview(showBackground = true, name = "Settings — lock on")
@Composable
private fun SettingsLockOnPreview() {
    TouchgrassTheme(darkTheme = false) {
        SettingsScreen(
            state =
                SettingsUiState(
                    lockEnabled = true,
                    lockEmail = "you@example.com",
                    frictionMode = FrictionMode.Breathing,
                ),
            onSetPauseButtonVisible = {},
            onSetDailyBudget = {},
            onSetFrictionMode = {},
            onSetQuickPeekEnabled = {},
            onBeginLockEnrollment = {},
            onBeginLockDisenrollment = {},
            onLockChallengeCompleted = {},
            onLockChallengeCancelled = {},
            onNavigateBack = {},
            onOpenTrustDashboard = {},
            onOpenAppSelection = {},
        )
    }
}

@Preview(showBackground = true, name = "Settings — dark")
@Composable
private fun SettingsDarkPreview() {
    TouchgrassTheme(darkTheme = true) {
        SettingsScreen(
            state =
                SettingsUiState(
                    pauseButtonVisible = false,
                    dailyBudgetMinutes = 30L,
                    frictionMode = FrictionMode.Breathing,
                    lockEnabled = true,
                    lockEmail = "you@example.com",
                ),
            onSetPauseButtonVisible = {},
            onSetDailyBudget = {},
            onSetFrictionMode = {},
            onSetQuickPeekEnabled = {},
            onBeginLockEnrollment = {},
            onBeginLockDisenrollment = {},
            onLockChallengeCompleted = {},
            onLockChallengeCancelled = {},
            onNavigateBack = {},
            onOpenTrustDashboard = {},
            onOpenAppSelection = {},
        )
    }
}

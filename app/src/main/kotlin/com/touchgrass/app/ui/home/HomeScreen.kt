package com.touchgrass.app.ui.home

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touchgrass.app.ui.components.PrimaryButton
import com.touchgrass.app.ui.components.SecondaryButton
import com.touchgrass.app.ui.lock.LockChallenge
import com.touchgrass.app.ui.theme.TouchgrassTheme
import kotlinx.coroutines.delay

/**
 * Hilt-aware entry point — pulls the [HomeViewModel] and forwards the static [HomeScreen].
 * Splitting the stateful route from the stateless screen keeps previews + tests easy.
 */
@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    onStartOnboarding: () -> Unit = {},
    onOpenTrustDashboard: () -> Unit = {},
    onOpenStats: () -> Unit = {},
    onOpenPause: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingLockChallenge by viewModel.pendingLockChallenge.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LifecycleResumeEffect(Unit) {
        viewModel.refreshAccessibilityStatus()
        onPauseOrDispose { /* no-op */ }
    }

    Box(modifier = modifier.fillMaxSize()) {
        HomeScreen(
            state = uiState,
            onToggle = viewModel::setEnabled,
            onStartOnboarding = onStartOnboarding,
            onOpenAccessibilitySettings = {
                context.startActivity(
                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    },
                )
            },
            onOpenTrustDashboard = onOpenTrustDashboard,
            onOpenStats = onOpenStats,
            onOpenPause = onOpenPause,
            onOpenSettings = onOpenSettings,
            onCancelPause = viewModel::cancelPause,
            modifier = Modifier.fillMaxSize(),
        )

        if (pendingLockChallenge) {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxSize(),
            ) {
                LockChallenge(
                    purpose = "Turn off Touchgrass",
                    requiresEmail = false,
                    onVerified = { viewModel.confirmTurnOff() },
                    onCancel = viewModel::cancelLockChallenge,
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onToggle: (Boolean) -> Unit,
    onStartOnboarding: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenTrustDashboard: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenPause: () -> Unit,
    onOpenSettings: () -> Unit,
    onCancelPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(bottom = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (state) {
                    HomeUiState.Loading -> Unit
                    HomeUiState.NeedsOnboarding -> NeedsOnboardingContent(onStartOnboarding)
                    HomeUiState.AccessibilityOff -> AccessibilityOffContent(onOpenAccessibilitySettings)
                    is HomeUiState.On ->
                        OnContent(
                            state = state,
                            onTurnOff = { onToggle(false) },
                            onOpenTrustDashboard = onOpenTrustDashboard,
                            onOpenStats = onOpenStats,
                            onOpenPause = onOpenPause,
                        )
                    is HomeUiState.Paused ->
                        PausedContent(
                            state = state,
                            onCancelPause = onCancelPause,
                            onOpenTrustDashboard = onOpenTrustDashboard,
                        )
                    HomeUiState.Off -> OffContent(onTurnOn = { onToggle(true) })
                }
            }
        }

        // Settings entry sits top-right and is reachable from every state except the splash and
        // pre-onboarding (where there's nothing to configure).
        if (state !is HomeUiState.Loading && state !is HomeUiState.NeedsOnboarding) {
            IconButton(
                onClick = onOpenSettings,
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.SettingsSuggest,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun NeedsOnboardingContent(onStartOnboarding: () -> Unit) {
    Title("Welcome.")
    Spacer(Modifier.height(8.dp))
    Subtitle("Let's get you set up so Touchgrass can do its job and you can do yours.")
    Spacer(Modifier.height(32.dp))
    PrimaryButton(text = "Get started", onClick = onStartOnboarding)
}

@Composable
private fun AccessibilityOffContent(onOpenAccessibilitySettings: () -> Unit) {
    Title("Touchgrass needs the keys.")
    Spacer(Modifier.height(8.dp))
    Subtitle(
        "The Accessibility permission is how Touchgrass notices when you open a reel or short. " +
            "Without it, it can't help.",
    )
    Spacer(Modifier.height(32.dp))
    PrimaryButton(text = "Open Accessibility settings", onClick = onOpenAccessibilitySettings)
}

@Composable
private fun OnContent(
    state: HomeUiState.On,
    onTurnOff: () -> Unit,
    onOpenTrustDashboard: () -> Unit,
    onOpenStats: () -> Unit,
    onOpenPause: () -> Unit,
) {
    Title("Touchgrass is on.")
    Spacer(Modifier.height(8.dp))
    Subtitle(
        if (state.todaysBlockCount == 0) {
            "Reels and shorts are blocked."
        } else {
            "Saves today: ${state.todaysBlockCount}"
        },
    )
    Spacer(Modifier.height(32.dp))
    if (state.todaysBlockCount > 0) {
        PrimaryButton(text = "See all stats", onClick = onOpenStats)
        Spacer(Modifier.height(12.dp))
    }
    if (state.pauseButtonVisible) {
        SecondaryButton(text = "Take a peek", onClick = onOpenPause)
        Spacer(Modifier.height(12.dp))
    }
    SecondaryButton(text = "Turn off", onClick = onTurnOff)
    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onOpenTrustDashboard) {
        Text(
            text = "What can Touchgrass see?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PausedContent(
    state: HomeUiState.Paused,
    onCancelPause: () -> Unit,
    onOpenTrustDashboard: () -> Unit,
) {
    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.pauseEndsAtMs) {
        while (true) {
            nowMs = System.currentTimeMillis()
            if (nowMs >= state.pauseEndsAtMs) break
            delay(1_000L)
        }
    }
    val secondsRemaining = ((state.pauseEndsAtMs - nowMs) / 1_000L).coerceAtLeast(0L)
    val minutes = secondsRemaining / 60L
    val seconds = secondsRemaining % 60L

    Title("Paused.")
    Spacer(Modifier.height(8.dp))
    Subtitle(
        if (secondsRemaining == 0L) {
            "Pause is over — Touchgrass is back on."
        } else {
            "$minutes:${seconds.toString().padStart(2, '0')} left"
        },
    )
    Spacer(Modifier.height(32.dp))
    SecondaryButton(text = "Cancel pause", onClick = onCancelPause)
    Spacer(Modifier.height(8.dp))
    TextButton(onClick = onOpenTrustDashboard) {
        Text(
            text = "What can Touchgrass see?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OffContent(onTurnOn: () -> Unit) {
    Title("Touchgrass is off.")
    Spacer(Modifier.height(8.dp))
    Subtitle("Nothing's being blocked. Turn it back on when you're ready.")
    Spacer(Modifier.height(32.dp))
    PrimaryButton(text = "Turn on", onClick = onTurnOn)
}

@Composable
private fun Title(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun Subtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@Preview(showBackground = true, name = "Home — NeedsOnboarding")
@Composable
private fun NeedsOnboardingPreview() {
    TouchgrassTheme(darkTheme = false) {
        HomeScreen(HomeUiState.NeedsOnboarding, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Home — AccessibilityOff")
@Composable
private fun AccessibilityOffPreview() {
    TouchgrassTheme(darkTheme = false) {
        HomeScreen(HomeUiState.AccessibilityOff, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

@Preview(showBackground = true, name = "Home — On (light)")
@Composable
private fun OnLightPreview() {
    TouchgrassTheme(darkTheme = false) {
        HomeScreen(
            HomeUiState.On(todaysBlockCount = 47, pauseButtonVisible = true),
            {},
            {},
            {},
            {},
            {},
            {},
            {},
            {},
        )
    }
}

@Preview(showBackground = true, name = "Home — On (dark)")
@Composable
private fun OnDarkPreview() {
    TouchgrassTheme(darkTheme = true) {
        HomeScreen(
            HomeUiState.On(todaysBlockCount = 47, pauseButtonVisible = true),
            {},
            {},
            {},
            {},
            {},
            {},
            {},
            {},
        )
    }
}

@Preview(showBackground = true, name = "Home — Paused")
@Composable
private fun PausedPreview() {
    TouchgrassTheme(darkTheme = false) {
        HomeScreen(
            HomeUiState.Paused(
                pauseEndsAtMs = System.currentTimeMillis() + 4L * 60L * 1_000L + 32L * 1_000L,
                todaysBlockCount = 23,
            ),
            {},
            {},
            {},
            {},
            {},
            {},
            {},
            {},
        )
    }
}

@Preview(showBackground = true, name = "Home — Off")
@Composable
private fun OffPreview() {
    TouchgrassTheme(darkTheme = false) {
        HomeScreen(HomeUiState.Off, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

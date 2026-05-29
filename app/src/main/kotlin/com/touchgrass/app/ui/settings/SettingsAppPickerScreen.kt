package com.touchgrass.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touchgrass.app.data.model.SupportedApp
import com.touchgrass.app.ui.components.AppSelectionList
import com.touchgrass.app.ui.theme.TouchgrassTheme

@Composable
fun SettingsAppPickerRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: SettingsAppPickerViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsAppPickerScreen(
        state = state,
        onTogglePackages = viewModel::togglePackages,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}

@Composable
fun SettingsAppPickerScreen(
    state: SettingsAppPickerUiState,
    onTogglePackages: (Set<String>) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Apps to block") },
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
                    .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Toggle off any app you'd rather leave alone. Changes apply immediately.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            AppSelectionList(
                supportedApps = SupportedApp.ALL,
                selectedPackages = state.selectedPackages,
                onTogglePackages = onTogglePackages,
            )
        }
    }
}

@Preview(showBackground = true, name = "Apps to block — all on")
@Composable
private fun SettingsAppPickerAllOnPreview() {
    TouchgrassTheme(darkTheme = false) {
        SettingsAppPickerScreen(
            state =
                SettingsAppPickerUiState(
                    selectedPackages = SupportedApp.ALL.flatMap { it.packageNames }.toSet(),
                ),
            onTogglePackages = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, name = "Apps to block — half on (dark)")
@Composable
private fun SettingsAppPickerHalfOnDarkPreview() {
    TouchgrassTheme(darkTheme = true) {
        SettingsAppPickerScreen(
            state =
                SettingsAppPickerUiState(
                    selectedPackages =
                        SupportedApp.ALL
                            .take(3)
                            .flatMap { it.packageNames }
                            .toSet(),
                ),
            onTogglePackages = {},
            onNavigateBack = {},
        )
    }
}

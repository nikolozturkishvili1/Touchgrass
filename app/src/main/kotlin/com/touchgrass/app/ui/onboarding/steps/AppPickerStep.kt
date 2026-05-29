package com.touchgrass.app.ui.onboarding.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.touchgrass.app.data.model.SupportedApp
import com.touchgrass.app.ui.components.AppSelectionList
import com.touchgrass.app.ui.components.PrimaryButton

@Composable
fun AppPickerStep(
    selectedPackages: Set<String>,
    onTogglePackages: (Set<String>) -> Unit,
    finishing: Boolean,
    onFinish: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Pick what to block.",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Toggle off any app you'd rather leave alone. You can change this later.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            AppSelectionList(
                supportedApps = SupportedApp.ALL,
                selectedPackages = selectedPackages,
                onTogglePackages = onTogglePackages,
            )
        }

        PrimaryButton(
            text = if (finishing) "Setting up..." else "Done — turn on Touchgrass",
            onClick = onFinish,
            enabled = !finishing,
        )
    }
}

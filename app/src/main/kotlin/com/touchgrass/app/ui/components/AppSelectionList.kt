package com.touchgrass.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.touchgrass.app.data.model.SupportedApp

/**
 * The toggleable list of supported apps shared by onboarding's app-picker step and the Settings
 * "Apps to block" sub-screen. Stateless: the caller owns the selected-packages set and decides
 * how to persist toggles.
 *
 * `onTogglePackages` receives the full `Set<String>` of package variants for the row the user
 * tapped — the receiver decides whether that means "add all" or "remove all" based on the
 * current state. This matches the semantics in [SupportedApp]: one logical app may map to
 * several package names (e.g. Instagram = main + Lite) which must always be toggled together.
 */
@Composable
fun AppSelectionList(
    supportedApps: List<SupportedApp>,
    selectedPackages: Set<String>,
    onTogglePackages: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(supportedApps, key = { it.id }) { app ->
            AppRow(
                app = app,
                enabled = selectedPackages.containsAll(app.packageNames),
                onToggle = { onTogglePackages(app.packageNames) },
            )
        }
    }
}

@Composable
private fun AppRow(
    app: SupportedApp,
    enabled: Boolean,
    onToggle: () -> Unit,
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
                Text(
                    text = app.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = app.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = enabled, onCheckedChange = { onToggle() })
        }
    }
}

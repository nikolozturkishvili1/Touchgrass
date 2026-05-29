package com.touchgrass.app.ui.onboarding.steps

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.touchgrass.app.oem.OemId
import com.touchgrass.app.oem.OemWalkthrough
import com.touchgrass.app.ui.components.PrimaryButton
import com.touchgrass.app.ui.components.SecondaryButton
import timber.log.Timber

@Composable
fun BatteryStep(
    walkthrough: OemWalkthrough?,
    oemId: OemId,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "Keep Touchgrass awake.",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text =
                    if (walkthrough != null) {
                        "Your ${walkthrough.displayName} phone tries hard to save battery by killing " +
                            "background apps. Touchgrass needs to stay running to do its job. Here's " +
                            "how to tell your phone to leave it alone."
                    } else {
                        "Android sometimes kills background apps to save battery. Touchgrass needs " +
                            "to stay awake. The good news: on your phone, the default settings are " +
                            "usually fine. Tap the button below to confirm."
                    },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            if (walkthrough != null) {
                walkthrough.steps.forEachIndexed { index, step ->
                    StepCard(stepNumber = index + 1, title = step.title, description = step.description)
                    Spacer(Modifier.height(12.dp))
                }
            }
        }

        Column {
            PrimaryButton(
                text = if (walkthrough != null) "Open battery settings" else "Confirm battery settings",
                onClick = {
                    openBatterySettings(
                        context = context,
                        deepLinkAction = walkthrough?.deepLinkIntent,
                    )
                },
            )
            Spacer(Modifier.height(12.dp))
            SecondaryButton(
                text = "I've done this — continue",
                onClick = onContinue,
            )
        }
    }
}

private fun openBatterySettings(
    context: android.content.Context,
    deepLinkAction: String?,
) {
    val intents =
        buildList {
            if (!deepLinkAction.isNullOrBlank()) add(Intent(deepLinkAction))
            add(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            add(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                },
            )
        }
    for (intent in intents) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
            return
        } catch (e: ActivityNotFoundException) {
            Timber.d(e, "battery settings intent not resolved: %s", intent.action)
        }
    }
    Timber.w("no battery-settings intent resolved on this device")
}

@Composable
private fun StepCard(
    stepNumber: Int,
    title: String,
    description: String,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = stepNumber.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.size(width = 12.dp, height = 0.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

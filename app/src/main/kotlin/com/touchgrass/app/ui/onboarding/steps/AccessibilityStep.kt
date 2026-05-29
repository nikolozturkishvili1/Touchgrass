package com.touchgrass.app.ui.onboarding.steps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.touchgrass.app.ui.components.PrimaryButton
import com.touchgrass.app.ui.components.SecondaryButton

@Composable
fun AccessibilityStep(
    accessibilityEnabled: Boolean,
    onContinue: () -> Unit,
    onOpenTrustDashboard: () -> Unit,
    onNotificationsPermissionGranted: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // POST_NOTIFICATIONS runtime permission (Android 13+). Without it, the watchdog can't
    // tell the user "Touchgrass stopped working" — that's the whole reliability wedge.
    var notificationsAllowed by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                true
            } else {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            },
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted ->
            notificationsAllowed = granted
            onNotificationsPermissionGranted(granted)
        }

    LaunchedEffect(notificationsAllowed) {
        onNotificationsPermissionGranted(notificationsAllowed)
    }

    val canContinue = accessibilityEnabled && notificationsAllowed

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "One permission.",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text =
                    "Touchgrass uses the Accessibility service to notice when you open a " +
                        "reel or short — and only then. It can't read your messages, banking, " +
                        "keyboard, or anything in apps you didn't put on the block list.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onOpenTrustDashboard) {
                Text("See exactly what Touchgrass can and can't see →")
            }
            Spacer(Modifier.height(24.dp))

            StatusCard(
                title = "Accessibility",
                granted = accessibilityEnabled,
                grantedText = "Granted. You're good.",
                ungrantedText = "Tap the button below, then find Touchgrass and turn it on.",
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Spacer(Modifier.height(12.dp))
                StatusCard(
                    title = "Notifications",
                    granted = notificationsAllowed,
                    grantedText = "Granted. You'll know if Touchgrass ever stops.",
                    ungrantedText =
                        "Lets us alert you if Touchgrass stops working. " +
                            "Without it, you wouldn't know.",
                )
            }
        }

        Column {
            if (!accessibilityEnabled) {
                PrimaryButton(
                    text = "Open Accessibility settings",
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            },
                        )
                    },
                )
                Spacer(Modifier.height(12.dp))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsAllowed) {
                SecondaryButton(
                    text = "Allow notifications",
                    onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                )
                Spacer(Modifier.height(12.dp))
            }
            PrimaryButton(
                text = "Continue",
                onClick = onContinue,
                enabled = canContinue,
            )
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    granted: Boolean,
    grantedText: String,
    ungrantedText: String,
) {
    Card(
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                title = title,
                granted = granted,
            )
            Text(
                text = if (granted) grantedText else ungrantedText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Row(
    title: String,
    granted: Boolean,
) {
    androidx.compose.foundation.layout.Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(0.dp))
        androidx.compose.foundation.layout
            .Spacer(Modifier.weight(1f))
        Text(
            text = if (granted) "✓" else "—",
            style = MaterialTheme.typography.titleLarge,
            color =
                if (granted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}

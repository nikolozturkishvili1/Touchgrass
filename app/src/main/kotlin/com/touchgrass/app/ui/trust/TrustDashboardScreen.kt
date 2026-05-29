package com.touchgrass.app.ui.trust

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.touchgrass.app.ui.components.SecondaryButton
import com.touchgrass.app.ui.theme.TouchgrassTheme
import timber.log.Timber

/**
 * Trust Dashboard (spec §3.1.C, §6.4). The most important UI screen in the app from a brand
 * perspective: this is what users tap when they're skeptical of accessibility permissions.
 *
 * The copy here is deliberately specific (named services, named exclusions). If you find
 * yourself softening the language, see [feedback-brand-promises] — generic privacy boilerplate
 * is the install-killer we're trying to avoid.
 */
@Composable
fun TrustDashboardScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("What Touchgrass can see") },
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
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Nothing leaves your phone.",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text =
                    "Touchgrass blocks short-form feeds entirely on-device. " +
                        "Here's exactly what that means.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            Section(
                title = "What Touchgrass can see",
                bullets =
                    listOf(
                        "Which app is in the foreground — only when it's one you put on the block list.",
                        "Screen elements inside those apps, just long enough to detect a reel or short.",
                        "The fact that you opened Touchgrass itself.",
                    ),
            )
            Spacer(Modifier.height(16.dp))
            Section(
                title = "What Touchgrass cannot see",
                bullets =
                    listOf(
                        "Your messages, emails, banking apps, photos, or keyboard input.",
                        "Any app not on your block list — even though Android could let us, we've " +
                            "scoped the Accessibility service strictly.",
                        "Your location, contacts, calendar, microphone, or camera.",
                    ),
            )
            Spacer(Modifier.height(16.dp))
            Section(
                title = "Where your data lives",
                bullets =
                    listOf(
                        "On your phone. Nowhere else.",
                        "Your settings and block counts are stored locally.",
                        "There's no Touchgrass server that knows you exist.",
                    ),
            )
            Spacer(Modifier.height(16.dp))
            Section(
                title = "When Touchgrass uses the internet",
                bullets =
                    listOf(
                        "Commitment lock: when you enroll or verify, we send a 6-digit code to your " +
                            "email via Resend (resend.com). Resend sees the recipient address and the " +
                            "email body (which contains the OTP). Nothing else.",
                        "Your email is stored on your phone only. There's no copy on our side.",
                        "Play Billing handles any purchases. We never see your payment info. " +
                            "(V1 is free — no IAP at launch.)",
                        "That's it. No analytics, no tracking SDKs, no crash reporter without an " +
                            "explicit opt-in disclosed right here.",
                    ),
            )
            Spacer(Modifier.height(24.dp))

            SecondaryButton(
                text = "Verify for yourself — open source on GitHub",
                onClick = { openUrl(context, GITHUB_URL) },
            )
            Spacer(Modifier.height(12.dp))
            SecondaryButton(
                text = "Read the privacy policy",
                onClick = { openUrl(context, PRIVACY_URL) },
            )
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Section(
    title: String,
    bullets: List<String>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            bullets.forEach { bullet ->
                Text(
                    text = "•  $bullet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun openUrl(
    context: android.content.Context,
    url: String,
) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    } catch (e: android.content.ActivityNotFoundException) {
        Timber.w(e, "could not open url %s", url)
    }
}

private const val GITHUB_URL = "https://github.com/nikolozturkishvili1/Touchgrass"
private const val PRIVACY_URL = "https://gettouchgrass.app/privacy"

@Preview(showBackground = true, name = "Trust Dashboard — light")
@Composable
private fun TrustDashboardLightPreview() {
    TouchgrassTheme(darkTheme = false) { TrustDashboardScreen(onNavigateBack = {}) }
}

@Preview(showBackground = true, name = "Trust Dashboard — dark")
@Composable
private fun TrustDashboardDarkPreview() {
    TouchgrassTheme(darkTheme = true) { TrustDashboardScreen(onNavigateBack = {}) }
}

package com.touchgrass.app.ui.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.touchgrass.app.ui.components.PrimaryButton
import com.touchgrass.app.ui.components.SecondaryButton
import com.touchgrass.app.ui.theme.TouchgrassTheme

/**
 * Reusable Composable for the commitment-lock OTP challenge.
 *
 * Two modes via [requiresEmail]:
 *  - `true` — enrollment. User types an email, we send the OTP, user verifies.
 *  - `false` — verification. We use the saved email, send the OTP automatically, user verifies.
 *
 * Call from any screen that needs to gate an action. Embed in a Box/dialog. The host owns the
 * dismiss path via [onCancel] and [onVerified].
 */
@Composable
fun LockChallenge(
    purpose: String,
    requiresEmail: Boolean,
    onVerified: (email: String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: LockChallengeViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.initialise(requiresEmail) }

    LaunchedEffect(state.verified) {
        if (state.verified) {
            val email =
                state.sentToEmail
                    ?: state.savedEmail
                    ?: state.typedEmail
            onVerified(email)
        }
    }

    LockChallengeContent(
        purpose = purpose,
        state = state,
        onTypedEmailChange = viewModel::setTypedEmail,
        onTypedOtpChange = viewModel::setTypedOtp,
        onSend = viewModel::sendCode,
        onVerify = viewModel::verify,
        onCancel = onCancel,
        modifier = modifier,
    )
}

@Composable
private fun LockChallengeContent(
    purpose: String,
    state: LockChallengeUiState,
    onTypedEmailChange: (String) -> Unit,
    onTypedOtpChange: (String) -> Unit,
    onSend: () -> Unit,
    onVerify: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
            Text(
                text = "Commitment lock",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = purpose,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(24.dp))

            when (state.phase) {
                LockChallengePhase.NeedsEmail ->
                    EmailEntry(
                        typedEmail = state.typedEmail,
                        onTypedEmailChange = onTypedEmailChange,
                        errorMessage = state.errorMessage,
                        inFlight = state.inFlight,
                    )
                LockChallengePhase.AwaitingCode ->
                    OtpEntry(
                        sentToEmail = state.sentToEmail ?: state.savedEmail ?: state.typedEmail,
                        typedOtp = state.typedOtp,
                        onTypedOtpChange = onTypedOtpChange,
                        errorMessage = state.errorMessage,
                        cooldownSecondsRemaining = state.cooldownSecondsRemaining,
                        inFlight = state.inFlight,
                    )
                LockChallengePhase.Done -> Unit
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            when (state.phase) {
                LockChallengePhase.NeedsEmail ->
                    PrimaryButton(
                        text = if (state.inFlight) "Sending..." else "Send code",
                        onClick = onSend,
                        enabled = !state.inFlight && state.typedEmail.isNotBlank(),
                    )
                LockChallengePhase.AwaitingCode -> {
                    PrimaryButton(
                        text = if (state.inFlight) "Verifying..." else "Verify",
                        onClick = onVerify,
                        enabled = state.canVerify && !state.inFlight,
                    )
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton(
                        text =
                            if (state.cooldownSecondsRemaining > 0) {
                                "Resend in ${state.cooldownSecondsRemaining}s"
                            } else {
                                "Resend code"
                            },
                        onClick = onSend,
                        enabled = state.cooldownSecondsRemaining == 0 && !state.inFlight,
                    )
                }
                LockChallengePhase.Done -> Unit
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onCancel) {
                Text(text = "Never mind", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmailEntry(
    typedEmail: String,
    onTypedEmailChange: (String) -> Unit,
    errorMessage: String?,
    inFlight: Boolean,
) {
    Text(
        text =
            "We'll email you a 6-digit code. Use an address you actually check — this is how " +
                "you'll get back in if you change your mind.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = typedEmail,
        onValueChange = onTypedEmailChange,
        label = { Text("Email") },
        singleLine = true,
        isError = errorMessage != null,
        supportingText = {
            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
    )
    if (inFlight) {
        Spacer(Modifier.height(16.dp))
        CircularProgressIndicator()
    }
}

@Composable
private fun OtpEntry(
    sentToEmail: String,
    typedOtp: String,
    onTypedOtpChange: (String) -> Unit,
    errorMessage: String?,
    cooldownSecondsRemaining: Int,
    inFlight: Boolean,
) {
    Text(
        text = "Sent a 6-digit code to $sentToEmail.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(16.dp))
    OutlinedTextField(
        value = typedOtp,
        onValueChange = onTypedOtpChange,
        label = { Text("6-digit code") },
        singleLine = true,
        isError = errorMessage != null,
        supportingText = {
            errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = formatOtpHint(typedOtp),
        fontFamily = FontFamily.Monospace,
        fontSize = 18.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    if (inFlight) {
        Spacer(Modifier.height(16.dp))
        CircularProgressIndicator()
    }
}

private fun formatOtpHint(typed: String): String {
    val padded = typed.padEnd(6, '·')
    return "${padded.substring(0, 3)} ${padded.substring(3, 6)}"
}

@Preview(showBackground = true, name = "LockChallenge — email entry")
@Composable
private fun LockChallengeEmailPreview() {
    TouchgrassTheme {
        LockChallengeContent(
            purpose = "Enable commitment lock",
            state = LockChallengeUiState(phase = LockChallengePhase.NeedsEmail),
            onTypedEmailChange = {},
            onTypedOtpChange = {},
            onSend = {},
            onVerify = {},
            onCancel = {},
        )
    }
}

@Preview(showBackground = true, name = "LockChallenge — OTP entry")
@Composable
private fun LockChallengeOtpPreview() {
    TouchgrassTheme {
        LockChallengeContent(
            purpose = "Turn off Touchgrass",
            state =
                LockChallengeUiState(
                    phase = LockChallengePhase.AwaitingCode,
                    sentToEmail = "you@example.com",
                    typedOtp = "147",
                    cooldownSecondsRemaining = 42,
                ),
            onTypedEmailChange = {},
            onTypedOtpChange = {},
            onSend = {},
            onVerify = {},
            onCancel = {},
        )
    }
}

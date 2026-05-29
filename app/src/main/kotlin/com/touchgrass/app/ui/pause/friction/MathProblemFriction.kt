package com.touchgrass.app.ui.pause.friction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.touchgrass.app.domain.MathProblem
import com.touchgrass.app.domain.MathProblemGenerator
import com.touchgrass.app.ui.components.PrimaryButton
import com.touchgrass.app.ui.components.SecondaryButton

@Composable
fun MathProblemFriction(
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val problem = remember { MathProblemGenerator.next() }
    var input by remember { mutableStateOf("") }
    var attemptFailed by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Solve this first.",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "A small thinking moment before the pause begins.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(40.dp))
        Text(
            text = problem.text,
            fontSize = 56.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it.filter { ch -> ch.isDigit() || ch == '-' }
                attemptFailed = false
            },
            label = { Text("Answer") },
            isError = attemptFailed,
            supportingText = {
                if (attemptFailed) {
                    Text(
                        "Not quite. Try again.",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Check",
            onClick = { handleCheck(input, problem, onComplete) { attemptFailed = true; input = "" } },
            enabled = input.isNotBlank(),
        )
        Spacer(Modifier.height(8.dp))
        SecondaryButton(text = "Never mind", onClick = onCancel)
    }
}

private fun handleCheck(
    input: String,
    problem: MathProblem,
    onCorrect: () -> Unit,
    onWrong: () -> Unit,
) {
    val parsed = input.toIntOrNull() ?: run { onWrong(); return }
    if (parsed == problem.answer) onCorrect() else onWrong()
}

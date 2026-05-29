package com.touchgrass.app.ui.pause.friction

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.touchgrass.app.ui.components.SecondaryButton
import kotlinx.coroutines.delay

private enum class BreathPhase(val label: String, val durationMs: Long, val targetScale: Float) {
    InhaleStart("Breathe in", 4_000L, 1f),
    HoldHigh("Hold", 4_000L, 1f),
    ExhaleStart("Breathe out", 6_000L, 0.55f),
    HoldLow("Hold", 2_000L, 0.55f),
    ;

    fun next(): BreathPhase = entries[(ordinal + 1) % entries.size]
}

/** Total duration the user must breathe for (spec §3.1.D: "breathing for 30s"). */
private const val TOTAL_DURATION_MS: Long = 30_000L
private const val MIN_SCALE = 0.55f

@Composable
fun BreathingFriction(
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var phase by remember { mutableStateOf(BreathPhase.InhaleStart) }
    var secondsLeft by remember { mutableIntStateOf((TOTAL_DURATION_MS / 1_000L).toInt()) }
    var completed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val startedAt = System.currentTimeMillis()
        var currentPhase = BreathPhase.InhaleStart
        while (!completed) {
            phase = currentPhase
            delay(currentPhase.durationMs)
            currentPhase = currentPhase.next()
            val elapsed = System.currentTimeMillis() - startedAt
            secondsLeft = ((TOTAL_DURATION_MS - elapsed) / 1_000L).coerceAtLeast(0L).toInt()
            if (elapsed >= TOTAL_DURATION_MS) {
                completed = true
                onComplete()
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = phase.targetScale,
        animationSpec = tween(durationMillis = phase.durationMs.toInt(), easing = LinearEasing),
        label = "breath-scale",
    )

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = phase.label,
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "${secondsLeft}s left",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(48.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size((240 * scale).dp.coerceAtLeast((240 * MIN_SCALE).dp)),
            ) { Box(Modifier.fillMaxSize()) }
        }
        Spacer(Modifier.height(48.dp))
        SecondaryButton(text = "Never mind", onClick = onCancel)
    }
}

package com.touchgrass.app.ui.pause.friction

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.touchgrass.app.domain.FrictionMode

/**
 * Dispatches to the right friction Composable based on the selected [FrictionMode].
 *
 * Why have a dispatcher Composable instead of branching in `PauseScreen`? Each friction owns
 * its own private state (timers, animations, RNG seeds) and Compose preserves that state across
 * recompositions only when the call site is stable. A dedicated dispatcher keeps each friction
 * variant's call site stable as the parent recomposes.
 */
@Composable
fun FrictionGate(
    mode: FrictionMode,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    waitTimerSeconds: Int = DEFAULT_WAIT_TIMER_SECONDS,
) {
    when (mode) {
        FrictionMode.None,
        FrictionMode.WaitTimer -> WaitTimerFriction(
            waitSeconds = waitTimerSeconds,
            onComplete = onComplete,
            onCancel = onCancel,
            modifier = modifier,
        )
        FrictionMode.MathProblem -> MathProblemFriction(
            onComplete = onComplete,
            onCancel = onCancel,
            modifier = modifier,
        )
        FrictionMode.RandomCode -> RandomCodeFriction(
            onComplete = onComplete,
            onCancel = onCancel,
            modifier = modifier,
        )
        FrictionMode.Breathing -> BreathingFriction(
            onComplete = onComplete,
            onCancel = onCancel,
            modifier = modifier,
        )
    }
}

private const val DEFAULT_WAIT_TIMER_SECONDS = 5

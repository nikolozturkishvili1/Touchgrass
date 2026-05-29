package com.touchgrass.app.domain

import kotlin.random.Random

/**
 * A randomly-generated arithmetic problem the user must solve to pass through the
 * [FrictionMode.MathProblem] gate (spec §3.1.D).
 *
 * Difficulty is calibrated to "non-trivial but doable in 10s without a calculator". The point
 * isn't IQ-test difficulty — it's putting *deliberate cognitive load* between impulse and pause.
 */
data class MathProblem(val a: Int, val b: Int, val operator: MathOperator) {
    val answer: Int = when (operator) {
        MathOperator.Plus -> a + b
        MathOperator.Minus -> a - b
        MathOperator.Times -> a * b
    }
    val text: String = "$a ${operator.symbol} $b"
}

enum class MathOperator(val symbol: String) {
    Plus("+"),
    Minus("−"), // proper minus sign (not hyphen) for legibility
    Times("×"), // multiplication sign
}

object MathProblemGenerator {
    /**
     * Mix of three difficulties so users can't pattern-match a single recipe:
     *  - 3-digit + 3-digit addition
     *  - 3-digit − 2/3-digit subtraction (always positive result)
     *  - small × small multiplication (11–29 × 11–29)
     */
    fun next(random: Random = Random.Default): MathProblem = when (random.nextInt(3)) {
        0 -> MathProblem(random.nextInt(100, 1000), random.nextInt(100, 1000), MathOperator.Plus)
        1 -> {
            val a = random.nextInt(200, 1000)
            val b = random.nextInt(10, a - 1)
            MathProblem(a, b, MathOperator.Minus)
        }
        else -> MathProblem(random.nextInt(11, 30), random.nextInt(11, 30), MathOperator.Times)
    }
}

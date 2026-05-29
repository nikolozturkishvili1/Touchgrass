package com.touchgrass.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class MathProblemGeneratorTest {
    @Test
    fun `addition produces three-digit operands with correct answer`() {
        // Random.nextInt(3) returns 0 for seed=0 first call → addition branch.
        val problem = MathProblemGenerator.next(Random(seed = 0))

        assertEquals(MathOperator.Plus, problem.operator)
        assertTrue("a in range: ${problem.a}", problem.a in 100..999)
        assertTrue("b in range: ${problem.b}", problem.b in 100..999)
        assertEquals(problem.a + problem.b, problem.answer)
    }

    @Test
    fun `text format matches operator symbol`() {
        repeat(50) { seed ->
            val problem = MathProblemGenerator.next(Random(seed.toLong()))
            assertEquals("${problem.a} ${problem.operator.symbol} ${problem.b}", problem.text)
        }
    }

    @Test
    fun `subtraction always yields a positive answer`() {
        repeat(200) { seed ->
            val problem = MathProblemGenerator.next(Random(seed.toLong()))
            if (problem.operator == MathOperator.Minus) {
                assertTrue("a >= b for seed=$seed: ${problem.a} - ${problem.b}", problem.a > problem.b)
                assertTrue("positive answer", problem.answer > 0)
            }
        }
    }

    @Test
    fun `multiplication operands stay in the 11 to 29 range`() {
        repeat(200) { seed ->
            val problem = MathProblemGenerator.next(Random(seed.toLong()))
            if (problem.operator == MathOperator.Times) {
                assertTrue("a in range: ${problem.a}", problem.a in 11..29)
                assertTrue("b in range: ${problem.b}", problem.b in 11..29)
                assertEquals(problem.a * problem.b, problem.answer)
            }
        }
    }

    @Test
    fun `all three operators are observed across many seeds`() {
        val seen = mutableSetOf<MathOperator>()
        repeat(200) { seed ->
            seen += MathProblemGenerator.next(Random(seed.toLong())).operator
        }
        assertEquals(MathOperator.entries.toSet(), seen)
    }
}

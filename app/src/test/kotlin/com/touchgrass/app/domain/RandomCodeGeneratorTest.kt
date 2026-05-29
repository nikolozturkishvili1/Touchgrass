package com.touchgrass.app.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class RandomCodeGeneratorTest {

    @Test
    fun `code length matches the constant`() {
        repeat(20) { seed ->
            val code = RandomCodeGenerator.next(Random(seed.toLong()))
            assertEquals(RandomCodeGenerator.CODE_LENGTH, code.length)
        }
    }

    @Test
    fun `code avoids visually-confusable characters`() {
        repeat(50) { seed ->
            val code = RandomCodeGenerator.next(Random(seed.toLong()))
            for (ch in code) {
                assertTrue("forbidden char '$ch' in $code", ch != 'O' && ch != 'I' && ch != 'L' && ch != '0' && ch != '1')
            }
        }
    }

    @Test
    fun `code uses only uppercase letters and digits 2-9`() {
        repeat(50) { seed ->
            val code = RandomCodeGenerator.next(Random(seed.toLong()))
            for (ch in code) {
                val isUpperLetter = ch in 'A'..'Z' && ch != 'O' && ch != 'I' && ch != 'L'
                val isAllowedDigit = ch in '2'..'9'
                assertTrue("char '$ch' is not in the allowed alphabet", isUpperLetter || isAllowedDigit)
            }
        }
    }

    @Test
    fun `two distinct seeds produce distinct codes`() {
        val a = RandomCodeGenerator.next(Random(seed = 42))
        val b = RandomCodeGenerator.next(Random(seed = 1337))
        assertNotEquals(a, b)
    }

    @Test
    fun `same seed produces the same code (determinism for testing)`() {
        val a = RandomCodeGenerator.next(Random(seed = 99))
        val b = RandomCodeGenerator.next(Random(seed = 99))
        assertEquals(a, b)
    }
}

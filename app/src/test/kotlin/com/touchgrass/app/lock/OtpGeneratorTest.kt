package com.touchgrass.app.lock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class OtpGeneratorTest {

    @Test
    fun `length is always exactly six characters`() {
        repeat(200) { seed ->
            val otp = OtpGenerator.next(Random(seed.toLong()))
            assertEquals("seed=$seed", 6, otp.length)
        }
    }

    @Test
    fun `output is always 6 ASCII digits`() {
        repeat(200) { seed ->
            val otp = OtpGenerator.next(Random(seed.toLong()))
            assertTrue("seed=$seed otp=$otp", otp.all { it.isDigit() })
        }
    }

    @Test
    fun `zeroes are padded on the left (small numbers don't shrink)`() {
        // Random(seed=0).nextInt(0, 1_000_000) returns a small int with this implementation,
        // but we just need to confirm that *whenever* the number is small it's padded.
        val explicitlySmall = OtpGenerator.next(object : Random() {
            private var consumed = false
            override fun nextBits(bitCount: Int): Int = if (!consumed) { consumed = true; 0 } else 0
            override fun nextInt(): Int = 0
            override fun nextInt(until: Int): Int = 7
            override fun nextInt(from: Int, until: Int): Int = 7
        })
        assertEquals("000007", explicitlySmall)
    }

    @Test
    fun `same seed reproduces same output (determinism for tests)`() {
        val a = OtpGenerator.next(Random(seed = 42))
        val b = OtpGenerator.next(Random(seed = 42))
        assertEquals(a, b)
    }

    @Test
    fun `different seeds produce different outputs (almost always)`() {
        val a = OtpGenerator.next(Random(seed = 1))
        val b = OtpGenerator.next(Random(seed = 2))
        assertNotEquals(a, b)
    }
}

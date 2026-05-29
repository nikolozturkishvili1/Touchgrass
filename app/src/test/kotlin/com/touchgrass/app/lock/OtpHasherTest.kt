package com.touchgrass.app.lock

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OtpHasherTest {
    @Test
    fun `hash output is 64 hex characters (SHA-256)`() {
        val hashed = OtpHasher.hash("123456")
        assertEquals(64, hashed.length)
        assertTrue(hashed.all { it.isDigit() || it in 'a'..'f' })
    }

    @Test
    fun `same input produces the same hash`() {
        assertEquals(OtpHasher.hash("000000"), OtpHasher.hash("000000"))
    }

    @Test
    fun `different inputs produce different hashes`() {
        assertNotEquals(OtpHasher.hash("000000"), OtpHasher.hash("000001"))
    }

    @Test
    fun `matches returns true for the same value`() {
        val hash = OtpHasher.hash("987654")
        assertTrue(OtpHasher.matches("987654", hash))
    }

    @Test
    fun `matches returns false for any other value`() {
        val hash = OtpHasher.hash("987654")
        assertFalse(OtpHasher.matches("987655", hash))
        assertFalse(OtpHasher.matches("098765", hash))
        assertFalse(OtpHasher.matches("", hash))
    }
}

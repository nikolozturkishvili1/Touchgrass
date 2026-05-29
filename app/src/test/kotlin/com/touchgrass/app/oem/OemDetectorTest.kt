package com.touchgrass.app.oem

import org.junit.Assert.assertEquals
import org.junit.Test

class OemDetectorTest {
    private val detector = OemDetector()

    @Test
    fun `Samsung manufacturer maps to Samsung`() {
        assertEquals(OemId.Samsung, detector.detect("samsung", "samsung"))
        assertEquals(OemId.Samsung, detector.detect("Samsung", "Samsung"))
    }

    @Test
    fun `Xiaomi manufacturer maps to Xiaomi`() {
        assertEquals(OemId.Xiaomi, detector.detect("Xiaomi", "Xiaomi"))
    }

    @Test
    fun `Redmi brand under Xiaomi manufacturer maps to Xiaomi`() {
        assertEquals(OemId.Xiaomi, detector.detect("Xiaomi", "Redmi"))
    }

    @Test
    fun `POCO brand maps to Xiaomi even when manufacturer is empty`() {
        assertEquals(OemId.Xiaomi, detector.detect("", "POCO"))
    }

    @Test
    fun `OnePlus maps to OnePlus`() {
        assertEquals(OemId.OnePlus, detector.detect("OnePlus", "OnePlus"))
    }

    @Test
    fun `Pixel brand maps to Google`() {
        assertEquals(OemId.Google, detector.detect("Google", "google"))
        assertEquals(OemId.Google, detector.detect("Google", "Pixel"))
    }

    @Test
    fun `unknown manufacturer maps to Generic`() {
        assertEquals(OemId.Generic, detector.detect("FairphoneOpenOS", "fairphone"))
        assertEquals(OemId.Generic, detector.detect("", ""))
    }

    @Test
    fun `fromKey is case insensitive`() {
        assertEquals(OemId.Samsung, OemId.fromKey("samsung"))
        assertEquals(OemId.Samsung, OemId.fromKey("Samsung"))
        assertEquals(OemId.Samsung, OemId.fromKey("SAMSUNG"))
    }

    @Test
    fun `fromKey returns null for unknown keys`() {
        assertEquals(null, OemId.fromKey("nokia"))
    }
}

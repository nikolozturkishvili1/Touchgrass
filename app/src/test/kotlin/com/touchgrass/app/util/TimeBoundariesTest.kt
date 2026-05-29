package com.touchgrass.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class TimeBoundariesTest {

    private val utc = ZoneId.of("UTC")
    private val nyc = ZoneId.of("America/New_York")

    @Test
    fun `startOfToday at noon UTC returns 00 UTC`() {
        val noonJan15 = LocalDate.of(2026, 1, 15)
            .atTime(LocalTime.NOON)
            .atZone(utc)
            .toInstant()
            .toEpochMilli()

        val result = TimeBoundaries.startOfToday(noonJan15, utc)
        val expected = LocalDate.of(2026, 1, 15)
            .atStartOfDay(utc)
            .toInstant()
            .toEpochMilli()

        assertEquals(expected, result)
    }

    @Test
    fun `startOfToday respects timezone — late evening NYC is still same day`() {
        // 23:00 NYC on Jan 15 is 04:00 UTC on Jan 16; startOfToday in NYC must be Jan 15 NYC, not Jan 16 UTC.
        val nineteenHoursNyc = LocalDate.of(2026, 1, 15)
            .atTime(23, 0)
            .atZone(nyc)
            .toInstant()
            .toEpochMilli()

        val result = TimeBoundaries.startOfToday(nineteenHoursNyc, nyc)
        val expected = LocalDate.of(2026, 1, 15)
            .atStartOfDay(nyc)
            .toInstant()
            .toEpochMilli()

        assertEquals(expected, result)
    }

    @Test
    fun `startOfWeek on a Wednesday returns Monday 00`() {
        val wedNoonUtc = LocalDate.of(2026, 1, 14) // a Wednesday
            .atTime(LocalTime.NOON)
            .atZone(utc)
            .toInstant()
            .toEpochMilli()

        val result = TimeBoundaries.startOfWeek(wedNoonUtc, utc)
        val expectedMonday = LocalDate.of(2026, 1, 12) // Monday of that ISO week
            .atStartOfDay(utc)
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedMonday, result)
    }

    @Test
    fun `startOfWeek on a Monday returns same day 00`() {
        val mondayMorningUtc = LocalDate.of(2026, 1, 12)
            .atTime(8, 30)
            .atZone(utc)
            .toInstant()
            .toEpochMilli()

        val result = TimeBoundaries.startOfWeek(mondayMorningUtc, utc)
        val expectedMonday = LocalDate.of(2026, 1, 12)
            .atStartOfDay(utc)
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedMonday, result)
    }

    @Test
    fun `startOfWeek on a Sunday returns previous Monday`() {
        val sundayUtc = LocalDate.of(2026, 1, 18)
            .atTime(12, 0)
            .atZone(utc)
            .toInstant()
            .toEpochMilli()

        val result = TimeBoundaries.startOfWeek(sundayUtc, utc)
        val expectedMonday = LocalDate.of(2026, 1, 12)
            .atStartOfDay(utc)
            .toInstant()
            .toEpochMilli()

        assertEquals(expectedMonday, result)
    }
}

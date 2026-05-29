package com.touchgrass.app.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * Pure helpers for the time windows the Stats screen uses ("today", "this week").
 *
 * Extracted from any class for testability — these are stateless, deterministic functions given
 * a `nowMillis` input and a [ZoneId].
 *
 * For .NET devs: `java.time.LocalDate` ≈ `DateOnly`, `ZoneId` ≈ `TimeZoneInfo`, `Instant` ≈ `DateTimeOffset`
 * in UTC.
 */
object TimeBoundaries {

    /**
     * Epoch-ms of 00:00 in [zone] on the calendar day containing [nowMillis].
     */
    fun startOfToday(nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        Instant.ofEpochMilli(nowMillis)
            .atZone(zone)
            .toLocalDate()
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()

    /**
     * Epoch-ms of 00:00 on the most recent Monday on-or-before the calendar day containing
     * [nowMillis], in [zone]. ISO week (Monday-start) for V1 — users in Sunday-start locales
     * get the same answer either way most of the time and the spec doesn't require locale-aware
     * weeks.
     */
    fun startOfWeek(nowMillis: Long, zone: ZoneId = ZoneId.systemDefault()): Long {
        val today: LocalDate = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return monday.atStartOfDay(zone).toInstant().toEpochMilli()
    }
}

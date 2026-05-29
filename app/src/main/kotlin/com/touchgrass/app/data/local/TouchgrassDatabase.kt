package com.touchgrass.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * App database (spec §4.1 — persistence). Single-version for V1; first migration lands when
 * the schema changes.
 *
 * `exportSchema = false` for V1 simplicity. Flip to `true` and configure
 * `ksp { arg("room.schemaLocation", "...") }` in `app/build.gradle.kts` before the first
 * schema migration so we have the prior version's JSON to diff against.
 */
@Database(
    entities = [BlockEventEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class TouchgrassDatabase : RoomDatabase() {
    abstract fun blockEventDao(): BlockEventDao
}

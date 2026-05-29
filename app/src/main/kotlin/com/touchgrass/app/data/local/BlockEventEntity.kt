package com.touchgrass.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per successful block (spec §3.1.F). Stored on-device only — there is no server.
 *
 * Schema note: `timestampMs` is indexed because every stats query filters by a time range.
 * `packageName` + `surface` are kept separate (not a composite key) because future detectors
 * may emit multiple surfaces per package (e.g. Instagram's four reels surfaces).
 */
@Entity(
    tableName = "block_events",
    indices = [Index("timestampMs")],
)
data class BlockEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestampMs: Long,
    val packageName: String,
    val surface: String,
)

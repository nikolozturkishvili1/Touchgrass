package com.touchgrass.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Block-event reads and writes. All read methods return [Flow], so observing screens recompose
 * automatically when the AccessibilityService records a new block.
 *
 * Kotlin/Room note for .NET devs: this is the EF-Core-DbContext-equivalent except Room generates
 * an implementation at compile time via KSP. The `Flow<T>` return is Room's reactive query: it
 * re-runs the query when the underlying tables change.
 */
@Dao
interface BlockEventDao {

    @Insert
    suspend fun insert(event: BlockEventEntity)

    @Query("SELECT COUNT(*) FROM block_events WHERE timestampMs >= :sinceMs")
    fun countSinceFlow(sinceMs: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM block_events")
    fun totalCountFlow(): Flow<Int>

    @Query(
        """
        SELECT surface, COUNT(*) AS count
        FROM block_events
        WHERE timestampMs >= :sinceMs
        GROUP BY surface
        ORDER BY count DESC
        LIMIT :limit
        """,
    )
    fun topSurfacesSinceFlow(sinceMs: Long, limit: Int): Flow<List<SurfaceCount>>

    /** Used by a future retention worker; not wired in V1. */
    @Query("DELETE FROM block_events WHERE timestampMs < :beforeMs")
    suspend fun pruneOlderThan(beforeMs: Long): Int
}

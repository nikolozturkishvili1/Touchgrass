package com.touchgrass.app.data.repository

import com.touchgrass.app.data.local.BlockEventDao
import com.touchgrass.app.data.local.BlockEventEntity
import com.touchgrass.app.data.local.SurfaceCount
import com.touchgrass.app.util.Clock
import com.touchgrass.app.util.TimeBoundaries
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use-case-shaped wrapper over [BlockEventDao]. ViewModels depend on this, not on the DAO
 * directly — keeping the DAO an implementation detail lets us swap storage later (e.g. add a
 * caching layer) without touching the UI tier.
 *
 * Time-window note: each `xxxCountFlow()` captures the day/week boundary at subscription time.
 * If the app stays open across midnight, the "today" count stays anchored on the original day.
 * Acceptable for V1; the Stats screen re-anchors on open.
 */
@Singleton
class BlockEventRepository @Inject constructor(
    private val dao: BlockEventDao,
    private val clock: Clock,
) {
    suspend fun record(packageName: String, surface: String) {
        dao.insert(
            BlockEventEntity(
                timestampMs = clock.nowMillis(),
                packageName = packageName,
                surface = surface,
            ),
        )
    }

    fun todayCountFlow(): Flow<Int> =
        dao.countSinceFlow(TimeBoundaries.startOfToday(clock.nowMillis()))

    fun thisWeekCountFlow(): Flow<Int> =
        dao.countSinceFlow(TimeBoundaries.startOfWeek(clock.nowMillis()))

    fun allTimeCountFlow(): Flow<Int> = dao.totalCountFlow()

    fun topSurfacesThisWeekFlow(limit: Int = TOP_SURFACES_DEFAULT_LIMIT): Flow<List<SurfaceCount>> =
        dao.topSurfacesSinceFlow(
            sinceMs = TimeBoundaries.startOfWeek(clock.nowMillis()),
            limit = limit,
        )

    companion object {
        const val TOP_SURFACES_DEFAULT_LIMIT: Int = 5
    }
}

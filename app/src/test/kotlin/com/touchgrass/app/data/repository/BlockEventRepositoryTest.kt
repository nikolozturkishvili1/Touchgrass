package com.touchgrass.app.data.repository

import com.touchgrass.app.data.local.BlockEventDao
import com.touchgrass.app.data.local.BlockEventEntity
import com.touchgrass.app.data.local.SurfaceCount
import com.touchgrass.app.util.Clock
import com.touchgrass.app.util.TimeBoundaries
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class BlockEventRepositoryTest {

    private class FixedClock(private val nowMs: Long) : Clock {
        override fun nowMillis(): Long = nowMs
        override fun elapsedMillis(): Long = nowMs
    }

    private val noonJan14Utc = LocalDate.of(2026, 1, 14)
        .atTime(12, 0)
        .atZone(ZoneId.of("UTC"))
        .toInstant()
        .toEpochMilli()

    @Test
    fun `record passes the current clock through to the DAO insert`() = runTest {
        val dao = mockk<BlockEventDao>(relaxed = true)
        val clock = FixedClock(noonJan14Utc)
        val repo = BlockEventRepository(dao, clock)

        val inserted = slot<BlockEventEntity>()
        coEvery { dao.insert(capture(inserted)) } returns Unit

        repo.record("com.google.android.youtube", "youtube-shorts")

        coVerify(exactly = 1) { dao.insert(any()) }
        assertEquals(noonJan14Utc, inserted.captured.timestampMs)
        assertEquals("com.google.android.youtube", inserted.captured.packageName)
        assertEquals("youtube-shorts", inserted.captured.surface)
        assertEquals(0L, inserted.captured.id)
    }

    @Test
    fun `todayCountFlow asks the DAO for blocks since the start of today`() = runTest {
        val dao = mockk<BlockEventDao>()
        val clock = FixedClock(noonJan14Utc)
        val repo = BlockEventRepository(dao, clock)

        val expectedSince = TimeBoundaries.startOfToday(noonJan14Utc)
        every { dao.countSinceFlow(expectedSince) } returns flowOf(7)

        val emitted = repo.todayCountFlow().toList()

        assertEquals(listOf(7), emitted)
    }

    @Test
    fun `thisWeekCountFlow uses Monday-start boundary`() = runTest {
        val dao = mockk<BlockEventDao>()
        val clock = FixedClock(noonJan14Utc) // Wednesday
        val repo = BlockEventRepository(dao, clock)

        val expectedSince = TimeBoundaries.startOfWeek(noonJan14Utc)
        every { dao.countSinceFlow(expectedSince) } returns flowOf(42)

        val emitted = repo.thisWeekCountFlow().toList()

        assertEquals(listOf(42), emitted)
    }

    @Test
    fun `topSurfacesThisWeekFlow passes the default limit when none specified`() = runTest {
        val dao = mockk<BlockEventDao>()
        val clock = FixedClock(noonJan14Utc)
        val repo = BlockEventRepository(dao, clock)

        val expectedSince = TimeBoundaries.startOfWeek(noonJan14Utc)
        val rows = listOf(SurfaceCount("youtube-shorts", 10), SurfaceCount("instagram-reels-tab", 8))
        every { dao.topSurfacesSinceFlow(expectedSince, BlockEventRepository.TOP_SURFACES_DEFAULT_LIMIT) } returns flowOf(rows)

        val emitted = repo.topSurfacesThisWeekFlow().toList()

        assertEquals(listOf(rows), emitted)
    }

    @Test
    fun `allTimeCountFlow delegates straight through`() = runTest {
        val dao = mockk<BlockEventDao>()
        val clock = FixedClock(noonJan14Utc)
        val repo = BlockEventRepository(dao, clock)

        every { dao.totalCountFlow() } returns flowOf(1042)

        val emitted = repo.allTimeCountFlow().toList()

        assertEquals(listOf(1042), emitted)
    }
}

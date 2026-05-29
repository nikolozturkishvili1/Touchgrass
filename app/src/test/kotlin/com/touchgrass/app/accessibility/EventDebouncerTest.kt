package com.touchgrass.app.accessibility

import com.touchgrass.app.util.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventDebouncerTest {

    private class FakeClock(var elapsed: Long = 0L) : Clock {
        override fun nowMillis(): Long = elapsed
        override fun elapsedMillis(): Long = elapsed
    }

    @Test
    fun `first call for a key returns true`() {
        val clock = FakeClock()
        val debouncer = EventDebouncer(clock, windowMs = 1_000)

        assertTrue(debouncer.shouldBlock("youtube:youtube-shorts"))
    }

    @Test
    fun `second call inside the window returns false`() {
        val clock = FakeClock()
        val debouncer = EventDebouncer(clock, windowMs = 1_000)

        debouncer.shouldBlock("k")
        clock.elapsed = 999
        assertFalse(debouncer.shouldBlock("k"))
    }

    @Test
    fun `second call exactly at the window boundary returns true`() {
        val clock = FakeClock()
        val debouncer = EventDebouncer(clock, windowMs = 1_000)

        debouncer.shouldBlock("k")
        clock.elapsed = 1_000
        assertTrue(debouncer.shouldBlock("k"))
    }

    @Test
    fun `second call after the window returns true and resets the timer`() {
        val clock = FakeClock()
        val debouncer = EventDebouncer(clock, windowMs = 1_000)

        debouncer.shouldBlock("k")
        clock.elapsed = 1_500
        assertTrue(debouncer.shouldBlock("k"))

        clock.elapsed = 1_800
        assertFalse(
            "second block at 1800 should still be inside the 1000ms window starting at 1500",
            debouncer.shouldBlock("k"),
        )
    }

    @Test
    fun `different keys do not interfere`() {
        val clock = FakeClock()
        val debouncer = EventDebouncer(clock, windowMs = 1_000)

        assertTrue(debouncer.shouldBlock("youtube:youtube-shorts"))
        assertTrue(debouncer.shouldBlock("instagram:instagram-reels"))

        clock.elapsed = 500
        assertFalse(debouncer.shouldBlock("youtube:youtube-shorts"))
        assertFalse(debouncer.shouldBlock("instagram:instagram-reels"))
    }

    @Test
    fun `reset clears one key but leaves others`() {
        val clock = FakeClock()
        val debouncer = EventDebouncer(clock, windowMs = 1_000)

        debouncer.shouldBlock("a")
        debouncer.shouldBlock("b")

        debouncer.reset("a")

        clock.elapsed = 100
        assertTrue("a was reset; should pass", debouncer.shouldBlock("a"))
        assertFalse("b was not reset; still cooling down", debouncer.shouldBlock("b"))
    }

    @Test
    fun `resetAll clears every key`() {
        val clock = FakeClock()
        val debouncer = EventDebouncer(clock, windowMs = 1_000)

        debouncer.shouldBlock("a")
        debouncer.shouldBlock("b")
        debouncer.shouldBlock("c")

        debouncer.resetAll()

        clock.elapsed = 100
        listOf("a", "b", "c").forEach { key ->
            assertTrue("$key should pass after resetAll", debouncer.shouldBlock(key))
        }
    }

    @Test
    fun `default window matches spec`() {
        assertEquals(1_500L, EventDebouncer.DEFAULT_WINDOW_MS)
    }
}

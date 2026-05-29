package com.touchgrass.app.accessibility

import com.touchgrass.app.util.Clock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Suppresses repeat blocks for the same surface within a short window. Without this, a user who
 * spam-taps to return to a feed could re-enter it faster than the AccessibilityService can react,
 * and we'd flap back-and-forth forever (spec §4.4, complaint #2 from §2.1).
 *
 * The default window of 1.5s matches the spec recommendation; bump it via [windowMs] if you
 * need to tune. Per spec, debouncing is **per-package-and-surface**, not global, so blocks of
 * IG Reels don't suppress blocks of TikTok For You.
 *
 * Thread-safe (uses [ConcurrentHashMap]). Safe to call from the AccessibilityService thread.
 */
@Singleton
class EventDebouncer constructor(
    private val clock: Clock,
    private val windowMs: Long,
) {
    // Hilt-bindable constructor. Dagger does not honour Kotlin default parameter values,
    // so we expose a no-arg-for-windowMs constructor that hard-codes the production default.
    // Tests construct the class with the primary constructor and supply their own windowMs.
    @Inject
    constructor(clock: Clock) : this(clock, DEFAULT_WINDOW_MS)

    private val lastBlockAt = ConcurrentHashMap<String, Long>()

    /**
     * Decide whether a block should fire right now for the given key.
     *
     * @param key opaque debounce key, conventionally `"$packageName:$surface"`.
     * @return `true` if the caller should proceed with the block (and the timer is now reset);
     *   `false` if we're still inside the cooldown window from the last block.
     */
    fun shouldBlock(key: String): Boolean {
        val now = clock.elapsedMillis()
        val previous = lastBlockAt[key]
        return if (previous == null || now - previous >= windowMs) {
            lastBlockAt[key] = now
            true
        } else {
            false
        }
    }

    /** Clear the cooldown for a single surface. Useful when the user has explicitly paused. */
    fun reset(key: String) {
        lastBlockAt.remove(key)
    }

    /** Clear every cooldown. */
    fun resetAll() {
        lastBlockAt.clear()
    }

    companion object {
        const val DEFAULT_WINDOW_MS: Long = 1_500L
    }
}

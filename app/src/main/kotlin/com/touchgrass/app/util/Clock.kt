package com.touchgrass.app.util

/**
 * Indirection over the system clock so tests can advance time deterministically.
 *
 * C# analogy: equivalent of injecting `ISystemClock` (ASP.NET Core's `Microsoft.Extensions.Internal.ISystemClock`)
 * instead of calling `DateTime.UtcNow` directly.
 */
interface Clock {
    /** Wall-clock time in milliseconds since the Unix epoch. */
    fun nowMillis(): Long

    /**
     * Monotonic time in milliseconds since boot. Unaffected by user clock changes.
     * Use this for elapsed-time calculations (debouncing, watchdog gaps).
     */
    fun elapsedMillis(): Long

    /** Production implementation backed by [System.currentTimeMillis] and [android.os.SystemClock.elapsedRealtime]. */
    object System : Clock {
        override fun nowMillis(): Long = java.lang.System.currentTimeMillis()
        override fun elapsedMillis(): Long = android.os.SystemClock.elapsedRealtime()
    }
}

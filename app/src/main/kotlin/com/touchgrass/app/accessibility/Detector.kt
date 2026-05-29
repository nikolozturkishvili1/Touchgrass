package com.touchgrass.app.accessibility

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

/**
 * One detector per blocked app (spec §4.4). Implementations are isolated — adding support for a
 * new app means dropping in a new `Detector` class, which is why per-app detectors are the
 * canonical "parallel-sub-agent" fan-out work (spec §12.4 Recipe 1).
 *
 * Detectors must be fast and side-effect-free. They receive thousands of events per session;
 * any expensive work happens *after* a positive detection, in [BlockingStrategy].
 */
interface Detector {
    /**
     * The Android package names this detector handles.
     *
     * Most apps ship under multiple packages — Instagram main vs Lite, TikTok international vs
     * the `ugc.trill` build, Facebook main vs Lite. One detector handles all variants for one
     * surface family. The first element is the canonical/preferred package for logging.
     */
    val packageNames: Set<String>

    /**
     * Inspect an event from this detector's target app.
     *
     * @param event the OS-pushed accessibility event.
     * @param root the live root node of the active window, or `null` if the OS denied it. May be
     *   cheap or expensive to walk depending on the screen — be conservative.
     * @return [Detection.ShortFormFeed] if a blockable feed is in view; otherwise [Detection.NotInteresting].
     */
    fun detect(event: AccessibilityEvent, root: AccessibilityNodeInfo?): Detection
}

/**
 * Result of running a [Detector] on a single event. Sealed so callers' `when` clauses are exhaustive.
 *
 * C# analogy: discriminated union (`OneOf<NotInteresting, ShortFormFeed>` or `record` hierarchy).
 */
sealed interface Detection {
    /** This event isn't a short-form feed; the service should do nothing. */
    data object NotInteresting : Detection

    /**
     * A short-form feed is in view.
     *
     * @param surface stable identifier used for stats + debouncing.
     *   Examples: `"youtube-shorts"`, `"instagram-reels-tab"`, `"instagram-reels-dm"`,
     *   `"tiktok-foryou"`, `"facebook-reels"`, `"snapchat-spotlight"`.
     */
    data class ShortFormFeed(val surface: String) : Detection
}

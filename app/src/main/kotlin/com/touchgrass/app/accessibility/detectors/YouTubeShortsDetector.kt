package com.touchgrass.app.accessibility.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.touchgrass.app.accessibility.Detection
import com.touchgrass.app.accessibility.Detector
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects when the user is viewing YouTube **Shorts** (spec §3.1.A).
 *
 * Strategy: cheap class-name check first, then a node-tree scan for the Shorts player container
 * if we have to. View IDs and class names in the YouTube app change across releases — this
 * detector is intentionally tolerant (multiple fallbacks) and easy to extend as YouTube ships
 * new builds. When YouTube breaks us, this is one of the first files to revisit.
 *
 * Stays well within the [Detector] contract: side-effect-free, no I/O, no allocations of note.
 */
@Singleton
class YouTubeShortsDetector @Inject constructor() : Detector {

    override val packageNames: Set<String> = setOf(PACKAGE_NAME)

    override fun detect(event: AccessibilityEvent, root: AccessibilityNodeInfo?): Detection {
        if (event.packageName?.toString() != PACKAGE_NAME) return Detection.NotInteresting

        // Fast path: class name on window-state changes is a reliable signal for the Shorts player.
        val className = event.className?.toString().orEmpty()
        if (className.containsAny(SHORTS_CLASS_HINTS)) {
            return Detection.ShortFormFeed(SURFACE)
        }

        // Slow path: scan the active window tree. `rootInActiveWindow` returns null on contentChanged
        // events for windows we don't own; that's fine — we just bail.
        val rootNode = root ?: return Detection.NotInteresting

        if (rootNode.hasShortsContainer()) {
            return Detection.ShortFormFeed(SURFACE)
        }

        return Detection.NotInteresting
    }

    private fun AccessibilityNodeInfo.hasShortsContainer(): Boolean {
        for (viewId in SHORTS_VIEW_IDS) {
            val matches = findAccessibilityNodeInfosByViewId(viewId)
            if (!matches.isNullOrEmpty()) return true
        }
        return false
    }

    private fun String.containsAny(needles: Array<String>): Boolean {
        for (needle in needles) if (contains(needle, ignoreCase = true)) return true
        return false
    }

    companion object {
        const val PACKAGE_NAME: String = "com.google.android.youtube"

        /** Stable surface identifier used by [Detection.ShortFormFeed] and the stats DB. */
        const val SURFACE: String = "youtube-shorts"

        /**
         * Class-name substrings observed in YouTube's Shorts player across recent versions.
         * Update this list when YouTube ships a UI change that breaks detection.
         */
        @JvmStatic
        internal val SHORTS_CLASS_HINTS: Array<String> = arrayOf(
            "ShortsPlayer",
            "ShortsHostFragment",
            "ShortsTabbedActivity",
            "reel_watch_fragment",
        )

        /**
         * View IDs of the Shorts reel recycler observed across recent YouTube releases.
         * The first one is the canonical id; the others are fallbacks for older/newer builds.
         */
        @JvmStatic
        internal val SHORTS_VIEW_IDS: Array<String> = arrayOf(
            "$PACKAGE_NAME:id/reel_recycler",
            "$PACKAGE_NAME:id/reel_player_page_container",
            "$PACKAGE_NAME:id/shorts_player",
        )
    }
}

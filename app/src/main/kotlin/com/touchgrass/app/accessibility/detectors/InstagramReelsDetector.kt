package com.touchgrass.app.accessibility.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.touchgrass.app.accessibility.Detection
import com.touchgrass.app.accessibility.Detector
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects when the user is viewing Instagram **Reels** (spec §3.1.A).
 *
 * Reels reach the user through four distinct surfaces, all of which this detector flags:
 *  - **`instagram-reels-tab`** — the dedicated Reels tab opened from the bottom navigation.
 *  - **`instagram-reels-feed`** — an inline Reel scrolled into view in the main home feed.
 *  - **`instagram-reels-explore`** — a Reel opened from the Explore grid.
 *  - **`instagram-reels-dm`** — a Reel watched after tapping a DM-shared share (spec §2.1, complaint #3:
 *    competitors typically miss this surface, which is the most common late-night relapse vector).
 *
 * Strategy mirrors [YouTubeShortsDetector]: cheap class-name check first, then a node-tree scan for
 * one of the known `clips_*` containers. Instagram internally names Reels "clips" (confirmed by view
 * IDs such as `clips_tab` and `clips_header_title`, and by the `/clips/discover/stream/` endpoint),
 * so most fallback IDs follow that prefix.
 *
 * Handles both the main app (`com.instagram.android`) and Instagram Lite (`com.instagram.lite`),
 * which share the same internal naming for the Reels viewer.
 *
 * Stays well within the [Detector] contract: side-effect-free, no I/O, no allocations of note.
 */
@Singleton
class InstagramReelsDetector
    @Inject
    constructor() : Detector {
        override val packageNames: Set<String> = setOf(PACKAGE_MAIN, PACKAGE_LITE)

        override fun detect(
            event: AccessibilityEvent,
            root: AccessibilityNodeInfo?,
        ): Detection {
            val pkg = event.packageName?.toString() ?: return Detection.NotInteresting
            if (pkg !in packageNames) return Detection.NotInteresting

            // Fast path: class name on window-state changes is a reliable signal for the Reels viewer.
            val className = event.className?.toString().orEmpty()
            classifyByClassName(className)?.let { return Detection.ShortFormFeed(it) }

            // Slow path: scan the active window tree. `rootInActiveWindow` returns null on contentChanged
            // events for windows we don't own; that's fine — we just bail.
            val rootNode = root ?: return Detection.NotInteresting

            classifyByViewIds(rootNode, pkg)?.let { return Detection.ShortFormFeed(it) }

            return Detection.NotInteresting
        }

        /**
         * Map a class-name hint to a specific Reels surface.
         *
         * Returns `null` if no class-name pattern matches; the caller falls back to the view-ID scan.
         * Matching is case-insensitive and substring-based — Instagram ships obfuscated class names,
         * so we look for human-readable fragments inside whatever the OS hands us.
         */
        private fun classifyByClassName(className: String): String? {
            if (className.isEmpty()) return null
            for ((needle, surface) in REELS_CLASS_HINTS) {
                if (className.contains(needle, ignoreCase = true)) return surface
            }
            return null
        }

        /**
         * Map a view ID found in the live node tree to a specific Reels surface.
         *
         * IDs are checked most-specific first (DM, explore, tab) and the generic clips viewer last —
         * because the generic viewer is reused for every entry path, a more specific match higher up
         * gives us a better surface label for stats.
         */
        private fun classifyByViewIds(
            root: AccessibilityNodeInfo,
            pkg: String,
        ): String? {
            for ((suffix, surface) in REELS_VIEW_IDS) {
                val fullId = "$pkg:id/$suffix"
                val matches = root.findAccessibilityNodeInfosByViewId(fullId)
                if (!matches.isNullOrEmpty()) return surface
            }
            return null
        }

        companion object {
            /** Canonical Instagram package (the one used in logs and stats). */
            const val PACKAGE_MAIN: String = "com.instagram.android"

            /** Instagram Lite — separate APK, same internal `clips_*` naming. */
            const val PACKAGE_LITE: String = "com.instagram.lite"

            /** Stable surface identifier for the dedicated Reels tab in the bottom navigation. */
            const val SURFACE_TAB: String = "instagram-reels-tab"

            /** Stable surface identifier for an inline Reel scrolled into view in the home feed. */
            const val SURFACE_FEED: String = "instagram-reels-feed"

            /** Stable surface identifier for a Reel opened from the Explore grid. */
            const val SURFACE_EXPLORE: String = "instagram-reels-explore"

            /** Stable surface identifier for a Reel watched after tapping a DM-shared share. */
            const val SURFACE_DM: String = "instagram-reels-dm"

            /**
             * Class-name substring hints, paired with the surface they identify.
             *
             * Instagram ships obfuscated classes, so reliable hints are limited to the small set of
             * fragments/activities that retain human-readable names. Update this list when Instagram
             * ships a UI change that breaks detection. Ordering is most-specific-first so that a DM or
             * Explore match wins over the generic `ClipsViewer*` match.
             *
             * Sources:
             *  - fischman/DisableDistractions (Instagram Reels accessibility detection):
             *    https://github.com/fischman/DisableDistractions
             *  - ReSo7200/InstaEclipse (Xposed module hooking ClipsOrganicMediaItemViewMoreOptionsController):
             *    https://github.com/ReSo7200/InstaEclipse
             */
            @JvmStatic
            internal val REELS_CLASS_HINTS: Array<Pair<String, String>> =
                arrayOf(
                    "ReelsFromDirect" to SURFACE_DM,
                    "DirectReels" to SURFACE_DM,
                    "ClipsViewerFromDirect" to SURFACE_DM,
                    "ClipsViewerExplore" to SURFACE_EXPLORE,
                    "ReelsExplore" to SURFACE_EXPLORE,
                    "ClipsTabFragment" to SURFACE_TAB,
                    "ReelsTabFragment" to SURFACE_TAB,
                    "ClipsViewerFragment" to SURFACE_TAB,
                    "ReelsViewerFragment" to SURFACE_TAB,
                    "ClipsViewerActivity" to SURFACE_TAB,
                    "IgReelsHostFragment" to SURFACE_TAB,
                )

            /**
             * View-ID suffixes for Reels containers, paired with the surface they identify.
             *
             * Format is the bare suffix (e.g. `"clips_viewer"`) so the same array works for both the
             * main and Lite packages — at lookup time we prepend the active package name. Ordering is
             * most-specific-first: the DM/Explore-prefixed containers win, then the dedicated tab pager,
             * and finally the generic `clips_viewer` fallback which is reused across every entry path.
             *
             * Sources:
             *  - fischman/DisableDistractions (confirms `clips_tab` and `clips_header_title` view IDs):
             *    https://github.com/fischman/DisableDistractions
             *  - Community APK analysis of `com.instagram.android` Reels layouts (the `clips_*` prefix
             *    is Instagram's internal name for the Reels feature, also visible in the
             *    `/clips/discover/stream/` API endpoint hooked by ReSo7200/InstaEclipse).
             */
            @JvmStatic
            internal val REELS_VIEW_IDS: Array<Pair<String, String>> =
                arrayOf(
                    "direct_clips_viewer" to SURFACE_DM,
                    "explore_clips_viewer" to SURFACE_EXPLORE,
                    "clips_header_title" to SURFACE_FEED,
                    "clips_tab" to SURFACE_TAB,
                    "clips_viewer_view_pager" to SURFACE_TAB,
                    "clips_viewer" to SURFACE_TAB,
                    "clips_video_container" to SURFACE_TAB,
                    "reels_player_video_layout" to SURFACE_TAB,
                )
        }
    }

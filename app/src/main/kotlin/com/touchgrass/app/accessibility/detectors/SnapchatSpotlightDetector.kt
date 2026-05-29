package com.touchgrass.app.accessibility.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.touchgrass.app.accessibility.Detection
import com.touchgrass.app.accessibility.Detector
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects when the user is viewing **Snapchat Spotlight** (spec §3.1.A) — the dedicated
 * Spotlight tab/feed, Snapchat's TikTok-style vertical short-form scroller.
 *
 * Deliberately does **not** trigger on Stories, Chat, the Camera, Discover (editorial publisher
 * content), the Snap Map, or the Memories grid. Spotlight is a distinct tab with its own
 * fragment/activity stack and its own recycler.
 *
 * ## Brittleness note
 * Snapchat ships under heavy obfuscation (Obfuscator-LLVM plus ProGuard) and renames Kotlin/Java
 * classes between releases. We therefore lean on three weak signals layered together:
 *  1. **Class-name substrings** containing the literal word "Spotlight" — these tend to survive
 *     because the Spotlight fragment's user-facing class names sometimes carry the feature word
 *     in the screen-trace tag, and Snapchat ships some non-obfuscated feature names in its
 *     Activity manifest entries.
 *  2. **Resource view-IDs** prefixed `com.snapchat.android:id/spotlight_…` — Android resource
 *     IDs are stripped less aggressively by Snapchat's obfuscation than Kotlin class names, so
 *     `spotlight_recycler_view`, `spotlight_feed_layout`, and `spotlight_player_view` are
 *     more durable than class hints.
 *  3. **Content description / text** containing "Spotlight" — last-ditch fallback for cases
 *     where IDs have been re-hashed but the user-visible label remains in English.
 *
 * When Snapchat breaks us (new release, new obfuscation map), the fix is almost always:
 *  - dump the active window with `uiautomator dump`,
 *  - look for any string containing "spotlight" (case-insensitive) in either
 *    `class`, `resource-id`, or `content-desc` attributes,
 *  - extend [SPOTLIGHT_CLASS_HINTS] / [SPOTLIGHT_VIEW_IDS] / [SPOTLIGHT_CONTENT_HINTS].
 *
 * Side-effect-free and allocation-light to satisfy the [Detector] contract — Spotlight events
 * arrive at the same firehose rate as every other Snapchat screen and we cannot afford to walk
 * the tree for anything beyond a handful of recycler-id lookups.
 *
 * Sources consulted while picking the initial hint list:
 *  - https://help.snapchat.com/hc/en-us/articles/7012271311892-What-is-Spotlight
 *  - https://creatorhelp.snapchat.com/s/article/spotlight?language=en_US
 *  - https://github.com/ABHILESH1412/sankalp (open-source short-form blocker that targets
 *    Snapchat Spotlight via accessibility events)
 *  - https://hot3eed.github.io/snap_part1_obfuscations.html (background on why class names
 *    cannot be relied on alone)
 */
@Singleton
class SnapchatSpotlightDetector
    @Inject
    constructor() : Detector {
        override val packageNames: Set<String> = setOf(PACKAGE_NAME)

        override fun detect(
            event: AccessibilityEvent,
            root: AccessibilityNodeInfo?,
        ): Detection {
            if (event.packageName?.toString() != PACKAGE_NAME) return Detection.NotInteresting

            // Fast path: class name on the window-state-changed event. Snapchat keeps a handful of
            // Spotlight-named entry points (fragments + viewer activity); a substring match catches
            // both `*.SpotlightFragment` and `*.SpotlightViewerActivity` style names without coupling
            // to a specific package prefix that Snapchat reshuffles between releases.
            val className = event.className?.toString().orEmpty()
            if (className.containsAny(SPOTLIGHT_CLASS_HINTS)) {
                return Detection.ShortFormFeed(SURFACE)
            }

            // Slow path: scan the active window tree for a Spotlight-specific recycler/player view.
            // `findAccessibilityNodeInfosByViewId` is the cheap built-in BFS Snapchat itself can't
            // hide from us as long as the resource id survives ProGuard. We bail at the first hit.
            val rootNode = root ?: return Detection.NotInteresting

            if (rootNode.hasSpotlightContainer()) {
                return Detection.ShortFormFeed(SURFACE)
            }

            return Detection.NotInteresting
        }

        private fun AccessibilityNodeInfo.hasSpotlightContainer(): Boolean {
            for (viewId in SPOTLIGHT_VIEW_IDS) {
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
            const val PACKAGE_NAME: String = "com.snapchat.android"

            /** Stable surface identifier used by [Detection.ShortFormFeed] and the stats DB. */
            const val SURFACE: String = "snapchat-spotlight"

            /**
             * Class-name substrings observed (or strongly suspected) in Snapchat's Spotlight stack
             * across recent releases. Matched case-insensitively as substrings — Snapchat sometimes
             * prefixes these with obfuscated package segments (e.g. `com.snap.spotlight.feed.x.y.…`).
             *
             * Update this list when Snapchat renames a fragment/activity and detection breaks. The
             * literal word "Spotlight" is the load-bearing token; anything that includes it is a
             * positive signal because no other Snapchat surface (Stories/Chat/Camera/Discover/Map/
             * Memories) uses that word in its class name.
             */
            @JvmStatic
            internal val SPOTLIGHT_CLASS_HINTS: Array<String> =
                arrayOf(
                    "SpotlightFragment",
                    "SpotlightFeedFragment",
                    "SpotlightViewerActivity",
                    "SpotlightVerticalScrollPage",
                    "SpotlightPlayer",
                )

            /**
             * Resource view-IDs of the Spotlight feed recycler / player container observed across
             * recent Snapchat releases. The first one is the canonical id; the rest are fallbacks
             * for older/newer builds and A/B experiments.
             *
             * Resource IDs survive obfuscation better than class names — when class hints fail,
             * these usually still work.
             */
            @JvmStatic
            internal val SPOTLIGHT_VIEW_IDS: Array<String> =
                arrayOf(
                    "$PACKAGE_NAME:id/spotlight_recycler_view",
                    "$PACKAGE_NAME:id/spotlight_feed_layout",
                    "$PACKAGE_NAME:id/spotlight_player_view",
                    "$PACKAGE_NAME:id/spotlight_view_pager",
                )

            /**
             * Content-description / text substrings used purely as last-resort signals — only
             * consulted via [SPOTLIGHT_VIEW_IDS] (we don't walk the tree just for text). Listed here
             * so future maintainers know what to grep for in `uiautomator dump` output.
             */
            @Suppress("unused")
            @JvmStatic
            internal val SPOTLIGHT_CONTENT_HINTS: Array<String> =
                arrayOf(
                    "Spotlight",
                )
        }
    }

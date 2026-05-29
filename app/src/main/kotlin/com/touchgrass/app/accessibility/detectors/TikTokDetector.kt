package com.touchgrass.app.accessibility.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.touchgrass.app.accessibility.Detection
import com.touchgrass.app.accessibility.Detector
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects when the user is viewing a TikTok vertical-scroll feed — either **For You** or
 * **Following** (spec §3.1.A). Both surfaces are blockable; the rest of TikTok (DMs, profile,
 * search, notifications, the camera/recording screen) is NOT.
 *
 * **Maintenance hazard: TikTok class names are obfuscated in release builds.**
 * TikTok ships under R8/ProGuard with aggressive obfuscation, so `event.className` tends to
 * look like `X.A0` or similar — useless as a signal. View IDs are also reported to rotate
 * across builds (some are even dynamically generated at runtime). That means this detector
 * leans on the most stable signals available: well-known feed container view IDs as a
 * fast-path, falling back to the unique cluster of right-rail action buttons (like / comment /
 * share) by content description, and the top tab labels ("For You" / "Following") to pick a
 * surface name.
 *
 * Strategy, in order:
 *  1. Cheap package check.
 *  2. Scan the node tree for a known feed-container view ID. Bail with [Detection.NotInteresting]
 *     if none is found — this both excludes DMs/profile/search and short-circuits the more
 *     expensive content-description sweep below.
 *  3. Confirm with the right-rail action cluster (like + comment + share content descriptions).
 *     This guards against false positives on screens that happen to share a container id.
 *  4. Pick a [Detection.ShortFormFeed] surface — `tiktok-following` if a "Following" tab is
 *     marked selected, otherwise `tiktok-foryou` as the conservative default (the For You feed
 *     is what opens on cold launch and is by far the more common surface).
 *
 * If the tab labels are not visible in the tree (some builds collapse them after scroll), we
 * still ship a positive detection — the user IS in a feed — we just default to `tiktok-foryou`.
 * Combining the two into a single `tiktok-feed` surface was considered, but per spec §3.1.A
 * both `tiktok-foryou` and `tiktok-following` are first-class surfaces, and the tab text is
 * stable user-visible UI so the discrimination is worth attempting.
 *
 * **Research sources for the hints below:**
 *  - Package list: TikTok Asia/SEA region uses `com.ss.android.ugc.trill`; rest-of-world uses
 *    `com.zhiliaoapp.musically`. Confirmed via APKPure and Microsoft Security Research Center
 *    coverage of the 2022 CVE-2022-28799 disclosure.
 *  - View ID volatility: community reports on BlackHatWorld and Appium forums note TikTok
 *    rotates resource IDs on the For You feed, which is why the right-rail content-description
 *    confirmation is required.
 *
 * Stays well within the [Detector] contract: side-effect-free, no I/O.
 */
@Singleton
class TikTokDetector @Inject constructor() : Detector {

    override val packageNames: Set<String> = setOf(PACKAGE_MUSICALLY, PACKAGE_TRILL)

    override fun detect(event: AccessibilityEvent, root: AccessibilityNodeInfo?): Detection {
        val pkg = event.packageName?.toString() ?: return Detection.NotInteresting
        if (pkg !in packageNames) return Detection.NotInteresting

        val rootNode = root ?: return Detection.NotInteresting

        // Step 1: cheap-ish view-id sweep for any known feed container.
        if (!rootNode.hasFeedContainer(pkg)) return Detection.NotInteresting

        // Step 2: confirm with the right-rail action cluster. This excludes screens that might
        // re-use a similarly-named container (rare, but worth the guard).
        if (!rootNode.hasFeedActionCluster()) return Detection.NotInteresting

        // Step 3: pick the surface. Prefer "Following" only if its tab is explicitly selected;
        // default to For You otherwise.
        val surface = if (rootNode.followingTabSelected()) SURFACE_FOLLOWING else SURFACE_FOR_YOU
        return Detection.ShortFormFeed(surface)
    }

    private fun AccessibilityNodeInfo.hasFeedContainer(pkg: String): Boolean {
        for (idSuffix in FEED_VIEW_ID_SUFFIXES) {
            val matches = findAccessibilityNodeInfosByViewId("$pkg:id/$idSuffix")
            if (!matches.isNullOrEmpty()) return true
        }
        return false
    }

    private fun AccessibilityNodeInfo.hasFeedActionCluster(): Boolean {
        var likeSeen = false
        var commentSeen = false
        var shareSeen = false
        walk { node ->
            val desc = node.contentDescription?.toString()?.lowercase() ?: return@walk
            if (!likeSeen && LIKE_HINTS.any { desc.contains(it) }) likeSeen = true
            if (!commentSeen && COMMENT_HINTS.any { desc.contains(it) }) commentSeen = true
            if (!shareSeen && SHARE_HINTS.any { desc.contains(it) }) shareSeen = true
        }
        // Require at least two of the three — TikTok occasionally relabels one button per locale
        // or A/B test, but it's extremely unlikely two of like/comment/share are simultaneously
        // missing on a real feed screen.
        val score = (if (likeSeen) 1 else 0) + (if (commentSeen) 1 else 0) + (if (shareSeen) 1 else 0)
        return score >= MIN_ACTION_CLUSTER_MATCHES
    }

    private fun AccessibilityNodeInfo.followingTabSelected(): Boolean {
        var found = false
        walk { node ->
            if (found) return@walk
            val text = node.text?.toString()?.lowercase() ?: return@walk
            if (FOLLOWING_TAB_LABELS.any { text == it } && node.isSelected) {
                found = true
            }
        }
        return found
    }

    /**
     * Depth-first walk over the node tree, applying [visit] to each node. Bounded by
     * [MAX_NODES_WALKED] so a pathological tree can't hang us — TikTok's feed cells are
     * deep but well under this ceiling in practice.
     */
    private inline fun AccessibilityNodeInfo.walk(visit: (AccessibilityNodeInfo) -> Unit) {
        val stack = ArrayDeque<AccessibilityNodeInfo>()
        stack.addLast(this)
        var visited = 0
        while (stack.isNotEmpty() && visited < MAX_NODES_WALKED) {
            val node = stack.removeLast()
            visit(node)
            visited++
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                stack.addLast(child)
            }
        }
    }

    companion object {
        /** Rest-of-world TikTok build (canonical, used for logging). */
        const val PACKAGE_MUSICALLY: String = "com.zhiliaoapp.musically"

        /** East/Southeast Asia TikTok build — same UI, same detector. */
        const val PACKAGE_TRILL: String = "com.ss.android.ugc.trill"

        /** Stable surface identifier for the For You feed. */
        const val SURFACE_FOR_YOU: String = "tiktok-foryou"

        /** Stable surface identifier for the Following feed. */
        const val SURFACE_FOLLOWING: String = "tiktok-following"

        /**
         * View ID suffixes (the part after `<pkg>:id/`) observed on the TikTok feed across recent
         * builds. The list is intentionally broad because TikTok rotates these — update it when
         * a release breaks detection. Each candidate is tried against BOTH package names.
         */
        @JvmStatic
        internal val FEED_VIEW_ID_SUFFIXES: Array<String> = arrayOf(
            "feed_layout",
            "feed_recycler_view",
            "video_play_recycler",
            "main_tab_feed",
            "vertical_view_pager",
            "video_container",
        )

        /**
         * Content-description substrings that identify the right-rail "Like" affordance.
         * Lowercased before matching. Multi-locale hints kept short to limit false positives.
         */
        @JvmStatic
        internal val LIKE_HINTS: Array<String> = arrayOf(
            "like",
            "likes",
        )

        /** Content-description substrings that identify the right-rail "Comment" affordance. */
        @JvmStatic
        internal val COMMENT_HINTS: Array<String> = arrayOf(
            "comment",
            "comments",
        )

        /** Content-description substrings that identify the right-rail "Share" affordance. */
        @JvmStatic
        internal val SHARE_HINTS: Array<String> = arrayOf(
            "share",
        )

        /**
         * Top-tab labels for the Following feed. Lowercased before matching. Add localized
         * variants here if a market starts reporting false-negatives.
         */
        @JvmStatic
        internal val FOLLOWING_TAB_LABELS: Array<String> = arrayOf(
            "following",
        )

        /** Minimum number of like/comment/share buttons that must match to confirm a feed. */
        internal const val MIN_ACTION_CLUSTER_MATCHES: Int = 2

        /** Safety bound on the node-tree walk. TikTok cells are deep but nowhere near this. */
        internal const val MAX_NODES_WALKED: Int = 2_000
    }
}

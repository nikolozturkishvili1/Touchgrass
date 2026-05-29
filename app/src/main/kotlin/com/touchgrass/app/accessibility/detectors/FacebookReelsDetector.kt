package com.touchgrass.app.accessibility.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.touchgrass.app.accessibility.Detection
import com.touchgrass.app.accessibility.Detector
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects when the user is viewing Facebook **Reels** (spec §3.1.A).
 *
 * Two surfaces are distinguished:
 *  - [SURFACE_VIEWER] — the immersive full-screen Reels viewer that opens when the user taps a
 *    Reel from anywhere in the app. This is the high-engagement doomscroll trap V1 promises to
 *    block.
 *  - [SURFACE_TAB] — the Reels tab opened from the bottom nav / hamburger menu.
 *
 * Per the spec (§2.1, §3.1.A) Facebook Reels bypassing is the #3 review complaint about
 * competitor apps; this detector is one of the most user-visible in the suite.
 *
 * ### What we deliberately do NOT trigger on
 * Inline Reels carousels embedded in the News Feed are *not* doomscroll traps — they're discrete
 * previews the user has to actually tap into. We restrict positive detection to the immersive
 * viewer (or the dedicated Reels tab). If we triggered on every inline Reel we'd block normal
 * feed browsing, which would erode trust faster than missing the occasional Reel session.
 * See [NEWS_FEED_CLASS_HINTS] for the explicit anti-match list that suppresses news-feed
 * activity even if a Reels-shaped substring happens to appear in some inner view id.
 *
 * ### Why detection is fuzzy
 * Facebook ships a heavily R8/ProGuard-obfuscated APK with frequent A/B-tested UI variants.
 * Most internal class names rotate every few releases and many user-facing fragments end up
 * as opaque names like `X.Apc` post-obfuscation. We therefore lean on two signals that are
 * *less* aggressively obfuscated:
 *  1. Class-name substrings of activities/fragments whose names are retained because they are
 *     entry points referenced from `AndroidManifest.xml` (e.g. `FbMainTabActivity`) or from
 *     deep-link/intent handling (e.g. anything in the public `com.facebook.reels.*` namespace).
 *  2. Android resource IDs (`packageName:id/...`). Resource IDs survive obfuscation because the
 *     resource table is keyed by name; Facebook's `aapt`-generated R values stay stable across
 *     a release. View IDs containing literal `reel`/`reels` tokens are the most reliable
 *     long-lived hooks we have.
 *
 * Both hint arrays are intentionally a little over-broad — false positives on a non-Reels
 * Facebook screen are cheap (a CTA shows), false negatives mean a user gets a free doomscroll
 * session.
 *
 * ### Facebook Lite (`com.facebook.lite`)
 * Facebook Lite is a separate, much smaller code base with a different UI shell. As of writing
 * Lite uses a stripped video viewer rather than a dedicated Reels tab; detection on Lite is
 * **best-effort and weaker** than on the main app — most of the hints below are designed for
 * `com.facebook.katana` and may not match on Lite at all. Future maintainers should consider
 * adding Lite-specific hints (likely with a `_lite` view-id prefix) when Lite gains a real
 * Reels surface.
 *
 * ### Maintenance
 * When Facebook breaks this detector (expect it once or twice a year), update
 * [REELS_VIEWER_CLASS_HINTS], [REELS_TAB_CLASS_HINTS], and [REELS_VIEW_IDS] below. The class
 * and view-ID lists are the only knobs; the algorithm should stay the same.
 *
 * ### Research sources
 *  - Facebook Android manifest survey (snarfed/open-in-app) — confirms `FbMainTabActivity` and
 *    a number of `com.facebook.katana.activity.*` activity classes that retain their names
 *    through obfuscation:
 *    https://github.com/snarfed/open-in-app/blob/master/app_manifests/com.facebook.katana_AndroidManifest.xml
 *  - Meta Reels SDK docs — confirms the `com.facebook.reels.*` intent/action namespace as the
 *    public-facing Reels component family:
 *    https://developers.facebook.com/docs/android/sharing-to-reels-facebook
 */
@Singleton
class FacebookReelsDetector @Inject constructor() : Detector {

    override val packageNames: Set<String> = setOf(PACKAGE_KATANA, PACKAGE_LITE)

    override fun detect(event: AccessibilityEvent, root: AccessibilityNodeInfo?): Detection {
        val pkg = event.packageName?.toString() ?: return Detection.NotInteresting
        if (pkg !in packageNames) return Detection.NotInteresting

        // TEMPORARY diagnostic — see [DIAGNOSTIC]. Wrapped so it can never alter detection.
        if (DIAGNOSTIC) runCatching { logFacebookDiagnostics(event, root) }

        val className = event.className?.toString().orEmpty()

        // Anti-match: News Feed activities/fragments. Inline Reels in the feed must not trigger
        // us. If the foreground class is unambiguously the news feed, bail before any further
        // matching — even if the feed happens to contain a "reel"-shaped node inside it.
        if (className.containsAny(NEWS_FEED_CLASS_HINTS)) {
            return Detection.NotInteresting
        }

        // Fast path A: immersive Reels viewer class name.
        if (className.containsAny(REELS_VIEWER_CLASS_HINTS)) {
            return Detection.ShortFormFeed(SURFACE_VIEWER)
        }

        // Fast path B: dedicated Reels tab.
        if (className.containsAny(REELS_TAB_CLASS_HINTS)) {
            return Detection.ShortFormFeed(SURFACE_TAB)
        }

        // Slow path: scan the active window for known Reels-viewer resource IDs. We only walk
        // the tree if we have one (events for foreign windows often arrive with `root == null`).
        val rootNode = root ?: return Detection.NotInteresting

        if (rootNode.hasReelsContainer(pkg)) {
            return Detection.ShortFormFeed(SURFACE_VIEWER)
        }

        return Detection.NotInteresting
    }

    /**
     * Returns true if [this] active-window root contains any node whose view ID matches one of
     * [REELS_VIEW_IDS]. We test against both the katana and lite package namespaces so a single
     * implementation handles both builds.
     */
    private fun AccessibilityNodeInfo.hasReelsContainer(pkg: String): Boolean {
        for (suffix in REELS_VIEW_IDS) {
            val matches = findAccessibilityNodeInfosByViewId("$pkg:id/$suffix")
            if (!matches.isNullOrEmpty()) return true
        }
        return false
    }

    private fun String.containsAny(needles: Array<String>): Boolean {
        for (needle in needles) if (contains(needle, ignoreCase = true)) return true
        return false
    }

    // ----------------------------------------------------------------------------------------
    // TEMPORARY diagnostic instrumentation — DELETE once the hint arrays are updated.
    // ----------------------------------------------------------------------------------------

    /** Last dumped tree signature — suppresses the content-changed flood so each distinct
     *  screen logs once, not dozens of times. Not synchronized by design: accessibility events
     *  are delivered serially, and a stray duplicate log line is harmless. */
    private var lastDiagSignature: String? = null

    /**
     * Dumps every signal the accessibility tree exposes for a Facebook screen — class names,
     * view ids, content descriptions and text — so we can find a usable detection signal.
     * Facebook's Litho-rendered UI exposes almost no view ids, so content descriptions / text
     * are the last hope. Logs only; never changes detection. De-duplicated per screen.
     */
    private fun logFacebookDiagnostics(event: AccessibilityEvent, root: AccessibilityNodeInfo?) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
        ) {
            return
        }
        val node = root ?: return
        val classes = LinkedHashSet<String>()
        val ids = LinkedHashSet<String>()
        val descs = LinkedHashSet<String>()
        val texts = LinkedHashSet<String>()
        collectSignals(node, classes, ids, descs, texts, depth = 0)

        val signature = "$classes|$ids|$descs|$texts"
        if (signature == lastDiagSignature) return
        lastDiagSignature = signature

        Timber.tag(DIAG_TAG).i("--- Facebook screen (eventClass=%s) ---", event.className)
        Timber.tag(DIAG_TAG).i("nodeClasses(%d): %s", classes.size, classes.joinToString(" "))
        Timber.tag(DIAG_TAG).i("viewIds(%d): %s", ids.size, ids.joinToString(" "))
        Timber.tag(DIAG_TAG).i("contentDescriptions(%d): %s", descs.size, descs.joinToString(" | "))
        Timber.tag(DIAG_TAG).i("texts(%d): %s", texts.size, texts.joinToString(" | "))
    }

    /** Depth-first walk collecting class names, view ids, content descriptions and text. */
    private fun collectSignals(
        node: AccessibilityNodeInfo,
        classes: MutableSet<String>,
        ids: MutableSet<String>,
        descs: MutableSet<String>,
        texts: MutableSet<String>,
        depth: Int,
    ) {
        if (depth > 80) return
        node.className?.toString()?.takeIf { it.isNotBlank() }?.let { classes.add(it) }
        node.viewIdResourceName?.takeIf { it.isNotBlank() }?.let { ids.add(it) }
        node.contentDescription?.toString()?.takeIf { it.isNotBlank() }?.let { descs.add(it) }
        node.text?.toString()?.takeIf { it.isNotBlank() }?.let { texts.add(it) }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            collectSignals(child, classes, ids, descs, texts, depth + 1)
        }
    }

    companion object {
        /**
         * TEMPORARY diagnostic switch (added 2026-05-19 to debug Facebook Reels detection).
         *
         * While `true`, [detect] logs the class name and resource ids of every Facebook screen
         * to Logcat under the [DIAG_TAG] tag — **without changing detection behaviour**. Build
         * the *debug* variant (release builds plant no log tree), open Facebook, scroll into
         * Reels, and the log reveals the real class names / view ids to put in the hint arrays.
         *
         * Set back to `false` and delete [logFacebookDiagnostics] / [collectViewIds] once the
         * hints are updated — production detectors must stay side-effect-free and fast.
         */
        const val DIAGNOSTIC: Boolean = true

        /** Logcat tag for the temporary diagnostic. Filter the Logcat window on this string. */
        const val DIAG_TAG: String = "FB-DIAG"

        /** Main Facebook app package. Canonical for logging. */
        const val PACKAGE_KATANA: String = "com.facebook.katana"

        /**
         * Facebook Lite. Detection here is best-effort — see KDoc on the class for caveats.
         */
        const val PACKAGE_LITE: String = "com.facebook.lite"

        /** Stable surface id for the immersive full-screen Reels viewer. */
        const val SURFACE_VIEWER: String = "facebook-reels-viewer"

        /** Stable surface id for the dedicated Reels tab (bottom nav / menu entry). */
        const val SURFACE_TAB: String = "facebook-reels-tab"

        /**
         * Class-name substrings observed on Facebook's immersive Reels viewer across recent
         * releases. Substring + case-insensitive matching is intentional: Facebook ships builds
         * with both `ReelsViewerActivity` and obfuscated wrappers like
         * `com.facebook.reels.viewer.ReelsViewerActivity$Companion`.
         *
         * Update this list when Facebook ships a UI change that breaks detection.
         */
        @JvmStatic
        internal val REELS_VIEWER_CLASS_HINTS: Array<String> = arrayOf(
            "ReelsViewerActivity",
            "ReelsWatchFragment",
            "WatchReelFragment",
            "ImmersiveReelsActivity",
            "FullScreenReelsViewerActivity",
            "com.facebook.reels.viewer",
        )

        /**
         * Class-name substrings observed on the dedicated Reels tab (the one reachable from the
         * bottom nav / hamburger menu). The Watch feed historically rendered Reels but Facebook
         * has progressively split them into a separate destination; both are listed here.
         */
        @JvmStatic
        internal val REELS_TAB_CLASS_HINTS: Array<String> = arrayOf(
            "ReelsTabFragment",
            "ReelsFeedFragment",
            "WatchFeedFragment",
            "WatchReelsFragment",
            "com.facebook.reels.tab",
        )

        /**
         * Class-name substrings that unambiguously identify the News Feed surface. We use this
         * as an explicit anti-match so inline Reel previews inside the feed never trigger the
         * detector. If you find yourself triggering on the feed, *add* to this list rather than
         * removing entries from the Reels hint arrays.
         *
         * ### Every entry MUST be specific to the News Feed
         * The anti-match in [detect] runs **before** the positive Reels checks and uses
         * case-insensitive *substring* matching. So if an entry here is a substring of a Reels
         * hint, it silently turns that Reels hint into dead code. A bare `"FeedFragment"` used
         * to live here and shadowed `"ReelsFeedFragment"` / `"WatchFeedFragment"` in
         * [REELS_TAB_CLASS_HINTS] — Facebook's Reels tab could never be detected. Keep entries
         * narrow (`NewsFeed*`, the `com.facebook.feed` package). `FacebookReelsDetectorTest`
         * has a regression guard that fails if any entry shadows a Reels hint.
         */
        @JvmStatic
        internal val NEWS_FEED_CLASS_HINTS: Array<String> = arrayOf(
            "NewsFeedFragment",
            "NewsFeedActivity",
            "com.facebook.feed",
        )

        /**
         * Resource-name suffixes (without the `packageName:id/` prefix) of containers observed
         * inside the immersive Reels viewer. We probe each one in turn until a hit. The first
         * entry is the canonical/most-stable id; subsequent ids are fallbacks for older or
         * regional builds.
         *
         * Resource names survive R8/ProGuard obfuscation because they're keyed by string in the
         * resource table; these have historically been some of the longest-lived hooks we have
         * into the Facebook app.
         */
        @JvmStatic
        internal val REELS_VIEW_IDS: Array<String> = arrayOf(
            "reels_video_player",
            "reels_fragment_container",
            "reel_viewer_recycler",
            "reels_viewer_pager",
            "reels_viewer_root",
            "reel_player_view",
            "reels_immersive_viewer",
            "reels_tab_fragment_container",
        )
    }
}

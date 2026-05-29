package com.touchgrass.app.accessibility.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.touchgrass.app.accessibility.Detection
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Detector unit tests use MockK to fake the framework `AccessibilityEvent` and
 * `AccessibilityNodeInfo` classes. `unitTests.isReturnDefaultValues = true` in
 * `app/build.gradle.kts` makes the Android stubs return defaults instead of throwing.
 */
class FacebookReelsDetectorTest {
    private val detector = FacebookReelsDetector()

    private fun event(
        pkg: String?,
        className: String? = null,
    ): AccessibilityEvent =
        mockk {
            every { packageName } returns pkg
            every { this@mockk.className } returns className
        }

    /** A node where exactly [viewId] resolves; all other ids return empty. */
    private fun nodeWithViewId(viewId: String): AccessibilityNodeInfo =
        mockk {
            every { findAccessibilityNodeInfosByViewId(any()) } answers {
                if (firstArg<String>() == viewId) listOf(mockk(relaxed = true)) else emptyList()
            }
        }

    private fun emptyNode(): AccessibilityNodeInfo =
        mockk {
            every { findAccessibilityNodeInfosByViewId(any()) } returns emptyList()
        }

    // ---- packageNames ----

    @Test
    fun `packageNames covers both katana and facebook lite`() {
        assertEquals(
            setOf("com.facebook.katana", "com.facebook.lite"),
            detector.packageNames,
        )
    }

    // ---- non-Facebook packages ----

    @Test
    fun `ignores events from non-Facebook packages`() {
        val result = detector.detect(event("com.instagram.android", "ReelsViewerActivity"), null)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores events from YouTube even with reels-like class name`() {
        val result = detector.detect(event("com.google.android.youtube", "ReelsWatchFragment"), null)
        assertEquals(Detection.NotInteresting, result)
    }

    // ---- viewer class-name detection ----

    @Test
    fun `detects immersive Reels viewer by ReelsViewerActivity class name`() {
        val result =
            detector.detect(
                event(FacebookReelsDetector.PACKAGE_KATANA, "com.facebook.reels.viewer.ReelsViewerActivity"),
                null,
            )
        assertEquals(Detection.ShortFormFeed(FacebookReelsDetector.SURFACE_VIEWER), result)
    }

    @Test
    fun `detects immersive Reels viewer by ReelsWatchFragment class name (case insensitive)`() {
        val result =
            detector.detect(
                event(FacebookReelsDetector.PACKAGE_KATANA, "com.facebook.katana.app.reelswatchfragment"),
                null,
            )
        assertEquals(Detection.ShortFormFeed(FacebookReelsDetector.SURFACE_VIEWER), result)
    }

    @Test
    fun `detects immersive viewer on Facebook Lite by class name`() {
        val result =
            detector.detect(
                event(FacebookReelsDetector.PACKAGE_LITE, "com.facebook.reels.viewer.LiteReelsViewerActivity"),
                null,
            )
        assertEquals(Detection.ShortFormFeed(FacebookReelsDetector.SURFACE_VIEWER), result)
    }

    // ---- tab class-name detection ----

    @Test
    fun `detects Reels tab by ReelsTabFragment class name`() {
        val result =
            detector.detect(
                event(FacebookReelsDetector.PACKAGE_KATANA, "com.facebook.katana.tab.ReelsTabFragment"),
                null,
            )
        assertEquals(Detection.ShortFormFeed(FacebookReelsDetector.SURFACE_TAB), result)
    }

    @Test
    fun `detects Reels tab by WatchFeedFragment class name`() {
        // Regression: "WatchFeedFragment" contains the substring "FeedFragment". A too-broad
        // News-Feed anti-match hint once suppressed this surface before the tab check ran.
        val result =
            detector.detect(
                event(FacebookReelsDetector.PACKAGE_KATANA, "com.facebook.video.watch.WatchFeedFragment"),
                null,
            )
        assertEquals(Detection.ShortFormFeed(FacebookReelsDetector.SURFACE_TAB), result)
    }

    @Test
    fun `detects Reels tab by ReelsFeedFragment class name`() {
        // Regression: "ReelsFeedFragment" also contains the substring "FeedFragment" and was
        // shadowed by the same too-broad anti-match hint.
        val result =
            detector.detect(
                event(FacebookReelsDetector.PACKAGE_KATANA, "com.facebook.katana.reels.ReelsFeedFragment"),
                null,
            )
        assertEquals(Detection.ShortFormFeed(FacebookReelsDetector.SURFACE_TAB), result)
    }

    // ---- view-id detection ----

    @Test
    fun `detects via reels_video_player view id`() {
        val root = nodeWithViewId("${FacebookReelsDetector.PACKAGE_KATANA}:id/reels_video_player")
        val result =
            detector.detect(
                event(FacebookReelsDetector.PACKAGE_KATANA, "androidx.fragment.app.FragmentContainerView"),
                root,
            )
        assertEquals(Detection.ShortFormFeed(FacebookReelsDetector.SURFACE_VIEWER), result)
    }

    @Test
    fun `detects via fallback reel_viewer_recycler view id`() {
        val root = nodeWithViewId("${FacebookReelsDetector.PACKAGE_KATANA}:id/reel_viewer_recycler")
        val result =
            detector.detect(
                event(FacebookReelsDetector.PACKAGE_KATANA, "FrameLayout"),
                root,
            )
        assertEquals(Detection.ShortFormFeed(FacebookReelsDetector.SURFACE_VIEWER), result)
    }

    @Test
    fun `detects via reels_fragment_container view id on Facebook Lite`() {
        val root = nodeWithViewId("${FacebookReelsDetector.PACKAGE_LITE}:id/reels_fragment_container")
        val result =
            detector.detect(
                event(FacebookReelsDetector.PACKAGE_LITE, "FrameLayout"),
                root,
            )
        assertEquals(Detection.ShortFormFeed(FacebookReelsDetector.SURFACE_VIEWER), result)
    }

    // ---- News Feed anti-match ----

    @Test
    fun `does not trigger on NewsFeedFragment even with reels-shaped className substring`() {
        // Tricky case: the foreground is the News Feed, but the class string happens to contain
        // the word "Reels" (e.g. an inline carousel hosted inside the feed fragment).
        val result =
            detector.detect(
                event(
                    FacebookReelsDetector.PACKAGE_KATANA,
                    "com.facebook.feed.NewsFeedFragment\$ReelsCarouselSection",
                ),
                null,
            )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `does not trigger on plain NewsFeedFragment`() {
        val result =
            detector.detect(
                event(FacebookReelsDetector.PACKAGE_KATANA, "com.facebook.feed.NewsFeedFragment"),
                emptyNode(),
            )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `does not trigger on NewsFeedActivity`() {
        val result =
            detector.detect(
                event(FacebookReelsDetector.PACKAGE_KATANA, "com.facebook.katana.NewsFeedActivity"),
                null,
            )
        assertEquals(Detection.NotInteresting, result)
    }

    // ---- generic / negative cases ----

    @Test
    fun `ignores generic Facebook screens with no Reels markers`() {
        val result =
            detector.detect(
                event(FacebookReelsDetector.PACKAGE_KATANA, "com.facebook.katana.activity.FbMainTabActivity"),
                emptyNode(),
            )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores Facebook events when root is null and class is non-Reels`() {
        val result =
            detector.detect(
                event(FacebookReelsDetector.PACKAGE_KATANA, "FrameLayout"),
                null,
            )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores Facebook events with null package`() {
        val result = detector.detect(event(null, "ReelsViewerActivity"), null)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores Facebook events with null className and empty root`() {
        val result =
            detector.detect(
                event(FacebookReelsDetector.PACKAGE_KATANA, null),
                emptyNode(),
            )
        assertEquals(Detection.NotInteresting, result)
    }

    // ---- sanity: the hint arrays are populated ----

    @Test
    fun `viewer class hints are non-empty so detection is actually wired up`() {
        assertTrue(FacebookReelsDetector.REELS_VIEWER_CLASS_HINTS.isNotEmpty())
    }

    @Test
    fun `no News Feed anti-match hint shadows a Reels class hint`() {
        // The anti-match runs before the positive Reels checks and matches by substring, so an
        // anti-match entry that is a substring of a Reels hint turns that hint into dead code.
        // A bare "FeedFragment" once shadowed "ReelsFeedFragment" / "WatchFeedFragment".
        val reelsHints =
            FacebookReelsDetector.REELS_VIEWER_CLASS_HINTS +
                FacebookReelsDetector.REELS_TAB_CLASS_HINTS
        for (antiHint in FacebookReelsDetector.NEWS_FEED_CLASS_HINTS) {
            for (reelsHint in reelsHints) {
                assertFalse(
                    "News-feed anti-match \"$antiHint\" is a substring of Reels hint " +
                        "\"$reelsHint\" — it would suppress detection of that surface",
                    reelsHint.contains(antiHint, ignoreCase = true),
                )
            }
        }
    }

    @Test
    fun `view id fallback list has multiple entries`() {
        // Spec mandates "up to 8" fallback IDs — make sure we actually have a fallback list, not
        // a single brittle id.
        assertTrue(FacebookReelsDetector.REELS_VIEW_IDS.size >= 2)
        assertTrue(FacebookReelsDetector.REELS_VIEW_IDS.size <= 8)
    }
}

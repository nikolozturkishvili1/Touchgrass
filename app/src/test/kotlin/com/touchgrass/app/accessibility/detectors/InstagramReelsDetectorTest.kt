package com.touchgrass.app.accessibility.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.touchgrass.app.accessibility.Detection
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Detector unit tests use MockK to fake the framework `AccessibilityEvent` and
 * `AccessibilityNodeInfo` classes. `unitTests.isReturnDefaultValues = true` in
 * `app/build.gradle.kts` makes the Android stubs return defaults instead of throwing.
 */
class InstagramReelsDetectorTest {

    private val detector = InstagramReelsDetector()

    private fun event(pkg: String?, className: String? = null): AccessibilityEvent = mockk {
        every { packageName } returns pkg
        every { this@mockk.className } returns className
    }

    private fun nodeWithViewId(fullViewId: String): AccessibilityNodeInfo = mockk {
        every { findAccessibilityNodeInfosByViewId(any()) } answers {
            if (firstArg<String>() == fullViewId) listOf(mockk(relaxed = true)) else emptyList()
        }
    }

    private fun emptyNode(): AccessibilityNodeInfo = mockk {
        every { findAccessibilityNodeInfosByViewId(any()) } returns emptyList()
    }

    @Test
    fun `ignores events from other packages`() {
        val result = detector.detect(event("com.google.android.youtube", "ClipsViewerFragment"), null)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores events with null package name`() {
        val result = detector.detect(event(null, "ClipsViewerFragment"), null)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `detects reels tab via ClipsViewerFragment class name`() {
        val result = detector.detect(
            event(InstagramReelsDetector.PACKAGE_MAIN, "com.instagram.clips.viewer.ClipsViewerFragment"),
            null,
        )
        assertEquals(Detection.ShortFormFeed(InstagramReelsDetector.SURFACE_TAB), result)
    }

    @Test
    fun `detects reels tab via ClipsViewerActivity class name (case insensitive)`() {
        val result = detector.detect(
            event(InstagramReelsDetector.PACKAGE_MAIN, "com.instagram.clips.intf.clipsvieweractivity"),
            null,
        )
        assertEquals(Detection.ShortFormFeed(InstagramReelsDetector.SURFACE_TAB), result)
    }

    @Test
    fun `detects dm-shared reels via ReelsFromDirect class name`() {
        val result = detector.detect(
            event(InstagramReelsDetector.PACKAGE_MAIN, "com.instagram.direct.ReelsFromDirectFragment"),
            null,
        )
        assertEquals(Detection.ShortFormFeed(InstagramReelsDetector.SURFACE_DM), result)
    }

    @Test
    fun `detects explore reels via ClipsViewerExplore class name`() {
        val result = detector.detect(
            event(InstagramReelsDetector.PACKAGE_MAIN, "com.instagram.explore.ClipsViewerExploreFragment"),
            null,
        )
        assertEquals(Detection.ShortFormFeed(InstagramReelsDetector.SURFACE_EXPLORE), result)
    }

    @Test
    fun `detects reels tab via clips_viewer view id when class name is generic`() {
        val root = nodeWithViewId("${InstagramReelsDetector.PACKAGE_MAIN}:id/clips_viewer")
        val result = detector.detect(event(InstagramReelsDetector.PACKAGE_MAIN, "FrameLayout"), root)
        assertEquals(Detection.ShortFormFeed(InstagramReelsDetector.SURFACE_TAB), result)
    }

    @Test
    fun `detects reels tab via fallback clips_viewer_view_pager view id`() {
        val root = nodeWithViewId("${InstagramReelsDetector.PACKAGE_MAIN}:id/clips_viewer_view_pager")
        val result = detector.detect(event(InstagramReelsDetector.PACKAGE_MAIN, "FrameLayout"), root)
        assertEquals(Detection.ShortFormFeed(InstagramReelsDetector.SURFACE_TAB), result)
    }

    @Test
    fun `detects inline feed reel via clips_header_title view id`() {
        val root = nodeWithViewId("${InstagramReelsDetector.PACKAGE_MAIN}:id/clips_header_title")
        val result = detector.detect(event(InstagramReelsDetector.PACKAGE_MAIN, "RecyclerView"), root)
        assertEquals(Detection.ShortFormFeed(InstagramReelsDetector.SURFACE_FEED), result)
    }

    @Test
    fun `detects dm reel via direct_clips_viewer view id`() {
        val root = nodeWithViewId("${InstagramReelsDetector.PACKAGE_MAIN}:id/direct_clips_viewer")
        val result = detector.detect(event(InstagramReelsDetector.PACKAGE_MAIN, "FrameLayout"), root)
        assertEquals(Detection.ShortFormFeed(InstagramReelsDetector.SURFACE_DM), result)
    }

    @Test
    fun `detects explore reel via explore_clips_viewer view id`() {
        val root = nodeWithViewId("${InstagramReelsDetector.PACKAGE_MAIN}:id/explore_clips_viewer")
        val result = detector.detect(event(InstagramReelsDetector.PACKAGE_MAIN, "FrameLayout"), root)
        assertEquals(Detection.ShortFormFeed(InstagramReelsDetector.SURFACE_EXPLORE), result)
    }

    @Test
    fun `detects reels in Instagram Lite via class name`() {
        val result = detector.detect(
            event(InstagramReelsDetector.PACKAGE_LITE, "com.instagram.clips.viewer.ClipsViewerFragment"),
            null,
        )
        assertEquals(Detection.ShortFormFeed(InstagramReelsDetector.SURFACE_TAB), result)
    }

    @Test
    fun `detects reels in Instagram Lite via Lite-prefixed view id`() {
        val root = nodeWithViewId("${InstagramReelsDetector.PACKAGE_LITE}:id/clips_viewer")
        val result = detector.detect(event(InstagramReelsDetector.PACKAGE_LITE, "FrameLayout"), root)
        assertEquals(Detection.ShortFormFeed(InstagramReelsDetector.SURFACE_TAB), result)
    }

    @Test
    fun `ignores generic Instagram home feed without inline reel markers`() {
        val result = detector.detect(
            event(InstagramReelsDetector.PACKAGE_MAIN, "com.instagram.android.activity.MainTabActivity"),
            emptyNode(),
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores Instagram profile screen`() {
        val result = detector.detect(
            event(InstagramReelsDetector.PACKAGE_MAIN, "com.instagram.profile.fragment.UserDetailFragment"),
            emptyNode(),
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores Instagram search screen`() {
        val result = detector.detect(
            event(InstagramReelsDetector.PACKAGE_MAIN, "com.instagram.search.SearchHomeFragment"),
            emptyNode(),
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores Instagram events with null root and non-reels class name`() {
        val result = detector.detect(event(InstagramReelsDetector.PACKAGE_MAIN, "FrameLayout"), null)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `packageNames returns exactly the two Instagram packages`() {
        assertEquals(setOf("com.instagram.android", "com.instagram.lite"), detector.packageNames)
    }
}

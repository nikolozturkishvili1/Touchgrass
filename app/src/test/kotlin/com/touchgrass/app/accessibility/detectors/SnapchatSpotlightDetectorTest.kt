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
 *
 * These cases pin down Spotlight detection vs the neighbouring Snapchat surfaces we must NOT
 * trigger on: Stories, Chat, Camera, Discover, Map, Memories.
 */
class SnapchatSpotlightDetectorTest {

    private val detector = SnapchatSpotlightDetector()

    private fun event(pkg: String?, className: String? = null): AccessibilityEvent = mockk {
        every { packageName } returns pkg
        every { this@mockk.className } returns className
    }

    private fun nodeWithSpotlightId(viewId: String): AccessibilityNodeInfo = mockk {
        every { findAccessibilityNodeInfosByViewId(any()) } answers {
            if (firstArg<String>() == viewId) listOf(mockk(relaxed = true)) else emptyList()
        }
    }

    private fun emptyNode(): AccessibilityNodeInfo = mockk {
        every { findAccessibilityNodeInfosByViewId(any()) } returns emptyList()
    }

    @Test
    fun `ignores events from other packages`() {
        val result = detector.detect(event("com.instagram.android", "SpotlightFragment"), null)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores YouTube package even when class contains Spotlight word`() {
        val result = detector.detect(event("com.google.android.youtube", "SpotlightFragment"), null)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `detects via SpotlightFragment class name`() {
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, "SpotlightFragment"),
            null,
        )
        assertEquals(Detection.ShortFormFeed(SnapchatSpotlightDetector.SURFACE), result)
    }

    @Test
    fun `detects via SpotlightViewerActivity class name`() {
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, "com.snap.spotlight.SpotlightViewerActivity"),
            null,
        )
        assertEquals(Detection.ShortFormFeed(SnapchatSpotlightDetector.SURFACE), result)
    }

    @Test
    fun `detects via SpotlightVerticalScrollPage class name case insensitive`() {
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, "com.snap.modules.spotlight.spotlightverticalscrollpage"),
            null,
        )
        assertEquals(Detection.ShortFormFeed(SnapchatSpotlightDetector.SURFACE), result)
    }

    @Test
    fun `detects via spotlight_recycler_view view id`() {
        val root = nodeWithSpotlightId(
            "${SnapchatSpotlightDetector.PACKAGE_NAME}:id/spotlight_recycler_view",
        )
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, "FrameLayout"),
            root,
        )
        assertEquals(Detection.ShortFormFeed(SnapchatSpotlightDetector.SURFACE), result)
    }

    @Test
    fun `detects via fallback spotlight_player_view view id`() {
        val root = nodeWithSpotlightId(
            "${SnapchatSpotlightDetector.PACKAGE_NAME}:id/spotlight_player_view",
        )
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, "FrameLayout"),
            root,
        )
        assertEquals(Detection.ShortFormFeed(SnapchatSpotlightDetector.SURFACE), result)
    }

    @Test
    fun `detects via fallback spotlight_feed_layout view id`() {
        val root = nodeWithSpotlightId(
            "${SnapchatSpotlightDetector.PACKAGE_NAME}:id/spotlight_feed_layout",
        )
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, "FrameLayout"),
            root,
        )
        assertEquals(Detection.ShortFormFeed(SnapchatSpotlightDetector.SURFACE), result)
    }

    @Test
    fun `does not trigger on Stories fragment`() {
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, "com.snap.stories.StoriesFragment"),
            emptyNode(),
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `does not trigger on Chat fragment`() {
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, "com.snap.messaging.chat.ChatFragment"),
            emptyNode(),
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `does not trigger on Camera activity`() {
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, "com.snap.mushroom.MainActivity\$CameraActivity"),
            emptyNode(),
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `does not trigger on Discover publisher feed`() {
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, "com.snap.discover.DiscoverFeedFragment"),
            emptyNode(),
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `does not trigger on Snap Map`() {
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, "com.snap.map.MapFragment"),
            emptyNode(),
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `does not trigger on Memories grid`() {
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, "com.snap.memories.MemoriesGridFragment"),
            emptyNode(),
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores generic Snapchat screens with no Spotlight markers`() {
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, "com.snap.mushroom.MainActivity"),
            emptyNode(),
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `handles null root with non-Spotlight class as not interesting`() {
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, "FrameLayout"),
            null,
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `handles null class name with null root as not interesting`() {
        val result = detector.detect(
            event(SnapchatSpotlightDetector.PACKAGE_NAME, null),
            null,
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores events with null package`() {
        val result = detector.detect(event(null, "SpotlightFragment"), null)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `packageNames returns the expected single-element Snapchat set`() {
        assertEquals(setOf("com.snapchat.android"), detector.packageNames)
    }
}

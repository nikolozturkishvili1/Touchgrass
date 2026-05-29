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
class YouTubeShortsDetectorTest {

    private val detector = YouTubeShortsDetector()

    private fun event(pkg: String?, className: String? = null): AccessibilityEvent = mockk {
        every { packageName } returns pkg
        every { this@mockk.className } returns className
    }

    private fun nodeWithShortsId(viewId: String): AccessibilityNodeInfo = mockk {
        every { findAccessibilityNodeInfosByViewId(any()) } answers {
            if (firstArg<String>() == viewId) listOf(mockk(relaxed = true)) else emptyList()
        }
    }

    private fun emptyNode(): AccessibilityNodeInfo = mockk {
        every { findAccessibilityNodeInfosByViewId(any()) } returns emptyList()
    }

    @Test
    fun `ignores events from other packages`() {
        val result = detector.detect(event("com.instagram.android", "ShortsPlayer"), null)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `detects via Shorts player class name`() {
        val result = detector.detect(event(YouTubeShortsDetector.PACKAGE_NAME, "ShortsPlayer"), null)
        assertEquals(Detection.ShortFormFeed(YouTubeShortsDetector.SURFACE), result)
    }

    @Test
    fun `detects via ShortsHostFragment class name (case insensitive)`() {
        val result = detector.detect(
            event(YouTubeShortsDetector.PACKAGE_NAME, "com.google.android.apps.youtube.app.shortshostfragment"),
            null,
        )
        assertEquals(Detection.ShortFormFeed(YouTubeShortsDetector.SURFACE), result)
    }

    @Test
    fun `detects via reel_recycler view id in node tree`() {
        val root = nodeWithShortsId("${YouTubeShortsDetector.PACKAGE_NAME}:id/reel_recycler")
        val result = detector.detect(event(YouTubeShortsDetector.PACKAGE_NAME, "FrameLayout"), root)
        assertEquals(Detection.ShortFormFeed(YouTubeShortsDetector.SURFACE), result)
    }

    @Test
    fun `detects via fallback shorts_player view id`() {
        val root = nodeWithShortsId("${YouTubeShortsDetector.PACKAGE_NAME}:id/shorts_player")
        val result = detector.detect(event(YouTubeShortsDetector.PACKAGE_NAME, "FrameLayout"), root)
        assertEquals(Detection.ShortFormFeed(YouTubeShortsDetector.SURFACE), result)
    }

    @Test
    fun `ignores generic YouTube screens with no Shorts markers`() {
        val result = detector.detect(
            event(YouTubeShortsDetector.PACKAGE_NAME, "com.google.android.youtube.HomeActivity"),
            emptyNode(),
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores YouTube events when root is null and class is non-Shorts`() {
        val result = detector.detect(event(YouTubeShortsDetector.PACKAGE_NAME, "FrameLayout"), null)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores YouTube events with null package`() {
        val result = detector.detect(event(null, "ShortsPlayer"), null)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `package name matches the YouTube Android package`() {
        assertEquals(setOf("com.google.android.youtube"), detector.packageNames)
    }
}

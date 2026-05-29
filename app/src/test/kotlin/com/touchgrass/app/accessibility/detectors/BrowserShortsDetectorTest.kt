package com.touchgrass.app.accessibility.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.touchgrass.app.accessibility.Detection
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [BrowserShortsDetector]. MockK fakes both `AccessibilityEvent` and
 * `AccessibilityNodeInfo`; `unitTests.isReturnDefaultValues = true` in `app/build.gradle.kts`
 * keeps unmocked framework calls from throwing.
 *
 * Each test builds an "active window root" with a single URL-bar EditText node whose text is the
 * URL we want to simulate the user looking at.
 */
class BrowserShortsDetectorTest {

    private val detector = BrowserShortsDetector()

    // --- mock builders ----------------------------------------------------------------

    private fun event(pkg: String?): AccessibilityEvent = mockk {
        every { packageName } returns pkg
        every { className } returns null
    }

    /** Mock URL-bar node whose `text` is the supplied URL string. */
    private fun urlBarNode(url: String?, contentDesc: String? = null): AccessibilityNodeInfo = mockk {
        every { text } returns url
        every { contentDescription } returns contentDesc
    }

    /**
     * Mock active-window root where a single URL-bar node is reachable via the supplied view ID.
     * Any other view-ID lookup returns an empty list — same behaviour as the real OS when the
     * node isn't in the tree.
     */
    private fun rootWithUrlBar(viewId: String, urlBar: AccessibilityNodeInfo): AccessibilityNodeInfo = mockk {
        every { findAccessibilityNodeInfosByViewId(any()) } answers {
            if (firstArg<String>() == viewId) listOf(urlBar) else emptyList()
        }
    }

    /** Mock root that has no URL-bar node at all (e.g., we're on the tab switcher). */
    private fun rootWithoutUrlBar(): AccessibilityNodeInfo = mockk {
        every { findAccessibilityNodeInfosByViewId(any()) } returns emptyList()
    }

    // --- tests ------------------------------------------------------------------------

    @Test
    fun `ignores events from non-supported packages`() {
        val result = detector.detect(
            event("com.google.android.youtube"),
            rootWithUrlBar(
                "com.android.chrome:id/url_bar",
                urlBarNode("https://m.youtube.com/shorts/abc123"),
            ),
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `detects YouTube Shorts in Chrome URL bar`() {
        val root = rootWithUrlBar(
            "com.android.chrome:id/url_bar",
            urlBarNode("https://m.youtube.com/shorts/abcXYZ_123"),
        )
        val result = detector.detect(event(BrowserShortsDetector.PACKAGE_CHROME), root)
        assertEquals(Detection.ShortFormFeed(BrowserShortsDetector.SURFACE_CHROME), result)
    }

    @Test
    fun `detects YouTube Shorts in Chrome with www subdomain`() {
        val root = rootWithUrlBar(
            "com.android.chrome:id/url_bar",
            urlBarNode("https://www.youtube.com/shorts/abc"),
        )
        val result = detector.detect(event(BrowserShortsDetector.PACKAGE_CHROME), root)
        assertEquals(Detection.ShortFormFeed(BrowserShortsDetector.SURFACE_CHROME), result)
    }

    @Test
    fun `detects YouTube Shorts feed tab in Chrome`() {
        val root = rootWithUrlBar(
            "com.android.chrome:id/url_bar",
            urlBarNode("https://m.youtube.com/feed/shorts"),
        )
        val result = detector.detect(event(BrowserShortsDetector.PACKAGE_CHROME), root)
        assertEquals(Detection.ShortFormFeed(BrowserShortsDetector.SURFACE_CHROME), result)
    }

    @Test
    fun `detects YouTube Shorts in Samsung Internet URL bar`() {
        val root = rootWithUrlBar(
            "com.sec.android.app.sbrowser:id/location_bar_edit_text",
            urlBarNode("https://m.youtube.com/shorts/zzz"),
        )
        val result = detector.detect(event(BrowserShortsDetector.PACKAGE_SAMSUNG_INTERNET), root)
        assertEquals(Detection.ShortFormFeed(BrowserShortsDetector.SURFACE_SAMSUNG_INTERNET), result)
    }

    @Test
    fun `falls back to contentDescription when text is null`() {
        val root = rootWithUrlBar(
            "com.android.chrome:id/url_bar",
            urlBarNode(url = null, contentDesc = "youtube.com/shorts/foo"),
        )
        val result = detector.detect(event(BrowserShortsDetector.PACKAGE_CHROME), root)
        assertEquals(Detection.ShortFormFeed(BrowserShortsDetector.SURFACE_CHROME), result)
    }

    @Test
    fun `ignores regular YouTube watch URLs in Chrome`() {
        val root = rootWithUrlBar(
            "com.android.chrome:id/url_bar",
            urlBarNode("https://www.youtube.com/watch?v=dQw4w9WgXcQ"),
        )
        val result = detector.detect(event(BrowserShortsDetector.PACKAGE_CHROME), root)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores regular YouTube watch URLs in Samsung Internet`() {
        val root = rootWithUrlBar(
            "com.sec.android.app.sbrowser:id/location_bar_edit_text",
            urlBarNode("https://m.youtube.com/watch?v=abc"),
        )
        val result = detector.detect(event(BrowserShortsDetector.PACKAGE_SAMSUNG_INTERNET), root)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores unrelated URLs like google_com`() {
        val root = rootWithUrlBar(
            "com.android.chrome:id/url_bar",
            urlBarNode("https://www.google.com/search?q=cats"),
        )
        val result = detector.detect(event(BrowserShortsDetector.PACKAGE_CHROME), root)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores youtu_be short links (regular videos, not Shorts)`() {
        val root = rootWithUrlBar(
            "com.android.chrome:id/url_bar",
            urlBarNode("https://youtu.be/abc123"),
        )
        val result = detector.detect(event(BrowserShortsDetector.PACKAGE_CHROME), root)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores search queries containing the word shorts`() {
        val root = rootWithUrlBar(
            "com.android.chrome:id/url_bar",
            urlBarNode("https://www.youtube.com/results?search_query=shorts"),
        )
        val result = detector.detect(event(BrowserShortsDetector.PACKAGE_CHROME), root)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `returns NotInteresting (not an exception) when URL bar node is absent`() {
        val result = detector.detect(
            event(BrowserShortsDetector.PACKAGE_CHROME),
            rootWithoutUrlBar(),
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `returns NotInteresting when URL bar text is blank`() {
        val root = rootWithUrlBar(
            "com.android.chrome:id/url_bar",
            urlBarNode(url = "   ", contentDesc = null),
        )
        val result = detector.detect(event(BrowserShortsDetector.PACKAGE_CHROME), root)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `returns NotInteresting when root is null`() {
        val result = detector.detect(event(BrowserShortsDetector.PACKAGE_CHROME), null)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `returns NotInteresting when package is null`() {
        val result = detector.detect(
            event(null),
            rootWithUrlBar(
                "com.android.chrome:id/url_bar",
                urlBarNode("https://m.youtube.com/shorts/x"),
            ),
        )
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `URL matching is case insensitive`() {
        val root = rootWithUrlBar(
            "com.android.chrome:id/url_bar",
            urlBarNode("HTTPS://M.YOUTUBE.COM/SHORTS/AbC"),
        )
        val result = detector.detect(event(BrowserShortsDetector.PACKAGE_CHROME), root)
        assertEquals(Detection.ShortFormFeed(BrowserShortsDetector.SURFACE_CHROME), result)
    }

    @Test
    fun `packageNames covers Chrome stable and Samsung Internet stable`() {
        assertEquals(
            setOf("com.android.chrome", "com.sec.android.app.sbrowser"),
            detector.packageNames,
        )
    }

    @Test
    fun `packageNames does not include beta or canary channels in V1`() {
        // Documented in BrowserShortsDetector's KDoc: V1 is conservative and only covers stable
        // channels. This test pins that decision so any future expansion is intentional.
        assertTrue("com.chrome.beta" !in detector.packageNames)
        assertTrue("com.chrome.dev" !in detector.packageNames)
        assertTrue("com.chrome.canary" !in detector.packageNames)
        assertTrue("com.sec.android.app.sbrowser.beta" !in detector.packageNames)
    }
}

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
 * Detector unit tests use MockK to fake the framework `AccessibilityEvent` and
 * `AccessibilityNodeInfo` classes. `unitTests.isReturnDefaultValues = true` in
 * `app/build.gradle.kts` makes the Android stubs return defaults instead of throwing.
 *
 * Tests are structured around the TikTok detector's three-step strategy:
 *  1. view-id container check,
 *  2. right-rail action cluster confirmation,
 *  3. For You vs Following surface discrimination.
 */
class TikTokDetectorTest {

    private val detector = TikTokDetector()

    // ---- mock helpers --------------------------------------------------------------------

    private fun event(pkg: String?): AccessibilityEvent = mockk {
        every { packageName } returns pkg
        every { className } returns null
    }

    /**
     * Build a leaf node with a content description, text, and selected flag. `childCount` is
     * zero so the walker treats it as terminal.
     */
    private fun leaf(
        contentDescription: String? = null,
        text: String? = null,
        selected: Boolean = false,
    ): AccessibilityNodeInfo = mockk {
        every { this@mockk.contentDescription } returns contentDescription
        every { this@mockk.text } returns text
        every { isSelected } returns selected
        every { childCount } returns 0
        every { getChild(any()) } returns null
        // findAccessibilityNodeInfosByViewId is queried on the root only; default to empty.
        every { findAccessibilityNodeInfosByViewId(any()) } returns emptyList()
    }

    /**
     * Build a root node that:
     *  - returns a non-empty match for [matchingViewId] (if non-null), empty for everything else,
     *  - has [children] as its direct descendants for the tree walk.
     */
    private fun root(
        matchingViewId: String? = null,
        children: List<AccessibilityNodeInfo> = emptyList(),
        contentDescription: String? = null,
        text: String? = null,
        selected: Boolean = false,
    ): AccessibilityNodeInfo = mockk {
        every { findAccessibilityNodeInfosByViewId(any()) } answers {
            if (matchingViewId != null && firstArg<String>() == matchingViewId) {
                listOf(mockk(relaxed = true))
            } else {
                emptyList()
            }
        }
        every { this@mockk.contentDescription } returns contentDescription
        every { this@mockk.text } returns text
        every { isSelected } returns selected
        every { childCount } returns children.size
        every { getChild(any()) } answers {
            val i = firstArg<Int>()
            if (i in children.indices) children[i] else null
        }
    }

    private fun likeNode() = leaf(contentDescription = "Like video")
    private fun commentNode() = leaf(contentDescription = "Comment on video")
    private fun shareNode() = leaf(contentDescription = "Share video")

    private fun rightRail(): List<AccessibilityNodeInfo> = listOf(
        likeNode(), commentNode(), shareNode(),
    )

    // ---- packageNames --------------------------------------------------------------------

    @Test
    fun `packageNames covers both TikTok builds`() {
        assertEquals(
            setOf(TikTokDetector.PACKAGE_MUSICALLY, TikTokDetector.PACKAGE_TRILL),
            detector.packageNames,
        )
    }

    // ---- non-TikTok packages -------------------------------------------------------------

    @Test
    fun `ignores events from other packages`() {
        val r = root(
            matchingViewId = "com.instagram.android:id/feed_layout",
            children = rightRail(),
        )
        val result = detector.detect(event("com.instagram.android"), r)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores events with null package`() {
        val result = detector.detect(event(null), root())
        assertEquals(Detection.NotInteresting, result)
    }

    // ---- null root -----------------------------------------------------------------------

    @Test
    fun `ignores TikTok events when root is null`() {
        val result = detector.detect(event(TikTokDetector.PACKAGE_MUSICALLY), null)
        assertEquals(Detection.NotInteresting, result)
    }

    // ---- positive detections -------------------------------------------------------------

    @Test
    fun `detects For You feed via feed_layout view id and full action cluster`() {
        val r = root(
            matchingViewId = "${TikTokDetector.PACKAGE_MUSICALLY}:id/feed_layout",
            children = rightRail(),
        )
        val result = detector.detect(event(TikTokDetector.PACKAGE_MUSICALLY), r)
        assertEquals(Detection.ShortFormFeed(TikTokDetector.SURFACE_FOR_YOU), result)
    }

    @Test
    fun `detects For You feed on the trill (Asia) package`() {
        val r = root(
            matchingViewId = "${TikTokDetector.PACKAGE_TRILL}:id/feed_layout",
            children = rightRail(),
        )
        val result = detector.detect(event(TikTokDetector.PACKAGE_TRILL), r)
        assertEquals(Detection.ShortFormFeed(TikTokDetector.SURFACE_FOR_YOU), result)
    }

    @Test
    fun `detects via fallback vertical_view_pager view id`() {
        val r = root(
            matchingViewId = "${TikTokDetector.PACKAGE_MUSICALLY}:id/vertical_view_pager",
            children = rightRail(),
        )
        val result = detector.detect(event(TikTokDetector.PACKAGE_MUSICALLY), r)
        assertEquals(Detection.ShortFormFeed(TikTokDetector.SURFACE_FOR_YOU), result)
    }

    @Test
    fun `detects with only two of three right-rail buttons present`() {
        // Drop "share" — score should still be 2 of 3 and pass the threshold.
        val r = root(
            matchingViewId = "${TikTokDetector.PACKAGE_MUSICALLY}:id/feed_layout",
            children = listOf(likeNode(), commentNode()),
        )
        val result = detector.detect(event(TikTokDetector.PACKAGE_MUSICALLY), r)
        assertEquals(Detection.ShortFormFeed(TikTokDetector.SURFACE_FOR_YOU), result)
    }

    @Test
    fun `picks Following surface when the Following tab is selected`() {
        val followingTab = leaf(text = "Following", selected = true)
        val forYouTab = leaf(text = "For You", selected = false)
        val r = root(
            matchingViewId = "${TikTokDetector.PACKAGE_MUSICALLY}:id/feed_layout",
            children = rightRail() + followingTab + forYouTab,
        )
        val result = detector.detect(event(TikTokDetector.PACKAGE_MUSICALLY), r)
        assertEquals(Detection.ShortFormFeed(TikTokDetector.SURFACE_FOLLOWING), result)
    }

    @Test
    fun `defaults to For You when Following tab exists but is not selected`() {
        val followingTab = leaf(text = "Following", selected = false)
        val forYouTab = leaf(text = "For You", selected = true)
        val r = root(
            matchingViewId = "${TikTokDetector.PACKAGE_MUSICALLY}:id/feed_layout",
            children = rightRail() + followingTab + forYouTab,
        )
        val result = detector.detect(event(TikTokDetector.PACKAGE_MUSICALLY), r)
        assertEquals(Detection.ShortFormFeed(TikTokDetector.SURFACE_FOR_YOU), result)
    }

    // ---- negative detections (DM / profile / search / camera) ----------------------------

    @Test
    fun `ignores TikTok DM screen with no feed container`() {
        // DM screen has chat bubbles (Text) and an input field — no feed container view id,
        // no like/comment/share cluster.
        val chatBubble = leaf(text = "Hey, did you see this?")
        val inputField = leaf(contentDescription = "Message")
        val r = root(matchingViewId = null, children = listOf(chatBubble, inputField))
        val result = detector.detect(event(TikTokDetector.PACKAGE_MUSICALLY), r)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores TikTok profile screen`() {
        val followers = leaf(text = "1.2K Followers")
        val editProfile = leaf(contentDescription = "Edit profile")
        val r = root(matchingViewId = null, children = listOf(followers, editProfile))
        val result = detector.detect(event(TikTokDetector.PACKAGE_MUSICALLY), r)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores TikTok search screen`() {
        val searchBar = leaf(contentDescription = "Search")
        val r = root(matchingViewId = null, children = listOf(searchBar))
        val result = detector.detect(event(TikTokDetector.PACKAGE_MUSICALLY), r)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores TikTok camera-record screen`() {
        val recordButton = leaf(contentDescription = "Start recording")
        val flipCamera = leaf(contentDescription = "Flip camera")
        val r = root(matchingViewId = null, children = listOf(recordButton, flipCamera))
        val result = detector.detect(event(TikTokDetector.PACKAGE_MUSICALLY), r)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores screen that has a feed container view id but no action cluster`() {
        // Guards against a false-positive: a screen that re-uses a known id but isn't a feed.
        val r = root(
            matchingViewId = "${TikTokDetector.PACKAGE_MUSICALLY}:id/feed_layout",
            children = listOf(leaf(text = "Loading...")),
        )
        val result = detector.detect(event(TikTokDetector.PACKAGE_MUSICALLY), r)
        assertEquals(Detection.NotInteresting, result)
    }

    @Test
    fun `ignores screen with only one right-rail button present`() {
        val r = root(
            matchingViewId = "${TikTokDetector.PACKAGE_MUSICALLY}:id/feed_layout",
            children = listOf(likeNode()),
        )
        val result = detector.detect(event(TikTokDetector.PACKAGE_MUSICALLY), r)
        assertEquals(Detection.NotInteresting, result)
    }

    // ---- sanity --------------------------------------------------------------------------

    @Test
    fun `surface constants match the spec wire format`() {
        assertEquals("tiktok-foryou", TikTokDetector.SURFACE_FOR_YOU)
        assertEquals("tiktok-following", TikTokDetector.SURFACE_FOLLOWING)
    }

    @Test
    fun `feed view id suffix list is non-empty (regression guard)`() {
        assertTrue(TikTokDetector.FEED_VIEW_ID_SUFFIXES.isNotEmpty())
    }
}

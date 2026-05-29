package com.touchgrass.app.accessibility.detectors

import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.touchgrass.app.accessibility.Detection
import com.touchgrass.app.accessibility.Detector
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects YouTube **Shorts** viewed inside a mobile browser (spec §3.1.A, complaint #10 from
 * §2.1 — competing blockers ignore browser-based shorts and users notice). Covers two engines:
 *
 *  - Google Chrome (`com.android.chrome`) → surface [SURFACE_CHROME]
 *  - Samsung Internet (`com.sec.android.app.sbrowser`) → surface [SURFACE_SAMSUNG_INTERNET]
 *
 * ## Privacy note (Trust Dashboard)
 *
 * Browsing data is sensitive. This detector deliberately reads **only the URL bar's text** — a
 * single `EditText` node found by view ID — and never walks into the rendered page body, never
 * reads form fields, never inspects tab titles. The URL is checked locally against a literal
 * substring (`youtube.com/shorts/`) and is **not** stored, logged, or transmitted: positive
 * matches result only in a [Detection.ShortFormFeed] with a static surface string. Document this
 * exact scope in the Trust Dashboard so users understand what "monitor my browser" means here.
 *
 * ## How detection works
 *
 * Both browsers expose their address bar as an `EditText` with a stable resource ID. We look up
 * that node by ID on every event for our packages, read its `text` (falling back to
 * `contentDescription`), and check whether the string contains a YouTube Shorts URL.
 *
 * The browsers' visible/internal layouts shift between releases; the most likely break is a view
 * ID rename. When that happens, append the new ID to the corresponding constants list below and
 * keep the old ones as fallbacks so a single APK works across staggered rollouts.
 *
 * ## V1 conservatism
 *
 * Only stable / mainline channels are matched. Chrome Beta (`com.chrome.beta`), Dev
 * (`com.chrome.dev`), Canary (`com.chrome.canary`), and Samsung Internet Beta
 * (`com.sec.android.app.sbrowser.beta`) are intentionally **not** included in V1: those audiences
 * are small and adding them risks false-positives when their internal view IDs diverge. Revisit
 * after we get telemetry from the stable channels.
 *
 * ## Why not WebView content?
 *
 * Reading the rendered page DOM through accessibility is (a) unreliable across browsers, (b) a
 * privacy escalation we don't want, and (c) blocked outright on Samsung Internet (its WebView
 * does not expose content to accessibility services). The URL-bar approach sidesteps all three.
 *
 * View ID references:
 *  - Chrome: `com.android.chrome:id/url_bar` — long-standing canonical ID, used by Chrome's own
 *    accessibility code and widely observed in third-party trackers
 *    (see "Track web browser usage in Android using Accessibility Service",
 *    https://medium.com/nerd-for-tech/track-web-browser-usage-in-android-using-accessibility-service-800bfa2745d2).
 *  - Samsung Internet: `com.sec.android.app.sbrowser:id/location_bar_edit_text` — the standard
 *    Samsung Internet address-bar EditText ID. Verified ID; add new IDs to [SAMSUNG_INTERNET_URL_BAR_IDS]
 *    if Samsung renames it.
 */
@Singleton
class BrowserShortsDetector @Inject constructor() : Detector {

    override val packageNames: Set<String> = setOf(
        PACKAGE_CHROME,
        PACKAGE_SAMSUNG_INTERNET,
    )

    override fun detect(event: AccessibilityEvent, root: AccessibilityNodeInfo?): Detection {
        val pkg = event.packageName?.toString() ?: return Detection.NotInteresting

        val (surface, viewIds) = when (pkg) {
            PACKAGE_CHROME -> SURFACE_CHROME to CHROME_URL_BAR_IDS
            PACKAGE_SAMSUNG_INTERNET -> SURFACE_SAMSUNG_INTERNET to SAMSUNG_INTERNET_URL_BAR_IDS
            else -> return Detection.NotInteresting
        }

        val rootNode = root ?: return Detection.NotInteresting
        val url = rootNode.findUrlBarText(viewIds) ?: return Detection.NotInteresting

        return if (url.isYouTubeShortsUrl()) {
            Detection.ShortFormFeed(surface)
        } else {
            Detection.NotInteresting
        }
    }

    /**
     * Locate the address-bar node and return its current text. The browsers expose the URL as
     * the EditText's `text`; on some Samsung builds the visible-text mirror lives in
     * `contentDescription`, so we try both. Returns `null` if no candidate node exists or every
     * candidate is empty — that's the normal "user is on a chrome surface but not focused on a
     * tab" case (e.g., the tab switcher).
     */
    private fun AccessibilityNodeInfo.findUrlBarText(viewIds: Array<String>): String? {
        for (viewId in viewIds) {
            val matches = findAccessibilityNodeInfosByViewId(viewId) ?: continue
            for (node in matches) {
                if (node == null) continue
                val text = node.text?.toString()?.takeIf { it.isNotBlank() }
                    ?: node.contentDescription?.toString()?.takeIf { it.isNotBlank() }
                if (text != null) return text
            }
        }
        return null
    }

    /**
     * Match `youtube.com/shorts/` under any host prefix we've seen browsers render in the bar:
     * the bare apex, the mobile subdomain, and `www`. Comparison is case-insensitive because
     * Chrome occasionally normalises the host case in the displayed string. We deliberately do
     * **not** match `youtu.be/...` — that short-link host serves regular videos, not Shorts.
     */
    private fun String.isYouTubeShortsUrl(): Boolean {
        val lower = lowercase()
        return SHORTS_URL_NEEDLES.any { lower.contains(it) }
    }

    companion object {
        /** Google Chrome stable channel. */
        const val PACKAGE_CHROME: String = "com.android.chrome"

        /** Samsung Internet stable channel. */
        const val PACKAGE_SAMSUNG_INTERNET: String = "com.sec.android.app.sbrowser"

        /** Surface ID emitted when YouTube Shorts is detected inside Chrome. */
        const val SURFACE_CHROME: String = "chrome-youtube-shorts"

        /** Surface ID emitted when YouTube Shorts is detected inside Samsung Internet. */
        const val SURFACE_SAMSUNG_INTERNET: String = "samsung-internet-youtube-shorts"

        /**
         * Chrome's address-bar EditText IDs in priority order. `url_bar` is the long-standing
         * canonical ID; additional IDs can be appended here if a future Chrome release renames it.
         */
        @JvmStatic
        internal val CHROME_URL_BAR_IDS: Array<String> = arrayOf(
            "$PACKAGE_CHROME:id/url_bar",
        )

        /**
         * Samsung Internet's address-bar EditText IDs in priority order.
         * `location_bar_edit_text` is the standard ID; if Samsung renames it in a future build,
         * add the new resource name to this list and keep the old one as a fallback.
         */
        @JvmStatic
        internal val SAMSUNG_INTERNET_URL_BAR_IDS: Array<String> = arrayOf(
            "$PACKAGE_SAMSUNG_INTERNET:id/location_bar_edit_text",
        )

        /**
         * URL substrings that uniquely identify a YouTube Shorts watch page. All entries must
         * include the trailing `/shorts/` segment so we never match the home page, the dedicated
         * Shorts feed tab (`/feed/shorts` — which we also catch via the second needle), or
         * unrelated paths like `/results?search_query=shorts`.
         */
        @JvmStatic
        internal val SHORTS_URL_NEEDLES: Array<String> = arrayOf(
            "youtube.com/shorts/",
            "youtube.com/feed/shorts",
        )
    }
}

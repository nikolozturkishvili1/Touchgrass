package com.touchgrass.app.data.model

import com.touchgrass.app.data.local.PreferencesRepository

/**
 * One row in the onboarding app-picker (and later, the settings screen).
 *
 * Each entry groups all package variants of one logical app so the user toggles "Instagram"
 * once, not "Instagram main + Instagram Lite" separately.
 */
data class SupportedApp(
    val id: String,
    val displayName: String,
    val description: String,
    val packageNames: Set<String>,
) {
    companion object {
        /**
         * Canonical day-1 list. Must be kept in sync with:
         *  - [PreferencesRepository.DEFAULT_ENABLED_PACKAGES]
         *  - `app/src/main/res/xml/accessibility_service_config.xml` (`packageNames` attr)
         *  - `AndroidManifest.xml` `<queries>`
         */
        val ALL: List<SupportedApp> = listOf(
            SupportedApp(
                id = "youtube",
                displayName = "YouTube",
                description = "Blocks Shorts.",
                packageNames = setOf("com.google.android.youtube"),
            ),
            SupportedApp(
                id = "instagram",
                displayName = "Instagram",
                description = "Blocks Reels (feed, Explore, Reels tab, DM-shared).",
                packageNames = setOf("com.instagram.android", "com.instagram.lite"),
            ),
            SupportedApp(
                id = "tiktok",
                displayName = "TikTok",
                description = "Blocks For You and Following feeds.",
                packageNames = setOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill"),
            ),
            SupportedApp(
                id = "facebook",
                displayName = "Facebook",
                description = "Blocks the Reels viewer + Reels tab.",
                packageNames = setOf("com.facebook.katana", "com.facebook.lite"),
            ),
            SupportedApp(
                id = "snapchat",
                displayName = "Snapchat",
                description = "Blocks Spotlight.",
                packageNames = setOf("com.snapchat.android"),
            ),
            SupportedApp(
                id = "chrome",
                displayName = "Chrome",
                description = "Blocks youtube.com/shorts URLs.",
                packageNames = setOf("com.android.chrome"),
            ),
            SupportedApp(
                id = "samsung_internet",
                displayName = "Samsung Internet",
                description = "Blocks youtube.com/shorts URLs.",
                packageNames = setOf("com.sec.android.app.sbrowser"),
            ),
        )
    }
}

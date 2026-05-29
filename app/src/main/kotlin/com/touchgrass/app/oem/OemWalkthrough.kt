package com.touchgrass.app.oem

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Schema for `assets/oem/{key}.json`. This is the contract every per-OEM JSON file must satisfy.
 *
 * Parallel sub-agents producing OEM walkthroughs (spec §12.4 Recipe 2) target this shape.
 *
 * Kotlin note for .NET devs: `@Serializable` is the `kotlinx.serialization` equivalent of
 * `[JsonSerializable]` on a record; `Json.decodeFromString<OemWalkthrough>(text)` is the
 * equivalent of `JsonSerializer.Deserialize<OemWalkthrough>(text)`.
 */
@Serializable
data class OemWalkthrough(
    val oemId: String,
    val displayName: String,
    val manufacturerMatches: List<String> = emptyList(),
    val brandMatches: List<String> = emptyList(),
    val steps: List<OemStep>,
    @SerialName("deepLinkIntent")
    val deepLinkIntent: String? = null,
    val sources: List<String> = emptyList(),
)

@Serializable
data class OemStep(
    val title: String,
    val description: String,
    /** Drawable resource name (without extension), e.g. `oem_xiaomi_step1`. Optional. */
    val screenshot: String? = null,
)

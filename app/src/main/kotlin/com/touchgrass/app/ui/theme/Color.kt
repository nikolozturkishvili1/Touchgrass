package com.touchgrass.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Brand palette per spec §5.2.
 *
 * Calm-over-clinical: warm naturals, moss greens, soft sand. Terracotta for danger — never red.
 * Dark mode is first-class (spec §5.1).
 */
internal object TouchgrassPalette {
    val BackgroundLight = Color(0xFFFAF8F3)
    val BackgroundDark = Color(0xFF1A1C18)

    val Primary = Color(0xFF3E5E3A)
    val Accent = Color(0xFFC9D5A4)
    val Danger = Color(0xFFA24E3E)

    val TextPrimaryLight = Color(0xFF1F231C)
    val TextPrimaryDark = Color(0xFFE9EBE2)
    val TextSecondary = Color(0xFF5C645A)

    val SurfaceLight = Color(0xFFF4F1E9)
    val SurfaceDark = Color(0xFF24261F)
}

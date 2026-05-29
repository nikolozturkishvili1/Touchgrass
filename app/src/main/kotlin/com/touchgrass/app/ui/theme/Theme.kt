package com.touchgrass.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

/**
 * Root Compose theme. Light/dark are equal-quality per spec §5.1.
 *
 * Dynamic color (Material You) is intentionally NOT used here — the brand palette is part of
 * the product (warm moss, terracotta danger). If the developer later wants Material You, gate
 * it behind a setting; do not enable it silently as it would dilute the brand.
 */
@Composable
fun TouchgrassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = TouchgrassTypography,
        content = content,
    )
}

private val LightColors = lightColorScheme(
    primary = TouchgrassPalette.Primary,
    onPrimary = TouchgrassPalette.BackgroundLight,
    primaryContainer = TouchgrassPalette.Accent,
    onPrimaryContainer = TouchgrassPalette.TextPrimaryLight,
    secondary = TouchgrassPalette.Accent,
    onSecondary = TouchgrassPalette.TextPrimaryLight,
    background = TouchgrassPalette.BackgroundLight,
    onBackground = TouchgrassPalette.TextPrimaryLight,
    surface = TouchgrassPalette.SurfaceLight,
    onSurface = TouchgrassPalette.TextPrimaryLight,
    onSurfaceVariant = TouchgrassPalette.TextSecondary,
    error = TouchgrassPalette.Danger,
    onError = TouchgrassPalette.BackgroundLight,
)

private val DarkColors = darkColorScheme(
    primary = TouchgrassPalette.Accent,
    onPrimary = TouchgrassPalette.BackgroundDark,
    primaryContainer = TouchgrassPalette.Primary,
    onPrimaryContainer = TouchgrassPalette.TextPrimaryDark,
    secondary = TouchgrassPalette.Accent,
    onSecondary = TouchgrassPalette.BackgroundDark,
    background = TouchgrassPalette.BackgroundDark,
    onBackground = TouchgrassPalette.TextPrimaryDark,
    surface = TouchgrassPalette.SurfaceDark,
    onSurface = TouchgrassPalette.TextPrimaryDark,
    onSurfaceVariant = TouchgrassPalette.TextSecondary,
    error = TouchgrassPalette.Danger,
    onError = TouchgrassPalette.BackgroundDark,
)

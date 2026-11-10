package com.edumind.ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Violet600,
    onPrimary = Color.White,
    primaryContainer = Violet100,
    onPrimaryContainer = Ink,
    secondary = Blue600,
    onSecondary = Color.White,
    secondaryContainer = Blue100,
    onSecondaryContainer = Ink,
    tertiary = Violet400,
    background = LavenderBg,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = Violet50,
    onSurfaceVariant = InkSecondary,
    outline = CardBorder,
    error = Error,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Violet400,
    onPrimary = Ink,
    primaryContainer = Violet600,
    onPrimaryContainer = Color.White,
    secondary = Blue500,
    onSecondary = Color.White,
    secondaryContainer = Blue600,
    onSecondaryContainer = Color.White,
    tertiary = Violet500,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = Color(0xFFF87171),
    onError = Ink,
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun EduMindTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = EduMindTypography,
        content = content,
    )
}

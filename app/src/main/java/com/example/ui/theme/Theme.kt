package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ThemePrimary,
    secondary = ThemeSecondary,
    tertiary = ThemeTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    error = AccentRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force Dark theme for the immersive racing game experience
    dynamicColor: Boolean = false, // Curated custom aesthetics instead of random device system colors
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

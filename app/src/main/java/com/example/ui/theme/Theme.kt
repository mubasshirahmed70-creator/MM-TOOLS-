package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MMToolsColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = NavyBackground,
    primaryContainer = CyanContainer,
    onPrimaryContainer = CyanGlow,
    secondary = BlueLight,
    onSecondary = NavyBackground,
    secondaryContainer = NavySurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = BluePrimary,
    onTertiary = Color.White,
    background = NavyBackground,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = NavyCardBorder,
    error = DangerRed,
    onError = Color.White
)

@Composable
fun MMToolsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MMToolsColorScheme,
        typography = Typography,
        content = content
    )
}

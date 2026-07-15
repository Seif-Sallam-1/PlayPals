// File: app/src/main/java/com/example/ui/theme/Theme.kt
package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VibrantColorScheme = lightColorScheme(
    primary = DeepPurple,
    secondary = LightPurple,
    tertiary = Pink,
    background = VibrantBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    surfaceVariant = ContainerBg,
    onSurfaceVariant = TextSecondary,
    outline = CardBorderColor
)

private val DarkVibrantColorScheme = darkColorScheme(
    primary = LightPurple,
    secondary = DeepPurple,
    tertiary = Pink,
    background = Color(0xFF1C1B1F), // dark neutral
    surface = Color(0xFF2B2930),
    onPrimary = Color(0xFF21005D),
    onSecondary = Color.White,
    onTertiary = TextPrimary,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to preserve our custom Vibrant Palette theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkVibrantColorScheme else VibrantColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.memora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GoldenDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFD700),    // Metallic Gold
    secondary = Color(0xFFDAA520),  // Goldenrod
    tertiary = Color(0xFFFFEDBA),   // Champagne
    background = Color(0xFF001F3F), // Midnight Navy
    surface = Color(0xFF002B5B),    // Imperial Navy
    onPrimary = Color(0xFF001F3F),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF003366),
    onSurfaceVariant = Color(0xFFB0B0B0)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB45309), // Warm Gold
    secondary = Color(0xFF92400E),
    tertiary = Color(0xFF4338CA)
)

@Composable
fun MemoraTheme(
    darkTheme: Boolean = true, // Force dark for the luxury look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) GoldenDarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

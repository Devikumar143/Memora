package com.memora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GoldenDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFFFFF),    // Arctic White
    secondary = Color(0xFFB0B0B0),  // Muted Silver
    tertiary = Color(0xFF707070),   // Gunmetal
    background = Color(0xFF000000), // Pure Black
    surface = Color(0xFF121212),    // Soft Carbon
    onPrimary = Color(0xFF000000),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1A1A1A),
    onSurfaceVariant = Color(0xFF7A7A7A)
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

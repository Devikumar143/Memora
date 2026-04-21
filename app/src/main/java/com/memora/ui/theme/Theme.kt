package com.memora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GoldenDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFBBF24), // Amber Gold
    secondary = Color(0xFFF59E0B), // Darker Gold
    tertiary = Color(0xFF6366F1), // Indigo accent
    background = Color(0xFF0F172A), // Deep Midnight Navy
    surface = Color(0xFF1E293B), // Slate Navy
    onPrimary = Color(0xFF451A03),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1)
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

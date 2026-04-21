package com.memora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFAEC6FF),
    secondary = Color(0xFFBBC6E4),
    tertiary = Color(0xFFE5BADD)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF005AC1),
    secondary = Color(0xFF515E7D),
    tertiary = Color(0xFF7C5276)
)

@Composable
fun MemoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}

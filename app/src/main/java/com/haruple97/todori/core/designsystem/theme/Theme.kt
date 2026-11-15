package com.haruple97.todori.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CoralPinkDark,
    onPrimary = Color(0xFF4A1F2A),
    secondary = LeafGreen,
    background = CreamBackgroundDark,
    onBackground = CocoaTextDark,
    surface = CreamBackgroundDark,
    onSurface = CocoaTextDark,
    surfaceVariant = PeachCardDark,
    onSurfaceVariant = CoralPinkDark
)

private val LightColorScheme = lightColorScheme(
    primary = CoralPink,
    onPrimary = Color.White,
    secondary = LeafGreenDeep,
    background = CreamBackground,
    onBackground = CocoaText,
    surface = CreamBackground,
    onSurface = CocoaText,
    surfaceVariant = PeachCard,
    onSurfaceVariant = Color(0xFFD9627C)
)

@Composable
fun TodoriTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}

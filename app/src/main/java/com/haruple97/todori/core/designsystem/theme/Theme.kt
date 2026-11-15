package com.haruple97.todori.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = TomatoRedDark,
    secondary = LeafGreenDark,
    background = CreamBackgroundDark,
    surface = CreamBackgroundDark
)

private val LightColorScheme = lightColorScheme(
    primary = TomatoRed,
    secondary = LeafGreen,
    background = CreamBackground,
    surface = CreamBackground
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

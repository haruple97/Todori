package com.haruple97.todori.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.haruple97.todori.core.model.SkinId
import com.haruple97.todori.core.model.ThemeId

/** 어두운 모양으로 그려지는지. 항상 어두운 테마이거나 시스템이 어두운 모드일 때. */
@Composable
fun isDarkFor(theme: ThemeId): Boolean = theme.alwaysDark || isSystemInDarkTheme()

@Composable
fun TodoriTheme(
    theme: ThemeId = ThemeId.Pink,
    skin: SkinId = SkinId.Tomato,
    content: @Composable () -> Unit
) {
    val dark = isDarkFor(theme)
    val colors = remember(theme, dark) { todoriColors(theme, dark) }
    CompositionLocalProvider(LocalTodoriColors provides colors, LocalSkin provides skin) {
        MaterialTheme(
            colorScheme = colors.scheme,
            typography = Typography,
            content = content
        )
    }
}

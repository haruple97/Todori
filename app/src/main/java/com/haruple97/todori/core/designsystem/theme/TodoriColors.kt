package com.haruple97.todori.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.haruple97.todori.core.model.SkinId
import com.haruple97.todori.core.model.ThemeId

@Immutable
class GradientColors(val top: Color, val bottom: Color)

/**
 * 테마가 정하는 색 묶음. 젤리 버튼·진행 링·말풍선 테두리·배경 등 화면 곳곳이 이 값을 읽는다.
 * Material 기본 색은 [scheme]에 담아 MaterialTheme에 넘긴다.
 */
@Immutable
class TodoriColors(
    val accentLight: Color,
    val accentDeep: Color,
    val glow: Color,
    val decor: List<Color>,
    val calm: GradientColors,
    val focus: GradientColors,
    val celebrate: GradientColors,
    val scheme: ColorScheme
)

val LocalTodoriColors = staticCompositionLocalOf { todoriColors(ThemeId.Pink, dark = false) }

/** 지금 적용된 캐릭터. [TomatoCharacter]가 기본값으로 읽는다. */
val LocalSkin = staticCompositionLocalOf { SkinId.Tomato }

val todoriColors: TodoriColors
    @Composable @ReadOnlyComposable get() = LocalTodoriColors.current

private fun scheme(
    dark: Boolean,
    primary: Color,
    onPrimary: Color,
    background: Color,
    onBackground: Color,
    secondary: Color,
    surfaceVariant: Color,
    onSurfaceVariant: Color
): ColorScheme = if (dark) {
    darkColorScheme(
        primary = primary, onPrimary = onPrimary, secondary = secondary,
        background = background, onBackground = onBackground,
        surface = background, onSurface = onBackground,
        surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant
    )
} else {
    lightColorScheme(
        primary = primary, onPrimary = onPrimary, secondary = secondary,
        background = background, onBackground = onBackground,
        surface = background, onSurface = onBackground,
        surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant
    )
}

private fun g(top: Long, bottom: Long) = GradientColors(Color(top), Color(bottom))

/** [theme]의 색. [dark]는 어두운 모드 여부이며, 항상 어두운 테마는 값과 상관없이 어두운 색을 쓴다. */
fun todoriColors(theme: ThemeId, dark: Boolean): TodoriColors = when (theme) {
    ThemeId.Pink -> if (dark) pinkDark else pinkLight
    ThemeId.Lavender -> if (dark) lavenderDark else lavenderLight
    ThemeId.Mint -> if (dark) mintDark else mintLight
    ThemeId.Midnight -> midnight
}

private val pinkLight = TodoriColors(
    accentLight = Color(0xFFFFA9BA), accentDeep = Color(0xFFFF7C93), glow = Color(0xFFFFC6B8),
    decor = listOf(Color(0xFFFFA9BA), Color(0xFFFFC6B8), Color(0xFFE9D8FF)),
    calm = g(0xFFFFF7F9, 0xFFFFE6EC), focus = g(0xFFFFF1F4, 0xFFFFD6E0), celebrate = g(0xFFFFF5EC, 0xFFFFD9CB),
    scheme = scheme(
        false, Color(0xFFFF8FA3), Color.White, Color(0xFFFFF5F7), Color(0xFF5A3A3F),
        Color(0xFF6DBB82), Color(0xFFFFE3E0), Color(0xFFD9627C)
    )
)

private val pinkDark = TodoriColors(
    accentLight = Color(0xFFFFA9BA), accentDeep = Color(0xFFFF7C93), glow = Color(0xFFFFC6B8),
    decor = listOf(Color(0xFFFFA9BA), Color(0xFFFFC6B8), Color(0xFFE9D8FF)),
    calm = g(0xFF2A1E22, 0xFF38252C), focus = g(0xFF2F2028, 0xFF402733), celebrate = g(0xFF32231F, 0xFF45291F),
    scheme = scheme(
        true, Color(0xFFFFB3C1), Color(0xFF4A1F2A), Color(0xFF2A1E22), Color(0xFFFFEDEF),
        Color(0xFF8FD19E), Color(0xFF3F2C32), Color(0xFFFFB3C1)
    )
)

private val lavenderLight = TodoriColors(
    accentLight = Color(0xFFBBA8FF), accentDeep = Color(0xFF9378F2), glow = Color(0xFFF1C9FF),
    decor = listOf(Color(0xFFBBA8FF), Color(0xFFF1C9FF), Color(0xFFD7E6FF)),
    calm = g(0xFFF9F6FF, 0xFFEDE5FF), focus = g(0xFFF3EEFF, 0xFFE0D4FF), celebrate = g(0xFFFFF3FB, 0xFFF5DAFF),
    scheme = scheme(
        false, Color(0xFF9F89F5), Color.White, Color(0xFFF9F6FF), Color(0xFF3F3560),
        Color(0xFF8E74F0), Color(0xFFE9E0FF), Color(0xFF7A5FD6)
    )
)

private val lavenderDark = TodoriColors(
    accentLight = Color(0xFFC9B8FF), accentDeep = Color(0xFFA58BFF), glow = Color(0xFFE2B8FF),
    decor = listOf(Color(0xFFC9B8FF), Color(0xFFE2B8FF), Color(0xFFB8D0FF)),
    calm = g(0xFF211A33, 0xFF2D2347), focus = g(0xFF261E3C, 0xFF382C5C), celebrate = g(0xFF2E2140, 0xFF47305F),
    scheme = scheme(
        true, Color(0xFFCBB9FF), Color(0xFF2B1F52), Color(0xFF211A33), Color(0xFFEDE7FF),
        Color(0xFFB39DFF), Color(0xFF372C57), Color(0xFFCBB9FF)
    )
)

private val mintLight = TodoriColors(
    accentLight = Color(0xFF8FDAC0), accentDeep = Color(0xFF4FBF9F), glow = Color(0xFFFFE3A3),
    decor = listOf(Color(0xFF8FDAC0), Color(0xFFFFE3A3), Color(0xFFCFF3E6)),
    calm = g(0xFFF4FFFA, 0xFFDDF7EE), focus = g(0xFFEDFBF5, 0xFFCBEFE0), celebrate = g(0xFFFFFAEC, 0xFFDFF5E6),
    scheme = scheme(
        false, Color(0xFF5CC8A8), Color.White, Color(0xFFF4FFFA), Color(0xFF2F4A44),
        Color(0xFF4FBF9F), Color(0xFFD9F5EA), Color(0xFF2E9C7F)
    )
)

private val mintDark = TodoriColors(
    accentLight = Color(0xFF8FE0C4), accentDeep = Color(0xFF52C7A3), glow = Color(0xFFFFE3A3),
    decor = listOf(Color(0xFF8FE0C4), Color(0xFFFFE3A3), Color(0xFFB7F0DD)),
    calm = g(0xFF16261F, 0xFF1F3830), focus = g(0xFF1A2D25, 0xFF264638), celebrate = g(0xFF282A1B, 0xFF37402A),
    scheme = scheme(
        true, Color(0xFF8FE0C4), Color(0xFF0F3A2D), Color(0xFF16261F), Color(0xFFE3FAF1),
        Color(0xFF6FD3B1), Color(0xFF24443A), Color(0xFF8FE0C4)
    )
)

/** 한밤 파스텔: 남색 밤하늘에 분홍·하늘·노랑 파스텔 포인트. 시스템 모드와 상관없이 항상 어둡다. */
private val midnight = TodoriColors(
    accentLight = Color(0xFFFFB4C8), accentDeep = Color(0xFFFF86A6), glow = Color(0xFFA9C7FF),
    decor = listOf(Color(0xFFA9C7FF), Color(0xFFFFD9A0), Color(0xFFFFB4C8)),
    calm = g(0xFF14172B, 0xFF222850), focus = g(0xFF181C35, 0xFF2B3260), celebrate = g(0xFF241C38, 0xFF3D2A5A),
    scheme = scheme(
        true, Color(0xFFFFB4C8), Color(0xFF4A1F30), Color(0xFF14172B), Color(0xFFE8ECFF),
        Color(0xFFA9C7FF), Color(0xFF2A3158), Color(0xFFFFB4C8)
    )
)

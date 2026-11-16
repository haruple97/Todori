package com.haruple97.todori.feature.record.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.haruple97.todori.core.model.HeatStyle

// 밝은 배경: 많을수록 진하다.
private val RipeningLight = listOf(Color(0xFFBFE6A8), Color(0xFFFFDE8A), Color(0xFFFFA3B3), Color(0xFFF2485A))
private val BlushLight = listOf(Color(0xFFFFD9DE), Color(0xFFFFAEBA), Color(0xFFFF7488), Color(0xFFD92B47))

// 어두운 배경: 옅은 색이 가장 튀므로 반대로 많을수록 밝고 선명하게 한다(GitHub 다크 잔디와 같은 방식).
private val RipeningDark = listOf(Color(0xFF5E8F55), Color(0xFFB89A3E), Color(0xFFE0708A), Color(0xFFFF6B7D))
private val BlushDark = listOf(Color(0xFF6B3A47), Color(0xFFA8506A), Color(0xFFE0607C), Color(0xFFFF9BB0))

@Composable
private fun isDarkSurface(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

/** [level](1~4)의 칸 색. 0(기록 없음)은 색이 없으므로 호출하지 않는다. 어두운 테마에서는 어두운 팔레트를 쓴다. */
@Composable
fun heatColor(style: HeatStyle, level: Int): Color {
    val ripening = style == HeatStyle.Ripening
    val palette = when {
        isDarkSurface() -> if (ripening) RipeningDark else BlushDark
        else -> if (ripening) RipeningLight else BlushLight
    }
    return palette[(level - 1).coerceIn(0, palette.lastIndex)]
}

/** 칸 위 글자색. 칸이 진하면 흰 글씨, 옅으면 어두운 글씨가 읽힌다. */
@Composable
fun heatOnColor(level: Int): Color = when {
    isDarkSurface() -> if (level >= 4) Color(0xFF3A1F27) else Color.White
    else -> if (level >= 3) Color.White else Color(0xFF5A3A3F)
}

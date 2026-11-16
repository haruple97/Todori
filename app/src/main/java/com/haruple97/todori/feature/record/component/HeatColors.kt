package com.haruple97.todori.feature.record.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.haruple97.todori.core.model.HeatStyle

// 토마토 빨강 하나의 투명도로 농도를 나타낸다. 배경 위에 얹히므로 라이트에서는 연분홍 → 진한 빨강,
// 다크에서는 어두운 색 → 선명한 빨강으로 같은 공식이 알아서 맞는다.
private val RedLight = Color(0xFFE8334F)
private val RedDark = Color(0xFFFF5C75)
private val RedAlphas = listOf(0.28f, 0.52f, 0.76f, 1f)

// 선택지로 남겨 둔 숙성 팔레트(초록 → 노랑 → 분홍 → 빨강).
private val RipeningLight = listOf(Color(0xFFBFE6A8), Color(0xFFFFDE8A), Color(0xFFFFA3B3), Color(0xFFF2485A))
private val RipeningDark = listOf(Color(0xFF5E8F55), Color(0xFFB89A3E), Color(0xFFE0708A), Color(0xFFFF6B7D))

@Composable
private fun isDarkSurface(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

/** [level](1~4)의 칸 색. 0(기록 없음)은 색이 없으므로 호출하지 않는다. */
@Composable
fun heatColor(style: HeatStyle, level: Int): Color {
    val index = (level - 1).coerceIn(0, 3)
    val dark = isDarkSurface()
    return when (style) {
        HeatStyle.Blush -> (if (dark) RedDark else RedLight).copy(alpha = RedAlphas[index])
        HeatStyle.Ripening -> (if (dark) RipeningDark else RipeningLight)[index]
    }
}

/** 칸 위 글자색. 다크에서는 어떤 단계든 흰 글씨, 라이트에서는 진한 칸에서만 흰 글씨가 읽힌다. */
@Composable
fun heatOnColor(level: Int): Color = when {
    isDarkSurface() -> Color.White
    else -> if (level >= 3) Color.White else Color(0xFF5A3A3F)
}

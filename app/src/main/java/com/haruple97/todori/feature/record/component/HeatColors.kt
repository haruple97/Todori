package com.haruple97.todori.feature.record.component

import androidx.compose.ui.graphics.Color
import com.haruple97.todori.core.model.HeatStyle

private val Ripening = listOf(
    Color(0xFFBFE6A8), // 1 덜 익음(연두)
    Color(0xFFFFDE8A), // 2 익는 중(노랑)
    Color(0xFFFFA3B3), // 3 거의 익음(분홍)
    Color(0xFFF2485A)  // 4 완숙(빨강)
)

private val Blush = listOf(
    Color(0xFFFFD9DE),
    Color(0xFFFFAEBA),
    Color(0xFFFF7488),
    Color(0xFFD92B47)
)

/** [level](1~4)의 칸 색. 0(기록 없음)은 색이 없으므로 호출하지 않는다. */
fun heatColor(style: HeatStyle, level: Int): Color {
    val palette = if (style == HeatStyle.Ripening) Ripening else Blush
    return palette[(level - 1).coerceIn(0, palette.lastIndex)]
}

/** 진한 칸 위에서는 흰 글씨, 옅은 칸 위에서는 어두운 글씨가 읽힌다. */
fun heatOnColor(level: Int): Color = if (level >= 3) Color.White else Color(0xFF5A3A3F)

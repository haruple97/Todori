package com.haruple97.todori.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.haruple97.todori.core.designsystem.theme.FaceDark
import com.haruple97.todori.core.designsystem.theme.LeafGreen
import com.haruple97.todori.core.designsystem.theme.LeafGreenDeep
import com.haruple97.todori.core.designsystem.theme.TomatoRed

/** 기록 달력에 채워지는 작은 토마토 스티커. 애니메이션 없이 한 번만 그린다. */
@Composable
fun MiniTomato(modifier: Modifier = Modifier, size: Dp = 26.dp) {
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension
        val cx = this.size.width / 2f
        val cy = s * 0.58f
        val rx = s * 0.44f
        val ry = s * 0.38f
        drawOval(
            brush = Brush.radialGradient(
                listOf(Color(0xFFFFA39A), TomatoRed),
                center = Offset(cx - rx * 0.3f, cy - ry * 0.4f),
                radius = rx * 1.5f
            ),
            topLeft = Offset(cx - rx, cy - ry),
            size = Size(rx * 2, ry * 2)
        )
        val pivot = Offset(cx, cy - ry + s * 0.03f)
        listOf(-70f, 70f).forEach { angle ->
            rotate(angle, pivot) {
                drawOval(
                    color = if (angle < 0) LeafGreen else LeafGreenDeep,
                    topLeft = Offset(pivot.x - s * 0.06f, pivot.y - s * 0.24f),
                    size = Size(s * 0.12f, s * 0.24f)
                )
            }
        }
        drawCircle(FaceDark, radius = s * 0.045f, center = Offset(cx - s * 0.15f, cy))
        drawCircle(FaceDark, radius = s * 0.045f, center = Offset(cx + s * 0.15f, cy))
    }
}

package com.haruple97.todori.feature.timer.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.haruple97.todori.core.designsystem.theme.JellyPinkDeep
import com.haruple97.todori.core.designsystem.theme.JellyPinkLight
import com.haruple97.todori.feature.timer.DialStepMinutes

/** 토마토 옆 세로 눈금. 위로 갈수록 시간이 길고, 현재 값 위치에서 동그란 손잡이가 따라 움직인다. */
@Composable
fun DialRail(minutes: Int, modifier: Modifier = Modifier) {
    val index = DialStepMinutes.indexOf(minutes).coerceAtLeast(0)
    val position by animateFloatAsState(
        targetValue = index.toFloat(),
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "railThumb"
    )
    Canvas(modifier.width(14.dp)) {
        val r = size.width / 2f
        val usable = size.height - 2f * r
        val lastIndex = DialStepMinutes.lastIndex
        fun y(i: Float) = size.height - r - usable * (i / lastIndex)

        drawLine(
            color = JellyPinkLight.copy(alpha = 0.5f),
            start = Offset(r, r),
            end = Offset(r, size.height - r),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
        for (i in 0..lastIndex) {
            drawCircle(JellyPinkLight, radius = 2.5.dp.toPx(), center = Offset(r, y(i.toFloat())))
        }
        val thumb = Offset(r, y(position))
        drawCircle(JellyPinkDeep.copy(alpha = 0.25f), radius = r * 1.4f, center = thumb)
        drawCircle(Color.White, radius = r, center = thumb)
        drawCircle(JellyPinkDeep, radius = r * 0.62f, center = thumb)
    }
}

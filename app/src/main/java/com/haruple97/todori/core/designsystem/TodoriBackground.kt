package com.haruple97.todori.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.haruple97.todori.core.designsystem.theme.todoriColors

enum class BackgroundTone { Calm, Focus, Celebrate }

/** 위(밝은)→아래 세로 그라데이션 배경. 상태에 따라, 그리고 테마가 바뀔 때도 색조가 부드럽게 바뀐다. */
@Composable
fun TodoriBackground(
    tone: BackgroundTone,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = todoriColors
    val palette = when (tone) {
        BackgroundTone.Calm -> colors.calm
        BackgroundTone.Focus -> colors.focus
        BackgroundTone.Celebrate -> colors.celebrate
    }
    val top by animateColorAsState(palette.top, tween(700), label = "bgTop")
    val bottom by animateColorAsState(palette.bottom, tween(700), label = "bgBottom")
    Box(modifier.background(Brush.verticalGradient(listOf(top, bottom)))) {
        content()
    }
}

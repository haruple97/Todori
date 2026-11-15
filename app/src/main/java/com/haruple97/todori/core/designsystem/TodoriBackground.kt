package com.haruple97.todori.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class BackgroundTone { Calm, Focus, Celebrate }

private data class ToneColors(val top: Color, val bottom: Color)

private val LightTones = mapOf(
    BackgroundTone.Calm to ToneColors(Color(0xFFFFF7F9), Color(0xFFFFE6EC)),
    BackgroundTone.Focus to ToneColors(Color(0xFFFFF1F4), Color(0xFFFFD6E0)),
    BackgroundTone.Celebrate to ToneColors(Color(0xFFFFF5EC), Color(0xFFFFD9CB))
)

private val DarkTones = mapOf(
    BackgroundTone.Calm to ToneColors(Color(0xFF2A1E22), Color(0xFF38252C)),
    BackgroundTone.Focus to ToneColors(Color(0xFF2F2028), Color(0xFF402733)),
    BackgroundTone.Celebrate to ToneColors(Color(0xFF32231F), Color(0xFF45291F))
)

/** 크림→연핑크 세로 그라데이션 배경. 상태에 따라 색조가 부드럽게 바뀐다. */
@Composable
fun TodoriBackground(
    tone: BackgroundTone,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val palette = (if (isSystemInDarkTheme()) DarkTones else LightTones).getValue(tone)
    val top by animateColorAsState(palette.top, tween(700), label = "bgTop")
    val bottom by animateColorAsState(palette.bottom, tween(700), label = "bgBottom")
    Box(modifier.background(Brush.verticalGradient(listOf(top, bottom)))) {
        content()
    }
}

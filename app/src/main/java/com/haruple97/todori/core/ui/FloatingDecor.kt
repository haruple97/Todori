package com.haruple97.todori.core.ui

import android.provider.Settings
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.haruple97.todori.core.designsystem.theme.JellyPinkLight
import com.haruple97.todori.core.designsystem.theme.Lavender
import com.haruple97.todori.core.designsystem.theme.PeachGlow
import kotlin.math.PI
import kotlin.math.sin

private enum class DecorKind { Heart, Sparkle, Dot }

private class Decor(val x: Float, val y: Float, val size: Float, val speed: Float, val phase: Float, val kind: DecorKind, val color: Color)

private val Decors = listOf(
    Decor(0.08f, 0.15f, 0.014f, 0.10f, 0.0f, DecorKind.Heart, JellyPinkLight),
    Decor(0.90f, 0.22f, 0.012f, 0.08f, 0.3f, DecorKind.Sparkle, PeachGlow),
    Decor(0.18f, 0.48f, 0.010f, 0.12f, 0.6f, DecorKind.Dot, Lavender),
    Decor(0.84f, 0.55f, 0.016f, 0.09f, 0.1f, DecorKind.Heart, PeachGlow),
    Decor(0.50f, 0.08f, 0.010f, 0.11f, 0.8f, DecorKind.Sparkle, Lavender),
    Decor(0.06f, 0.78f, 0.012f, 0.10f, 0.4f, DecorKind.Sparkle, JellyPinkLight),
    Decor(0.93f, 0.85f, 0.010f, 0.13f, 0.7f, DecorKind.Dot, JellyPinkLight),
    Decor(0.35f, 0.92f, 0.014f, 0.08f, 0.2f, DecorKind.Heart, Lavender),
    Decor(0.70f, 0.35f, 0.009f, 0.12f, 0.9f, DecorKind.Dot, PeachGlow)
)

/** 배경에서 천천히 떠다니는 하트·반짝이·점. 시스템 애니메이션이 꺼져 있으면 멈춘다. */
@Composable
fun FloatingDecor(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val reduceMotion = remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
    val transition = rememberInfiniteTransition(label = "decor")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(24_000, easing = LinearEasing), RepeatMode.Restart),
        label = "decorT"
    )
    Canvas(modifier.fillMaxSize()) {
        val progress = if (reduceMotion) 0f else t
        val m = minOf(size.width, size.height)
        Decors.forEach { d ->
            val yFrac = ((d.y - progress * d.speed * 4f) % 1f + 1f) % 1f
            val sway = sin((progress * 2f + d.phase) * 2f * PI.toFloat()) * 0.015f
            val center = Offset((d.x + sway) * size.width, yFrac * size.height)
            val color = d.color.copy(alpha = 0.45f)
            val s = d.size * m * 2f
            when (d.kind) {
                DecorKind.Heart -> drawHeart(center, s, color)
                DecorKind.Sparkle -> drawSparkle(center, s * 1.4f, color)
                DecorKind.Dot -> drawCircle(color, s * 0.7f, center)
            }
        }
    }
}

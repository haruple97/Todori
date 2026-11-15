package com.haruple97.todori.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** shape: 0=하트, 1=반짝이, 2=점 */
private class Particle(val angle: Float, val speed: Float, val size: Float, val color: Color, val shape: Int)

private val ConfettiColors = listOf(
    Color(0xFFFF8FA3), Color(0xFFFFB3C1), Color(0xFFFFD6A5), Color(0xFFB8E0A8), Color(0xFFFFC8DD)
)

/** [trigger]가 true가 되는 순간 하트·점 파티클이 화면 중앙에서 터진다. */
@Composable
fun ConfettiBurst(trigger: Boolean, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    val particles = remember {
        List(28) {
            Particle(
                angle = Random.nextFloat() * 2f * PI.toFloat(),
                speed = 0.25f + Random.nextFloat() * 0.35f,
                size = 0.012f + Random.nextFloat() * 0.012f,
                color = ConfettiColors.random(),
                shape = it % 3
            )
        }
    }
    LaunchedEffect(trigger) {
        progress.snapTo(0f)
        if (trigger) progress.animateTo(1f, tween(1700, easing = FastOutSlowInEasing))
    }
    Canvas(modifier.fillMaxSize()) {
        val p = progress.value
        if (p <= 0f || p >= 1f) return@Canvas
        val m = size.minDimension
        particles.forEach { particle ->
            val dist = particle.speed * m * p
            val x = center.x + cos(particle.angle) * dist
            val y = center.y + sin(particle.angle) * dist + 0.3f * m * p * p
            val color = particle.color.copy(alpha = 1f - p)
            val r = particle.size * m
            when (particle.shape) {
                0 -> drawHeart(Offset(x, y), r, color)
                1 -> drawSparkle(Offset(x, y), r * 1.5f, color)
                else -> drawCircle(color, r * 0.7f, Offset(x, y))
            }
        }
    }
}

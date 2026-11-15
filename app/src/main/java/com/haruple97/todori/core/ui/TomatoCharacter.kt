package com.haruple97.todori.core.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import com.haruple97.todori.core.designsystem.theme.CheekPink
import com.haruple97.todori.core.designsystem.theme.FaceDark
import com.haruple97.todori.core.designsystem.theme.LeafGreen
import com.haruple97.todori.core.designsystem.theme.LeafGreenDeep
import com.haruple97.todori.core.designsystem.theme.PeachGlow
import com.haruple97.todori.core.designsystem.theme.TomatoRed
import com.haruple97.todori.core.designsystem.theme.UnripeGreen
import kotlin.random.Random
import kotlinx.coroutines.delay

enum class TomatoMood { Idle, Focus, Sleepy, Happy, Relaxed }

/** 토돌이. 전부 Canvas로 그려 에셋 없이 mood별 표정·애니메이션을 낸다. [ripeness] 0=덜 익음, 1=잘 익음. */
@Composable
fun TomatoCharacter(
    mood: TomatoMood,
    ripeness: Float,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "tomato")
    val breath by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(if (mood == TomatoMood.Sleepy) 3600 else 2000, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "breath"
    )
    val sway by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "sway"
    )
    val hop by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(450, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "hop"
    )
    val blink = remember { Animatable(1f) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2500, 4500))
            blink.animateTo(0.08f, tween(70))
            blink.animateTo(1f, tween(110))
        }
    }

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                val happy = mood == TomatoMood.Happy
                val base = 1f + 0.03f * breath
                scaleX = base + if (happy) 0.03f * hop else 0f
                scaleY = base - if (happy) 0.03f * hop else 0f
                translationY = if (happy) -size.height * 0.08f * hop else 0f
                transformOrigin = TransformOrigin(0.5f, 0.95f)
            }
    ) {
        drawTomato(mood, ripeness.coerceIn(0f, 1f), sway, breath, blink.value, hop)
    }
}

private fun DrawScope.drawTomato(
    mood: TomatoMood,
    ripeness: Float,
    sway: Float,
    breath: Float,
    blink: Float,
    hop: Float
) {
    val s = size.minDimension
    val cx = size.width / 2f
    val cy = s * 0.57f
    val rx = s * 0.44f
    val ry = s * 0.39f
    val base = lerp(UnripeGreen, TomatoRed, ripeness)

    // 그림자
    drawOval(
        Color.Black.copy(alpha = 0.08f),
        topLeft = Offset(cx - rx * (0.75f - 0.03f * breath), cy + ry - s * 0.02f),
        size = Size(rx * 2 * (0.75f - 0.03f * breath), s * 0.07f)
    )

    // 몸통
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(lerp(base, Color.White, 0.35f), base),
            center = Offset(cx - rx * 0.3f, cy - ry * 0.4f),
            radius = rx * 1.4f
        ),
        topLeft = Offset(cx - rx, cy - ry),
        size = Size(rx * 2, ry * 2)
    )
    // 하이라이트
    drawArc(
        color = Color.White.copy(alpha = 0.5f),
        startAngle = 200f,
        sweepAngle = 45f,
        useCenter = false,
        topLeft = Offset(cx - rx * 0.82f, cy - ry * 0.82f),
        size = Size(rx * 1.64f, ry * 1.64f),
        style = Stroke(width = s * 0.03f, cap = StrokeCap.Round)
    )

    // 잎·꼭지
    val pivot = Offset(cx, cy - ry + s * 0.02f)
    listOf(-78f, -40f, 40f, 78f).forEach { angle ->
        rotate(angle + sway * 3f, pivot) {
            drawOval(
                color = if (angle < 0) LeafGreen else LeafGreenDeep,
                topLeft = Offset(pivot.x - s * 0.045f, pivot.y - s * 0.2f),
                size = Size(s * 0.09f, s * 0.2f)
            )
        }
    }
    rotate(sway * 2f, pivot) {
        drawRoundRect(
            color = LeafGreenDeep,
            topLeft = Offset(pivot.x - s * 0.018f, pivot.y - s * 0.1f),
            size = Size(s * 0.036f, s * 0.12f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(s * 0.018f)
        )
    }

    // 볼터치
    val cheekY = cy + s * 0.07f
    listOf(-1f, 1f).forEach { side ->
        drawCircle(CheekPink.copy(alpha = 0.55f), radius = s * 0.065f, center = Offset(cx + side * s * 0.27f, cheekY))
    }

    drawEyes(mood, cx, cy, s, blink)
    drawMouth(mood, cx, cy + s * 0.1f, s)
    if (mood == TomatoMood.Focus) drawSweat(cx + s * 0.28f, cy - s * 0.15f + breath * s * 0.01f, s)
    if (mood == TomatoMood.Sleepy) drawSleepZ(cx + s * 0.34f, cy - s * 0.3f, s, (sway + 1f) / 2f)
    if (mood == TomatoMood.Happy) {
        drawSparkle(Offset(cx - s * 0.4f, cy - s * 0.32f), s * 0.05f * (0.6f + 0.6f * hop), Color.White)
        drawSparkle(Offset(cx + s * 0.42f, cy - s * 0.1f), s * 0.04f * (1.2f - 0.6f * hop), PeachGlow)
    }
}

/** 졸린 토마토 머리 위로 떠오르는 두 개의 z. [t]는 0..1 반복 진행도. */
private fun DrawScope.drawSleepZ(x: Float, y: Float, s: Float, t: Float) {
    listOf(0f to 0.05f, 0.5f to 0.035f).forEach { (offset, size) ->
        val p = (t + offset) % 1f
        val z = s * size
        val cx = x + p * s * 0.05f
        val cy = y - p * s * 0.12f
        val color = Color.White.copy(alpha = 0.9f * (1f - p))
        val path = Path().apply {
            moveTo(cx - z, cy - z)
            lineTo(cx + z, cy - z)
            lineTo(cx - z, cy + z)
            lineTo(cx + z, cy + z)
        }
        drawPath(path, color, style = Stroke(width = s * 0.016f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}

private fun DrawScope.drawEyes(mood: TomatoMood, cx: Float, cy: Float, s: Float, blink: Float) {
    val eyeY = cy - s * 0.01f
    val stroke = Stroke(width = s * 0.024f, cap = StrokeCap.Round)
    listOf(-1f, 1f).forEach { side ->
        val ex = cx + side * s * 0.16f
        when (mood) {
            TomatoMood.Idle, TomatoMood.Focus -> {
                val rx = s * 0.038f
                val ry = s * 0.05f * blink
                drawOval(FaceDark, topLeft = Offset(ex - rx, eyeY - ry), size = Size(rx * 2, ry * 2))
                if (blink > 0.5f) {
                    drawCircle(Color.White, radius = s * 0.012f, center = Offset(ex - s * 0.01f, eyeY - s * 0.02f))
                }
            }
            TomatoMood.Happy -> drawArc(
                FaceDark, 200f, 140f, false,
                Offset(ex - s * 0.05f, eyeY - s * 0.035f), Size(s * 0.1f, s * 0.07f), style = stroke
            )
            TomatoMood.Relaxed -> drawArc(
                FaceDark, 20f, 140f, false,
                Offset(ex - s * 0.05f, eyeY - s * 0.035f), Size(s * 0.1f, s * 0.07f), style = stroke
            )
            TomatoMood.Sleepy -> drawArc(
                FaceDark, 20f, 140f, false,
                Offset(ex - s * 0.05f, eyeY - s * 0.015f), Size(s * 0.1f, s * 0.03f), style = stroke
            )
        }
    }
}

private fun DrawScope.drawMouth(mood: TomatoMood, cx: Float, y: Float, s: Float) {
    val stroke = Stroke(width = s * 0.022f, cap = StrokeCap.Round)
    when (mood) {
        TomatoMood.Idle -> drawArc(
            FaceDark, 20f, 140f, false, Offset(cx - s * 0.06f, y - s * 0.04f), Size(s * 0.12f, s * 0.08f), style = stroke
        )
        TomatoMood.Focus -> drawArc(
            FaceDark, 40f, 100f, false, Offset(cx - s * 0.04f, y - s * 0.02f), Size(s * 0.08f, s * 0.05f), style = stroke
        )
        TomatoMood.Relaxed -> drawArc(
            FaceDark, 20f, 140f, false, Offset(cx - s * 0.08f, y - s * 0.05f), Size(s * 0.16f, s * 0.1f), style = stroke
        )
        TomatoMood.Sleepy -> drawOval(
            FaceDark.copy(alpha = 0.8f), Offset(cx - s * 0.02f, y - s * 0.015f), Size(s * 0.04f, s * 0.035f)
        )
        TomatoMood.Happy -> {
            drawArc(
                FaceDark, 0f, 180f, true, Offset(cx - s * 0.07f, y - s * 0.05f), Size(s * 0.14f, s * 0.12f)
            )
            drawCircle(CheekPink, radius = s * 0.025f, center = Offset(cx, y + s * 0.045f))
        }
    }
}

private fun DrawScope.drawSweat(x: Float, y: Float, s: Float) {
    val r = s * 0.028f
    val drop = Path().apply {
        moveTo(x, y - r * 2.2f)
        lineTo(x - r * 0.9f, y - r * 0.4f)
        lineTo(x + r * 0.9f, y - r * 0.4f)
        close()
    }
    val color = Color(0xFFBAE3FF)
    drawPath(drop, color)
    drawCircle(color, radius = r, center = Offset(x, y))
}

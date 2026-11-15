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
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import com.haruple97.todori.core.designsystem.theme.CheekPink
import com.haruple97.todori.core.designsystem.theme.FaceDark
import com.haruple97.todori.core.designsystem.theme.LeafGreen
import com.haruple97.todori.core.designsystem.theme.LeafGreenDeep
import com.haruple97.todori.core.designsystem.theme.LocalSkin
import com.haruple97.todori.core.model.SkinId
import kotlin.random.Random
import kotlinx.coroutines.delay

enum class TomatoMood { Idle, Focus, Sleepy, Happy, Relaxed }

private enum class BodyShape { Oval, Strawberry, Peach, Cherry, Lemon }

/** 친구별 생김새. 반지름·중심은 캔버스 한 변 대비 비율이고, faceScale은 작은 몸에 맞춰 얼굴을 줄이는 값. */
private class SkinSpec(
    val unripe: Color,
    val ripe: Color,
    val shape: BodyShape,
    val rx: Float,
    val ry: Float,
    val cy: Float,
    val faceScale: Float = 1f,
    val faceDy: Float = 0f
)

private fun SkinId.spec(): SkinSpec = when (this) {
    SkinId.Tomato -> SkinSpec(Color(0xFFB8E0A8), Color(0xFFFF6B6B), BodyShape.Oval, 0.44f, 0.39f, 0.57f)
    SkinId.Strawberry -> SkinSpec(Color(0xFFDDEBB0), Color(0xFFFF5C7E), BodyShape.Strawberry, 0.42f, 0.41f, 0.58f, 0.92f, -0.02f)
    SkinId.Peach -> SkinSpec(Color(0xFFC8E6A0), Color(0xFFFFA58E), BodyShape.Peach, 0.43f, 0.41f, 0.57f)
    SkinId.Cherry -> SkinSpec(Color(0xFFB8DDA0), Color(0xFFFF4D6D), BodyShape.Cherry, 0.35f, 0.35f, 0.64f, 0.82f)
    SkinId.Lemon -> SkinSpec(Color(0xFFC5E1A5), Color(0xFFFFDD55), BodyShape.Lemon, 0.45f, 0.33f, 0.62f, 0.9f)
}

/**
 * 토돌이와 친구들. 전부 Canvas로 그려 에셋 없이 mood별 표정·애니메이션을 낸다.
 * [ripeness] 0=덜 익음, 1=잘 익음. [skin]은 기본적으로 앱에서 고른 친구를 따른다.
 */
@Composable
fun TomatoCharacter(
    mood: TomatoMood,
    ripeness: Float,
    modifier: Modifier = Modifier,
    skin: SkinId = LocalSkin.current
) {
    val spec = remember(skin) { skin.spec() }
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
        drawFriend(spec, mood, ripeness.coerceIn(0f, 1f), sway, breath, blink.value, hop)
    }
}

private fun DrawScope.drawFriend(
    spec: SkinSpec,
    mood: TomatoMood,
    ripeness: Float,
    sway: Float,
    breath: Float,
    blink: Float,
    hop: Float
) {
    val s = size.minDimension
    val cx = size.width / 2f
    val cy = s * spec.cy
    val rx = s * spec.rx
    val ry = s * spec.ry
    val base = lerp(spec.unripe, spec.ripe, ripeness)

    // 그림자
    drawOval(
        Color.Black.copy(alpha = 0.08f),
        topLeft = Offset(cx - rx * (0.75f - 0.03f * breath), cy + ry - s * 0.02f),
        size = Size(rx * 2 * (0.75f - 0.03f * breath), s * 0.07f)
    )

    val brush = Brush.radialGradient(
        colors = listOf(lerp(base, Color.White, 0.35f), base),
        center = Offset(cx - rx * 0.3f, cy - ry * 0.4f),
        radius = rx * 1.4f
    )
    drawBody(spec, brush, base, cx, cy, rx, ry, s, sway)

    // 얼굴은 몸 크기에 맞춰 줄이고, 몸 중심에 둔다.
    val faceY = cy + s * spec.faceDy
    scale(spec.faceScale, Offset(cx, faceY)) {
        val cheekY = faceY + s * 0.07f
        listOf(-1f, 1f).forEach { side ->
            drawCircle(CheekPink.copy(alpha = 0.55f), radius = s * 0.065f, center = Offset(cx + side * s * 0.27f, cheekY))
        }
        drawEyes(mood, cx, faceY, s, blink)
        drawMouth(mood, cx, faceY + s * 0.1f, s)
    }

    val k = rx / (s * 0.44f)
    if (mood == TomatoMood.Focus) drawSweat(cx + s * 0.28f * k, cy - s * 0.15f + breath * s * 0.01f, s)
    if (mood == TomatoMood.Sleepy) drawSleepZ(cx + s * 0.34f * k, cy - s * 0.3f, s, (sway + 1f) / 2f)
    if (mood == TomatoMood.Happy) {
        drawSparkle(Offset(cx - s * 0.4f * k, cy - s * 0.32f), s * 0.05f * (0.6f + 0.6f * hop), Color.White)
        drawSparkle(Offset(cx + s * 0.42f * k, cy - s * 0.1f), s * 0.04f * (1.2f - 0.6f * hop), Color(0xFFFFC6B8))
    }
}

/** 몸통과 그 위의 장식(잎·씨앗·갈라짐·줄기)을 모양별로 그린다. */
private fun DrawScope.drawBody(
    spec: SkinSpec,
    brush: Brush,
    base: Color,
    cx: Float,
    cy: Float,
    rx: Float,
    ry: Float,
    s: Float,
    sway: Float
) {
    val highlight = Color.White.copy(alpha = 0.5f)
    val highlightStroke = Stroke(width = s * 0.03f, cap = StrokeCap.Round)
    when (spec.shape) {
        BodyShape.Oval -> {
            drawOval(brush, Offset(cx - rx, cy - ry), Size(rx * 2, ry * 2))
            drawArc(highlight, 200f, 45f, false, Offset(cx - rx * 0.82f, cy - ry * 0.82f), Size(rx * 1.64f, ry * 1.64f), style = highlightStroke)
            val pivot = Offset(cx, cy - ry + s * 0.02f)
            drawLeaves(pivot, s, listOf(-78f, -40f, 40f, 78f), 0.2f, 0.09f, sway = sway * 3f)
            drawStem(pivot, s, LeafGreenDeep, 0.12f)
        }
        BodyShape.Strawberry -> {
            drawPath(strawberryPath(cx, cy, rx, ry), brush)
            drawArc(highlight, 205f, 40f, false, Offset(cx - rx * 0.8f, cy - ry * 0.85f), Size(rx * 1.6f, ry * 1.6f), style = highlightStroke)
            val seed = Color(0xFFFFEBB0).copy(alpha = 0.9f)
            listOf(
                -0.55f to 0.25f, 0.55f to 0.25f, -0.32f to 0.62f, 0.32f to 0.62f, 0f to 0.82f,
                -0.62f to -0.35f, 0.62f to -0.35f, -0.25f to -0.5f, 0.25f to -0.5f
            ).forEach { (dx, dy) ->
                drawOval(seed, Offset(cx + dx * rx - s * 0.008f, cy + dy * ry - s * 0.013f), Size(s * 0.016f, s * 0.026f))
            }
            val pivot = Offset(cx, cy - ry * 0.74f)
            drawLeaves(pivot, s, listOf(-64f, -32f, 0f, 32f, 64f), 0.16f, 0.07f, sway = sway * 2f)
            drawStem(pivot, s, LeafGreenDeep, 0.08f)
        }
        BodyShape.Peach -> {
            drawOval(brush, Offset(cx - rx, cy - ry), Size(rx * 2, ry * 2))
            // 복숭아 특유의 갈라진 곡선과 볼그레한 기운
            drawArc(
                Color.White.copy(alpha = 0.35f), 250f, 70f, false,
                Offset(cx - rx * 0.1f, cy - ry * 1.05f), Size(rx * 1.5f, ry * 1.9f),
                style = Stroke(width = s * 0.018f, cap = StrokeCap.Round)
            )
            drawCircle(Color(0xFFFF8FA3).copy(alpha = 0.18f), radius = rx * 0.32f, center = Offset(cx + rx * 0.5f, cy + ry * 0.32f))
            drawArc(highlight, 200f, 45f, false, Offset(cx - rx * 0.82f, cy - ry * 0.82f), Size(rx * 1.64f, ry * 1.64f), style = highlightStroke)
            val pivot = Offset(cx + s * 0.03f, cy - ry + s * 0.02f)
            drawStem(pivot, s, Color(0xFF8B6B4A), 0.07f)
            rotate(48f + sway * 3f, pivot) {
                drawOval(LeafGreen, Offset(pivot.x - s * 0.05f, pivot.y - s * 0.2f), Size(s * 0.1f, s * 0.2f))
            }
        }
        BodyShape.Cherry -> {
            // 줄기는 몸통 뒤에서 위로 휘어 올라간다.
            val topY = cy - ry
            val stem = Path().apply {
                moveTo(cx, topY + s * 0.02f)
                quadraticTo(cx + s * 0.02f, topY - s * 0.2f, cx + s * 0.2f, topY - s * 0.3f)
            }
            drawPath(stem, Color(0xFF7C9460), style = Stroke(width = s * 0.03f, cap = StrokeCap.Round))
            drawOval(brush, Offset(cx - rx, cy - ry), Size(rx * 2, ry * 2))
            drawArc(highlight, 200f, 50f, false, Offset(cx - rx * 0.8f, cy - ry * 0.8f), Size(rx * 1.6f, ry * 1.6f), style = highlightStroke)
            val tip = Offset(cx + s * 0.2f, topY - s * 0.3f)
            rotate(55f, tip) {
                drawOval(LeafGreen, Offset(tip.x - s * 0.05f, tip.y - s * 0.17f), Size(s * 0.1f, s * 0.17f))
            }
        }
        BodyShape.Lemon -> {
            val pivot = Offset(cx, cy)
            rotate(-12f, pivot) {
                // 양 끝이 살짝 뾰족한 레몬
                drawCircle(base, radius = s * 0.06f, center = Offset(cx - rx * 1.0f, cy - s * 0.01f))
                drawCircle(base, radius = s * 0.06f, center = Offset(cx + rx * 1.0f, cy - s * 0.01f))
                drawOval(brush, Offset(cx - rx, cy - ry), Size(rx * 2, ry * 2))
                drawArc(highlight, 205f, 45f, false, Offset(cx - rx * 0.8f, cy - ry * 0.8f), Size(rx * 1.6f, ry * 1.6f), style = highlightStroke)
            }
            val top = Offset(cx - rx * 0.1f, cy - ry - s * 0.05f)
            drawStem(top, s, Color(0xFF8B7A3A), 0.06f)
            rotate(-52f + sway * 3f, top) {
                drawOval(LeafGreen, Offset(top.x - s * 0.05f, top.y - s * 0.19f), Size(s * 0.1f, s * 0.19f))
            }
        }
    }
}

private fun strawberryPath(cx: Float, cy: Float, rx: Float, ry: Float): Path = Path().apply {
    moveTo(cx, cy - ry * 0.78f)
    cubicTo(cx + rx * 0.55f, cy - ry * 1.05f, cx + rx * 1.12f, cy - ry * 0.6f, cx + rx * 0.95f, cy - ry * 0.05f)
    cubicTo(cx + rx * 0.8f, cy + ry * 0.55f, cx + rx * 0.35f, cy + ry * 1.0f, cx, cy + ry * 1.02f)
    cubicTo(cx - rx * 0.35f, cy + ry * 1.0f, cx - rx * 0.8f, cy + ry * 0.55f, cx - rx * 0.95f, cy - ry * 0.05f)
    cubicTo(cx - rx * 1.12f, cy - ry * 0.6f, cx - rx * 0.55f, cy - ry * 1.05f, cx, cy - ry * 0.78f)
    close()
}

private fun DrawScope.drawLeaves(pivot: Offset, s: Float, angles: List<Float>, length: Float, width: Float, sway: Float) {
    angles.forEach { angle ->
        rotate(angle + sway, pivot) {
            drawOval(
                color = if (angle < 0) LeafGreen else LeafGreenDeep,
                topLeft = Offset(pivot.x - s * width / 2f, pivot.y - s * length),
                size = Size(s * width, s * length)
            )
        }
    }
}

private fun DrawScope.drawStem(pivot: Offset, s: Float, color: Color, height: Float) {
    drawRoundRect(
        color = color,
        topLeft = Offset(pivot.x - s * 0.018f, pivot.y - s * height * 0.85f),
        size = Size(s * 0.036f, s * height),
        cornerRadius = CornerRadius(s * 0.018f)
    )
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

/** 졸린 친구 머리 위로 떠오르는 두 개의 z. [t]는 0..1 반복 진행도. */
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

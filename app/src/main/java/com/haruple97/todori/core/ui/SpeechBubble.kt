package com.haruple97.todori.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.haruple97.todori.core.designsystem.theme.todoriColors

private val TailHeight = 12.dp

/** 둥근 사각형 아래 중앙에 부드러운 꼬리가 하나의 외곽선으로 이어진 말풍선 모양. */
private class BubbleShape(private val corner: Dp, private val tailWidth: Dp, private val tailHeight: Dp) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = with(density) { corner.toPx() }
        val tw = with(density) { tailWidth.toPx() }
        val th = with(density) { tailHeight.toPx() }
        val w = size.width
        val bottom = size.height - th
        val cx = w / 2f
        val path = Path().apply {
            moveTo(r, 0f)
            lineTo(w - r, 0f)
            arcTo(Rect(w - 2 * r, 0f, w, 2 * r), -90f, 90f, false)
            lineTo(w, bottom - r)
            arcTo(Rect(w - 2 * r, bottom - 2 * r, w, bottom), 0f, 90f, false)
            lineTo(cx + tw / 2f, bottom)
            cubicTo(cx + tw * 0.15f, bottom, cx + tw * 0.12f, size.height, cx, size.height)
            cubicTo(cx - tw * 0.12f, size.height, cx - tw * 0.15f, bottom, cx - tw / 2f, bottom)
            lineTo(r, bottom)
            arcTo(Rect(0f, bottom - 2 * r, 2 * r, bottom), 90f, 90f, false)
            lineTo(0f, r)
            arcTo(Rect(0f, 0f, 2 * r, 2 * r), 180f, 90f, false)
            close()
        }
        return Outline.Generic(path)
    }
}

/** 토마토 머리 위에 뜨는 말풍선. 꼬리까지 테두리가 이어져 있고 꼬리가 아래(토마토)를 가리킨다. */
@Composable
fun SpeechBubble(text: String, modifier: Modifier = Modifier) {
    val shape = remember { BubbleShape(corner = 24.dp, tailWidth = 22.dp, tailHeight = TailHeight) }
    Box(
        modifier = modifier
            .shadow(4.dp, shape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), shape)
            .border(1.5.dp, todoriColors.accentLight, shape)
            // 문구 길이가 바뀌어도 크기가 부드럽게 따라가게 한다. 그림자가 잘리지 않도록 가장 안쪽에 둔다.
            .animateContentSize()
    ) {
        AnimatedContent(
            targetState = text,
            transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
            label = "bubbleText"
        ) { current ->
            Text(
                text = current,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp + TailHeight)
            )
        }
    }
}

package com.haruple97.todori.feature.timer.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.haruple97.todori.R
import com.haruple97.todori.core.designsystem.theme.todoriColors
import com.haruple97.todori.feature.timer.DialStepMinutes
import com.haruple97.todori.feature.timer.nearestIndex
import kotlin.math.roundToInt

private val RailWidth = 14.dp
private val TouchWidth = 44.dp

/**
 * 토마토 옆 세로 눈금 슬라이더. 위로 갈수록 시간이 길고, 손잡이를 끌거나 눈금을 눌러 시간을 바꿀 수 있다.
 * 터치 영역은 눈금 폭보다 넓게 잡아 한 손으로도 쉽게 잡힌다.
 */
@Composable
fun DialRail(
    minutes: Int,
    onMinutesChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val index = nearestIndex(minutes)
    val position by animateFloatAsState(
        targetValue = index.toFloat(),
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "railThumb"
    )
    val currentIndex by rememberUpdatedState(index)
    val onChange by rememberUpdatedState(onMinutesChange)
    val haptics = LocalHapticFeedback.current
    val description = stringResource(R.string.minutes_format, minutes)
    val light = todoriColors.accentLight
    val deep = todoriColors.accentDeep

    Box(
        modifier = modifier
            .width(TouchWidth)
            .pointerInput(Unit) {
                val radius = RailWidth.toPx() / 2f
                val lastIndex = DialStepMinutes.lastIndex
                fun indexAt(y: Float): Int {
                    val usable = size.height - 2f * radius
                    val fraction = 1f - ((y - radius) / usable).coerceIn(0f, 1f)
                    return (fraction * lastIndex).roundToInt()
                }
                fun update(y: Float) {
                    val next = indexAt(y)
                    if (next != currentIndex) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onChange(DialStepMinutes[next])
                    }
                }
                awaitEachGesture {
                    val down = awaitFirstDown()
                    update(down.position.y)
                    drag(down.id) { change ->
                        change.consume()
                        update(change.position.y)
                    }
                }
            }
            .semantics { stateDescription = description },
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.width(RailWidth).fillMaxHeight()) {
            val r = size.width / 2f
            val usable = size.height - 2f * r
            val lastIndex = DialStepMinutes.lastIndex
            fun y(i: Float) = size.height - r - usable * (i / lastIndex)

            drawLine(
                color = light.copy(alpha = 0.5f),
                start = Offset(r, r),
                end = Offset(r, size.height - r),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            for (i in 0..lastIndex) {
                drawCircle(light, radius = 2.5.dp.toPx(), center = Offset(r, y(i.toFloat())))
            }
            val thumb = Offset(r, y(position))
            drawCircle(deep.copy(alpha = 0.25f), radius = r * 1.4f, center = thumb)
            drawCircle(Color.White, radius = r, center = thumb)
            drawCircle(deep, radius = r * 0.62f, center = thumb)
        }
    }
}

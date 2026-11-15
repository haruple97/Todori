package com.haruple97.todori.feature.timer.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.haruple97.todori.R
import com.haruple97.todori.core.ui.TomatoCharacter
import com.haruple97.todori.core.ui.TomatoMood
import com.haruple97.todori.feature.timer.dialMinutesFor
import com.haruple97.todori.feature.timer.dialScale
import com.haruple97.todori.feature.timer.stepMinutes

/** 위아래로 쓸면 시간이 바뀌고 토마토가 그만큼 커지는 키우기 다이얼. */
@Composable
fun TomatoDial(
    minutes: Int,
    onMinutesChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val currentMinutes by rememberUpdatedState(minutes)
    val onChange by rememberUpdatedState(onMinutesChange)
    val pxPerStep = with(LocalDensity.current) { 36.dp.toPx() }
    var startMinutes by remember { mutableIntStateOf(minutes) }
    var dragged by remember { mutableFloatStateOf(0f) }

    val scale by animateFloatAsState(
        targetValue = dialScale(minutes),
        animationSpec = spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium),
        label = "dialScale"
    )
    val minutesText = stringResource(R.string.minutes_format, minutes)
    val increase = stringResource(R.string.dial_increase)
    val decrease = stringResource(R.string.dial_decrease)

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = {
                        startMinutes = currentMinutes
                        dragged = 0f
                    },
                    onVerticalDrag = { change, amount ->
                        change.consume()
                        dragged += amount
                        val next = dialMinutesFor(startMinutes, dragged, pxPerStep)
                        if (next != currentMinutes) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onChange(next)
                        }
                    }
                )
            }
            .semantics {
                stateDescription = minutesText
                customActions = listOf(
                    CustomAccessibilityAction(increase) { onChange(stepMinutes(currentMinutes, 1)); true },
                    CustomAccessibilityAction(decrease) { onChange(stepMinutes(currentMinutes, -1)); true }
                )
            }
    ) {
        TomatoCharacter(
            mood = TomatoMood.Idle,
            ripeness = 1f,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    transformOrigin = TransformOrigin(0.5f, 1f)
                }
        )
    }
}

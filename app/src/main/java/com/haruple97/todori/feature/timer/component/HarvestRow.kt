package com.haruple97.todori.feature.timer.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.haruple97.todori.R
import com.haruple97.todori.core.designsystem.theme.JellyPinkLight
import com.haruple97.todori.core.ui.MiniTomato

private const val MAX_VISIBLE = 8
private const val EMPTY_SLOTS = 3

/** 오늘 끝낸 집중 횟수만큼 미니 토마토가 쌓이는 '오늘의 수확' 줄. */
@Composable
fun HarvestRow(count: Int, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            if (count == 0) {
                repeat(EMPTY_SLOTS) { EmptySlot() }
            } else {
                repeat(minOf(count, MAX_VISIBLE)) { PoppingTomato() }
                if (count > MAX_VISIBLE) {
                    Text("+${count - MAX_VISIBLE}", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (count == 0) stringResource(R.string.harvest_empty) else stringResource(R.string.harvest_today, count),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun PoppingTomato() {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring(dampingRatio = 0.4f)) }
    MiniTomato(
        Modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        }
    )
}

@Composable
private fun EmptySlot() {
    Canvas(Modifier.size(26.dp)) {
        drawCircle(
            color = JellyPinkLight,
            radius = size.minDimension / 2f - 1.dp.toPx(),
            style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)))
        )
    }
}

package com.haruple97.todori.feature.timer

import kotlin.math.abs
import kotlin.math.roundToInt

val DialStepMinutes = listOf(5, 10, 15, 20, 25, 30, 40, 45, 50, 60, 90)
val PresetMinutes = listOf(15, 25, 30, 45, 60)

private fun nearestIndex(minutes: Int): Int =
    DialStepMinutes.indices.minBy { abs(DialStepMinutes[it] - minutes) }

/** 위로 드래그(dragPx 음수)하면 시간이 늘어난다. 스냅 단계 사이를 [pxPerStep]마다 이동. */
fun dialMinutesFor(startMinutes: Int, dragPx: Float, pxPerStep: Float): Int {
    val delta = (-dragPx / pxPerStep).roundToInt()
    return DialStepMinutes[(nearestIndex(startMinutes) + delta).coerceIn(0, DialStepMinutes.lastIndex)]
}

fun stepMinutes(minutes: Int, direction: Int): Int =
    DialStepMinutes[(nearestIndex(minutes) + direction).coerceIn(0, DialStepMinutes.lastIndex)]

/** 시간이 길수록 토마토가 커진다. */
fun dialScale(minutes: Int): Float {
    val fraction = (minutes - DialStepMinutes.first()).toFloat() / (DialStepMinutes.last() - DialStepMinutes.first())
    return 0.72f + 0.46f * fraction.coerceIn(0f, 1f)
}

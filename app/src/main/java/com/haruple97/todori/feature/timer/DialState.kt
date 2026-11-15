package com.haruple97.todori.feature.timer

import kotlin.math.abs
import kotlin.math.roundToInt

/** 5분 간격, 5~60분. */
val DialStepMinutes: List<Int> = (5..60 step 5).toList()

/** 프리미엄이면 1분 단위로 최대 3시간까지 직접 정할 수 있다. */
const val PremiumMinMinutes = 1
const val PremiumMaxMinutes = 180

/** 5분 단위가 아니거나 60분을 넘는 값(프리미엄에서 정한 값)이 와도 슬라이더에서 가장 가까운 눈금 위치. */
fun nearestIndex(minutes: Int): Int =
    DialStepMinutes.indices.minBy { abs(DialStepMinutes[it] - minutes) }

fun snapMinutes(minutes: Int): Int = DialStepMinutes[nearestIndex(minutes)]

/**
 * 저장된 시간을 지금 권한에 맞는 값으로 정리한다.
 * 프리미엄이면 1~180분으로 그대로 두고, 아니면(구독이 끊긴 경우 포함) 5분 단위·60분 이하로 맞춘다.
 */
fun normalizedMinutes(saved: Int, premium: Boolean): Int =
    if (premium) saved.coerceIn(PremiumMinMinutes, PremiumMaxMinutes) else snapMinutes(saved)

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

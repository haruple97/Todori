package com.haruple97.todori.core.common

/** 농도 단계 개수(1~[HEAT_MAX_LEVEL]). 0은 기록 없음. */
const val HEAT_MAX_LEVEL = 4

private const val LEVEL_2_MINUTES = 30
private const val LEVEL_3_MINUTES = 90
private const val LEVEL_4_MINUTES = 180

/**
 * 하루 집중 시간으로 농도 단계를 정한다.
 * 0=기록 없음, 1=30분 미만, 2=30분~1시간 29분, 3=1시간 30분~2시간 59분, 4=3시간 이상.
 */
fun heatLevel(minutes: Int): Int = when {
    minutes <= 0 -> 0
    minutes < LEVEL_2_MINUTES -> 1
    minutes < LEVEL_3_MINUTES -> 2
    minutes < LEVEL_4_MINUTES -> 3
    else -> HEAT_MAX_LEVEL
}

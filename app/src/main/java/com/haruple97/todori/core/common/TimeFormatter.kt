package com.haruple97.todori.core.common

/** 남은 시간(ms)을 올림하여 "mm:ss"로 표시한다. 음수는 0으로 취급. */
fun formatRemainingTime(remainingMillis: Long): String {
    val totalSeconds = (remainingMillis.coerceAtLeast(0) + 999) / 1000
    return "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
}

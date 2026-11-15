package com.haruple97.todori.core.common

/** 남은 시간(ms)을 올림하여 "mm:ss"로, 1시간 이상이면 "h:mm:ss"로 표시한다. 음수는 0으로 취급. */
fun formatRemainingTime(remainingMillis: Long): String {
    val totalSeconds = (remainingMillis.coerceAtLeast(0) + 999) / 1000
    val hours = totalSeconds / 3600
    val minutes = totalSeconds % 3600 / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}

package com.haruple97.todori.feature.timer

import com.haruple97.todori.core.model.TimerStatus

data class TimerUiState(
    val status: TimerStatus = TimerStatus.Idle,
    val focusMinutes: Int = 25,
    val totalMillis: Long = 0L,
    val remainingMillis: Long = 0L,
    val todayCount: Int = 0
) {
    val progress: Float
        get() = if (totalMillis <= 0L) 0f else (1f - remainingMillis.toFloat() / totalMillis).coerceIn(0f, 1f)
}

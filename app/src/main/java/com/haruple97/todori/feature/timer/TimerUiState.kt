package com.haruple97.todori.feature.timer

import com.haruple97.todori.core.model.TimerPhase

data class TimerUiState(
    val phase: TimerPhase = TimerPhase.Focus,
    val remainingMillis: Long = 25 * 60 * 1000L,
    val isRunning: Boolean = false
)

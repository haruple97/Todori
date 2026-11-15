package com.haruple97.todori.core.model

enum class TimerStatus { Idle, Running, Paused, Finished }

data class TimerState(
    val status: TimerStatus = TimerStatus.Idle,
    val totalMillis: Long = 0L,
    val remainingMillis: Long = 0L
)

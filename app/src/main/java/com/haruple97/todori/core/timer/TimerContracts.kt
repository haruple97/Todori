package com.haruple97.todori.core.timer

import com.haruple97.todori.core.model.TimerStatus

interface TimeSource {
    fun elapsedRealtime(): Long
    fun wallClockMillis(): Long
}

data class TimerSnapshot(
    val status: TimerStatus,
    val totalMillis: Long,
    val remainingMillis: Long,
    val endElapsed: Long,
    val savedAtElapsed: Long,
    val savedAtWall: Long
)

interface TimerPersistence {
    /** null이면 저장된 타이머를 지운다. */
    fun save(snapshot: TimerSnapshot?)
    suspend fun load(): TimerSnapshot?
}

interface TimerPlatform {
    fun scheduleAlarm(triggerElapsedMillis: Long)
    fun cancelAlarm()
    fun showRunning(endElapsedMillis: Long)
    fun clearRunning()
    fun showDone()
}

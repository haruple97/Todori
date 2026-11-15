package com.haruple97.todori.core.timer

import com.haruple97.todori.core.common.IoDispatcher
import com.haruple97.todori.core.data.repository.SessionRepository
import com.haruple97.todori.core.model.SessionRecord
import com.haruple97.todori.core.model.TimerState
import com.haruple97.todori.core.model.TimerStatus
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 시간 누적 대신 종료 시각(elapsedRealtime)을 기준으로 남은 시간을 계산하는 타이머 상태머신.
 * 앱이 백그라운드/종료돼도 정확하며, 알람 수신 시 같은 엔진이 완료를 처리한다.
 */
@Singleton
class TimerEngine @Inject constructor(
    private val clock: TimeSource,
    private val persistence: TimerPersistence,
    private val platform: TimerPlatform,
    private val sessions: SessionRepository,
    @IoDispatcher dispatcher: CoroutineDispatcher
) {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private var endElapsed = 0L
    private var restored = false
    private val restoreMutex = Mutex()

    suspend fun restore() {
        restoreMutex.withLock {
            if (restored) return
            restored = true
            persistence.load()?.let { applySnapshot(it) }
        }
    }

    @Synchronized
    fun start(totalMillis: Long) {
        endElapsed = clock.elapsedRealtime() + totalMillis
        _state.value = TimerState(TimerStatus.Running, totalMillis, totalMillis)
        platform.scheduleAlarm(endElapsed)
        platform.showRunning(endElapsed)
        persist()
    }

    @Synchronized
    fun pause() {
        refreshLocked()
        val s = _state.value
        if (s.status != TimerStatus.Running) return
        _state.value = s.copy(status = TimerStatus.Paused)
        platform.cancelAlarm()
        platform.clearRunning()
        persist()
    }

    @Synchronized
    fun resume() {
        val s = _state.value
        if (s.status != TimerStatus.Paused) return
        endElapsed = clock.elapsedRealtime() + s.remainingMillis
        _state.value = s.copy(status = TimerStatus.Running)
        platform.scheduleAlarm(endElapsed)
        platform.showRunning(endElapsed)
        persist()
    }

    @Synchronized
    fun cancel() = reset()

    @Synchronized
    fun refresh() = refreshLocked()

    /** 알람은 목표 시각 직전에 도착할 수 있어 약간의 오차를 허용한다. */
    @Synchronized
    fun onAlarm() = refreshLocked(toleranceMillis = ALARM_TOLERANCE_MILLIS)

    @Synchronized
    private fun applySnapshot(s: TimerSnapshot) {
        if (_state.value.status != TimerStatus.Idle) return
        when (s.status) {
            TimerStatus.Running -> {
                if (looksRebooted(s)) {
                    reset()
                    return
                }
                endElapsed = s.endElapsed
                val remaining = (endElapsed - clock.elapsedRealtime()).coerceAtLeast(0L)
                _state.value = TimerState(TimerStatus.Running, s.totalMillis, remaining)
                refreshLocked()
            }
            TimerStatus.Paused, TimerStatus.Finished ->
                _state.value = TimerState(s.status, s.totalMillis, s.remainingMillis)
            TimerStatus.Idle -> Unit
        }
    }

    private fun looksRebooted(s: TimerSnapshot): Boolean {
        val elapsedSinceSave = clock.elapsedRealtime() - s.savedAtElapsed
        val wallSinceSave = clock.wallClockMillis() - s.savedAtWall
        return abs(elapsedSinceSave - wallSinceSave) > REBOOT_DETECT_MILLIS
    }

    private fun refreshLocked(toleranceMillis: Long = 0L) {
        val s = _state.value
        if (s.status != TimerStatus.Running) return
        val remaining = endElapsed - clock.elapsedRealtime()
        if (remaining <= toleranceMillis) finish(s) else _state.value = s.copy(remainingMillis = remaining)
    }

    private fun finish(s: TimerState) {
        _state.value = s.copy(status = TimerStatus.Finished, remainingMillis = 0L)
        platform.cancelAlarm()
        platform.clearRunning()
        platform.showDone()
        persist()
        val record = SessionRecord(
            durationMinutes = (s.totalMillis / 60_000L).toInt(),
            completedAtMillis = clock.wallClockMillis()
        )
        scope.launch { sessions.record(record) }
    }

    private fun reset() {
        _state.value = TimerState()
        platform.cancelAlarm()
        platform.clearRunning()
        persist()
    }

    private fun persist() {
        val s = _state.value
        persistence.save(
            if (s.status == TimerStatus.Idle) {
                null
            } else {
                TimerSnapshot(
                    status = s.status,
                    totalMillis = s.totalMillis,
                    remainingMillis = s.remainingMillis,
                    endElapsed = endElapsed,
                    savedAtElapsed = clock.elapsedRealtime(),
                    savedAtWall = clock.wallClockMillis()
                )
            }
        )
    }

    private companion object {
        const val ALARM_TOLERANCE_MILLIS = 1_000L
        const val REBOOT_DETECT_MILLIS = 60_000L
    }
}

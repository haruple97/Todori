package com.haruple97.todori.core.timer

import com.haruple97.todori.core.common.IoDispatcher
import com.haruple97.todori.core.data.repository.SessionRepository
import com.haruple97.todori.core.model.CyclePlan
import com.haruple97.todori.core.model.SessionRecord
import com.haruple97.todori.core.model.TimerPhase
import com.haruple97.todori.core.model.TimerState
import com.haruple97.todori.core.model.TimerStatus
import com.haruple97.todori.core.model.nextStep
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
 * 사이클 모드([CyclePlan])에서는 집중→휴식→… 단계를 엔진이 이어 간다. 세션 기록은 집중 단계만 남긴다.
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

    /** 집중 단계를 시작한다. [plan]이 있으면 사이클 모드로 1번째 집중부터 시작한다. */
    @Synchronized
    fun start(totalMillis: Long, plan: CyclePlan? = null) {
        begin(TimerPhase.Focus, round = 1, totalMillis = totalMillis, plan = plan)
    }

    /** 확인형 사이클에서 끝난 단계 다음을 시작한다. 사이클이 끝났으면 처음부터 새 사이클을 시작한다. */
    @Synchronized
    fun startNext() {
        val s = _state.value
        val plan = s.plan ?: return
        if (s.status != TimerStatus.Finished) return
        val next = nextStep(s.phase, s.round, plan)
        if (next == null) {
            begin(TimerPhase.Focus, 1, plan.minutesOf(TimerPhase.Focus) * MILLIS_PER_MINUTE, plan)
        } else {
            begin(next.phase, next.round, plan.minutesOf(next.phase) * MILLIS_PER_MINUTE, plan)
        }
    }

    /** 휴식을 건너뛰고 다음 집중으로 간다(진행 중·일시정지 휴식에서만). 건너뛴 휴식은 기록하지 않는다. */
    @Synchronized
    fun skipBreak() {
        val s = _state.value
        val plan = s.plan ?: return
        if (!s.isBreak || s.status == TimerStatus.Idle) return
        val next = nextStep(s.phase, s.round, plan)
        if (next == null) {
            reset()
        } else {
            begin(next.phase, next.round, plan.minutesOf(next.phase) * MILLIS_PER_MINUTE, plan)
        }
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
        platform.showRunning(s.phase, endElapsed)
        persist()
    }

    /** 일반 모드는 타이머를, 사이클 모드는 사이클 전체를 끝낸다. */
    @Synchronized
    fun cancel() = reset()

    @Synchronized
    fun refresh() = refreshLocked()

    /** 알람은 목표 시각 직전에 도착할 수 있어 약간의 오차를 허용한다. */
    @Synchronized
    fun onAlarm() = refreshLocked(toleranceMillis = ALARM_TOLERANCE_MILLIS)

    private fun begin(phase: TimerPhase, round: Int, totalMillis: Long, plan: CyclePlan?) {
        endElapsed = clock.elapsedRealtime() + totalMillis
        _state.value = TimerState(TimerStatus.Running, totalMillis, totalMillis, phase, round, plan)
        platform.scheduleAlarm(endElapsed)
        platform.showRunning(phase, endElapsed)
        persist()
    }

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
                _state.value = TimerState(TimerStatus.Running, s.totalMillis, remaining, s.phase, s.round, s.plan)
                refreshLocked()
            }
            TimerStatus.Paused, TimerStatus.Finished ->
                _state.value = TimerState(s.status, s.totalMillis, s.remainingMillis, s.phase, s.round, s.plan)
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
        platform.showDone(s.phase)
        persist()
        if (s.phase == TimerPhase.Focus) {
            val record = SessionRecord(
                durationMinutes = (s.totalMillis / MILLIS_PER_MINUTE).toInt(),
                completedAtMillis = clock.wallClockMillis()
            )
            scope.launch { sessions.record(record) }
        }
        // 자동 이어가기: 알림은 위에서 이미 보냈으므로 바로 다음 단계를 시작한다.
        val plan = s.plan
        if (plan != null && plan.auto) {
            nextStep(s.phase, s.round, plan)?.let { next ->
                begin(next.phase, next.round, plan.minutesOf(next.phase) * MILLIS_PER_MINUTE, plan)
            }
        }
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
                    savedAtWall = clock.wallClockMillis(),
                    phase = s.phase,
                    round = s.round,
                    plan = s.plan
                )
            }
        )
    }

    private companion object {
        const val ALARM_TOLERANCE_MILLIS = 1_000L
        const val REBOOT_DETECT_MILLIS = 60_000L
        const val MILLIS_PER_MINUTE = 60_000L
    }
}

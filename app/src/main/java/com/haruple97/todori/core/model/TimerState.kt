package com.haruple97.todori.core.model

enum class TimerStatus { Idle, Running, Paused, Finished }

/** 지금 단계. 사이클이 아니면 항상 [Focus]. */
enum class TimerPhase { Focus, ShortBreak, LongBreak }

/** 사이클 모드 계획. 시작할 때 고정(스냅샷)해서 진행 중에 설정을 바꿔도 영향이 없다. */
data class CyclePlan(
    val focusMinutes: Int,
    /** 이 횟수만큼 집중하면 긴 휴식. */
    val rounds: Int,
    val shortMinutes: Int,
    val longMinutes: Int,
    /** true면 단계가 끝나자마자 다음 단계를 알아서 시작한다. */
    val auto: Boolean
) {
    fun minutesOf(phase: TimerPhase): Int = when (phase) {
        TimerPhase.Focus -> focusMinutes
        TimerPhase.ShortBreak -> shortMinutes
        TimerPhase.LongBreak -> longMinutes
    }
}

/** 사용자가 정한 사이클 설정(저장 대상). 집중 시간은 메인 화면의 값을 쓴다. */
data class CycleSettings(
    val enabled: Boolean = false,
    val rounds: Int = 4,
    val shortMinutes: Int = 5,
    val longMinutes: Int = 15,
    val auto: Boolean = false
)

data class TimerState(
    val status: TimerStatus = TimerStatus.Idle,
    val totalMillis: Long = 0L,
    val remainingMillis: Long = 0L,
    val phase: TimerPhase = TimerPhase.Focus,
    /** 사이클에서 지금 몇 번째 집중인지(1부터). */
    val round: Int = 1,
    /** null이면 일반 모드. */
    val plan: CyclePlan? = null
) {
    val isBreak: Boolean get() = phase != TimerPhase.Focus
}

/** 다음 단계. 휴식이 끝나는 마지막(긴 휴식 뒤)이면 null로 사이클이 끝난다. */
data class CycleStep(val phase: TimerPhase, val round: Int)

fun nextStep(phase: TimerPhase, round: Int, plan: CyclePlan): CycleStep? = when (phase) {
    TimerPhase.Focus ->
        CycleStep(if (round % plan.rounds == 0) TimerPhase.LongBreak else TimerPhase.ShortBreak, round)
    TimerPhase.ShortBreak -> CycleStep(TimerPhase.Focus, round + 1)
    TimerPhase.LongBreak -> null
}

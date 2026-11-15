package com.haruple97.todori.feature.timer

import com.haruple97.todori.core.model.CyclePlan
import com.haruple97.todori.core.model.CycleSettings
import com.haruple97.todori.core.model.TimerPhase
import com.haruple97.todori.core.model.TimerStatus

data class TimerUiState(
    val status: TimerStatus = TimerStatus.Idle,
    val focusMinutes: Int = 25,
    /** 프리미엄이면 1분 단위·60분 초과 시간을 직접 정할 수 있다. */
    val isPremium: Boolean = false,
    val totalMillis: Long = 0L,
    val remainingMillis: Long = 0L,
    /** 오늘 끝낸 집중 횟수. 말풍선 문구를 고르는 데만 쓴다. */
    val todayCount: Int = 0,
    /** 지금까지의 전체 완료 횟수. 아직 불러오기 전이면 -1(처음 사용으로 오해하지 않도록). */
    val totalCount: Int = -1,
    val phase: TimerPhase = TimerPhase.Focus,
    /** 사이클에서 지금 몇 번째 집중인지(1부터). */
    val round: Int = 1,
    /** 진행 중인 사이클의 계획. null이면 일반 모드. */
    val plan: CyclePlan? = null,
    /** 저장된 사이클 설정(대기 화면의 사이클 칩과 설정 시트에 쓴다). */
    val cycleSettings: CycleSettings = CycleSettings()
) {
    val isBreak: Boolean get() = phase != TimerPhase.Focus

    /** 다음에 시작할 때 사이클로 시작하는지: 프리미엄이고 사이클을 켜 둔 경우. */
    val cycleOn: Boolean get() = isPremium && cycleSettings.enabled

    val progress: Float
        get() = if (totalMillis <= 0L) 0f else (1f - remainingMillis.toFloat() / totalMillis).coerceIn(0f, 1f)
}

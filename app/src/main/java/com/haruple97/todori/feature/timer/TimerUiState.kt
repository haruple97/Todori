package com.haruple97.todori.feature.timer

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
    val totalCount: Int = -1
) {
    val progress: Float
        get() = if (totalMillis <= 0L) 0f else (1f - remainingMillis.toFloat() / totalMillis).coerceIn(0f, 1f)
}

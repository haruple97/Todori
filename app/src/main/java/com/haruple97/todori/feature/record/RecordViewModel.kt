package com.haruple97.todori.feature.record

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruple97.todori.core.common.currentStreak
import com.haruple97.todori.core.common.groupByDay
import com.haruple97.todori.core.data.repository.SessionRepository
import com.haruple97.todori.core.model.SessionRecord
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecordUiState(
    val today: LocalDate = LocalDate.now(),
    val month: YearMonth = YearMonth.now(),
    val selected: LocalDate = LocalDate.now(),
    val days: Map<LocalDate, List<SessionRecord>> = emptyMap(),
    val totalCount: Int = 0,
    val totalMinutes: Int = 0,
    val streak: Int = 0
) {
    val canGoNext: Boolean get() = month.isBefore(YearMonth.from(today))
    val selectedSessions: List<SessionRecord> get() = days[selected].orEmpty()
}

/** 화면이 한 번만 처리하는 일회성 이벤트. */
sealed interface RecordEvent {
    /** 삭제가 끝났다. 화면은 되돌리기 스낵바를 띄우고, 누르면 [RecordViewModel.undoDelete]로 되살린다. */
    data class Deleted(val sessions: List<SessionRecord>) : RecordEvent
}

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    private val zone = ZoneId.systemDefault()
    private val today = LocalDate.now(zone)
    private val month = MutableStateFlow(YearMonth.from(today))
    private val selected = MutableStateFlow(today)
    private val _events = Channel<RecordEvent>(Channel.BUFFERED)

    val events: Flow<RecordEvent> = _events.receiveAsFlow()

    val uiState: StateFlow<RecordUiState> = combine(sessionRepository.sessions, month, selected) { sessions, m, sel ->
        val byDay = groupByDay(sessions, zone)
        RecordUiState(
            today = today,
            month = m,
            selected = sel,
            days = byDay,
            totalCount = sessions.size,
            totalMinutes = sessions.sumOf { it.durationMinutes },
            streak = currentStreak(byDay.keys, today)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordUiState(today = today))

    fun previousMonth() = moveTo(month.value.minusMonths(1))

    fun nextMonth() {
        if (month.value.isBefore(YearMonth.from(today))) moveTo(month.value.plusMonths(1))
    }

    fun select(date: LocalDate) {
        if (!date.isAfter(today)) selected.value = date
    }

    fun delete(sessions: List<SessionRecord>) {
        if (sessions.isEmpty()) return
        viewModelScope.launch {
            sessionRepository.delete(sessions.map { it.id })
            _events.send(RecordEvent.Deleted(sessions))
        }
    }

    /** 삭제한 기록을 원래 id 그대로 되살린다. */
    fun undoDelete(sessions: List<SessionRecord>) {
        viewModelScope.launch { sessions.forEach { sessionRepository.record(it) } }
    }

    /** 달을 넘기면 상세도 옮긴다: 이번 달이면 오늘, 아니면 그 달의 가장 최근 기록일(없으면 1일). */
    private fun moveTo(target: YearMonth) {
        month.value = target
        selected.value = when {
            target == YearMonth.from(today) -> today
            else -> uiState.value.days.keys.filter { YearMonth.from(it) == target }.maxOrNull() ?: target.atDay(1)
        }
    }
}

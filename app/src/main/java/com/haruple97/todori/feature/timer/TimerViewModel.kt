package com.haruple97.todori.feature.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruple97.todori.core.data.repository.SessionRepository
import com.haruple97.todori.core.data.repository.SettingsRepository
import com.haruple97.todori.core.model.TimerSettings
import com.haruple97.todori.core.timer.TimerEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val engine: TimerEngine,
    private val settingsRepository: SettingsRepository,
    sessionRepository: SessionRepository
) : ViewModel() {
    // 다이얼 조작이 DataStore 왕복에 끌려다니지 않도록 로컬 값을 먼저 반영하고 저장은 뒤따른다.
    private val focusMinutes = MutableStateFlow(TimerSettings().focusMinutes)

    /** (오늘 횟수, 전체 횟수). 말풍선 문구를 상황에 맞게 고르는 데 쓴다. */
    private val sessionStats = sessionRepository.sessions.map { sessions ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val todayCount = sessions.count { Instant.ofEpochMilli(it.completedAtMillis).atZone(zone).toLocalDate() == today }
        todayCount to sessions.size
    }

    val uiState: StateFlow<TimerUiState> = combine(engine.state, focusMinutes, sessionStats) { timer, focus, stats ->
        TimerUiState(
            status = timer.status,
            focusMinutes = focus,
            totalMillis = timer.totalMillis,
            remainingMillis = timer.remainingMillis,
            todayCount = stats.first,
            totalCount = stats.second
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerUiState())

    init {
        viewModelScope.launch {
            focusMinutes.value = snapMinutes(settingsRepository.settings.first().focusMinutes)
            engine.restore()
        }
    }

    fun refresh() = engine.refresh()

    suspend fun isReviewRequested(): Boolean = settingsRepository.isReviewRequested()

    fun markReviewRequested() {
        viewModelScope.launch { settingsRepository.markReviewRequested() }
    }

    fun start() = engine.start(focusMinutes.value * MILLIS_PER_MINUTE)

    fun pause() = engine.pause()

    fun resume() = engine.resume()

    fun stop() = engine.cancel()

    fun setFocusMinutes(minutes: Int) {
        focusMinutes.value = minutes
        viewModelScope.launch { settingsRepository.update(TimerSettings(minutes)) }
    }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}

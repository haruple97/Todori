package com.haruple97.todori.feature.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruple97.todori.core.data.repository.SessionRepository
import com.haruple97.todori.core.data.repository.SettingsRepository
import com.haruple97.todori.core.model.CyclePlan
import com.haruple97.todori.core.model.CycleSettings
import com.haruple97.todori.core.model.TimerSettings
import com.haruple97.todori.core.premium.PremiumRepository
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
    sessionRepository: SessionRepository,
    premiumRepository: PremiumRepository
) : ViewModel() {
    // 다이얼 조작이 DataStore 왕복에 끌려다니지 않도록 로컬 값을 먼저 반영하고 저장은 뒤따른다.
    // 저장된 값 그대로이고, 화면과 시작에는 지금 권한에 맞게 정리한 값(normalizedMinutes)을 쓴다.
    private val savedMinutes = MutableStateFlow(TimerSettings().focusMinutes)

    /** (오늘 횟수, 전체 횟수). 말풍선 문구를 상황에 맞게 고르는 데 쓴다. */
    private val sessionStats = sessionRepository.sessions.map { sessions ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val todayCount = sessions.count { Instant.ofEpochMilli(it.completedAtMillis).atZone(zone).toLocalDate() == today }
        todayCount to sessions.size
    }

    val uiState: StateFlow<TimerUiState> = combine(
        engine.state,
        combine(savedMinutes, premiumRepository.isPremium, settingsRepository.cycle) { saved, premium, cycle ->
            Triple(normalizedMinutes(saved, premium), premium, cycle)
        },
        sessionStats
    ) { timer, (focus, premium, cycle), stats ->
        TimerUiState(
            status = timer.status,
            focusMinutes = focus,
            isPremium = premium,
            totalMillis = timer.totalMillis,
            remainingMillis = timer.remainingMillis,
            todayCount = stats.first,
            totalCount = stats.second,
            phase = timer.phase,
            round = timer.round,
            plan = timer.plan,
            cycleSettings = cycle
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerUiState())

    init {
        viewModelScope.launch {
            savedMinutes.value = settingsRepository.settings.first().focusMinutes
            engine.restore()
        }
    }

    fun refresh() = engine.refresh()

    suspend fun isReviewRequested(): Boolean = settingsRepository.isReviewRequested()

    fun markReviewRequested() {
        viewModelScope.launch { settingsRepository.markReviewRequested() }
    }

    /** 프리미엄이고 사이클이 켜져 있으면 사이클로, 아니면 일반 타이머로 시작한다. */
    fun start() {
        val ui = uiState.value
        val plan = if (ui.cycleOn) {
            val c = ui.cycleSettings
            CyclePlan(ui.focusMinutes, c.rounds, c.shortMinutes, c.longMinutes, c.auto)
        } else {
            null
        }
        engine.start(ui.focusMinutes * MILLIS_PER_MINUTE, plan)
    }

    fun startNext() = engine.startNext()

    fun skipBreak() = engine.skipBreak()

    fun saveCycle(settings: CycleSettings) {
        viewModelScope.launch { settingsRepository.setCycle(settings) }
    }

    fun pause() = engine.pause()

    fun resume() = engine.resume()

    fun stop() = engine.cancel()

    fun setFocusMinutes(minutes: Int) {
        savedMinutes.value = minutes
        viewModelScope.launch { settingsRepository.update(TimerSettings(minutes)) }
    }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}

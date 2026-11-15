package com.haruple97.todori.feature.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruple97.todori.core.data.repository.SettingsRepository
import com.haruple97.todori.core.model.TimerPhase
import com.haruple97.todori.core.model.TimerSettings
import com.haruple97.todori.core.timer.TimerEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class TimerViewModel @Inject constructor(
    private val engine: TimerEngine,
    private val settingsRepository: SettingsRepository
) : ViewModel() {
    // 다이얼 조작이 DataStore 왕복에 끌려다니지 않도록 로컬 값을 먼저 반영하고 저장은 뒤따른다.
    private val focusMinutes = MutableStateFlow(TimerSettings().focusMinutes)
    private val breakMinutes = MutableStateFlow(TimerSettings().breakMinutes)

    val uiState: StateFlow<TimerUiState> = combine(engine.state, focusMinutes, breakMinutes) { timer, focus, rest ->
        TimerUiState(
            phase = timer.phase,
            status = timer.status,
            focusMinutes = focus,
            breakMinutes = rest,
            totalMillis = timer.totalMillis,
            remainingMillis = timer.remainingMillis
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerUiState())

    init {
        viewModelScope.launch {
            val saved = settingsRepository.settings.first()
            focusMinutes.value = saved.focusMinutes
            breakMinutes.value = saved.breakMinutes
            engine.restore()
        }
    }

    fun refresh() = engine.refresh()

    fun startFocus() = engine.start(TimerPhase.Focus, focusMinutes.value * MILLIS_PER_MINUTE)

    fun startBreak() = engine.start(TimerPhase.Break, breakMinutes.value * MILLIS_PER_MINUTE)

    fun pause() = engine.pause()

    fun resume() = engine.resume()

    fun stop() = engine.cancel()

    fun setFocusMinutes(minutes: Int) {
        focusMinutes.value = minutes
        persistSettings()
    }

    fun setBreakMinutes(minutes: Int) {
        breakMinutes.value = minutes
        persistSettings()
    }

    private fun persistSettings() {
        val settings = TimerSettings(focusMinutes.value, breakMinutes.value)
        viewModelScope.launch { settingsRepository.update(settings) }
    }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}

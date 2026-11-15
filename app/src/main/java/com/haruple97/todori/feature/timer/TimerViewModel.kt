package com.haruple97.todori.feature.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruple97.todori.core.data.repository.SettingsRepository
import com.haruple97.todori.core.model.TimerPhase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TimerViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {
    val uiState: StateFlow<TimerUiState> = settingsRepository.settings
        .map { TimerUiState(phase = TimerPhase.Focus, remainingMillis = it.focusMinutes * 60_000L) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TimerUiState())
}

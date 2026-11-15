package com.haruple97.todori.feature.customize

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruple97.todori.core.data.repository.SettingsRepository
import com.haruple97.todori.core.model.Appearance
import com.haruple97.todori.core.model.SkinId
import com.haruple97.todori.core.model.ThemeId
import com.haruple97.todori.core.premium.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CustomizeUiState(
    /** 사용자가 마지막으로 적용한 값(유료 여부와 상관없이 저장된 그대로). */
    val applied: Appearance = Appearance(),
    val isPremium: Boolean = false,
    val devToggleAvailable: Boolean = false
) {
    fun isLocked(skin: SkinId) = skin.premium && !isPremium
    fun isLocked(theme: ThemeId) = theme.premium && !isPremium
}

@HiltViewModel
class CustomizeViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val premiumRepository: PremiumRepository
) : ViewModel() {
    val uiState: StateFlow<CustomizeUiState> = combine(
        settingsRepository.appearance,
        premiumRepository.isPremium
    ) { applied, premium ->
        CustomizeUiState(applied, premium, premiumRepository.debugToggleAvailable)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CustomizeUiState(devToggleAvailable = premiumRepository.debugToggleAvailable)
    )

    /** 잠긴 항목은 적용하지 않는다(화면에서 막지만 여기서도 한 번 더 지킨다). */
    fun applySkin(skin: SkinId) {
        if (uiState.value.isLocked(skin)) return
        viewModelScope.launch { settingsRepository.setSkin(skin) }
    }

    fun applyTheme(theme: ThemeId) {
        if (uiState.value.isLocked(theme)) return
        viewModelScope.launch { settingsRepository.setTheme(theme) }
    }

    fun setDevPremium(enabled: Boolean) {
        viewModelScope.launch { premiumRepository.setDebugPremium(enabled) }
    }
}

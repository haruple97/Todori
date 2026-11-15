package com.haruple97.todori.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruple97.todori.core.data.repository.SettingsRepository
import com.haruple97.todori.core.model.Appearance
import com.haruple97.todori.core.premium.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** 앱 전체에 적용되는 모양(캐릭터·테마). 유료 항목은 구독이 있을 때만 적용해서 내보낸다. */
@HiltViewModel
class AppViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
    premiumRepository: PremiumRepository
) : ViewModel() {
    val appearance: StateFlow<Appearance> = combine(
        settingsRepository.appearance,
        premiumRepository.isPremium
    ) { chosen, premium -> chosen.effective(premium) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Appearance())
}

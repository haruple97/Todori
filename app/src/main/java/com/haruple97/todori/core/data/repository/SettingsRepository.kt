package com.haruple97.todori.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.haruple97.todori.core.model.Appearance
import com.haruple97.todori.core.model.SkinId
import com.haruple97.todori.core.model.ThemeId
import com.haruple97.todori.core.model.TimerSettings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    val settings: Flow<TimerSettings>
    suspend fun update(settings: TimerSettings)

    /** 고른 캐릭터와 테마. 유료 여부는 여기서 따지지 않는다(표시할 때 [Appearance.effective]로 처리). */
    val appearance: Flow<Appearance>
    suspend fun setSkin(skin: SkinId)
    suspend fun setTheme(theme: ThemeId)

    /** 인앱 리뷰를 이미 띄웠는지. 첫 성공 때 한 번만 요청하기 위한 표식. */
    suspend fun isReviewRequested(): Boolean

    suspend fun markReviewRequested()
}

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {
    override val settings: Flow<TimerSettings> = dataStore.data.map { prefs ->
        TimerSettings(focusMinutes = prefs[FOCUS_MINUTES] ?: TimerSettings().focusMinutes)
    }

    override suspend fun update(settings: TimerSettings) {
        dataStore.edit { it[FOCUS_MINUTES] = settings.focusMinutes }
    }

    override val appearance: Flow<Appearance> = dataStore.data.map { prefs ->
        Appearance(skin = SkinId.fromId(prefs[SKIN]), theme = ThemeId.fromId(prefs[THEME]))
    }

    override suspend fun setSkin(skin: SkinId) {
        dataStore.edit { it[SKIN] = skin.id }
    }

    override suspend fun setTheme(theme: ThemeId) {
        dataStore.edit { it[THEME] = theme.id }
    }

    override suspend fun isReviewRequested(): Boolean = dataStore.data.first()[REVIEW_REQUESTED] ?: false

    override suspend fun markReviewRequested() {
        dataStore.edit { it[REVIEW_REQUESTED] = true }
    }

    private companion object {
        val FOCUS_MINUTES = intPreferencesKey("focus_minutes")
        val SKIN = stringPreferencesKey("skin")
        val THEME = stringPreferencesKey("theme")
        val REVIEW_REQUESTED = booleanPreferencesKey("review_requested")
    }
}

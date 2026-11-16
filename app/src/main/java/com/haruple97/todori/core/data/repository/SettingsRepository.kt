package com.haruple97.todori.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.haruple97.todori.core.model.Appearance
import com.haruple97.todori.core.model.CalendarPrefs
import com.haruple97.todori.core.model.CycleSettings
import com.haruple97.todori.core.model.HeatStyle
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

    /** 사이클 모드 설정(프리미엄). 켜져 있어도 구독이 없으면 쓰지 않는다. */
    val cycle: Flow<CycleSettings>
    suspend fun setCycle(settings: CycleSettings)

    /** 기록 달력 보기 방식(스티커/농도)과 농도 색 스타일. */
    val calendarPrefs: Flow<CalendarPrefs>
    suspend fun setHeatMode(enabled: Boolean)
    suspend fun setHeatStyle(style: HeatStyle)

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

    override val cycle: Flow<CycleSettings> = dataStore.data.map { prefs ->
        val default = CycleSettings()
        CycleSettings(
            enabled = prefs[CYCLE_ENABLED] ?: default.enabled,
            rounds = prefs[CYCLE_ROUNDS] ?: default.rounds,
            shortMinutes = prefs[CYCLE_SHORT] ?: default.shortMinutes,
            longMinutes = prefs[CYCLE_LONG] ?: default.longMinutes,
            auto = prefs[CYCLE_AUTO] ?: default.auto
        )
    }

    override suspend fun setCycle(settings: CycleSettings) {
        dataStore.edit {
            it[CYCLE_ENABLED] = settings.enabled
            it[CYCLE_ROUNDS] = settings.rounds
            it[CYCLE_SHORT] = settings.shortMinutes
            it[CYCLE_LONG] = settings.longMinutes
            it[CYCLE_AUTO] = settings.auto
        }
    }

    override val calendarPrefs: Flow<CalendarPrefs> = dataStore.data.map { prefs ->
        CalendarPrefs(heatMode = prefs[HEAT_MODE] ?: false, style = HeatStyle.fromId(prefs[HEAT_STYLE]))
    }

    override suspend fun setHeatMode(enabled: Boolean) {
        dataStore.edit { it[HEAT_MODE] = enabled }
    }

    override suspend fun setHeatStyle(style: HeatStyle) {
        dataStore.edit { it[HEAT_STYLE] = style.id }
    }

    override suspend fun isReviewRequested(): Boolean = dataStore.data.first()[REVIEW_REQUESTED] ?: false

    override suspend fun markReviewRequested() {
        dataStore.edit { it[REVIEW_REQUESTED] = true }
    }

    private companion object {
        val FOCUS_MINUTES = intPreferencesKey("focus_minutes")
        val SKIN = stringPreferencesKey("skin")
        val THEME = stringPreferencesKey("theme")
        val HEAT_MODE = booleanPreferencesKey("heat_mode")
        val HEAT_STYLE = stringPreferencesKey("heat_style")
        val REVIEW_REQUESTED = booleanPreferencesKey("review_requested")
        val CYCLE_ENABLED = booleanPreferencesKey("cycle_enabled")
        val CYCLE_ROUNDS = intPreferencesKey("cycle_rounds")
        val CYCLE_SHORT = intPreferencesKey("cycle_short")
        val CYCLE_LONG = intPreferencesKey("cycle_long")
        val CYCLE_AUTO = booleanPreferencesKey("cycle_auto")
    }
}

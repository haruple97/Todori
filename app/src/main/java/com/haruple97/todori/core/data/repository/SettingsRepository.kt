package com.haruple97.todori.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.haruple97.todori.core.model.TimerSettings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    val settings: Flow<TimerSettings>
    suspend fun update(settings: TimerSettings)
}

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {
    override val settings: Flow<TimerSettings> = dataStore.data.map { prefs ->
        val default = TimerSettings()
        TimerSettings(
            focusMinutes = prefs[FOCUS_MINUTES] ?: default.focusMinutes,
            breakMinutes = prefs[BREAK_MINUTES] ?: default.breakMinutes
        )
    }

    override suspend fun update(settings: TimerSettings) {
        dataStore.edit {
            it[FOCUS_MINUTES] = settings.focusMinutes
            it[BREAK_MINUTES] = settings.breakMinutes
        }
    }

    private companion object {
        val FOCUS_MINUTES = intPreferencesKey("focus_minutes")
        val BREAK_MINUTES = intPreferencesKey("break_minutes")
    }
}

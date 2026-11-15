package com.haruple97.todori.core.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.haruple97.todori.core.model.TimerSettings
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

interface SettingsRepository {
    val settings: Flow<TimerSettings>
    suspend fun update(settings: TimerSettings)

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

    override suspend fun isReviewRequested(): Boolean = dataStore.data.first()[REVIEW_REQUESTED] ?: false

    override suspend fun markReviewRequested() {
        dataStore.edit { it[REVIEW_REQUESTED] = true }
    }

    private companion object {
        val FOCUS_MINUTES = intPreferencesKey("focus_minutes")
        val REVIEW_REQUESTED = booleanPreferencesKey("review_requested")
    }
}

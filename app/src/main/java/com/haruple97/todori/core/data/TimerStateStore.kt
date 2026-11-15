package com.haruple97.todori.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.haruple97.todori.core.common.IoDispatcher
import com.haruple97.todori.core.model.TimerPhase
import com.haruple97.todori.core.model.TimerStatus
import com.haruple97.todori.core.timer.TimerPersistence
import com.haruple97.todori.core.timer.TimerSnapshot
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class TimerStateStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @IoDispatcher dispatcher: CoroutineDispatcher
) : TimerPersistence {
    // 저장 순서를 보장하기 위해 단일 스레드로 직렬 실행한다.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(SupervisorJob() + dispatcher.limitedParallelism(1))

    override fun save(snapshot: TimerSnapshot?) {
        scope.launch {
            dataStore.edit { prefs ->
                if (snapshot == null) {
                    ALL_KEYS.forEach { prefs.remove(it) }
                } else {
                    prefs[PHASE] = snapshot.phase.name
                    prefs[STATUS] = snapshot.status.name
                    prefs[TOTAL] = snapshot.totalMillis
                    prefs[REMAINING] = snapshot.remainingMillis
                    prefs[END_ELAPSED] = snapshot.endElapsed
                    prefs[SAVED_ELAPSED] = snapshot.savedAtElapsed
                    prefs[SAVED_WALL] = snapshot.savedAtWall
                }
            }
        }
    }

    override suspend fun load(): TimerSnapshot? {
        val prefs = dataStore.data.first()
        val phase = prefs[PHASE]?.let { runCatching { TimerPhase.valueOf(it) }.getOrNull() } ?: return null
        val status = prefs[STATUS]?.let { runCatching { TimerStatus.valueOf(it) }.getOrNull() } ?: return null
        return TimerSnapshot(
            phase = phase,
            status = status,
            totalMillis = prefs[TOTAL] ?: return null,
            remainingMillis = prefs[REMAINING] ?: return null,
            endElapsed = prefs[END_ELAPSED] ?: return null,
            savedAtElapsed = prefs[SAVED_ELAPSED] ?: return null,
            savedAtWall = prefs[SAVED_WALL] ?: return null
        )
    }

    private companion object {
        val PHASE = stringPreferencesKey("timer_phase")
        val STATUS = stringPreferencesKey("timer_status")
        val TOTAL = longPreferencesKey("timer_total")
        val REMAINING = longPreferencesKey("timer_remaining")
        val END_ELAPSED = longPreferencesKey("timer_end_elapsed")
        val SAVED_ELAPSED = longPreferencesKey("timer_saved_elapsed")
        val SAVED_WALL = longPreferencesKey("timer_saved_wall")
        val ALL_KEYS = listOf(PHASE, STATUS, TOTAL, REMAINING, END_ELAPSED, SAVED_ELAPSED, SAVED_WALL)
    }
}

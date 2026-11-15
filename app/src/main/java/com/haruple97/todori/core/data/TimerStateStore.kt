package com.haruple97.todori.core.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.haruple97.todori.core.common.IoDispatcher
import com.haruple97.todori.core.model.CyclePlan
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
                    prefs[STATUS] = snapshot.status.name
                    prefs[TOTAL] = snapshot.totalMillis
                    prefs[REMAINING] = snapshot.remainingMillis
                    prefs[END_ELAPSED] = snapshot.endElapsed
                    prefs[SAVED_ELAPSED] = snapshot.savedAtElapsed
                    prefs[SAVED_WALL] = snapshot.savedAtWall
                    prefs[PHASE] = snapshot.phase.name
                    prefs[ROUND] = snapshot.round
                    val plan = snapshot.plan
                    if (plan == null) {
                        PLAN_KEYS.forEach { prefs.remove(it) }
                    } else {
                        prefs[PLAN_FOCUS] = plan.focusMinutes
                        prefs[PLAN_ROUNDS] = plan.rounds
                        prefs[PLAN_SHORT] = plan.shortMinutes
                        prefs[PLAN_LONG] = plan.longMinutes
                        prefs[PLAN_AUTO] = plan.auto
                    }
                }
            }
        }
    }

    override suspend fun load(): TimerSnapshot? {
        val prefs = dataStore.data.first()
        val status = prefs[STATUS]?.let { runCatching { TimerStatus.valueOf(it) }.getOrNull() } ?: return null
        return TimerSnapshot(
            status = status,
            totalMillis = prefs[TOTAL] ?: return null,
            remainingMillis = prefs[REMAINING] ?: return null,
            endElapsed = prefs[END_ELAPSED] ?: return null,
            savedAtElapsed = prefs[SAVED_ELAPSED] ?: return null,
            savedAtWall = prefs[SAVED_WALL] ?: return null,
            // 사이클 도입 전에 저장된 값에는 없으므로 기본값(집중·일반 모드)으로 읽는다.
            phase = prefs[PHASE]?.let { runCatching { TimerPhase.valueOf(it) }.getOrNull() } ?: TimerPhase.Focus,
            round = prefs[ROUND] ?: 1,
            plan = prefs[PLAN_FOCUS]?.let { focus ->
                CyclePlan(
                    focusMinutes = focus,
                    rounds = prefs[PLAN_ROUNDS] ?: return@let null,
                    shortMinutes = prefs[PLAN_SHORT] ?: return@let null,
                    longMinutes = prefs[PLAN_LONG] ?: return@let null,
                    auto = prefs[PLAN_AUTO] ?: false
                )
            }
        )
    }

    private companion object {
        val STATUS = stringPreferencesKey("timer_status")
        val TOTAL = longPreferencesKey("timer_total")
        val REMAINING = longPreferencesKey("timer_remaining")
        val END_ELAPSED = longPreferencesKey("timer_end_elapsed")
        val SAVED_ELAPSED = longPreferencesKey("timer_saved_elapsed")
        val SAVED_WALL = longPreferencesKey("timer_saved_wall")
        val PHASE = stringPreferencesKey("timer_phase")
        val ROUND = intPreferencesKey("timer_round")
        val PLAN_FOCUS = intPreferencesKey("timer_plan_focus")
        val PLAN_ROUNDS = intPreferencesKey("timer_plan_rounds")
        val PLAN_SHORT = intPreferencesKey("timer_plan_short")
        val PLAN_LONG = intPreferencesKey("timer_plan_long")
        val PLAN_AUTO = booleanPreferencesKey("timer_plan_auto")
        val PLAN_KEYS = listOf(PLAN_FOCUS, PLAN_ROUNDS, PLAN_SHORT, PLAN_LONG, PLAN_AUTO)
        val ALL_KEYS = listOf(STATUS, TOTAL, REMAINING, END_ELAPSED, SAVED_ELAPSED, SAVED_WALL, PHASE, ROUND) + PLAN_KEYS
    }
}

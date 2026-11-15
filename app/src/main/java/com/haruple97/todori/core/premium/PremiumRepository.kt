package com.haruple97.todori.core.premium

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * 프리미엄(구독) 여부의 단일 출처. 유료 기능은 전부 이 값만 보고 열리고 잠긴다.
 * 결제 연동 전이라 지금은 [LocalPremiumRepository]가 구현하며, 나중에 Play Billing 구현으로 바꿔 끼우면 된다.
 */
interface PremiumRepository {
    val isPremium: Flow<Boolean>

    /** 개발용 스위치. 디버그 빌드에서만 동작하고, 그 외에는 아무 일도 하지 않는다. */
    val debugToggleAvailable: Boolean
    suspend fun setDebugPremium(enabled: Boolean)
}

@Singleton
class LocalPremiumRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val dataStore: DataStore<Preferences>
) : PremiumRepository {
    private val debuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

    override val isPremium: Flow<Boolean> =
        if (debuggable) dataStore.data.map { it[DEBUG_PREMIUM] ?: false } else flowOf(false)

    override val debugToggleAvailable: Boolean = debuggable

    override suspend fun setDebugPremium(enabled: Boolean) {
        if (!debuggable) return
        dataStore.edit { it[DEBUG_PREMIUM] = enabled }
    }

    private companion object {
        val DEBUG_PREMIUM = booleanPreferencesKey("debug_premium")
    }
}

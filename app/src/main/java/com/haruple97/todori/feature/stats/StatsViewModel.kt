package com.haruple97.todori.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haruple97.todori.core.common.groupByDay
import com.haruple97.todori.core.common.weekOf
import com.haruple97.todori.core.data.repository.SessionRepository
import com.haruple97.todori.core.premium.PremiumRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatsUiState(
    val isPremium: Boolean = false,
    val today: LocalDate = LocalDate.now(),
    /** 이번 주(일~토). 요약 카드와 인사이트에 쓰며 무료로 공개된다. */
    val currentWeek: List<DayStat> = emptyList(),
    val insight: Insight = Insight.Empty,
    /** 주간 그래프가 보여주는 주. 프리미엄에서 이전·다음 주로 옮길 수 있다. */
    val chartWeek: List<DayStat> = emptyList(),
    val chartCanGoNext: Boolean = false,
    val buckets: Map<TimeBucket, Int> = emptyMap(),
    val bestBucket: TimeBucket? = null,
    val month: MonthTotals = MonthTotals(0, 0, 0),
    val longestStreak: Int = 0
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    premiumRepository: PremiumRepository
) : ViewModel() {
    private val zone = ZoneId.systemDefault()

    /** 0이면 이번 주, 음수면 그만큼 이전 주. */
    private val chartOffset = MutableStateFlow(0)

    val uiState: StateFlow<StatsUiState> = combine(
        sessionRepository.sessions,
        premiumRepository.isPremium,
        chartOffset
    ) { sessions, premium, offset ->
        val today = LocalDate.now(zone)
        val thisSunday = weekOf(today).first()
        val currentWeek = weekStats(sessions, thisSunday, zone)
        val lastWeekMinutes = weekStats(sessions, thisSunday.minusWeeks(1), zone).sumOf { it.minutes }
        val buckets = timeBucketMinutes(sessions, zone)
        StatsUiState(
            isPremium = premium,
            today = today,
            currentWeek = currentWeek,
            insight = weekInsight(currentWeek, lastWeekMinutes),
            chartWeek = weekStats(sessions, thisSunday.plusWeeks(offset.toLong()), zone),
            chartCanGoNext = offset < 0,
            buckets = buckets,
            bestBucket = bestBucket(buckets),
            month = monthTotals(sessions, YearMonth.from(today), zone),
            longestStreak = longestStreak(groupByDay(sessions, zone).keys)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

    fun previousWeek() {
        chartOffset.value -= 1
    }

    fun nextWeek() {
        if (chartOffset.value < 0) chartOffset.value += 1
    }
}

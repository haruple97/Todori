package com.haruple97.todori.feature.harvest

import com.haruple97.todori.core.model.SessionRecord
import com.haruple97.todori.feature.stats.longestStreak
import java.time.LocalDate
import java.time.YearMonth

/** 한 달 동안 수확한 토마토 요약. 공유 카드에 그대로 쓴다. */
data class Harvest(
    val month: YearMonth,
    val tomatoes: Int,
    val minutes: Int,
    val activeDays: Int,
    val bestStreak: Int,
    /** 가장 오래 집중한 날(없으면 null)과 그날의 분. */
    val bestDay: LocalDate?,
    val bestDayMinutes: Int,
    /** 날짜별 집중 분. 기록 없는 날은 없다. */
    val dayMinutes: Map<LocalDate, Int>
) {
    val isEmpty: Boolean get() = tomatoes == 0
}

/** 수확량에 따른 한마디 단계(0~3). */
fun harvestTier(tomatoes: Int): Int = when {
    tomatoes < 5 -> 0
    tomatoes < 15 -> 1
    tomatoes < 30 -> 2
    else -> 3
}

fun monthHarvest(days: Map<LocalDate, List<SessionRecord>>, month: YearMonth): Harvest {
    val inMonth = days.filterKeys { YearMonth.from(it) == month }.filterValues { it.isNotEmpty() }
    val dayMinutes = inMonth.mapValues { (_, list) -> list.sumOf { it.durationMinutes } }
    val best = dayMinutes.maxWithOrNull(compareBy<Map.Entry<LocalDate, Int>> { it.value }.thenByDescending { it.key })
    return Harvest(
        month = month,
        tomatoes = inMonth.values.sumOf { it.size },
        minutes = dayMinutes.values.sum(),
        activeDays = inMonth.size,
        bestStreak = longestStreak(inMonth.keys),
        bestDay = best?.key,
        bestDayMinutes = best?.value ?: 0,
        dayMinutes = dayMinutes
    )
}

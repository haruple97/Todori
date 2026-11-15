package com.haruple97.todori.feature.stats

import com.haruple97.todori.core.common.groupByDay
import com.haruple97.todori.core.model.SessionRecord
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/** 하루치 집중 통계. */
data class DayStat(val date: LocalDate, val minutes: Int, val count: Int)

/** 시간대 구간. 세션이 시작된 시각 기준. */
enum class TimeBucket(val hours: IntRange) {
    Dawn(0..5),
    Morning(6..11),
    Afternoon(12..17),
    Evening(18..23);

    companion object {
        fun ofHour(hour: Int): TimeBucket = entries.first { hour in it.hours }
    }
}

data class MonthTotals(val minutes: Int, val count: Int, val activeDays: Int)

/** 한 줄 인사이트. 화면에서 문구로 바꿔 보여준다. */
sealed interface Insight {
    data object Empty : Insight

    /** 지난주보다 [diffMinutes]분 더 집중. */
    data class Improved(val diffMinutes: Int) : Insight

    /** 이번 주에 가장 집중한 요일. */
    data class BestDay(val dayOfWeek: DayOfWeek) : Insight
}

private const val IMPROVED_MIN_DIFF_MINUTES = 10

/** [weekStart](일요일)부터 7일의 일별 집중 시간·개수. 기록 없는 날도 0으로 채운다. */
fun weekStats(sessions: List<SessionRecord>, weekStart: LocalDate, zone: ZoneId): List<DayStat> {
    val byDay = groupByDay(sessions, zone)
    return List(7) { offset ->
        val date = weekStart.plusDays(offset.toLong())
        val day = byDay[date].orEmpty()
        DayStat(date, minutes = day.sumOf { it.durationMinutes }, count = day.size)
    }
}

/** 시간대별 집중 분. 시작 시각은 (완료 시각 − 길이)로 추정하므로 자정을 넘긴 세션은 전날 밤으로 센다. */
fun timeBucketMinutes(sessions: List<SessionRecord>, zone: ZoneId): Map<TimeBucket, Int> {
    val result = TimeBucket.entries.associateWith { 0 }.toMutableMap()
    sessions.forEach { session ->
        val startMillis = session.completedAtMillis - session.durationMinutes * 60_000L
        val hour = Instant.ofEpochMilli(startMillis).atZone(zone).hour
        val bucket = TimeBucket.ofHour(hour)
        result[bucket] = result.getValue(bucket) + session.durationMinutes
    }
    return result
}

/** 가장 많이 집중한 시간대. 기록이 없으면 null, 동률이면 이른 시간대. */
fun bestBucket(minutes: Map<TimeBucket, Int>): TimeBucket? =
    TimeBucket.entries.filter { minutes[it] ?: 0 > 0 }.maxByOrNull { minutes.getValue(it) }

fun monthTotals(sessions: List<SessionRecord>, month: YearMonth, zone: ZoneId): MonthTotals {
    val inMonth = groupByDay(sessions, zone).filterKeys { YearMonth.from(it) == month }
    return MonthTotals(
        minutes = inMonth.values.sumOf { day -> day.sumOf { it.durationMinutes } },
        count = inMonth.values.sumOf { it.size },
        activeDays = inMonth.size
    )
}

/** 기록이 있는 날들 중 가장 길게 이어진 일수. */
fun longestStreak(days: Set<LocalDate>): Int {
    var best = 0
    days.forEach { day ->
        if (day.minusDays(1) !in days) {
            var length = 1
            while (day.plusDays(length.toLong()) in days) length++
            best = maxOf(best, length)
        }
    }
    return best
}

/** 기록이 있는 날만 대상으로 한 하루 평균 집중 분. 기록이 없으면 0. */
fun averageMinutesPerActiveDay(days: List<DayStat>): Int {
    val active = days.filter { it.minutes > 0 }
    return if (active.isEmpty()) 0 else active.sumOf { it.minutes } / active.size
}

/** 이번 주 한 줄 인사이트: 기록 없음 → 지난주보다 늘었으면 칭찬 → 아니면 가장 집중한 요일. */
fun weekInsight(thisWeek: List<DayStat>, lastWeekMinutes: Int): Insight {
    val total = thisWeek.sumOf { it.minutes }
    if (total == 0) return Insight.Empty
    if (lastWeekMinutes > 0 && total - lastWeekMinutes >= IMPROVED_MIN_DIFF_MINUTES) {
        return Insight.Improved(total - lastWeekMinutes)
    }
    val best = thisWeek.maxBy { it.minutes }
    return Insight.BestDay(best.date.dayOfWeek)
}

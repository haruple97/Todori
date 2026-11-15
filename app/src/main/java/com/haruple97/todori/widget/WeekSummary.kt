package com.haruple97.todori.widget

import com.haruple97.todori.core.model.SessionRecord
import com.haruple97.todori.feature.record.currentStreak
import com.haruple97.todori.feature.record.groupByDay
import com.haruple97.todori.feature.record.weekOf
import java.time.LocalDate
import java.time.ZoneId

data class WeekDay(val date: LocalDate, val count: Int, val isToday: Boolean, val isFuture: Boolean)

data class WeekSummary(val days: List<WeekDay>, val total: Int, val streak: Int)

/** 위젯에 그릴 이번 주(일~토) 기록 요약. */
fun buildWeekSummary(sessions: List<SessionRecord>, today: LocalDate, zone: ZoneId): WeekSummary {
    val byDay = groupByDay(sessions, zone)
    val days = weekOf(today).map { date ->
        WeekDay(date, byDay[date]?.size ?: 0, isToday = date == today, isFuture = date.isAfter(today))
    }
    return WeekSummary(days, total = days.sumOf { it.count }, streak = currentStreak(byDay.keys, today))
}

package com.haruple97.todori.feature.record

import com.haruple97.todori.core.model.SessionRecord
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/** 완료 기록을 [zone] 기준 날짜별로 묶는다. 하루 안에서는 완료 시각 순. */
fun groupByDay(sessions: List<SessionRecord>, zone: ZoneId): Map<LocalDate, List<SessionRecord>> =
    sessions
        .groupBy { Instant.ofEpochMilli(it.completedAtMillis).atZone(zone).toLocalDate() }
        .mapValues { (_, list) -> list.sortedBy { it.completedAtMillis } }

/** 오늘(없으면 어제)부터 거꾸로 이어진 연속 일수. 오늘 아직 안 했어도 어제까지 이어졌으면 끊기지 않은 것으로 본다. */
fun currentStreak(days: Set<LocalDate>, today: LocalDate): Int {
    var cursor = if (today in days) today else today.minusDays(1)
    var count = 0
    while (cursor in days) {
        count++
        cursor = cursor.minusDays(1)
    }
    return count
}

/** 일요일 시작 달력 격자. 달에 속하지 않는 칸은 null. */
fun monthGrid(month: YearMonth): List<List<LocalDate?>> {
    val leading = month.atDay(1).dayOfWeek.value % DAYS_IN_WEEK
    val days: List<LocalDate?> = List(leading) { null } + (1..month.lengthOfMonth()).map { month.atDay(it) }
    val trailing = (DAYS_IN_WEEK - days.size % DAYS_IN_WEEK) % DAYS_IN_WEEK
    return (days + List(trailing) { null }).chunked(DAYS_IN_WEEK)
}

private const val DAYS_IN_WEEK = 7

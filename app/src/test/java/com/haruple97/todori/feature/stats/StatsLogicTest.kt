package com.haruple97.todori.feature.stats

import com.haruple97.todori.core.model.SessionRecord
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatsLogicTest {
    private val seoul = ZoneId.of("Asia/Seoul")

    /** [dateTime]에 끝난 [minutes]분짜리 세션. */
    private fun session(dateTime: LocalDateTime, minutes: Int = 25) = SessionRecord(
        durationMinutes = minutes,
        completedAtMillis = dateTime.atZone(seoul).toInstant().toEpochMilli()
    )

    private fun at(y: Int, m: Int, d: Int, h: Int = 12, min: Int = 0) = LocalDateTime.of(y, m, d, h, min)

    @Test
    fun `주간 통계는 7일을 채우고 하루의 분과 개수를 합친다`() {
        val sunday = LocalDate.of(2026, 11, 15)
        val week = weekStats(
            listOf(
                session(at(2026, 11, 15, 10), 25),
                session(at(2026, 11, 15, 14), 50),
                session(at(2026, 11, 17, 9), 30),
                session(at(2026, 11, 22, 9), 25) // 다음 주
            ),
            sunday,
            seoul
        )

        assertEquals(7, week.size)
        assertEquals(DayStat(sunday, minutes = 75, count = 2), week[0])
        assertEquals(DayStat(sunday.plusDays(1), 0, 0), week[1])
        assertEquals(30, week[2].minutes)
        // 다음 주(22일) 기록은 포함되지 않는다
        assertEquals(105, week.sumOf { it.minutes })
    }

    @Test
    fun `시간대는 완료 시각이 아니라 시작 시각으로 센다`() {
        val buckets = timeBucketMinutes(
            listOf(
                session(at(2026, 11, 16, 0, 10), 25), // 23:45 시작 -> 저녁
                session(at(2026, 11, 16, 9, 0), 25), // 08:35 시작 -> 오전
                session(at(2026, 11, 16, 15, 0), 50), // 14:10 시작 -> 오후
                session(at(2026, 11, 16, 5, 30), 25) // 05:05 시작 -> 새벽
            ),
            seoul
        )

        assertEquals(25, buckets.getValue(TimeBucket.Evening))
        assertEquals(25, buckets.getValue(TimeBucket.Morning))
        assertEquals(50, buckets.getValue(TimeBucket.Afternoon))
        assertEquals(25, buckets.getValue(TimeBucket.Dawn))
        assertEquals(TimeBucket.Afternoon, bestBucket(buckets))
    }

    @Test
    fun `기록이 없으면 가장 집중한 시간대도 없고 동률이면 이른 시간대다`() {
        assertNull(bestBucket(timeBucketMinutes(emptyList(), seoul)))
        val tie = timeBucketMinutes(
            listOf(session(at(2026, 11, 16, 9), 25), session(at(2026, 11, 16, 20), 25)),
            seoul
        )
        assertEquals(TimeBucket.Morning, bestBucket(tie))
    }

    @Test
    fun `월 합계는 그 달의 분과 개수와 기록 있는 날 수만 센다`() {
        val totals = monthTotals(
            listOf(
                session(at(2026, 10, 31, 22), 25),
                session(at(2026, 11, 1, 10), 25),
                session(at(2026, 11, 1, 15), 50),
                session(at(2026, 11, 20, 9), 30),
                session(at(2026, 12, 1, 9), 25)
            ),
            YearMonth.of(2026, 11),
            seoul
        )

        assertEquals(MonthTotals(minutes = 105, count = 3, activeDays = 2), totals)
    }

    @Test
    fun `최장 연속 일수는 끊기는 곳마다 따로 세서 가장 긴 것을 고른다`() {
        val base = LocalDate.of(2026, 11, 1)
        val days = setOf(0, 1, 2, 4, 5, 6, 7, 8, 10).map { base.plusDays(it.toLong()) }.toSet()

        assertEquals(5, longestStreak(days))
        assertEquals(0, longestStreak(emptySet()))
        assertEquals(1, longestStreak(setOf(base)))
    }

    @Test
    fun `하루 평균은 기록 있는 날만 대상으로 한다`() {
        val base = LocalDate.of(2026, 11, 15)
        val days = listOf(
            DayStat(base, 60, 2), DayStat(base.plusDays(1), 0, 0), DayStat(base.plusDays(2), 30, 1)
        )

        assertEquals(45, averageMinutesPerActiveDay(days))
        assertEquals(0, averageMinutesPerActiveDay(listOf(DayStat(base, 0, 0))))
    }

    @Test
    fun `인사이트는 기록 없음, 지난주보다 늘어남, 가장 집중한 요일 순으로 고른다`() {
        val sunday = LocalDate.of(2026, 11, 15)
        val empty = weekStats(emptyList(), sunday, seoul)
        assertEquals(Insight.Empty, weekInsight(empty, lastWeekMinutes = 100))

        val week = weekStats(
            listOf(session(at(2026, 11, 16, 9), 25), session(at(2026, 11, 18, 9), 60)),
            sunday,
            seoul
        )
        // 이번 주 85분, 지난주 40분 -> 45분 더
        assertEquals(Insight.Improved(45), weekInsight(week, lastWeekMinutes = 40))
        // 지난주가 더 많거나 차이가 작으면 가장 집중한 요일(수요일)
        assertEquals(Insight.BestDay(DayOfWeek.WEDNESDAY), weekInsight(week, lastWeekMinutes = 200))
        assertEquals(Insight.BestDay(DayOfWeek.WEDNESDAY), weekInsight(week, lastWeekMinutes = 80))
        // 지난주 기록이 없으면 늘었다고 말하지 않는다
        assertEquals(Insight.BestDay(DayOfWeek.WEDNESDAY), weekInsight(week, lastWeekMinutes = 0))
    }

    @Test
    fun `연간 잔디는 오늘이 든 주를 마지막 열로 53주를 일요일 시작으로 채운다`() {
        val today = LocalDate.of(2026, 11, 18) // 수요일
        val weeks = yearWeeks(today)

        assertEquals(YEAR_WEEKS, weeks.size)
        assertEquals(LocalDate.of(2026, 11, 15), weeks.last().first())
        assertEquals(LocalDate.of(2025, 11, 16), weeks.first().first())
        assertEquals(true, weeks.all { it.size == 7 && it.first().dayOfWeek == DayOfWeek.SUNDAY })
        assertEquals(true, today in weeks.last())
    }

    @Test
    fun `날짜별 집중 분은 같은 날 세션을 합치고 기록 없는 날은 넣지 않는다`() {
        val result = minutesByDay(
            listOf(session(at(2026, 11, 15, 9), 25), session(at(2026, 11, 15, 20), 50), session(at(2026, 11, 16), 30)),
            seoul
        )

        assertEquals(mapOf(LocalDate.of(2026, 11, 15) to 75, LocalDate.of(2026, 11, 16) to 30), result)
    }
}

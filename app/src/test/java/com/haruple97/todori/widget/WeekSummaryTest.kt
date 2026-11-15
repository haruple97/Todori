package com.haruple97.todori.widget

import com.haruple97.todori.core.model.SessionRecord
import com.haruple97.todori.core.common.weekOf
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeekSummaryTest {
    private val seoul = ZoneId.of("Asia/Seoul")

    private fun session(date: LocalDate, hour: Int = 10) = SessionRecord(
        durationMinutes = 25,
        completedAtMillis = ZonedDateTime.of(date.atTime(hour, 0), seoul).toInstant().toEpochMilli()
    )

    @Test
    fun `주는 일요일부터 토요일까지이고 일요일과 토요일 모두 같은 주로 묶인다`() {
        val sunday = LocalDate.of(2026, 11, 15)
        val saturday = LocalDate.of(2026, 11, 21)

        assertEquals(DayOfWeek.SUNDAY, weekOf(sunday).first().dayOfWeek)
        assertEquals(weekOf(sunday), weekOf(saturday))
        assertEquals(saturday, weekOf(sunday).last())
    }

    @Test
    fun `이번 주 기록만 세고 지난주 기록은 제외하며 오늘과 미래 날을 표시한다`() {
        val today = LocalDate.of(2026, 11, 18) // 수요일
        val summary = buildWeekSummary(
            listOf(
                session(LocalDate.of(2026, 11, 14)), // 지난주 토요일
                session(LocalDate.of(2026, 11, 16)),
                session(LocalDate.of(2026, 11, 16), hour = 15),
                session(today)
            ),
            today,
            seoul
        )

        assertEquals(3, summary.total)
        assertEquals(listOf(0, 2, 0, 1, 0, 0, 0), summary.days.map { it.count })
        assertTrue(summary.days[3].isToday)
        assertFalse(summary.days[3].isFuture)
        assertTrue(summary.days[4].isFuture)
    }
}

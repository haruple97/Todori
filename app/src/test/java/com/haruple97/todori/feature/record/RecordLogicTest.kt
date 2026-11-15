package com.haruple97.todori.feature.record

import com.haruple97.todori.core.model.SessionRecord
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecordLogicTest {
    private val seoul = ZoneId.of("Asia/Seoul")

    private fun session(y: Int, m: Int, d: Int, h: Int, min: Int, minutes: Int = 25) = SessionRecord(
        durationMinutes = minutes,
        completedAtMillis = ZonedDateTime.of(y, m, d, h, min, 0, 0, seoul).toInstant().toEpochMilli()
    )

    @Test
    fun `자정을 기준으로 날짜별로 묶고 하루 안에서는 시각 순으로 정렬한다`() {
        val grouped = groupByDay(
            listOf(session(2026, 11, 15, 23, 59), session(2026, 11, 15, 9, 0), session(2026, 11, 16, 0, 1)),
            seoul
        )

        assertEquals(2, grouped.getValue(LocalDate.of(2026, 11, 15)).size)
        assertEquals(1, grouped.getValue(LocalDate.of(2026, 11, 16)).size)
        val times = grouped.getValue(LocalDate.of(2026, 11, 15)).map { it.completedAtMillis }
        assertEquals(times.sorted(), times)
    }

    @Test
    fun `연속 일수는 오늘 기록이 없으면 어제부터 세고 하루라도 비면 끊긴다`() {
        val today = LocalDate.of(2026, 11, 15)
        val days = setOf(today, today.minusDays(1), today.minusDays(2), today.minusDays(4))

        assertEquals(3, currentStreak(days, today))
        assertEquals(2, currentStreak(days - today, today))
        assertEquals(0, currentStreak(setOf(today.minusDays(2)), today))
    }

    @Test
    fun `달력 격자는 일요일 시작이고 달 밖의 칸은 비어 있다`() {
        // 2026-11-01은 일요일, 2026-12-01은 화요일
        val november = monthGrid(YearMonth.of(2026, 11))
        assertEquals(5, november.size)
        assertEquals(LocalDate.of(2026, 11, 1), november[0][0])
        assertEquals(LocalDate.of(2026, 11, 30), november[4][1])
        assertNull(november[4][2])

        val december = monthGrid(YearMonth.of(2026, 12))
        assertNull(december[0][1])
        assertEquals(LocalDate.of(2026, 12, 1), december[0][2])
    }
}

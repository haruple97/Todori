package com.haruple97.todori.feature.harvest

import com.haruple97.todori.core.model.SessionRecord
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HarvestLogicTest {
    private fun day(d: Int, vararg minutes: Int) =
        LocalDate.of(2026, 11, d) to minutes.mapIndexed { i, m -> SessionRecord(id = d * 100L + i, durationMinutes = m, completedAtMillis = 0) }

    private val november = YearMonth.of(2026, 11)

    @Test
    fun `그 달의 기록만 모아 개수·시간·집중한 날·최장 연속·최고의 날을 계산한다`() {
        val days = mapOf(
            day(1, 25, 25), day(2, 25), day(3, 90), day(10, 30),
            LocalDate.of(2026, 10, 31) to listOf(SessionRecord(1, 25, 0))
        )

        val harvest = monthHarvest(days, november)

        assertEquals(5, harvest.tomatoes)
        assertEquals(195, harvest.minutes)
        assertEquals(4, harvest.activeDays)
        assertEquals(3, harvest.bestStreak)
        assertEquals(LocalDate.of(2026, 11, 3), harvest.bestDay)
        assertEquals(90, harvest.bestDayMinutes)
    }

    @Test
    fun `기록이 없으면 비어 있다`() {
        val harvest = monthHarvest(emptyMap(), november)

        assertTrue(harvest.isEmpty)
        assertNull(harvest.bestDay)
    }

    @Test
    fun `수확량 단계는 5개와 15개와 30개에서 올라간다`() {
        assertEquals(listOf(0, 0, 1, 1, 2, 2, 3), listOf(0, 4, 5, 14, 15, 29, 30).map(::harvestTier))
    }
}

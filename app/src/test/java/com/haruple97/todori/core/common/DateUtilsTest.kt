package com.haruple97.todori.core.common

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {
    private val seoul = ZoneId.of("Asia/Seoul")

    private fun millis(y: Int, m: Int, d: Int, h: Int, min: Int) =
        ZonedDateTime.of(y, m, d, h, min, 0, 0, seoul).toInstant().toEpochMilli()

    @Test
    fun `자정을 기준으로 같은 날과 다른 날을 구분한다`() {
        val lateNight = millis(2026, 11, 15, 23, 59)
        assertTrue(isSameDay(lateNight, millis(2026, 11, 15, 0, 0), seoul))
        assertFalse(isSameDay(lateNight, millis(2026, 11, 16, 0, 0), seoul))
    }
}

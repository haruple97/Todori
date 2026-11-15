package com.haruple97.todori.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeFormatterTest {
    @Test
    fun `남은 시간은 초 단위로 올림해 표시한다`() {
        assertEquals("25:00", formatRemainingTime(25 * 60_000L))
        assertEquals("00:01", formatRemainingTime(1L))
        assertEquals("00:00", formatRemainingTime(-5L))
    }

    @Test
    fun `1시간 이상은 시간을 붙여 표시한다`() {
        assertEquals("59:59", formatRemainingTime(59 * 60_000L + 59_000L))
        assertEquals("1:00:00", formatRemainingTime(60 * 60_000L))
        assertEquals("2:05:09", formatRemainingTime((2 * 3600 + 5 * 60 + 9) * 1000L))
        assertEquals("3:00:00", formatRemainingTime(180 * 60_000L))
    }
}

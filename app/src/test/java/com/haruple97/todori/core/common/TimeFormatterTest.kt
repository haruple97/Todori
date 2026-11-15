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
}

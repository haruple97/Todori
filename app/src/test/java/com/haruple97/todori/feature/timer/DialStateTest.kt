package com.haruple97.todori.feature.timer

import org.junit.Assert.assertEquals
import org.junit.Test

class DialStateTest {
    @Test
    fun `5분 간격으로 이동하고 5분과 60분에서 멈춘다`() {
        assertEquals(30, dialMinutesFor(startMinutes = 25, dragPx = -40f, pxPerStep = 36f))
        assertEquals(35, dialMinutesFor(startMinutes = 25, dragPx = -80f, pxPerStep = 36f))
        assertEquals(20, dialMinutesFor(startMinutes = 25, dragPx = 40f, pxPerStep = 36f))
        assertEquals(60, dialMinutesFor(startMinutes = 55, dragPx = -1000f, pxPerStep = 36f))
        assertEquals(5, dialMinutesFor(startMinutes = 25, dragPx = 1000f, pxPerStep = 36f))
    }

    @Test
    fun `범위 밖이나 5분 단위가 아닌 저장값은 가장 가까운 단계로 맞춘다`() {
        assertEquals(60, snapMinutes(90))
        assertEquals(25, snapMinutes(27))
    }
}

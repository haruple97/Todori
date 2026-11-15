package com.haruple97.todori.feature.timer

import org.junit.Assert.assertEquals
import org.junit.Test

class DialStateTest {
    @Test
    fun `위로 드래그하면 다음 단계로 올라가고 양 끝에서 멈춘다`() {
        assertEquals(30, dialMinutesFor(startMinutes = 25, dragPx = -40f, pxPerStep = 36f))
        assertEquals(40, dialMinutesFor(startMinutes = 25, dragPx = -80f, pxPerStep = 36f))
        assertEquals(20, dialMinutesFor(startMinutes = 25, dragPx = 40f, pxPerStep = 36f))
        assertEquals(90, dialMinutesFor(startMinutes = 60, dragPx = -1000f, pxPerStep = 36f))
        assertEquals(5, dialMinutesFor(startMinutes = 25, dragPx = 1000f, pxPerStep = 36f))
    }

    @Test
    fun `단계에 없는 값은 가장 가까운 단계 기준으로 이동한다`() {
        assertEquals(30, dialMinutesFor(startMinutes = 27, dragPx = -36f, pxPerStep = 36f))
    }
}

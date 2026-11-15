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
    fun `프리미엄은 1~180분을 그대로 두고 무료는 5분 단위 60분 이하로 맞춘다`() {
        assertEquals(37, normalizedMinutes(37, premium = true))
        assertEquals(180, normalizedMinutes(500, premium = true))
        assertEquals(1, normalizedMinutes(0, premium = true))
        assertEquals(35, normalizedMinutes(37, premium = false))
        assertEquals(60, normalizedMinutes(120, premium = false))
    }

    @Test
    fun `슬라이더 위치는 5분 단위가 아니거나 60분을 넘어도 가장 가까운 눈금이다`() {
        assertEquals(DialStepMinutes.lastIndex, nearestIndex(120))
        assertEquals(DialStepMinutes.indexOf(35), nearestIndex(37))
        assertEquals(0, nearestIndex(1))
    }

    @Test
    fun `범위 밖이나 5분 단위가 아닌 저장값은 가장 가까운 단계로 맞춘다`() {
        assertEquals(60, snapMinutes(90))
        assertEquals(25, snapMinutes(27))
    }
}

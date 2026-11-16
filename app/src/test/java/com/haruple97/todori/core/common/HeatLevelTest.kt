package com.haruple97.todori.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class HeatLevelTest {
    @Test
    fun `집중 시간에 따라 0에서 4단계로 나뉜다`() {
        assertEquals(0, heatLevel(0))
        assertEquals(1, heatLevel(25))
        assertEquals(2, heatLevel(30))
        assertEquals(2, heatLevel(89))
        assertEquals(3, heatLevel(90))
        assertEquals(3, heatLevel(179))
        assertEquals(4, heatLevel(180))
        assertEquals(4, heatLevel(600))
    }
}

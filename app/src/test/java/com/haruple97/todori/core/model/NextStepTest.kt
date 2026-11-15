package com.haruple97.todori.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NextStepTest {
    private val plan = CyclePlan(focusMinutes = 25, rounds = 4, shortMinutes = 5, longMinutes = 15, auto = false)

    @Test
    fun `집중은 마지막 횟수에서만 긴 휴식으로 이어진다`() {
        assertEquals(CycleStep(TimerPhase.ShortBreak, 1), nextStep(TimerPhase.Focus, 1, plan))
        assertEquals(CycleStep(TimerPhase.ShortBreak, 3), nextStep(TimerPhase.Focus, 3, plan))
        assertEquals(CycleStep(TimerPhase.LongBreak, 4), nextStep(TimerPhase.Focus, 4, plan))
    }

    @Test
    fun `짧은 휴식은 다음 집중으로, 긴 휴식은 사이클 끝으로 이어진다`() {
        assertEquals(CycleStep(TimerPhase.Focus, 2), nextStep(TimerPhase.ShortBreak, 1, plan))
        assertNull(nextStep(TimerPhase.LongBreak, 4, plan))
    }

    @Test
    fun `단계별 시간은 계획에서 가져온다`() {
        assertEquals(25, plan.minutesOf(TimerPhase.Focus))
        assertEquals(5, plan.minutesOf(TimerPhase.ShortBreak))
        assertEquals(15, plan.minutesOf(TimerPhase.LongBreak))
    }
}

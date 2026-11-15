package com.haruple97.todori.feature.timer

import com.haruple97.todori.core.model.TimerPhase
import com.haruple97.todori.core.model.TimerStatus
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class BubbleMessagesTest {
    private val minute = 60_000L

    private fun running(totalMinutes: Int, elapsedMillis: Long) = bubbleKey(
        status = TimerStatus.Running,
        focusMinutes = totalMinutes,
        totalMillis = totalMinutes * minute,
        remainingMillis = totalMinutes * minute - elapsedMillis,
        todayCount = 0,
        totalCount = 5,
        hour = 14
    )

    private fun idle(focusMinutes: Int = 25, todayCount: Int = 0, totalCount: Int = 5, hour: Int = 14) = bubbleKey(
        TimerStatus.Idle, focusMinutes, 0, 0, todayCount, totalCount, hour
    )

    private fun done(totalMinutes: Int, todayCount: Int) = bubbleKey(
        TimerStatus.Finished, totalMinutes, totalMinutes * minute, 0, todayCount, todayCount, 14
    )

    @Test
    fun `시작하자마자에는 거의 다 익었다는 문구가 나오지 않고 단계별로 바뀐다`() {
        assertEquals(BubbleKey.FocusStart, running(25, elapsedMillis = 10_000))
        assertEquals(BubbleKey.FocusEarly, running(25, elapsedMillis = 5 * minute))
        assertEquals(BubbleKey.FocusMiddle, running(25, elapsedMillis = 12 * minute))
        assertEquals(BubbleKey.FocusLate, running(25, elapsedMillis = 20 * minute))
        assertEquals(BubbleKey.FocusFinal, running(25, elapsedMillis = 24 * minute + 10_000))
    }

    @Test
    fun `아주 짧은 타이머는 마지막 1분 단계를 건너뛴다`() {
        assertEquals(BubbleKey.FocusLate, running(2, elapsedMillis = 90_000))
    }

    @Test
    fun `대기 문구는 처음 사용, 고른 시간, 오늘 횟수, 시간대 순으로 우선한다`() {
        assertEquals(BubbleKey.IdleFirst, idle(totalCount = 0, todayCount = 0, focusMinutes = 60))
        assertEquals(BubbleKey.IdleShort, idle(focusMinutes = 10, todayCount = 2))
        assertEquals(BubbleKey.IdleLong, idle(focusMinutes = 60, todayCount = 2))
        assertEquals(BubbleKey.IdleAfterDone, idle(todayCount = 1))
        assertEquals(BubbleKey.IdleMorning, idle(hour = 8))
        assertEquals(BubbleKey.IdleAfternoon, idle(hour = 14))
        assertEquals(BubbleKey.IdleEvening, idle(hour = 19))
        assertEquals(BubbleKey.IdleNight, idle(hour = 23))
        assertEquals(BubbleKey.IdleNight, idle(hour = 3))
    }

    @Test
    fun `완료 문구는 긴 집중이 우선이고 그다음 오늘 몇 번째인지를 본다`() {
        assertEquals(BubbleKey.DoneLong, done(totalMinutes = 50, todayCount = 1))
        assertEquals(BubbleKey.DoneFirst, done(totalMinutes = 25, todayCount = 1))
        assertEquals(BubbleKey.DoneSecond, done(totalMinutes = 25, todayCount = 2))
        assertEquals(BubbleKey.DoneMany, done(totalMinutes = 25, todayCount = 4))
    }

    @Test
    fun `같은 상황에서는 직전과 같은 문구를 연달아 고르지 않는다`() {
        val history = BubbleHistory(Random(7))
        var previous = history.next(BubbleKey.IdleMorning, 4)
        repeat(60) {
            val current = history.next(BubbleKey.IdleMorning, 4)
            assertNotEquals(previous, current)
            previous = current
        }
    }

    @Test
    fun `사이클 중에는 휴식 단계와 끝난 뒤 제안 문구를 고르고 집중 중에는 일반 단계 문구를 쓴다`() {
        fun key(status: TimerStatus, phase: TimerPhase, elapsed: Long = 10_000) = bubbleKey(
            status, 25, 25 * minute, 25 * minute - elapsed, 0, 5, 14, phase = phase, cycle = true
        )

        assertEquals(BubbleKey.CycleShortBreak, key(TimerStatus.Running, TimerPhase.ShortBreak))
        assertEquals(BubbleKey.CycleLongBreak, key(TimerStatus.Running, TimerPhase.LongBreak))
        assertEquals(BubbleKey.FocusStart, key(TimerStatus.Running, TimerPhase.Focus))
        assertEquals(BubbleKey.CycleFocusDone, key(TimerStatus.Finished, TimerPhase.Focus))
        assertEquals(BubbleKey.CycleBreakDone, key(TimerStatus.Finished, TimerPhase.ShortBreak))
        assertEquals(BubbleKey.CycleComplete, key(TimerStatus.Finished, TimerPhase.LongBreak))
        // 일시정지와 대기는 사이클 여부와 상관없이 기존 문구
        assertEquals(BubbleKey.Paused, key(TimerStatus.Paused, TimerPhase.ShortBreak))
    }
}

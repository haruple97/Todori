package com.haruple97.todori.core.timer

import com.haruple97.todori.core.data.repository.SessionRepository
import com.haruple97.todori.core.model.SessionRecord
import com.haruple97.todori.core.model.CyclePlan
import com.haruple97.todori.core.model.TimerPhase
import com.haruple97.todori.core.model.TimerStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

private const val MINUTE = 60_000L

private class FakeClock(var elapsed: Long = 1_000_000L, var wall: Long = 1_700_000_000_000L) : TimeSource {
    override fun elapsedRealtime() = elapsed
    override fun wallClockMillis() = wall
    fun advance(millis: Long) {
        elapsed += millis
        wall += millis
    }
}

private class FakePersistence : TimerPersistence {
    var saved: TimerSnapshot? = null
    override fun save(snapshot: TimerSnapshot?) {
        saved = snapshot
    }
    override suspend fun load() = saved
}

private class FakePlatform : TimerPlatform {
    var doneCount = 0
    override fun scheduleAlarm(triggerElapsedMillis: Long) = Unit
    override fun cancelAlarm() = Unit
    var lastRunningPhase: TimerPhase? = null
    override fun showRunning(phase: TimerPhase, endElapsedMillis: Long) { lastRunningPhase = phase }
    override fun clearRunning() = Unit
    override fun showDone(phase: TimerPhase) { doneCount++ }
}

private class FakeSessions : SessionRepository {
    val records = mutableListOf<SessionRecord>()
    override val sessions: Flow<List<SessionRecord>> = flowOf(emptyList())
    override suspend fun record(session: SessionRecord) { records += session }
    override suspend fun delete(ids: List<Long>) { records.removeAll { it.id in ids } }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TimerEngineTest {
    private val clock = FakeClock()
    private val persistence = FakePersistence()
    private val platform = FakePlatform()
    private val sessions = FakeSessions()

    private fun newEngine() =
        TimerEngine(clock, persistence, platform, sessions, UnconfinedTestDispatcher())

    @Test
    fun `시작 후 경과 시간만큼 남은 시간이 줄어든다`() {
        val engine = newEngine()
        engine.start(25 * MINUTE)
        clock.advance(10_000)
        engine.refresh()

        assertEquals(TimerStatus.Running, engine.state.value.status)
        assertEquals(25 * MINUTE - 10_000, engine.state.value.remainingMillis)
    }

    @Test
    fun `일시정지 중에는 시간이 멈추고 재개하면 이어진다`() {
        val engine = newEngine()
        engine.start(25 * MINUTE)
        clock.advance(MINUTE)
        engine.pause()
        clock.advance(5 * MINUTE)
        engine.refresh()
        assertEquals(24 * MINUTE, engine.state.value.remainingMillis)

        engine.resume()
        clock.advance(MINUTE)
        engine.refresh()
        assertEquals(23 * MINUTE, engine.state.value.remainingMillis)
    }

    @Test
    fun `끝나면 한 번만 완료 처리되고 세션이 기록된다`() {
        val engine = newEngine()
        engine.start(25 * MINUTE)
        clock.advance(25 * MINUTE)
        engine.refresh()
        engine.onAlarm()

        assertEquals(TimerStatus.Finished, engine.state.value.status)
        assertEquals(1, platform.doneCount)
        assertEquals(listOf(25), sessions.records.map { it.durationMinutes })
    }

    @Test
    fun `프로세스 재시작 후 진행 중이던 타이머를 복원한다`() = runTest {
        newEngine().start(25 * MINUTE)
        clock.advance(2 * MINUTE)

        val restarted = newEngine()
        restarted.restore()

        assertEquals(TimerStatus.Running, restarted.state.value.status)
        assertEquals(23 * MINUTE, restarted.state.value.remainingMillis)
    }

    @Test
    fun `재부팅으로 보이면 저장된 타이머를 버린다`() = runTest {
        newEngine().start(25 * MINUTE)
        // 재부팅: elapsedRealtime은 초기화되고 벽시계는 계속 흐른다.
        clock.elapsed = 5_000L
        clock.wall += 10 * MINUTE

        val restarted = newEngine()
        restarted.restore()

        assertEquals(TimerStatus.Idle, restarted.state.value.status)
        assertNull(persistence.saved)
    }

    private fun plan(auto: Boolean = false, rounds: Int = 2) =
        CyclePlan(focusMinutes = 25, rounds = rounds, shortMinutes = 5, longMinutes = 15, auto = auto)

    /** 지금 단계를 끝까지 흘려보낸다. */
    private fun TimerEngine.finishCurrent() {
        clock.advance(state.value.totalMillis)
        refresh()
    }

    @Test
    fun `확인형 사이클은 집중, 짧은 휴식, 집중, 긴 휴식 순으로 한 단계씩 넘어가고 휴식은 기록하지 않는다`() {
        val engine = newEngine()
        engine.start(25 * MINUTE, plan())
        assertEquals(TimerPhase.Focus to 1, engine.state.value.phase to engine.state.value.round)

        engine.finishCurrent()
        assertEquals(TimerStatus.Finished, engine.state.value.status)
        engine.startNext()
        assertEquals(TimerPhase.ShortBreak, engine.state.value.phase)
        assertEquals(5 * MINUTE, engine.state.value.totalMillis)

        engine.finishCurrent()
        engine.startNext()
        assertEquals(TimerPhase.Focus to 2, engine.state.value.phase to engine.state.value.round)

        engine.finishCurrent()
        engine.startNext()
        assertEquals(TimerPhase.LongBreak, engine.state.value.phase)
        assertEquals(15 * MINUTE, engine.state.value.totalMillis)

        engine.finishCurrent()
        assertEquals(TimerStatus.Finished, engine.state.value.status)
        // 집중 두 번만 기록된다
        assertEquals(listOf(25, 25), sessions.records.map { it.durationMinutes })
    }

    @Test
    fun `긴 휴식이 끝난 뒤 다음 시작은 새 사이클의 첫 집중이다`() {
        val engine = newEngine()
        engine.start(25 * MINUTE, plan(rounds = 1))
        engine.finishCurrent()
        engine.startNext() // 긴 휴식
        engine.finishCurrent()
        engine.startNext() // 새 사이클

        assertEquals(TimerPhase.Focus to 1, engine.state.value.phase to engine.state.value.round)
        assertEquals(TimerStatus.Running, engine.state.value.status)
    }

    @Test
    fun `자동 이어가기는 알람 한 번으로 다음 단계를 바로 시작한다`() {
        val engine = newEngine()
        engine.start(25 * MINUTE, plan(auto = true))
        clock.advance(25 * MINUTE)
        engine.onAlarm()

        assertEquals(TimerStatus.Running, engine.state.value.status)
        assertEquals(TimerPhase.ShortBreak, engine.state.value.phase)
        assertEquals(TimerPhase.ShortBreak, platform.lastRunningPhase)
        assertEquals(1, platform.doneCount)
        assertEquals(1, sessions.records.size)
    }

    @Test
    fun `자동 이어가기도 사이클의 마지막(긴 휴식)에서는 멈춘다`() {
        val engine = newEngine()
        engine.start(25 * MINUTE, plan(auto = true, rounds = 1))
        engine.finishCurrent() // 긴 휴식으로 자동 이동
        assertEquals(TimerPhase.LongBreak, engine.state.value.phase)
        engine.finishCurrent()

        assertEquals(TimerStatus.Finished, engine.state.value.status)
    }

    @Test
    fun `휴식 건너뛰기는 다음 집중으로 가고 기록을 남기지 않는다`() {
        val engine = newEngine()
        engine.start(25 * MINUTE, plan())
        engine.finishCurrent()
        engine.startNext() // 짧은 휴식
        engine.skipBreak()

        assertEquals(TimerPhase.Focus to 2, engine.state.value.phase to engine.state.value.round)
        assertEquals(25 * MINUTE, engine.state.value.totalMillis)
        assertEquals(1, sessions.records.size)
    }

    @Test
    fun `사이클을 취소하면 처음 상태로 돌아간다`() {
        val engine = newEngine()
        engine.start(25 * MINUTE, plan())
        engine.cancel()

        assertEquals(TimerStatus.Idle, engine.state.value.status)
        assertNull(engine.state.value.plan)
        assertNull(persistence.saved)
    }

    @Test
    fun `프로세스가 다시 떠도 사이클 단계와 계획이 복원된다`() = runTest {
        val engine = newEngine()
        engine.start(25 * MINUTE, plan())
        engine.finishCurrent()
        engine.startNext()
        clock.advance(MINUTE)

        val restarted = newEngine()
        restarted.restore()

        assertEquals(TimerPhase.ShortBreak, restarted.state.value.phase)
        assertEquals(plan(), restarted.state.value.plan)
        assertEquals(4 * MINUTE, restarted.state.value.remainingMillis)
    }

    @Test
    fun `일반 모드는 사이클 정보 없이 기존처럼 동작한다`() {
        val engine = newEngine()
        engine.start(25 * MINUTE)
        engine.finishCurrent()

        assertEquals(TimerStatus.Finished, engine.state.value.status)
        assertNull(engine.state.value.plan)
        engine.startNext() // 계획이 없으면 아무 일도 하지 않는다
        assertEquals(TimerStatus.Finished, engine.state.value.status)
    }
}

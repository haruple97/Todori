package com.haruple97.todori.core.timer

import com.haruple97.todori.core.data.repository.SessionRepository
import com.haruple97.todori.core.model.SessionRecord
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
    var alarmScheduled = false
    var doneCount = 0
    override fun scheduleAlarm(triggerElapsedMillis: Long) { alarmScheduled = true }
    override fun cancelAlarm() { alarmScheduled = false }
    override fun showRunning(phase: TimerPhase, endElapsedMillis: Long) = Unit
    override fun clearRunning() = Unit
    override fun showDone(phase: TimerPhase) { doneCount++ }
}

private class FakeSessions : SessionRepository {
    val records = mutableListOf<SessionRecord>()
    override val sessions: Flow<List<SessionRecord>> = flowOf(emptyList())
    override suspend fun record(session: SessionRecord) { records += session }
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
        engine.start(TimerPhase.Focus, 25 * MINUTE)
        clock.advance(10_000)
        engine.refresh()

        assertEquals(TimerStatus.Running, engine.state.value.status)
        assertEquals(25 * MINUTE - 10_000, engine.state.value.remainingMillis)
    }

    @Test
    fun `일시정지 중에는 시간이 멈추고 재개하면 이어진다`() {
        val engine = newEngine()
        engine.start(TimerPhase.Focus, 25 * MINUTE)
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
    fun `집중이 끝나면 한 번만 완료 처리되고 세션이 기록된다`() {
        val engine = newEngine()
        engine.start(TimerPhase.Focus, 25 * MINUTE)
        clock.advance(25 * MINUTE)
        engine.refresh()
        engine.onAlarm()

        assertEquals(TimerStatus.Finished, engine.state.value.status)
        assertEquals(1, platform.doneCount)
        assertEquals(listOf(25), sessions.records.map { it.durationMinutes })
    }

    @Test
    fun `휴식 완료는 세션으로 기록하지 않는다`() {
        val engine = newEngine()
        engine.start(TimerPhase.Break, 5 * MINUTE)
        clock.advance(5 * MINUTE)
        engine.refresh()

        assertEquals(TimerStatus.Finished, engine.state.value.status)
        assertEquals(0, sessions.records.size)
    }

    @Test
    fun `프로세스 재시작 후 진행 중이던 타이머를 복원한다`() = runTest {
        newEngine().start(TimerPhase.Focus, 25 * MINUTE)
        clock.advance(2 * MINUTE)

        val restarted = newEngine()
        restarted.restore()

        assertEquals(TimerStatus.Running, restarted.state.value.status)
        assertEquals(23 * MINUTE, restarted.state.value.remainingMillis)
    }

    @Test
    fun `재부팅으로 보이면 저장된 타이머를 버린다`() = runTest {
        newEngine().start(TimerPhase.Focus, 25 * MINUTE)
        // 재부팅: elapsedRealtime은 초기화되고 벽시계는 계속 흐른다.
        clock.elapsed = 5_000L
        clock.wall += 10 * MINUTE

        val restarted = newEngine()
        restarted.restore()

        assertEquals(TimerStatus.Idle, restarted.state.value.status)
        assertNull(persistence.saved)
    }
}

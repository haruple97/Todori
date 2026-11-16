package com.haruple97.todori.feature.record

import app.cash.turbine.test
import com.haruple97.todori.core.data.repository.SessionRepository
import com.haruple97.todori.core.data.repository.SettingsRepository
import com.haruple97.todori.core.model.Appearance
import com.haruple97.todori.core.model.CalendarPrefs
import com.haruple97.todori.core.model.CycleSettings
import com.haruple97.todori.core.model.HeatStyle
import com.haruple97.todori.core.model.SessionRecord
import com.haruple97.todori.core.model.SkinId
import com.haruple97.todori.core.model.ThemeId
import com.haruple97.todori.core.model.TimerSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

private class FakeSessionRepository(initial: List<SessionRecord>) : SessionRepository {
    private val store = MutableStateFlow(initial)
    override val sessions = store
    override suspend fun record(session: SessionRecord) { store.value = store.value + session }
    override suspend fun delete(ids: List<Long>) { store.value = store.value.filterNot { it.id in ids } }
}

private class FakeSettingsRepository : SettingsRepository {
    override val settings = MutableStateFlow(TimerSettings())
    override suspend fun update(settings: TimerSettings) { this.settings.value = settings }
    override val appearance = MutableStateFlow(Appearance())
    override suspend fun setSkin(skin: SkinId) = Unit
    override suspend fun setTheme(theme: ThemeId) = Unit
    override val cycle = MutableStateFlow(CycleSettings())
    override suspend fun setCycle(settings: CycleSettings) { cycle.value = settings }
    override val calendarPrefs = MutableStateFlow(CalendarPrefs())
    override suspend fun setHeatMode(enabled: Boolean) { calendarPrefs.value = calendarPrefs.value.copy(heatMode = enabled) }
    override suspend fun setHeatStyle(style: HeatStyle) { calendarPrefs.value = calendarPrefs.value.copy(style = style) }
    override suspend fun isReviewRequested() = false
    override suspend fun markReviewRequested() = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class RecordViewModelTest {
    private val first = SessionRecord(id = 1, durationMinutes = 25, completedAtMillis = 1_000L)
    private val second = SessionRecord(id = 2, durationMinutes = 30, completedAtMillis = 2_000L)

    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `삭제하면 기록이 사라지고 삭제 이벤트가 나온다`() = runTest {
        val repository = FakeSessionRepository(listOf(first, second))
        val viewModel = RecordViewModel(repository, FakeSettingsRepository())

        viewModel.events.test {
            viewModel.delete(listOf(first))

            assertEquals(RecordEvent.Deleted(listOf(first)), awaitItem())
            assertEquals(listOf(second), repository.sessions.value)
        }
    }

    @Test
    fun `되돌리기는 삭제한 기록을 같은 id로 되살린다`() = runTest {
        val repository = FakeSessionRepository(listOf(first, second))
        val viewModel = RecordViewModel(repository, FakeSettingsRepository())

        viewModel.delete(listOf(first, second))
        assertEquals(emptyList<SessionRecord>(), repository.sessions.value)

        viewModel.undoDelete(listOf(first, second))
        assertEquals(listOf(1L, 2L), repository.sessions.value.map { it.id })
    }

    @Test
    fun `농도 모드와 색 스타일을 바꾸면 화면 상태에 반영된다`() = runTest {
        val viewModel = RecordViewModel(FakeSessionRepository(emptyList()), FakeSettingsRepository())

        viewModel.uiState.test {
            awaitItem()
            viewModel.setHeatMode(true)
            viewModel.setHeatStyle(HeatStyle.Blush)

            var latest = awaitItem()
            while (!(latest.calendar.heatMode && latest.calendar.style == HeatStyle.Blush)) latest = awaitItem()
            assertEquals(CalendarPrefs(heatMode = true, style = HeatStyle.Blush), latest.calendar)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

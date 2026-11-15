package com.haruple97.todori.feature.record

import app.cash.turbine.test
import com.haruple97.todori.core.data.repository.SessionRepository
import com.haruple97.todori.core.model.SessionRecord
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
        val viewModel = RecordViewModel(repository)

        viewModel.events.test {
            viewModel.delete(listOf(first))

            assertEquals(RecordEvent.Deleted(listOf(first)), awaitItem())
            assertEquals(listOf(second), repository.sessions.value)
        }
    }

    @Test
    fun `되돌리기는 삭제한 기록을 같은 id로 되살린다`() = runTest {
        val repository = FakeSessionRepository(listOf(first, second))
        val viewModel = RecordViewModel(repository)

        viewModel.delete(listOf(first, second))
        assertEquals(emptyList<SessionRecord>(), repository.sessions.value)

        viewModel.undoDelete(listOf(first, second))
        assertEquals(listOf(1L, 2L), repository.sessions.value.map { it.id })
    }
}

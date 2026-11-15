package com.haruple97.todori.core.data.repository

import com.haruple97.todori.core.data.database.SessionDao
import com.haruple97.todori.core.data.database.toEntity
import com.haruple97.todori.core.data.database.toModel
import com.haruple97.todori.core.model.SessionRecord
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SessionRepository {
    val sessions: Flow<List<SessionRecord>>
    /** 새 기록을 남긴다. id가 이미 있는 기록(삭제 되돌리기)은 같은 id로 다시 들어간다. */
    suspend fun record(session: SessionRecord)

    suspend fun delete(ids: List<Long>)
}

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val dao: SessionDao
) : SessionRepository {
    override val sessions: Flow<List<SessionRecord>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun record(session: SessionRecord) = dao.insert(session.toEntity())

    override suspend fun delete(ids: List<Long>) = dao.deleteByIds(ids)
}

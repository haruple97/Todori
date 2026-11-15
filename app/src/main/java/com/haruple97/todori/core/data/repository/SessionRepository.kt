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
    suspend fun record(session: SessionRecord)
}

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val dao: SessionDao
) : SessionRepository {
    override val sessions: Flow<List<SessionRecord>> =
        dao.observeAll().map { list -> list.map { it.toModel() } }

    override suspend fun record(session: SessionRecord) = dao.insert(session.toEntity())
}

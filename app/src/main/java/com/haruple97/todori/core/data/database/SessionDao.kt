package com.haruple97.todori.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)

    @Query("SELECT * FROM sessions ORDER BY completedAtMillis DESC")
    fun observeAll(): Flow<List<SessionEntity>>
}

package com.haruple97.todori.core.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.haruple97.todori.core.model.SessionRecord

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val durationMinutes: Int,
    val completedAtMillis: Long
)

fun SessionEntity.toModel() = SessionRecord(
    id = id,
    durationMinutes = durationMinutes,
    completedAtMillis = completedAtMillis
)

fun SessionRecord.toEntity() = SessionEntity(
    id = id,
    durationMinutes = durationMinutes,
    completedAtMillis = completedAtMillis
)

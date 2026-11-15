package com.haruple97.todori.core.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.haruple97.todori.core.model.SessionRecord
import com.haruple97.todori.core.model.TimerPhase

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phase: String,
    val durationMinutes: Int,
    val completedAtMillis: Long
)

fun SessionEntity.toModel() = SessionRecord(
    id = id,
    phase = TimerPhase.valueOf(phase),
    durationMinutes = durationMinutes,
    completedAtMillis = completedAtMillis
)

fun SessionRecord.toEntity() = SessionEntity(
    id = id,
    phase = phase.name,
    durationMinutes = durationMinutes,
    completedAtMillis = completedAtMillis
)

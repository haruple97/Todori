package com.haruple97.todori.core.model

data class SessionRecord(
    val id: Long = 0,
    val durationMinutes: Int,
    val completedAtMillis: Long
)

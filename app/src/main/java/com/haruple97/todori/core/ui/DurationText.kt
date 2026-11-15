package com.haruple97.todori.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.haruple97.todori.R

/** 분을 "45분", "2시간", "1시간 30분"처럼 읽기 좋게 바꾼다. */
@Composable
fun formatDuration(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0 -> stringResource(R.string.minutes_format, minutes)
        minutes == 0 -> stringResource(R.string.duration_hours, hours)
        else -> stringResource(R.string.duration_hours_minutes, hours, minutes)
    }
}

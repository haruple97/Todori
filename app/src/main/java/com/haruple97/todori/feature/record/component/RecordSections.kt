package com.haruple97.todori.feature.record.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haruple97.todori.R
import com.haruple97.todori.core.designsystem.theme.todoriColors
import com.haruple97.todori.core.model.SessionRecord
import com.haruple97.todori.core.ui.GlassIconButton
import com.haruple97.todori.core.ui.MiniTomato
import com.haruple97.todori.core.ui.TomatoCharacter
import com.haruple97.todori.core.ui.TomatoMood
import com.haruple97.todori.core.ui.icon.TodoriIcons
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
private fun SoftCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(1.5.dp, todoriColors.accentLight),
        content = content
    )
}

/** 전체 토마토 · 총 집중 시간 · 연속 일수 요약 카드 세 개. */
@Composable
fun RecordSummary(totalCount: Int, totalMinutes: Int, streak: Int, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(stringResource(R.string.count_format, totalCount), stringResource(R.string.stat_tomatoes), Modifier.weight(1f))
        StatCard(formatFocusMinutes(totalMinutes), stringResource(R.string.stat_focus_time), Modifier.weight(1f))
        StatCard(stringResource(R.string.days_format, streak), stringResource(R.string.stat_streak), Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    SoftCard(modifier) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp, lineHeight = 28.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun formatFocusMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0 -> stringResource(R.string.minutes_format, minutes)
        minutes == 0 -> stringResource(R.string.duration_hours, hours)
        else -> stringResource(R.string.duration_hours_minutes, hours, minutes)
    }
}

/**
 * 선택한 날짜에 채워진 토마토와 완료 시각 목록.
 * 줄마다 휴지통으로 바로 지울 수 있고(되돌리기 제공), 2개 이상이면 그날 전체 삭제도 나온다.
 */
@Composable
fun DayDetail(
    date: LocalDate,
    sessions: List<SessionRecord>,
    zone: ZoneId,
    onDelete: (SessionRecord) -> Unit,
    onDeleteAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]
    val dayPattern = stringResource(R.string.day_pattern)
    val title = remember(date, dayPattern, locale) { date.format(DateTimeFormatter.ofPattern(dayPattern, locale)) }
    val timeFormatter = remember(locale) { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale) }
    val deleteDescription = stringResource(R.string.record_delete)

    SoftCard(modifier.fillMaxWidth().animateContentSize()) {
        Column(Modifier.padding(start = 18.dp, end = 12.dp, top = 18.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            if (sessions.isEmpty()) {
                Text(
                    text = stringResource(R.string.record_day_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            } else {
                Text(
                    text = stringResource(
                        R.string.record_day_summary,
                        sessions.size,
                        formatFocusMinutes(sessions.sumOf { it.durationMinutes })
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                sessions.forEach { session ->
                    val time = Instant.ofEpochMilli(session.completedAtMillis).atZone(zone).toLocalTime()
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MiniTomato(size = 24.dp)
                        Text(
                            text = time.format(timeFormatter),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(R.string.minutes_format, session.durationMinutes),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        GlassIconButton(
                            icon = TodoriIcons.Trash,
                            contentDescription = deleteDescription,
                            onClick = { onDelete(session) },
                            size = 40.dp
                        )
                    }
                }
                if (sessions.size > 1) {
                    TextButton(onClick = onDeleteAll, modifier = Modifier.align(Alignment.End)) {
                        Text(
                            text = stringResource(R.string.record_delete_day),
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp),
                            color = todoriColors.accentDeep
                        )
                    }
                }
            }
        }
    }
}

/** 아직 기록이 하나도 없을 때 보여주는 졸린 토돌이와 안내. */
@Composable
fun EmptyRecordHint(modifier: Modifier = Modifier) {
    SoftCard(modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TomatoCharacter(TomatoMood.Sleepy, ripeness = 1f, modifier = Modifier.size(104.dp))
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.record_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.record_empty_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

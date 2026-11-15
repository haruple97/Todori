package com.haruple97.todori.feature.record.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haruple97.todori.R
import com.haruple97.todori.core.designsystem.theme.JellyPinkDeep
import com.haruple97.todori.core.designsystem.theme.JellyPinkLight
import com.haruple97.todori.core.model.SessionRecord
import com.haruple97.todori.core.ui.MiniTomato
import com.haruple97.todori.feature.record.monthGrid
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle

private val SundayFirst = listOf(
    DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY
)

/** 집중을 끝낸 날마다 토마토 스티커가 채워지는 월 달력. 하루에 여러 개면 숫자 배지가 붙는다. */
@Composable
fun MonthCalendar(
    month: YearMonth,
    today: LocalDate,
    selected: LocalDate,
    days: Map<LocalDate, List<SessionRecord>>,
    cellSize: Dp,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val weeks = remember(month) { monthGrid(month) }
    val locale = LocalConfiguration.current.locales[0]
    val dayPattern = stringResource(R.string.day_pattern)
    val dayFormatter = remember(dayPattern, locale) { DateTimeFormatter.ofPattern(dayPattern, locale) }
    val small = MaterialTheme.typography.bodyLarge.copy(fontSize = 11.sp)

    Column(modifier) {
        Row {
            SundayFirst.forEach { dayOfWeek ->
                Text(
                    text = dayOfWeek.getDisplayName(JavaTextStyle.SHORT, locale),
                    style = small,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(cellSize).padding(bottom = 4.dp)
                )
            }
        }
        weeks.forEach { week ->
            Row {
                week.forEach { date ->
                    DayCell(
                        date = date,
                        today = today,
                        selected = selected,
                        sessions = date?.let { days[it] }.orEmpty(),
                        cellSize = cellSize,
                        dayFormatter = dayFormatter,
                        onSelect = onSelect
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    today: LocalDate,
    selected: LocalDate,
    sessions: List<SessionRecord>,
    cellSize: Dp,
    dayFormatter: DateTimeFormatter,
    onSelect: (LocalDate) -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    if (date == null) {
        Box(Modifier.size(cellSize))
        return
    }
    val isFuture = date.isAfter(today)
    val isSelected = date == selected
    val description = stringResource(R.string.record_cell_description, date.format(dayFormatter), sessions.size)

    Box(
        modifier = Modifier
            .size(cellSize)
            .padding(2.dp)
            .clip(shape)
            .then(
                if (isSelected) {
                    Modifier
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                        .border(1.5.dp, JellyPinkLight, shape)
                } else {
                    Modifier
                }
            )
            .then(if (isFuture) Modifier else Modifier.clickable { onSelect(date) })
            .semantics(mergeDescendants = true) { contentDescription = description }
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 11.sp,
                fontWeight = if (date == today) FontWeight.ExtraBold else FontWeight.Medium
            ),
            color = when {
                date == today -> JellyPinkDeep
                isFuture -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.28f)
                else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            },
            modifier = Modifier.align(Alignment.TopStart).padding(start = 5.dp, top = 3.dp)
        )
        when {
            sessions.isNotEmpty() -> {
                PoppingTomato(cellSize * 0.54f, Modifier.align(Alignment.Center).offset(y = cellSize * 0.07f))
                if (sessions.size > 1) CountBadge(sessions.size, Modifier.align(Alignment.BottomEnd))
            }
            !isFuture -> EmptySlot(cellSize * 0.32f, Modifier.align(Alignment.Center))
        }
    }
}

/** 칸이 나타날 때 토마토가 통통 튀며 채워진다. */
@Composable
private fun PoppingTomato(size: Dp, modifier: Modifier = Modifier) {
    val scale = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, spring(dampingRatio = 0.4f)) }
    MiniTomato(
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        size = size
    )
}

@Composable
private fun CountBadge(count: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(3.dp).size(15.dp).background(JellyPinkDeep, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (count > 9) "9+" else count.toString(),
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 9.sp, lineHeight = 9.sp),
            color = Color.White
        )
    }
}

@Composable
private fun EmptySlot(size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        drawCircle(
            color = JellyPinkLight.copy(alpha = 0.7f),
            radius = this.size.minDimension / 2f - 1.dp.toPx(),
            style = Stroke(width = 1.2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)))
        )
    }
}

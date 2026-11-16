package com.haruple97.todori.feature.stats.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haruple97.todori.R
import com.haruple97.todori.core.common.HEAT_MAX_LEVEL
import com.haruple97.todori.core.common.heatLevel
import com.haruple97.todori.core.designsystem.theme.todoriColors
import com.haruple97.todori.core.model.HeatStyle
import com.haruple97.todori.core.ui.formatDuration
import com.haruple97.todori.feature.record.component.heatColor
import com.haruple97.todori.feature.stats.yearWeeks
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val CELL = 13.dp
private val GAP = 3.dp

/**
 * GitHub 잔디처럼 최근 1년을 주 단위 열로 그린 토마토 밭. 하루 집중 시간이 길수록 진하다.
 * [interactive]가 꺼져 있으면(잠긴 맛보기) 스크롤·탭 없이 보여주기만 한다.
 */
@Composable
fun YearHeatmap(
    today: LocalDate,
    minutes: Map<LocalDate, Int>,
    style: HeatStyle,
    interactive: Boolean,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]
    val weeks = remember(today) { yearWeeks(today) }
    val monthFormatter = remember(locale) { DateTimeFormatter.ofPattern("MMM", locale) }
    val dayPattern = stringResource(R.string.day_pattern)
    val dayFormatter = remember(dayPattern, locale) { DateTimeFormatter.ofPattern(dayPattern, locale) }
    var selected by remember { mutableStateOf<LocalDate?>(null) }
    val scroll = rememberScrollState()
    // 가장 최근 주가 보이도록 오른쪽 끝에서 시작한다.
    LaunchedEffect(scroll.maxValue) { scroll.scrollTo(scroll.maxValue) }

    val from = weeks.first().first()
    val inYear = remember(minutes, from, today) { minutes.filter { (d, m) -> !d.isBefore(from) && !d.isAfter(today) && m > 0 } }
    val summary = stringResource(R.string.stats_year_summary, inYear.size, formatDuration(inYear.values.sum()))
    val caption = selected?.let { day ->
        val m = minutes[day] ?: 0
        val name = day.format(dayFormatter)
        if (m > 0) stringResource(R.string.stats_year_caption, name, formatDuration(m))
        else stringResource(R.string.stats_year_caption_empty, name)
    } ?: summary
    val muted = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)

    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.horizontalScroll(scroll, enabled = interactive), horizontalArrangement = Arrangement.spacedBy(GAP)) {
            weeks.forEachIndexed { index, week ->
                val firstOfMonth = week.firstOrNull { it.dayOfMonth == 1 && it <= today }
                Column(verticalArrangement = Arrangement.spacedBy(GAP)) {
                    Box(Modifier.height(14.dp).width(CELL)) {
                        // 월 라벨은 열 폭을 넘겨 그리므로 다음 열을 가리지 않게 겹쳐 둔다.
                        if (firstOfMonth != null && index < weeks.size - 2) {
                            Text(
                                text = firstOfMonth.format(monthFormatter),
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 10.sp),
                                color = muted,
                                softWrap = false,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Visible
                            )
                        }
                    }
                    week.forEach { day -> YearCell(day, today, minutes[day] ?: 0, style, selected == day, interactive) { selected = it } }
                }
            }
        }
        Text(caption, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val label = MaterialTheme.typography.bodyLarge.copy(fontSize = 11.sp)
            Text(stringResource(R.string.record_heat_less), style = label, color = muted)
            (0..HEAT_MAX_LEVEL).forEach { level ->
                Box(Modifier.size(12.dp).background(levelColor(style, level), RoundedCornerShape(3.dp)))
            }
            Text(stringResource(R.string.record_heat_more), style = label, color = muted)
        }
    }
}

@Composable
private fun levelColor(style: HeatStyle, level: Int): Color =
    if (level == 0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f) else heatColor(style, level)

@Composable
private fun YearCell(
    day: LocalDate,
    today: LocalDate,
    minutes: Int,
    style: HeatStyle,
    isSelected: Boolean,
    interactive: Boolean,
    onSelect: (LocalDate) -> Unit
) {
    if (day.isAfter(today)) {
        Box(Modifier.size(CELL))
        return
    }
    val shape = RoundedCornerShape(4.dp)
    val description = if (minutes > 0) formatDuration(minutes) else null
    Box(
        Modifier
            .size(CELL)
            .background(levelColor(style, heatLevel(minutes)), shape)
            .then(if (isSelected || day == today) Modifier.border(1.5.dp, todoriColors.accentDeep, shape) else Modifier)
            .then(if (interactive) Modifier.clickable { onSelect(day) } else Modifier)
            .semantics { if (description != null) contentDescription = "$day, $description" }
    )
}

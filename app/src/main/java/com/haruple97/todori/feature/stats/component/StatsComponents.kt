package com.haruple97.todori.feature.stats.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haruple97.todori.R
import com.haruple97.todori.core.designsystem.theme.todoriColors
import com.haruple97.todori.core.ui.GlassIconButton
import com.haruple97.todori.core.ui.MiniTomato
import com.haruple97.todori.core.ui.SoftCard
import com.haruple97.todori.core.ui.StatCard
import com.haruple97.todori.core.ui.TomatoCharacter
import com.haruple97.todori.core.ui.TomatoMood
import com.haruple97.todori.core.ui.formatDuration
import com.haruple97.todori.core.ui.icon.TodoriIcons
import com.haruple97.todori.feature.stats.DayStat
import com.haruple97.todori.feature.stats.Insight
import com.haruple97.todori.feature.stats.MonthTotals
import com.haruple97.todori.feature.stats.TimeBucket
import com.haruple97.todori.feature.stats.averageMinutesPerActiveDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle
import kotlin.math.max

@Composable
private fun insightText(insight: Insight): String {
    val locale = LocalConfiguration.current.locales[0]
    return when (insight) {
        Insight.Empty -> stringResource(R.string.insight_empty)
        is Insight.Improved -> stringResource(R.string.insight_improved, formatDuration(insight.diffMinutes))
        is Insight.BestDay -> stringResource(
            R.string.insight_best_day,
            insight.dayOfWeek.getDisplayName(JavaTextStyle.FULL, locale)
        )
    }
}

/** 캐릭터가 이번 주 한 줄 인사이트를 말해주는 줄. */
@Composable
fun InsightRow(insight: Insight, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TomatoCharacter(mood = TomatoMood.Idle, ripeness = 1f, modifier = Modifier.size(76.dp))
        SoftCard(Modifier.weight(1f)) {
            Text(
                text = insightText(insight),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
            )
        }
    }
}

/** 이번 주 집중 시간 · 완료 개수 · 하루 평균. */
@Composable
fun WeekSummaryRow(week: List<DayStat>, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(formatDuration(week.sumOf { it.minutes }), stringResource(R.string.stats_summary_focus), Modifier.weight(1f))
        StatCard(stringResource(R.string.count_format, week.sumOf { it.count }), stringResource(R.string.stats_summary_count), Modifier.weight(1f))
        StatCard(formatDuration(averageMinutesPerActiveDay(week)), stringResource(R.string.stats_summary_avg), Modifier.weight(1f))
    }
}

/** 프리미엄 통계 묶음(주간 그래프 · 시간대 · 월간). 잠긴 맛보기에서는 같은 모양을 샘플 값으로 흐리게 그린다. */
@Composable
fun PremiumStatsBlock(
    today: LocalDate,
    chartWeek: List<DayStat>,
    chartCanGoNext: Boolean,
    buckets: Map<TimeBucket, Int>,
    bestBucket: TimeBucket?,
    month: MonthTotals,
    longestStreak: Int,
    yearMinutes: Map<LocalDate, Int>,
    heatStyle: com.haruple97.todori.core.model.HeatStyle,
    interactive: Boolean,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        SectionCard(stringResource(R.string.stats_section_year)) {
            YearHeatmap(today, yearMinutes, heatStyle, interactive)
        }
        SectionCard(stringResource(R.string.stats_section_week)) {
            WeekChart(chartWeek, today, chartCanGoNext, interactive, onPreviousWeek, onNextWeek)
        }
        SectionCard(stringResource(R.string.stats_section_time)) {
            TimeBuckets(buckets, bestBucket)
        }
        SectionCard(stringResource(R.string.stats_section_month)) {
            MonthStats(month, longestStreak)
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    SoftCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            content()
        }
    }
}

@Composable
private fun WeekChart(
    week: List<DayStat>,
    today: LocalDate,
    canGoNext: Boolean,
    interactive: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val locale = LocalConfiguration.current.locales[0]
    val rangePattern = stringResource(R.string.stats_range_pattern)
    val dayPattern = stringResource(R.string.day_pattern)
    val rangeFormatter = remember(rangePattern, locale) { DateTimeFormatter.ofPattern(rangePattern, locale) }
    val dayFormatter = remember(dayPattern, locale) { DateTimeFormatter.ofPattern(dayPattern, locale) }
    val best = week.indices.filter { week[it].minutes > 0 }.maxByOrNull { week[it].minutes }
    // 주가 바뀌면 최고 요일(없으면 오늘, 그것도 없으면 첫 칸)을 다시 선택한다.
    var selected by remember(week.firstOrNull()?.date) {
        mutableIntStateOf(best ?: week.indexOfFirst { it.date == today }.takeIf { it >= 0 } ?: 0)
    }

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        GlassIconButton(TodoriIcons.ChevronLeft, stringResource(R.string.stats_week_prev), onPrevious, size = 36.dp, enabled = interactive)
        Text(
            text = stringResource(
                R.string.stats_range_format,
                week.first().date.format(rangeFormatter),
                week.last().date.format(rangeFormatter)
            ),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        GlassIconButton(TodoriIcons.ChevronRight, stringResource(R.string.stats_week_next), onNext, size = 36.dp, enabled = interactive && canGoNext)
    }

    val maxMinutes = max(60, week.maxOf { it.minutes })
    Row(Modifier.fillMaxWidth().height(190.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        week.forEachIndexed { index, day ->
            Bar(
                day = day,
                maxMinutes = maxMinutes,
                isBest = index == best,
                isToday = day.date == today,
                isSelected = interactive && index == selected,
                onClick = if (interactive) ({ selected = index }) else null,
                label = day.date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, locale),
                modifier = Modifier.weight(1f)
            )
        }
    }

    val picked = week[selected.coerceIn(week.indices)]
    val dayText = picked.date.format(dayFormatter)
    Text(
        text = if (picked.minutes > 0) {
            stringResource(R.string.stats_bar_caption, dayText, formatDuration(picked.minutes), picked.count)
        } else {
            stringResource(R.string.stats_bar_empty, dayText)
        },
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun Bar(
    day: DayStat,
    maxMinutes: Int,
    isBest: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    onClick: (() -> Unit)?,
    label: String,
    modifier: Modifier = Modifier
) {
    val colors = todoriColors
    val shape = RoundedCornerShape(16.dp)
    val description = stringResource(R.string.stats_bar_description, label, day.minutes)
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clip(shape)
            .background(if (isSelected) colors.accentLight.copy(alpha = 0.2f) else Color.Transparent)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
            // 값 글자와 토마토가 들어갈 자리를 빼고 남는 높이에 비례해 막대를 그린다.
            val barArea = maxHeight - 46.dp
            val barHeight = max(10.dp.value, (barArea * (day.minutes.toFloat() / maxMinutes)).value).dp
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                if (isBest) MiniTomato(size = 22.dp)
                if (day.minutes > 0) {
                    Text(
                        day.minutes.toString(),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 11.sp, lineHeight = 14.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Spacer(Modifier.height(3.dp))
                Box(
                    Modifier
                        .width(22.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (day.minutes > 0) {
                                Brush.verticalGradient(listOf(colors.accentLight, colors.accentDeep))
                            } else {
                                Brush.verticalGradient(listOf(colors.accentLight.copy(alpha = 0.35f), colors.accentLight.copy(alpha = 0.35f)))
                            }
                        )
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 12.sp,
                fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Medium
            ),
            color = if (isToday) colors.accentDeep else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun TimeBuckets(buckets: Map<TimeBucket, Int>, best: TimeBucket?) {
    val colors = todoriColors
    val maxMinutes = max(1, buckets.values.maxOrNull() ?: 0)
    if (best == null) {
        Text(
            stringResource(R.string.bucket_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        return
    }
    TimeBucket.entries.forEach { bucket ->
        val minutes = buckets[bucket] ?: 0
        val isBest = bucket == best
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.width(76.dp)) {
                Text(
                    stringResource(bucketLabel(bucket)),
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 20.sp),
                    color = if (isBest) colors.accentDeep else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    stringResource(bucketRange(bucket)),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 11.sp, lineHeight = 14.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            BoxWithConstraints(Modifier.weight(1f).height(22.dp)) {
                Box(Modifier.fillMaxWidth().height(22.dp).clip(CircleShape).background(colors.accentLight.copy(alpha = 0.2f)))
                if (minutes > 0) {
                    Box(
                        Modifier
                            .width(maxWidth * (minutes.toFloat() / maxMinutes))
                            .height(22.dp)
                            .clip(CircleShape)
                            .background(
                                if (isBest) {
                                    Brush.horizontalGradient(listOf(colors.accentLight, colors.accentDeep))
                                } else {
                                    Brush.horizontalGradient(listOf(colors.accentLight.copy(alpha = 0.7f), colors.accentLight))
                                }
                            )
                    )
                }
            }
            Column(Modifier.width(64.dp), horizontalAlignment = Alignment.End) {
                Text(
                    formatDuration(minutes),
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp, lineHeight = 16.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (isBest) {
                    Text(
                        stringResource(R.string.bucket_best),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 11.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold),
                        color = colors.accentDeep
                    )
                }
            }
        }
    }
}

private fun bucketLabel(bucket: TimeBucket) = when (bucket) {
    TimeBucket.Dawn -> R.string.bucket_dawn
    TimeBucket.Morning -> R.string.bucket_morning
    TimeBucket.Afternoon -> R.string.bucket_afternoon
    TimeBucket.Evening -> R.string.bucket_evening
}

private fun bucketRange(bucket: TimeBucket) = when (bucket) {
    TimeBucket.Dawn -> R.string.bucket_range_dawn
    TimeBucket.Morning -> R.string.bucket_range_morning
    TimeBucket.Afternoon -> R.string.bucket_range_afternoon
    TimeBucket.Evening -> R.string.bucket_range_evening
}

@Composable
private fun MonthStats(month: MonthTotals, longestStreak: Int) {
    val averagePerDay = if (month.activeDays == 0) 0 else month.minutes / month.activeDays
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(formatDuration(month.minutes), stringResource(R.string.stats_month_focus), Modifier.weight(1f))
        StatCard(stringResource(R.string.days_format, longestStreak), stringResource(R.string.stats_month_streak), Modifier.weight(1f))
        StatCard(formatDuration(averagePerDay), stringResource(R.string.stats_month_avg), Modifier.weight(1f))
    }
}

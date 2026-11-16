package com.haruple97.todori.feature.harvest

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haruple97.todori.R
import com.haruple97.todori.core.common.heatLevel
import com.haruple97.todori.core.common.monthGrid
import com.haruple97.todori.core.designsystem.theme.todoriColors
import com.haruple97.todori.core.model.HeatStyle
import com.haruple97.todori.core.ui.TomatoCharacter
import com.haruple97.todori.core.ui.TomatoMood
import com.haruple97.todori.core.ui.formatDuration
import com.haruple97.todori.feature.record.component.heatColor
import java.time.format.DateTimeFormatter

/** 월말에 공유하는 "이달의 수확" 카드. 5:7 비율이며 6주짜리 달력이 들어가는 달도 통계 줄과 겹치지 않는 높이다. */
@Composable
fun HarvestCard(harvest: Harvest, style: HeatStyle, modifier: Modifier = Modifier) {
    val colors = todoriColors
    val locale = LocalConfiguration.current.locales[0]
    val monthName = remember(harvest.month, locale) { harvest.month.format(DateTimeFormatter.ofPattern("MMMM", locale)) }
    val weeks = remember(harvest.month) { monthGrid(harvest.month) }
    val message = stringResource(
        when (harvestTier(harvest.tomatoes)) {
            0 -> R.string.harvest_msg_0
            1 -> R.string.harvest_msg_1
            2 -> R.string.harvest_msg_2
            else -> R.string.harvest_msg_3
        }
    )
    val onCard = MaterialTheme.colorScheme.onBackground

    BoxWithConstraints(modifier.fillMaxWidth()) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // 5:7 이상으로, 6주짜리 달력이 들어가는 달은 내용만큼 더 길어진다(통계 줄이 격자에 붙지 않게).
            .defaultMinSize(minHeight = maxWidth * 7f / 5f)
            .clip(RoundedCornerShape(32.dp))
            .background(Brush.verticalGradient(listOf(colors.celebrate.top, colors.celebrate.bottom)))
            .padding(horizontal = 24.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.harvest_title, monthName),
            style = MaterialTheme.typography.headlineSmall,
            color = onCard
        )
        Spacer(Modifier.height(6.dp))
        TomatoCharacter(
            mood = TomatoMood.Happy,
            ripeness = (harvestTier(harvest.tomatoes) + 1) / 4f,
            modifier = Modifier.size(68.dp)
        )
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = harvest.tomatoes.toString(),
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = colors.accentDeep
            )
            Text(
                text = stringResource(R.string.harvest_unit),
                style = MaterialTheme.typography.titleMedium,
                color = onCard,
                modifier = Modifier.padding(start = 6.dp, bottom = 10.dp)
            )
        }
        Text(message, style = MaterialTheme.typography.bodyLarge, color = onCard.copy(alpha = 0.75f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(14.dp))
        MonthHeat(harvest, weeks, style)
        Spacer(Modifier.height(20.dp))
        Spacer(Modifier.weight(1f))
        // 격자와 통계가 한 덩어리로 보이지 않도록 통계는 반투명 알약 패널에 따로 담는다.
        Row(
            Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Stat(formatDuration(harvest.minutes), stringResource(R.string.stat_focus_time))
            Stat(stringResource(R.string.days_format, harvest.activeDays), stringResource(R.string.harvest_active_days))
            Stat(stringResource(R.string.days_format, harvest.bestStreak), stringResource(R.string.harvest_best_streak))
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.labelLarge,
            color = onCard.copy(alpha = 0.45f)
        )
    }
    }
}

@Composable
private fun MonthHeat(harvest: Harvest, weeks: List<List<java.time.LocalDate?>>, style: HeatStyle) {
    val empty = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        weeks.forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { date ->
                    val level = date?.let { heatLevel(harvest.dayMinutes[it] ?: 0) } ?: 0
                    Box(
                        Modifier
                            .size(24.dp)
                            .background(
                                when {
                                    date == null -> androidx.compose.ui.graphics.Color.Transparent
                                    level == 0 -> empty
                                    else -> heatColor(style, level)
                                },
                                RoundedCornerShape(7.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp), color = MaterialTheme.colorScheme.onBackground)
        Text(label, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 11.sp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
    }
}

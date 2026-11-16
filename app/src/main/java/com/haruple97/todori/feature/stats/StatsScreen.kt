package com.haruple97.todori.feature.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.haruple97.todori.R
import com.haruple97.todori.core.common.weekOf
import com.haruple97.todori.core.designsystem.BackgroundTone
import com.haruple97.todori.core.designsystem.TodoriBackground
import com.haruple97.todori.core.designsystem.theme.todoriColors
import com.haruple97.todori.core.ui.FloatingDecor
import com.haruple97.todori.core.ui.GlassIconButton
import com.haruple97.todori.core.ui.JellyButton
import com.haruple97.todori.core.ui.PremiumGateSheet
import com.haruple97.todori.core.ui.SoftCard
import com.haruple97.todori.core.ui.icon.TodoriIcons
import com.haruple97.todori.feature.stats.component.InsightRow
import com.haruple97.todori.feature.stats.component.PremiumStatsBlock
import com.haruple97.todori.feature.stats.component.WeekSummaryRow
import java.time.LocalDate

private val MAX_CONTENT_WIDTH = 520.dp

@Composable
fun StatsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    var showGate by rememberSaveable { mutableStateOf(false) }
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val twoPane = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
        !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND)

    val premiumArea: @Composable () -> Unit = {
        PremiumArea(
            ui = ui,
            onUnlock = { showGate = true },
            onPreviousWeek = viewModel::previousWeek,
            onNextWeek = viewModel::nextWeek
        )
    }

    TodoriBackground(BackgroundTone.Calm, modifier.fillMaxSize()) {
        FloatingDecor()
        Box(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            if (twoPane) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(
                        Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TopBar(onBack)
                        InsightRow(ui.insight)
                        WeekSummaryRow(ui.currentWeek.ifEmpty { emptyWeek(ui.today) })
                    }
                    Column(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
                        premiumArea()
                        Spacer(Modifier.height(16.dp))
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .widthIn(max = MAX_CONTENT_WIDTH)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TopBar(onBack)
                    InsightRow(ui.insight)
                    WeekSummaryRow(ui.currentWeek.ifEmpty { emptyWeek(ui.today) })
                    premiumArea()
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (showGate) PremiumGateSheet(onDismiss = { showGate = false })
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        GlassIconButton(
            icon = TodoriIcons.ChevronLeft,
            contentDescription = stringResource(R.string.record_back),
            onClick = onBack,
            size = 48.dp
        )
        Spacer(Modifier.width(14.dp))
        Text(
            stringResource(R.string.stats_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/** 프리미엄이면 실제 통계, 아니면 샘플을 흐리게 그린 위에 잠금 카드를 올린다. */
@Composable
private fun PremiumArea(
    ui: StatsUiState,
    onUnlock: () -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    if (ui.isPremium) {
        PremiumStatsBlock(
            today = ui.today,
            chartWeek = ui.chartWeek.ifEmpty { emptyWeek(ui.today) },
            chartCanGoNext = ui.chartCanGoNext,
            buckets = ui.buckets,
            bestBucket = ui.bestBucket,
            month = ui.month,
            longestStreak = ui.longestStreak,
            yearMinutes = ui.yearMinutes,
            heatStyle = ui.heatStyle,
            interactive = true,
            onPreviousWeek = onPreviousWeek,
            onNextWeek = onNextWeek
        )
        return
    }
    Box(Modifier.fillMaxWidth()) {
        PremiumStatsBlock(
            today = ui.today,
            chartWeek = remember(ui.today) { sampleWeek(ui.today) },
            chartCanGoNext = false,
            buckets = SampleBuckets,
            bestBucket = TimeBucket.Afternoon,
            month = SampleMonth,
            longestStreak = 5,
            yearMinutes = remember(ui.today) { sampleYear(ui.today) },
            heatStyle = ui.heatStyle,
            interactive = false,
            onPreviousWeek = {},
            onNextWeek = {},
            modifier = Modifier.alpha(0.28f).clearAndSetSemantics { }
        )
        LockCard(onUnlock, Modifier.align(Alignment.Center).padding(horizontal = 12.dp))
    }
}

@Composable
private fun LockCard(onUnlock: () -> Unit, modifier: Modifier = Modifier) {
    val colors = todoriColors
    SoftCard(modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(52.dp).background(colors.accentLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(TodoriIcons.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(
                stringResource(R.string.stats_locked_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.stats_locked_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            JellyButton(
                text = stringResource(R.string.customize_unlock),
                icon = TodoriIcons.Lock,
                onClick = onUnlock,
                height = 48.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private fun emptyWeek(today: LocalDate): List<DayStat> =
    weekOf(today).map { DayStat(it, minutes = 0, count = 0) }

private fun sampleWeek(today: LocalDate): List<DayStat> {
    val minutes = listOf(0, 50, 75, 25, 100, 60, 0)
    val counts = listOf(0, 2, 3, 1, 4, 2, 0)
    return weekOf(today).mapIndexed { index, date -> DayStat(date, minutes[index], counts[index]) }
}

private val SampleBuckets = mapOf(
    TimeBucket.Dawn to 0,
    TimeBucket.Morning to 125,
    TimeBucket.Afternoon to 175,
    TimeBucket.Evening to 60
)

private val SampleMonth = MonthTotals(minutes = 500, count = 14, activeDays = 8)

/** 잠긴 맛보기용 연간 샘플. 날짜마다 정해진 값이라 다시 그려도 바뀌지 않는다. */
private fun sampleYear(today: LocalDate): Map<LocalDate, Int> =
    (0 until 364).mapNotNull { back ->
        val day = today.minusDays(back.toLong())
        val seed = (day.toEpochDay() * 2654435761L).mod(11L).toInt()
        if (seed < 4) null else day to listOf(20, 45, 75, 100, 130, 160, 200, 240)[seed - 4]
    }.toMap()

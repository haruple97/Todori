package com.haruple97.todori.feature.record

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.haruple97.todori.R
import com.haruple97.todori.core.designsystem.BackgroundTone
import com.haruple97.todori.core.designsystem.TodoriBackground
import com.haruple97.todori.core.ui.FloatingDecor
import com.haruple97.todori.core.ui.GlassIconButton
import com.haruple97.todori.core.ui.icon.TodoriIcons
import com.haruple97.todori.feature.record.component.DayDetail
import com.haruple97.todori.feature.record.component.EmptyRecordHint
import com.haruple97.todori.feature.record.component.MonthCalendar
import com.haruple97.todori.feature.record.component.RecordSummary
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val MAX_CONTENT_WIDTH = 480.dp
private val TOP_BAR_HEIGHT = 48.dp
private val MONTH_HEADER_HEIGHT = 44.dp
private val WEEKDAY_ROW_HEIGHT = 24.dp

private class RecordActions(
    val onBack: () -> Unit,
    val onPreviousMonth: () -> Unit,
    val onNextMonth: () -> Unit,
    val onSelect: (java.time.LocalDate) -> Unit
)

@Composable
fun RecordScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val twoPane = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
        !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND)
    val actions = RecordActions(onBack, viewModel::previousMonth, viewModel::nextMonth, viewModel::select)

    TodoriBackground(BackgroundTone.Calm, modifier.fillMaxSize()) {
        FloatingDecor()
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            if (twoPane) {
                TwoPane(ui, actions, maxWidth, maxHeight)
            } else {
                SinglePane(ui, actions, minOf(maxWidth, MAX_CONTENT_WIDTH))
            }
        }
    }
}

@Composable
private fun BoxScope.SinglePane(ui: RecordUiState, actions: RecordActions, contentWidth: Dp) {
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .widthIn(max = MAX_CONTENT_WIDTH)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TopBar(actions.onBack)
        RecordSummary(ui.totalCount, ui.totalMinutes, ui.streak)
        CalendarSection(ui, actions, cellSize = contentWidth / 7, modifier = Modifier.align(Alignment.CenterHorizontally))
        DetailSection(ui)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TwoPane(ui: RecordUiState, actions: RecordActions, width: Dp, height: Dp) {
    val rows = remember(ui.month) { monthGrid(ui.month).size }
    // 달력이 세로 공간을 최대한 쓰도록 상단 바는 오른쪽 열로 보낸다.
    val leftWidth = (width - 40.dp - 24.dp) / 2
    val fixedHeight = MONTH_HEADER_HEIGHT + WEEKDAY_ROW_HEIGHT + 12.dp
    val cellSize = minOf(leftWidth / 7, (height - 32.dp - fixedHeight) / rows)

    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
            CalendarSection(ui, actions, cellSize)
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TopBar(actions.onBack)
            RecordSummary(ui.totalCount, ui.totalMinutes, ui.streak)
            DetailSection(ui)
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        GlassIconButton(
            icon = TodoriIcons.ChevronLeft,
            contentDescription = stringResource(R.string.record_back),
            onClick = onBack,
            size = TOP_BAR_HEIGHT
        )
        Spacer(Modifier.width(14.dp))
        Text(
            text = stringResource(R.string.record_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun CalendarSection(ui: RecordUiState, actions: RecordActions, cellSize: Dp, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val monthPattern = stringResource(R.string.month_pattern)
    val monthTitle = remember(ui.month, monthPattern, locale) {
        ui.month.format(DateTimeFormatter.ofPattern(monthPattern, locale))
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.width(cellSize * 7).padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIconButton(
                icon = TodoriIcons.ChevronLeft,
                contentDescription = stringResource(R.string.record_prev_month),
                onClick = actions.onPreviousMonth,
                size = 40.dp
            )
            Text(monthTitle, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
            GlassIconButton(
                icon = TodoriIcons.ChevronRight,
                contentDescription = stringResource(R.string.record_next_month),
                onClick = actions.onNextMonth,
                size = 40.dp,
                enabled = ui.canGoNext
            )
        }
        MonthCalendar(
            month = ui.month,
            today = ui.today,
            selected = ui.selected,
            days = ui.days,
            cellSize = cellSize,
            onSelect = actions.onSelect
        )
    }
}

@Composable
private fun DetailSection(ui: RecordUiState) {
    if (ui.totalCount == 0) {
        EmptyRecordHint()
    } else {
        DayDetail(date = ui.selected, sessions = ui.selectedSessions, zone = ZoneId.systemDefault())
    }
}

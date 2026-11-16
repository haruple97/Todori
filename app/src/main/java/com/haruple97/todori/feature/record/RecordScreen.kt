package com.haruple97.todori.feature.record

import com.haruple97.todori.core.common.monthGrid
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.haruple97.todori.R
import com.haruple97.todori.core.designsystem.BackgroundTone
import com.haruple97.todori.core.designsystem.TodoriBackground
import com.haruple97.todori.core.designsystem.theme.todoriColors
import com.haruple97.todori.core.model.SessionRecord
import com.haruple97.todori.core.ui.FloatingDecor
import com.haruple97.todori.core.ui.GlassIconButton
import com.haruple97.todori.core.ui.icon.TodoriIcons
import com.haruple97.todori.feature.record.component.CalendarModeToggle
import com.haruple97.todori.feature.record.component.DayDetail
import com.haruple97.todori.feature.record.component.HeatLegend
import com.haruple97.todori.feature.record.component.EmptyRecordHint
import com.haruple97.todori.feature.record.component.MonthCalendar
import com.haruple97.todori.feature.record.component.RecordSummary
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

private val MAX_CONTENT_WIDTH = 480.dp
private val TOP_BAR_HEIGHT = 48.dp
private val MONTH_HEADER_HEIGHT = 44.dp
private val WEEKDAY_ROW_HEIGHT = 24.dp

private class RecordActions(
    val onBack: () -> Unit,
    val onOpenStats: () -> Unit,
    val onPreviousMonth: () -> Unit,
    val onNextMonth: () -> Unit,
    val onSelect: (java.time.LocalDate) -> Unit,
    val onDelete: (SessionRecord) -> Unit,
    val onDeleteDay: () -> Unit,
    val onHeatMode: (Boolean) -> Unit,
    val onHeatStyle: (com.haruple97.todori.core.model.HeatStyle) -> Unit
)

@Composable
fun RecordScreen(
    onBack: () -> Unit,
    onOpenStats: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RecordViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val twoPane = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
        !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND)
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmDeleteDay by rememberSaveable { mutableStateOf(false) }
    val actions = RecordActions(
        onBack = onBack,
        onOpenStats = onOpenStats,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onSelect = viewModel::select,
        onDelete = { viewModel.delete(listOf(it)) },
        onDeleteDay = { confirmDeleteDay = true },
        onHeatMode = viewModel::setHeatMode,
        onHeatStyle = viewModel::setHeatStyle
    )

    // 삭제가 끝나면 되돌리기 스낵바를 띄운다. 확인 창 대신 되돌리기로 실수를 막는다.
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is RecordEvent.Deleted) {
                snackbarHostState.currentSnackbarData?.dismiss()
                launch {
                    val message = if (event.sessions.size == 1) {
                        context.getString(R.string.record_deleted_one)
                    } else {
                        context.getString(R.string.record_deleted_many, event.sessions.size)
                    }
                    val result = snackbarHostState.showSnackbar(
                        message = message,
                        actionLabel = context.getString(R.string.record_undo),
                        duration = SnackbarDuration.Long
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete(event.sessions)
                }
            }
        }
    }

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
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter).widthIn(max = MAX_CONTENT_WIDTH)
            ) { data -> CuteSnackbar(data) }
        }
    }

    if (confirmDeleteDay) {
        val sessions = ui.selectedSessions
        AlertDialog(
            onDismissRequest = { confirmDeleteDay = false },
            title = { Text(stringResource(R.string.record_delete_day_title)) },
            text = { Text(stringResource(R.string.record_delete_day_message, sessions.size)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDeleteDay = false
                    viewModel.delete(sessions)
                }) { Text(stringResource(R.string.record_delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteDay = false }) { Text(stringResource(R.string.dialog_cancel)) }
            }
        )
    }
}

@Composable
private fun CuteSnackbar(data: SnackbarData) {
    // 기본 Snackbar는 바깥 여백 때문에 테두리가 이중으로 보여, 본체에만 테두리를 두도록 직접 그린다.
    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, todoriColors.accentLight),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 18.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.visuals.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            data.visuals.actionLabel?.let { label ->
                TextButton(onClick = { data.performAction() }) {
                    Text(label, style = MaterialTheme.typography.labelLarge, color = todoriColors.accentDeep)
                }
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
        TopBar(actions.onBack, actions.onOpenStats)
        RecordSummary(ui.totalCount, ui.totalMinutes, ui.streak)
        CalendarSection(ui, actions, cellSize = contentWidth / 7, modifier = Modifier.align(Alignment.CenterHorizontally))
        DetailSection(ui, actions)
        // 하단 스낵바에 마지막 줄이 가려지지 않도록 여유를 둔다.
        Spacer(Modifier.height(72.dp))
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
            TopBar(actions.onBack, actions.onOpenStats)
            RecordSummary(ui.totalCount, ui.totalMinutes, ui.streak)
            DetailSection(ui, actions)
            Spacer(Modifier.height(72.dp))
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit, onOpenStats: () -> Unit, modifier: Modifier = Modifier) {
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
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        GlassIconButton(
            icon = TodoriIcons.Chart,
            contentDescription = stringResource(R.string.stats_open),
            onClick = onOpenStats,
            size = TOP_BAR_HEIGHT
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
        CalendarModeToggle(
            heatMode = ui.calendar.heatMode,
            onChange = actions.onHeatMode,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        MonthCalendar(
            heatStyle = if (ui.calendar.heatMode) ui.calendar.style else null,
            month = ui.month,
            today = ui.today,
            selected = ui.selected,
            days = ui.days,
            cellSize = cellSize,
            onSelect = actions.onSelect
        )
        if (ui.calendar.heatMode) {
            HeatLegend(ui.calendar.style, actions.onHeatStyle, width = cellSize * 7)
        }
    }
}

@Composable
private fun DetailSection(ui: RecordUiState, actions: RecordActions) {
    if (ui.totalCount == 0) {
        EmptyRecordHint()
    } else {
        DayDetail(
            date = ui.selected,
            sessions = ui.selectedSessions,
            showRipeness = ui.calendar.heatMode,
            zone = ZoneId.systemDefault(),
            onDelete = actions.onDelete,
            onDeleteAll = actions.onDeleteDay
        )
    }
}

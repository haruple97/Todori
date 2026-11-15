package com.haruple97.todori.feature.timer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.haruple97.todori.R
import com.haruple97.todori.core.common.formatRemainingTime
import com.haruple97.todori.core.designsystem.BackgroundTone
import com.haruple97.todori.core.designsystem.TodoriBackground
import com.haruple97.todori.core.model.TimerStatus
import com.haruple97.todori.core.ui.ConfettiBurst
import com.haruple97.todori.core.ui.FloatingDecor
import com.haruple97.todori.core.ui.SoftButton
import com.haruple97.todori.core.ui.TomatoCharacter
import com.haruple97.todori.core.ui.TomatoMood
import com.haruple97.todori.feature.timer.component.TomatoDial
import kotlinx.coroutines.delay

private class TimerActions(
    val onMinutesChange: (Int) -> Unit,
    val onStart: () -> Unit,
    val onPause: () -> Unit,
    val onResume: () -> Unit,
    val onGiveUp: () -> Unit,
    val onOneMore: () -> Unit,
    val onStop: () -> Unit
)

@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var showGiveUp by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(ui.status) {
        if (ui.status == TimerStatus.Running) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    viewModel.refresh()
                    delay(TICK_MILLIS)
                }
            }
        }
        if (ui.status == TimerStatus.Finished) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.start()
    }
    fun requestStart() {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) else viewModel.start()
    }

    // 가로가 충분히 넓고 세로가 넉넉하지 않으면(가로 모드 폰, 펼친 폴더블, 가로 태블릿) 좌우 2단으로 배치한다.
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val twoPane = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
        !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND)

    val actions = TimerActions(
        onMinutesChange = viewModel::setFocusMinutes,
        onStart = ::requestStart,
        onPause = viewModel::pause,
        onResume = viewModel::resume,
        onGiveUp = { showGiveUp = true },
        onOneMore = viewModel::start,
        onStop = viewModel::stop
    )

    val tone = when (ui.status) {
        TimerStatus.Idle -> BackgroundTone.Calm
        TimerStatus.Running, TimerStatus.Paused -> BackgroundTone.Focus
        TimerStatus.Finished -> BackgroundTone.Celebrate
    }
    TodoriBackground(tone, modifier.fillMaxSize()) {
        FloatingDecor()
        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp)
        ) {
            if (twoPane) {
                TwoPaneContent(ui, actions, maxWidth, maxHeight)
            } else {
                SinglePaneContent(ui, actions, maxHeight)
            }
        }
        ConfettiBurst(trigger = ui.status == TimerStatus.Finished)
    }

    if (showGiveUp) {
        AlertDialog(
            onDismissRequest = { showGiveUp = false },
            title = { Text(stringResource(R.string.give_up_title)) },
            text = { Text(stringResource(R.string.give_up_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showGiveUp = false
                    viewModel.stop()
                }) { Text(stringResource(R.string.give_up_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showGiveUp = false }) { Text(stringResource(R.string.give_up_cancel)) }
            }
        )
    }
}

@Composable
private fun BoxScope.SinglePaneContent(ui: TimerUiState, actions: TimerActions, height: Dp) {
    Column(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxHeight()
            .widthIn(max = MAX_CONTENT_WIDTH),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Header(ui)
        Spacer(Modifier.weight(1f))
        TomatoStage(ui, stageSize = minOf(STAGE_MAX, height * 0.38f), onMinutesChange = actions.onMinutesChange)
        Spacer(Modifier.height(8.dp))
        TimeAndHint(ui)
        Spacer(Modifier.weight(1f))
        Controls(ui.status, actions, horizontal = false)
    }
}

@Composable
private fun TwoPaneContent(ui: TimerUiState, actions: TimerActions, width: Dp, height: Dp) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
            TomatoStage(
                ui = ui,
                stageSize = minOf(STAGE_MAX_WIDE, height, width / 2 - 12.dp),
                onMinutesChange = actions.onMinutesChange
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Header(ui)
            Spacer(Modifier.height(16.dp))
            TimeAndHint(ui)
            Spacer(Modifier.height(24.dp))
            Controls(ui.status, actions, horizontal = true)
        }
    }
}

@Composable
private fun Header(ui: TimerUiState) {
    Text(
        stringResource(R.string.timer_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = stringResource(statusTextRes(ui)),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun TimeAndHint(ui: TimerUiState) {
    Text(
        text = timeText(ui),
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    if (ui.status == TimerStatus.Idle) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.dial_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TomatoStage(ui: TimerUiState, stageSize: Dp, onMinutesChange: (Int) -> Unit) {
    Box(Modifier.size(stageSize), contentAlignment = Alignment.Center) {
        if (ui.status == TimerStatus.Idle) {
            TomatoDial(
                minutes = ui.focusMinutes,
                onMinutesChange = onMinutesChange,
                modifier = Modifier.size(stageSize * DIAL_RATIO)
            )
        } else {
            ProgressRing(ui.progress, Modifier.size(stageSize))
            TomatoCharacter(
                mood = moodOf(ui.status),
                ripeness = if (ui.status == TimerStatus.Finished) 1f else ui.progress,
                modifier = Modifier.size(stageSize * TOMATO_RATIO)
            )
        }
    }
}

@Composable
private fun ProgressRing(progress: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(progress, tween(TICK_MILLIS.toInt(), easing = LinearEasing), label = "ring")
    val track = MaterialTheme.colorScheme.surfaceVariant
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier) {
        val stroke = 14.dp.toPx()
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(stroke / 2, stroke / 2)
        drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        if (animated > 0f) {
            drawArc(color, -90f, 360f * animated, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        }
    }
}

/** 버튼 두 개짜리 상태는 [horizontal]이면 나란히, 아니면 세로로 쌓는다. */
@Composable
private fun Controls(status: TimerStatus, actions: TimerActions, horizontal: Boolean) {
    val primaryText: String
    val primaryAction: () -> Unit
    var secondaryText: String? = null
    var secondaryAction: () -> Unit = {}
    when (status) {
        TimerStatus.Idle -> {
            primaryText = stringResource(R.string.btn_start)
            primaryAction = actions.onStart
        }
        TimerStatus.Running -> {
            primaryText = stringResource(R.string.btn_pause)
            primaryAction = actions.onPause
            secondaryText = stringResource(R.string.btn_give_up)
            secondaryAction = actions.onGiveUp
        }
        TimerStatus.Paused -> {
            primaryText = stringResource(R.string.btn_resume)
            primaryAction = actions.onResume
            secondaryText = stringResource(R.string.btn_give_up)
            secondaryAction = actions.onGiveUp
        }
        TimerStatus.Finished -> {
            primaryText = stringResource(R.string.btn_one_more)
            primaryAction = actions.onOneMore
            secondaryText = stringResource(R.string.btn_stop)
            secondaryAction = actions.onStop
        }
    }
    if (horizontal && secondaryText != null) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SoftButton(secondaryText, secondaryAction, Modifier.weight(1f), primary = false)
            SoftButton(primaryText, primaryAction, Modifier.weight(1f))
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SoftButton(primaryText, primaryAction, Modifier.fillMaxWidth())
            if (secondaryText != null) {
                SoftButton(secondaryText, secondaryAction, Modifier.fillMaxWidth(), primary = false)
            }
        }
    }
}

private fun moodOf(status: TimerStatus): TomatoMood = when (status) {
    TimerStatus.Idle -> TomatoMood.Idle
    TimerStatus.Paused -> TomatoMood.Sleepy
    TimerStatus.Finished -> TomatoMood.Happy
    TimerStatus.Running -> TomatoMood.Focus
}

private fun statusTextRes(ui: TimerUiState): Int = when (ui.status) {
    TimerStatus.Idle -> R.string.status_idle
    TimerStatus.Paused -> R.string.status_paused
    TimerStatus.Running -> R.string.status_focus
    TimerStatus.Finished -> R.string.status_done
}

@Composable
private fun timeText(ui: TimerUiState): String = when (ui.status) {
    TimerStatus.Idle -> stringResource(R.string.minutes_format, ui.focusMinutes)
    TimerStatus.Finished -> stringResource(R.string.done_label)
    else -> formatRemainingTime(ui.remainingMillis)
}

private val STAGE_MAX = 290.dp
private val STAGE_MAX_WIDE = 360.dp
private val MAX_CONTENT_WIDTH = 480.dp
private const val DIAL_RATIO = 0.76f
private const val TOMATO_RATIO = 0.72f
private const val TICK_MILLIS = 250L

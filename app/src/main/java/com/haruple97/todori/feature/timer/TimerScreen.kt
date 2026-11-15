package com.haruple97.todori.feature.timer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.haruple97.todori.R
import com.haruple97.todori.core.common.formatRemainingTime
import com.haruple97.todori.core.designsystem.theme.MintCream
import com.haruple97.todori.core.designsystem.theme.MintCreamDark
import com.haruple97.todori.core.model.TimerPhase
import com.haruple97.todori.core.model.TimerStatus
import com.haruple97.todori.core.ui.ConfettiBurst
import com.haruple97.todori.core.ui.PresetChip
import com.haruple97.todori.core.ui.SoftButton
import com.haruple97.todori.core.ui.TomatoCharacter
import com.haruple97.todori.core.ui.TomatoMood
import com.haruple97.todori.feature.timer.component.BreakStepper
import com.haruple97.todori.feature.timer.component.TomatoDial
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var showGiveUp by remember { mutableStateOf(false) }

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
        viewModel.startFocus()
    }
    fun requestStart() {
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) else viewModel.startFocus()
    }

    val isBreak = ui.phase == TimerPhase.Break && ui.status != TimerStatus.Idle
    val restBackground = if (isSystemInDarkTheme()) MintCreamDark else MintCream
    val background by animateColorAsState(
        targetValue = if (isBreak) restBackground else MaterialTheme.colorScheme.background,
        animationSpec = tween(600),
        label = "background"
    )

    Box(modifier.fillMaxSize().background(background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
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

            Spacer(Modifier.weight(1f))
            TomatoStage(ui, onMinutesChange = viewModel::setFocusMinutes)
            Spacer(Modifier.height(8.dp))
            Text(
                text = timeText(ui),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (ui.status == TimerStatus.Idle) {
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PresetMinutes.forEach { minutes ->
                        PresetChip(
                            label = stringResource(R.string.minutes_chip_format, minutes),
                            selected = ui.focusMinutes == minutes,
                            onClick = { viewModel.setFocusMinutes(minutes) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                BreakStepper(minutes = ui.breakMinutes, onMinutesChange = viewModel::setBreakMinutes)
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.dial_hint),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.weight(1f))

            Controls(
                ui = ui,
                onStart = ::requestStart,
                onPause = viewModel::pause,
                onResume = viewModel::resume,
                onGiveUp = { showGiveUp = true },
                onStartBreak = viewModel::startBreak,
                onOneMore = viewModel::startFocus,
                onStop = viewModel::stop
            )
            Spacer(Modifier.height(24.dp))
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
private fun TomatoStage(ui: TimerUiState, onMinutesChange: (Int) -> Unit) {
    Box(Modifier.size(STAGE_SIZE), contentAlignment = Alignment.Center) {
        if (ui.status == TimerStatus.Idle) {
            TomatoDial(
                minutes = ui.focusMinutes,
                onMinutesChange = onMinutesChange,
                modifier = Modifier.size(220.dp)
            )
        } else {
            ProgressRing(ui.progress, Modifier.size(STAGE_SIZE))
            TomatoCharacter(
                mood = moodOf(ui),
                ripeness = ripenessOf(ui),
                modifier = Modifier.size(210.dp)
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

@Composable
private fun Controls(
    ui: TimerUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onGiveUp: () -> Unit,
    onStartBreak: () -> Unit,
    onOneMore: () -> Unit,
    onStop: () -> Unit
) {
    val wide = Modifier.fillMaxWidth()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        when (ui.status) {
            TimerStatus.Idle -> SoftButton(stringResource(R.string.btn_start), onStart, wide)
            TimerStatus.Running -> {
                SoftButton(stringResource(R.string.btn_pause), onPause, wide)
                SoftButton(stringResource(R.string.btn_give_up), onGiveUp, wide, primary = false)
            }
            TimerStatus.Paused -> {
                SoftButton(stringResource(R.string.btn_resume), onResume, wide)
                SoftButton(stringResource(R.string.btn_give_up), onGiveUp, wide, primary = false)
            }
            TimerStatus.Finished -> {
                if (ui.phase == TimerPhase.Focus) {
                    SoftButton(stringResource(R.string.btn_start_break), onStartBreak, wide)
                } else {
                    SoftButton(stringResource(R.string.btn_one_more), onOneMore, wide)
                }
                SoftButton(stringResource(R.string.btn_stop), onStop, wide, primary = false)
            }
        }
    }
}

private fun moodOf(ui: TimerUiState): TomatoMood = when (ui.status) {
    TimerStatus.Idle -> TomatoMood.Idle
    TimerStatus.Paused -> TomatoMood.Sleepy
    TimerStatus.Finished -> TomatoMood.Happy
    TimerStatus.Running -> if (ui.phase == TimerPhase.Focus) TomatoMood.Focus else TomatoMood.Relaxed
}

/** 집중 중에는 시간이 갈수록 익고, 그 외에는 잘 익은 상태. */
private fun ripenessOf(ui: TimerUiState): Float =
    if (ui.phase == TimerPhase.Focus && ui.status != TimerStatus.Finished) ui.progress else 1f

private fun statusTextRes(ui: TimerUiState): Int = when (ui.status) {
    TimerStatus.Idle -> R.string.status_idle
    TimerStatus.Paused -> R.string.status_paused
    TimerStatus.Running -> if (ui.phase == TimerPhase.Focus) R.string.status_focus else R.string.status_break
    TimerStatus.Finished -> if (ui.phase == TimerPhase.Focus) R.string.status_done_focus else R.string.status_done_break
}

@Composable
private fun timeText(ui: TimerUiState): String = when (ui.status) {
    TimerStatus.Idle -> stringResource(R.string.minutes_format, ui.focusMinutes)
    TimerStatus.Finished -> stringResource(R.string.done_label)
    else -> formatRemainingTime(ui.remainingMillis)
}

private val STAGE_SIZE = 290.dp
private const val TICK_MILLIS = 250L

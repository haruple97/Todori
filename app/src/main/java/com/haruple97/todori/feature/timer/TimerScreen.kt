package com.haruple97.todori.feature.timer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.ArrayRes
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import com.haruple97.todori.core.ui.PremiumGateSheet
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringArrayResource
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
import com.haruple97.todori.core.review.findActivity
import com.haruple97.todori.core.review.requestInAppReview
import com.haruple97.todori.core.designsystem.theme.todoriColors
import com.haruple97.todori.core.model.TimerStatus
import com.haruple97.todori.core.ui.ConfettiBurst
import com.haruple97.todori.core.ui.FloatingDecor
import com.haruple97.todori.core.ui.GlassIconButton
import com.haruple97.todori.core.ui.JellyButton
import com.haruple97.todori.core.ui.SpeechBubble
import com.haruple97.todori.core.ui.TomatoCharacter
import com.haruple97.todori.core.ui.TomatoMood
import com.haruple97.todori.core.ui.drawHeart
import com.haruple97.todori.core.ui.icon.TodoriIcons
import com.haruple97.todori.feature.timer.component.DialRail
import com.haruple97.todori.feature.timer.component.GreetingHeader
import com.haruple97.todori.feature.timer.component.TimeSheet
import com.haruple97.todori.feature.timer.component.TomatoDial
import kotlin.math.PI
import kotlin.math.cos
import java.time.LocalDateTime
import kotlin.math.sin
import kotlinx.coroutines.delay

private class TimerActions(
    val onMinutesChange: (Int) -> Unit,
    val onStart: () -> Unit,
    val onPause: () -> Unit,
    val onResume: () -> Unit,
    val onGiveUp: () -> Unit,
    val onOneMore: () -> Unit,
    val onStop: () -> Unit,
    val onCustomTime: () -> Unit
)

@Composable
fun TimerScreen(
    onOpenRecord: () -> Unit,
    onOpenCustomize: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    var showGiveUp by rememberSaveable { mutableStateOf(false) }
    var showTimeSheet by rememberSaveable { mutableStateOf(false) }
    var showPremiumGate by rememberSaveable { mutableStateOf(false) }

    // 타이머가 돌아가는 동안에는 화면이 꺼지지 않게 한다. 일시정지·완료·화면 이탈 시 즉시 해제한다.
    val view = LocalView.current
    val keepScreenOn = ui.status == TimerStatus.Running
    DisposableEffect(keepScreenOn) {
        view.keepScreenOn = keepScreenOn
        onDispose { view.keepScreenOn = false }
    }

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

    // 첫 집중을 끝낸 직후 한 번만 인앱 리뷰를 요청한다. 완료 기록이 저장돼 totalCount가 1이 된 뒤에 동작하고,
    // 파티클·완료 연출을 먼저 볼 수 있게 잠시 기다린다. 요청이 성공한 경우에만 '요청함'으로 표시한다.
    LaunchedEffect(ui.status, ui.totalCount) {
        if (ui.status != TimerStatus.Finished || ui.totalCount != 1) return@LaunchedEffect
        if (viewModel.isReviewRequested()) return@LaunchedEffect
        delay(REVIEW_DELAY_MILLIS)
        val activity = context.findActivity() ?: return@LaunchedEffect
        if (requestInAppReview(activity)) viewModel.markReviewRequested()
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
        onStop = viewModel::stop,
        // 프리미엄이면 1분 단위 설정 시트, 아니면 프리미엄 안내 시트
        onCustomTime = { if (ui.isPremium) showTimeSheet = true else showPremiumGate = true }
    )

    val bubble = bubbleText(ui)
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
                TwoPaneContent(ui, actions, bubble, maxWidth, maxHeight)
            } else {
                SinglePaneContent(ui, actions, bubble)
            }
            Row(
                modifier = Modifier.align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassIconButton(
                    icon = TodoriIcons.Sparkles,
                    contentDescription = stringResource(R.string.customize_open),
                    onClick = onOpenCustomize,
                    size = 48.dp
                )
                GlassIconButton(
                    icon = TodoriIcons.Calendar,
                    contentDescription = stringResource(R.string.record_open),
                    onClick = onOpenRecord,
                    size = 48.dp
                )
            }
        }
        ConfettiBurst(trigger = ui.status == TimerStatus.Finished)
    }

    if (showTimeSheet) {
        TimeSheet(
            minutes = ui.focusMinutes,
            onDismiss = { showTimeSheet = false },
            onConfirm = {
                viewModel.setFocusMinutes(it)
                showTimeSheet = false
            }
        )
    }
    if (showPremiumGate) PremiumGateSheet(onDismiss = { showPremiumGate = false })

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

/**
 * 인사말(위)과 버튼(아래)은 고정하고 가운데(말풍선·토마토·시간)만 남는 높이에 맞춘다.
 * 토마토는 [MIN_STAGE]보다 작아지지 않고, 그보다 낮은 화면에서는 가운데가 스크롤된다.
 */
@Composable
private fun BoxScope.SinglePaneContent(ui: TimerUiState, actions: TimerActions, bubble: String) {
    Column(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxSize()
            .widthIn(max = MAX_CONTENT_WIDTH)
    ) {
        Spacer(Modifier.height(8.dp))
        GreetingHeader(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(end = 116.dp))
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val stageSize = (maxHeight - MIDDLE_FIXED_HEIGHT).coerceIn(MIN_STAGE, STAGE_MAX)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = maxHeight)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(12.dp))
                SpeechBubble(bubble)
                Spacer(Modifier.height(14.dp))
                TomatoStage(ui, stageSize = stageSize, onMinutesChange = actions.onMinutesChange)
                Spacer(Modifier.height(8.dp))
                TimeAndHint(ui, actions.onCustomTime)
                Spacer(Modifier.height(12.dp))
            }
        }
        Spacer(Modifier.height(12.dp))
        Controls(ui.status, actions)
    }
}

@Composable
private fun TwoPaneContent(ui: TimerUiState, actions: TimerActions, bubble: String, width: Dp, height: Dp) {
    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(
            modifier = Modifier.weight(1f).fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SpeechBubble(bubble)
            Spacer(Modifier.height(14.dp))
            TomatoStage(
                ui = ui,
                stageSize = minOf(STAGE_MAX_WIDE, height - BUBBLE_RESERVED_HEIGHT, width / 2 - 12.dp),
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
            GreetingHeader(horizontalAlignment = Alignment.CenterHorizontally)
            Spacer(Modifier.height(16.dp))
            TimeAndHint(ui, actions.onCustomTime)
            Spacer(Modifier.height(24.dp))
            Controls(ui.status, actions)
        }
    }
}

@Composable
private fun TimeAndHint(ui: TimerUiState, onCustomTime: () -> Unit) {
    Text(
        text = timeText(ui),
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    if (ui.status == TimerStatus.Idle) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.dial_hint),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(10.dp))
        CustomTimeChip(locked = !ui.isPremium, onClick = onCustomTime)
    }
}

/** 1분 단위·60분 초과 설정으로 들어가는 칩. 프리미엄이 아니면 자물쇠가 붙는다. */
@Composable
private fun CustomTimeChip(locked: Boolean, onClick: () -> Unit) {
    val colors = todoriColors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
        border = BorderStroke(1.5.dp, colors.accentLight)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (locked) TodoriIcons.Lock else TodoriIcons.Sparkles,
                contentDescription = null,
                tint = colors.accentDeep,
                modifier = Modifier.size(16.dp)
            )
            Text(
                stringResource(R.string.time_custom),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp, lineHeight = 18.sp),
                color = colors.accentDeep
            )
        }
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
            DialRail(
                minutes = ui.focusMinutes,
                onMinutesChange = onMinutesChange,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .height(stageSize * RAIL_RATIO)
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

/** 테마 색 스윕 그라데이션 링. 진행 끝에 하트가 달려 따라간다. */
@Composable
private fun ProgressRing(progress: Float, modifier: Modifier = Modifier) {
    val animated by animateFloatAsState(progress, tween(TICK_MILLIS.toInt(), easing = LinearEasing), label = "ring")
    val track = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
    val colors = todoriColors
    val deep = colors.accentDeep
    val sweep = listOf(deep, colors.accentLight, colors.glow, colors.accentLight, deep)
    Canvas(modifier) {
        val stroke = 14.dp.toPx()
        val arcSize = Size(size.width - stroke, size.height - stroke)
        val topLeft = Offset(stroke / 2, stroke / 2)
        drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
        if (animated > 0f) {
            rotate(-90f) {
                drawArc(
                    // 시작·끝 색을 같게 해서 한 바퀴를 채웠을 때 이음새가 보이지 않게 한다.
                    brush = Brush.sweepGradient(sweep),
                    startAngle = 0f,
                    sweepAngle = 360f * animated,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
            }
            val radius = (size.width - stroke) / 2f
            val angle = (-90f + 360f * animated) * PI.toFloat() / 180f
            val head = Offset(center.x + radius * cos(angle), center.y + radius * sin(angle))
            drawCircle(Color.White, radius = stroke * 0.95f, center = head)
            drawHeart(head, stroke * 0.5f, deep)
        }
    }
}

/** 메인은 젤리 알약, 포기/그만하기는 옆의 작은 원형 글라스 버튼. */
@Composable
private fun Controls(status: TimerStatus, actions: TimerActions) {
    when (status) {
        TimerStatus.Idle -> JellyButton(
            text = stringResource(R.string.btn_start),
            icon = TodoriIcons.Play,
            onClick = actions.onStart,
            modifier = Modifier.fillMaxWidth()
        )
        TimerStatus.Running -> ControlRow(
            secondaryDescription = stringResource(R.string.btn_give_up),
            onSecondary = actions.onGiveUp,
            primaryText = stringResource(R.string.btn_pause),
            primaryIcon = TodoriIcons.Pause,
            onPrimary = actions.onPause
        )
        TimerStatus.Paused -> ControlRow(
            secondaryDescription = stringResource(R.string.btn_give_up),
            onSecondary = actions.onGiveUp,
            primaryText = stringResource(R.string.btn_resume),
            primaryIcon = TodoriIcons.Play,
            onPrimary = actions.onResume
        )
        TimerStatus.Finished -> ControlRow(
            secondaryDescription = stringResource(R.string.btn_stop),
            onSecondary = actions.onStop,
            primaryText = stringResource(R.string.btn_one_more),
            primaryIcon = TodoriIcons.Heart,
            onPrimary = actions.onOneMore
        )
    }
}

@Composable
private fun ControlRow(
    secondaryDescription: String,
    onSecondary: () -> Unit,
    primaryText: String,
    primaryIcon: ImageVector,
    onPrimary: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlassIconButton(TodoriIcons.Close, secondaryDescription, onSecondary)
        JellyButton(primaryText, primaryIcon, onPrimary, Modifier.weight(1f))
    }
}

private fun moodOf(status: TimerStatus): TomatoMood = when (status) {
    TimerStatus.Idle -> TomatoMood.Idle
    TimerStatus.Paused -> TomatoMood.Sleepy
    TimerStatus.Finished -> TomatoMood.Happy
    TimerStatus.Running -> TomatoMood.Focus
}

/**
 * 지금 상황([bubbleKey])에 맞는 문구 묶음에서 하나를 고른다.
 * 상황이 바뀔 때만 새로 고르고(회전해도 유지), 같은 상황에서는 직전과 다른 문구가 나온다.
 */
@Composable
private fun bubbleText(ui: TimerUiState): String {
    val hour = remember { LocalDateTime.now().hour }
    val key = bubbleKey(
        status = ui.status,
        focusMinutes = ui.focusMinutes,
        totalMillis = ui.totalMillis,
        remainingMillis = ui.remainingMillis,
        todayCount = ui.todayCount,
        totalCount = ui.totalCount,
        hour = hour
    )
    val options = stringArrayResource(key.arrayRes())
    val index = rememberSaveable(key) { AppBubbleHistory.next(key, options.size) }
    return options[index % options.size].replace("{count}", ui.todayCount.toString())
}

@ArrayRes
private fun BubbleKey.arrayRes(): Int = when (this) {
    BubbleKey.IdleFirst -> R.array.bubble_idle_first
    BubbleKey.IdleAfterDone -> R.array.bubble_idle_after_done
    BubbleKey.IdleMorning -> R.array.bubble_idle_morning
    BubbleKey.IdleAfternoon -> R.array.bubble_idle_afternoon
    BubbleKey.IdleEvening -> R.array.bubble_idle_evening
    BubbleKey.IdleNight -> R.array.bubble_idle_night
    BubbleKey.IdleShort -> R.array.bubble_idle_short
    BubbleKey.IdleLong -> R.array.bubble_idle_long
    BubbleKey.FocusStart -> R.array.bubble_focus_start
    BubbleKey.FocusEarly -> R.array.bubble_focus_early
    BubbleKey.FocusMiddle -> R.array.bubble_focus_middle
    BubbleKey.FocusLate -> R.array.bubble_focus_late
    BubbleKey.FocusFinal -> R.array.bubble_focus_final
    BubbleKey.Paused -> R.array.bubble_paused
    BubbleKey.DoneLong -> R.array.bubble_done_long
    BubbleKey.DoneFirst -> R.array.bubble_done_first
    BubbleKey.DoneSecond -> R.array.bubble_done_second
    BubbleKey.DoneMany -> R.array.bubble_done_many
}

@Composable
private fun timeText(ui: TimerUiState): String = when (ui.status) {
    TimerStatus.Idle -> stringResource(R.string.minutes_format, ui.focusMinutes)
    TimerStatus.Finished -> stringResource(R.string.done_label)
    else -> formatRemainingTime(ui.remainingMillis)
}

private val STAGE_MAX = 290.dp
private val MIN_STAGE = 200.dp
private val STAGE_MAX_WIDE = 360.dp
private val MAX_CONTENT_WIDTH = 480.dp
private val BUBBLE_RESERVED_HEIGHT = 84.dp

/** 가운데 영역에서 토마토를 뺀 나머지(말풍선·간격·시간·안내문)가 차지하는 대략의 높이. */
private val MIDDLE_FIXED_HEIGHT = 246.dp
private const val DIAL_RATIO = 0.76f
private const val TOMATO_RATIO = 0.72f
private const val RAIL_RATIO = 0.62f
private const val TICK_MILLIS = 250L
private const val REVIEW_DELAY_MILLIS = 2_000L

package com.haruple97.todori.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.haruple97.todori.MainActivity
import com.haruple97.todori.R
import com.haruple97.todori.core.common.heatLevel
import com.haruple97.todori.core.data.repository.SessionRepository
import com.haruple97.todori.core.data.repository.SettingsRepository
import com.haruple97.todori.core.model.CalendarPrefs
import com.haruple97.todori.core.model.HeatStyle
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// 응답형 크기: 2x1 / 2x2 / 4x1 / 4x2. 기본 배치는 가장 낮은 4x1이고, 키우면 자세한 레이아웃이 된다.
private val MiniSize = DpSize(110.dp, 70.dp)
private val SmallSize = DpSize(110.dp, 110.dp)
private val CompactSize = DpSize(250.dp, 70.dp)
private val WideSize = DpSize(250.dp, 110.dp)

private val TextPrimary = ColorProvider(day = Color(0xFF5A3A3F), night = Color(0xFFFFEDEF))
private val TextSecondary = ColorProvider(day = Color(0xFF8A6A70), night = Color(0xFFD1B3BA))
private val Accent = ColorProvider(day = Color(0xFFE5607A), night = Color(0xFFFFB3C1))

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WeekWidgetEntryPoint {
    fun sessionRepository(): SessionRepository

    fun settingsRepository(): SettingsRepository
}

/**
 * 이번 주(일~토) 토마토 기록을 보여주는 홈 화면 위젯. 탭하면 기록 화면으로 이동한다.
 * 모양은 위젯마다 고정이다: 기본은 토마토 스티커와 개수 배지, [HeatWidget]은 집중 시간 농도 색 칸.
 * (앱의 달력 보기 방식과는 별개이며, 농도 위젯의 색 스타일만 앱 설정을 따른다.)
 */
open class WeekWidget(private val heat: Boolean = false) : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(setOf(MiniSize, SmallSize, CompactSize, WideSize))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, WeekWidgetEntryPoint::class.java)
        val sessions = entryPoint.sessionRepository().sessions
        val calendarPrefs = entryPoint.settingsRepository().calendarPrefs
        val zone = ZoneId.systemDefault()
        // 값을 한 번만 읽으면 이미 떠 있는 위젯 세션은 설정이 바뀌어도 다시 그려지지 않는다.
        // 흐름을 구독해 두면 기록·보기 방식·색 스타일이 바뀔 때마다 위젯이 스스로 갱신된다.
        val initialSummary = buildWeekSummary(sessions.first(), LocalDate.now(), zone)
        val initialPrefs = calendarPrefs.first()
        provideContent {
            val summary by remember { sessions.map { buildWeekSummary(it, LocalDate.now(), zone) } }
                .collectAsState(initialSummary)
            val prefs by calendarPrefs.collectAsState(initialPrefs)
            WeekWidgetContent(summary, prefs.copy(heatMode = heat))
        }
    }
}

@Composable
private fun WeekWidgetContent(summary: WeekSummary, prefs: CalendarPrefs) {
    val context = LocalContext.current
    val size = LocalSize.current
    val openRecord = Intent(Intent.ACTION_VIEW, "todori://record".toUri()).setClass(context, MainActivity::class.java)
    val wide = size.width >= CompactSize.width
    val tall = size.height >= SmallSize.height
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(ImageProvider(R.drawable.bg_widget))
            .clickable(actionStartActivity(openRecord))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        when {
            wide && tall -> WideLayout(summary, prefs)
            wide -> CompactLayout(summary, prefs)
            tall -> SmallLayout(summary)
            else -> MiniLayout(summary)
        }
    }
}

/** 4x2 이상: 제목·요약 한 줄 + 7일. */
@Composable
private fun WideLayout(summary: WeekSummary, prefs: CalendarPrefs) {
    val context = LocalContext.current
    Column(GlanceModifier.fillMaxSize()) {
        Row(GlanceModifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = context.getString(R.string.widget_title),
                style = TextStyle(color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold),
                modifier = GlanceModifier.defaultWeight()
            )
            Text(
                text = context.getString(R.string.widget_summary, summary.total, summary.streak),
                style = TextStyle(color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            )
        }
        // 위젯이 세로로 커져도 7일 줄이 가운데에 모이도록 남는 공간 안에서 세로 중앙 정렬한다.
        Box(GlanceModifier.fillMaxWidth().defaultWeight(), contentAlignment = Alignment.Center) {
            Row(GlanceModifier.fillMaxWidth()) {
                summary.days.forEach { day -> DayCell(day, prefs, labelSize = 11.sp, tomato = 26.dp, verticalPadding = 6.dp) }
            }
        }
    }
}

/** 4x1: 왼쪽에 이번 주 요약, 오른쪽에 7일. 가장 낮은 기본 크기. */
@Composable
private fun CompactLayout(summary: WeekSummary, prefs: CalendarPrefs) {
    val context = LocalContext.current
    Row(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = GlanceModifier.width(58.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = context.getString(R.string.widget_week_label),
                style = TextStyle(color = TextSecondary, fontSize = 10.sp)
            )
            Text(
                text = context.getString(R.string.count_format, summary.total),
                style = TextStyle(color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                text = context.getString(R.string.widget_small_streak, summary.streak),
                style = TextStyle(color = Accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            )
        }
        summary.days.forEach { day -> DayCell(day, prefs, labelSize = 10.sp, tomato = 22.dp, verticalPadding = 3.dp) }
    }
}

@Composable
private fun RowScope.DayCell(day: WeekDay, prefs: CalendarPrefs, labelSize: TextUnit, tomato: Dp, verticalPadding: Dp) {
    val context = LocalContext.current
    val label = day.date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, context.resources.configuration.locales[0])
    // 폭은 가중치로 균등 분배하고 높이는 내용에 맞춘다(fillMaxSize를 쓰면 한 칸이 줄 전체를 차지한다).
    val base = GlanceModifier.defaultWeight().padding(vertical = verticalPadding)
    val cellModifier = if (day.isToday) base.background(ImageProvider(R.drawable.bg_widget_today)) else base
    Column(
        modifier = cellModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                color = if (day.isToday) Accent else TextSecondary,
                fontSize = labelSize,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
            )
        )
        Spacer(GlanceModifier.height(2.dp))
        Box(GlanceModifier.size(tomato + 2.dp), contentAlignment = Alignment.Center) {
            val (drawable, size) = when {
                // 농도 모드: 하루 집중 시간에 따라 진해지는 둥근 칸(기록 탭·통계와 같은 단계·색).
                prefs.heatMode && day.count > 0 -> heatDrawable(prefs.style, heatLevel(day.minutes)) to tomato * 0.9f
                day.count > 0 -> R.drawable.widget_tomato to tomato
                day.isFuture -> R.drawable.widget_dot_future to tomato * 0.7f
                else -> R.drawable.widget_tomato_empty to tomato * 0.85f
            }
            Image(
                provider = ImageProvider(drawable),
                contentDescription = context.getString(R.string.widget_day_description, label, day.count),
                modifier = GlanceModifier.size(size)
            )
            if (day.count > 1 && !prefs.heatMode) CountBadge(day.count)
        }
    }
}

private fun heatDrawable(style: HeatStyle, level: Int): Int {
    val ripening = style == HeatStyle.Ripening
    return when (level.coerceIn(1, 4)) {
        1 -> if (ripening) R.drawable.widget_heat_ripening_1 else R.drawable.widget_heat_blush_1
        2 -> if (ripening) R.drawable.widget_heat_ripening_2 else R.drawable.widget_heat_blush_2
        3 -> if (ripening) R.drawable.widget_heat_ripening_3 else R.drawable.widget_heat_blush_3
        else -> if (ripening) R.drawable.widget_heat_ripening_4 else R.drawable.widget_heat_blush_4
    }
}

@Composable
private fun CountBadge(count: Int) {
    Box(GlanceModifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = GlanceModifier.size(12.dp).background(ImageProvider(R.drawable.bg_widget_badge)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 9) "9+" else count.toString(),
                style = TextStyle(
                    color = ColorProvider(day = Color.White, night = Color.White),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/** 2x2: 토마토 하나와 이번 주 개수·연속 일수. */
@Composable
private fun SmallLayout(summary: WeekSummary) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.widget_tomato),
            contentDescription = null,
            modifier = GlanceModifier.size(46.dp)
        )
        Spacer(GlanceModifier.height(4.dp))
        if (summary.total == 0) {
            Text(
                text = context.getString(R.string.widget_small_empty),
                style = TextStyle(
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )
        } else {
            Text(
                text = context.getString(R.string.widget_small_title, summary.total),
                style = TextStyle(color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                text = context.getString(R.string.widget_small_streak, summary.streak),
                style = TextStyle(color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}

/** 2x1: 토마토 옆에 이번 주 개수와 연속 일수. */
@Composable
private fun MiniLayout(summary: WeekSummary) {
    val context = LocalContext.current
    Row(GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
        Image(
            provider = ImageProvider(R.drawable.widget_tomato),
            contentDescription = null,
            modifier = GlanceModifier.size(34.dp)
        )
        Spacer(GlanceModifier.width(8.dp))
        Column {
            Text(
                text = context.getString(R.string.widget_small_title, summary.total),
                style = TextStyle(color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                text = context.getString(R.string.widget_small_streak, summary.streak),
                style = TextStyle(color = Accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}

/** 하루 집중 시간을 농도 색 칸으로 보여주는 위젯. 토마토 위젯과 따로 추가한다. */
class HeatWidget : WeekWidget(heat = true)

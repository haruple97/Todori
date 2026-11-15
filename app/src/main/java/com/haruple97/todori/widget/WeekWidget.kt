package com.haruple97.todori.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.color.ColorProvider
import com.haruple97.todori.MainActivity
import com.haruple97.todori.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle as JavaTextStyle
import kotlinx.coroutines.flow.first

private val SmallSize = DpSize(110.dp, 110.dp)
private val WideSize = DpSize(250.dp, 110.dp)
private val TallHeight = 150.dp

private val TextPrimary = ColorProvider(day = Color(0xFF5A3A3F), night = Color(0xFFFFEDEF))
private val TextSecondary = ColorProvider(day = Color(0xFF8A6A70), night = Color(0xFFD1B3BA))
private val Accent = ColorProvider(day = Color(0xFFE5607A), night = Color(0xFFFFB3C1))

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WeekWidgetEntryPoint {
    fun sessionRepository(): com.haruple97.todori.core.data.repository.SessionRepository
}

/** 이번 주(일~토) 토마토 기록을 보여주는 홈 화면 위젯. 탭하면 기록 화면으로 이동한다. */
class WeekWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Responsive(setOf(SmallSize, WideSize))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = EntryPointAccessors
            .fromApplication(context.applicationContext, WeekWidgetEntryPoint::class.java)
            .sessionRepository()
        val summary = buildWeekSummary(repository.sessions.first(), LocalDate.now(), ZoneId.systemDefault())
        provideContent { WeekWidgetContent(summary) }
    }
}

@Composable
private fun WeekWidgetContent(summary: WeekSummary) {
    val context = LocalContext.current
    val openRecord = Intent(Intent.ACTION_VIEW, "todori://record".toUri()).setClass(context, MainActivity::class.java)
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(ImageProvider(R.drawable.bg_widget))
            .clickable(actionStartActivity(openRecord))
            .padding(12.dp)
    ) {
        if (LocalSize.current.width >= WideSize.width) WideLayout(summary) else SmallLayout(summary)
    }
}

@Composable
private fun WideLayout(summary: WeekSummary) {
    val context = LocalContext.current
    val locale = context.resources.configuration.locales[0]
    val tall = LocalSize.current.height >= TallHeight
    Column(GlanceModifier.fillMaxSize()) {
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
                summary.days.forEach { day ->
                    DayCell(day, day.date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, locale), tall)
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayCell(day: WeekDay, label: String, tall: Boolean) {
    val context = LocalContext.current
    val scale = if (tall) 1.3f else 1f
    // 폭은 가중치로 균등 분배하고 높이는 내용에 맞춘다(fillMaxSize를 쓰면 한 칸이 줄 전체를 차지한다).
    val base = GlanceModifier.defaultWeight().padding(vertical = 6.dp)
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
                fontSize = (if (tall) 12 else 11).sp,
                fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal
            )
        )
        Spacer(GlanceModifier.height(3.dp))
        Box(GlanceModifier.size(28.dp * scale), contentAlignment = Alignment.Center) {
            val (drawable, size) = when {
                day.count > 0 -> R.drawable.widget_tomato to 26.dp * scale
                day.isFuture -> R.drawable.widget_dot_future to 18.dp * scale
                else -> R.drawable.widget_tomato_empty to 22.dp * scale
            }
            Image(
                provider = ImageProvider(drawable),
                contentDescription = context.getString(R.string.widget_day_description, label, day.count),
                modifier = GlanceModifier.size(size)
            )
            if (day.count > 1) CountBadge(day.count)
        }
    }
}

@Composable
private fun CountBadge(count: Int) {
    Box(GlanceModifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Box(
            modifier = GlanceModifier.size(13.dp).background(ImageProvider(R.drawable.bg_widget_badge)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 9) "9+" else count.toString(),
                style = TextStyle(color = ColorProvider(day = Color.White, night = Color.White), fontSize = 8.sp, fontWeight = FontWeight.Bold)
            )
        }
    }
}

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

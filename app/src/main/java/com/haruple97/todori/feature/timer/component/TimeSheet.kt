package com.haruple97.todori.feature.timer.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haruple97.todori.R
import com.haruple97.todori.core.designsystem.theme.todoriColors
import com.haruple97.todori.core.ui.GlassIconButton
import com.haruple97.todori.core.ui.JellyButton
import com.haruple97.todori.core.ui.icon.TodoriIcons
import com.haruple97.todori.feature.timer.PremiumMaxMinutes
import com.haruple97.todori.feature.timer.PremiumMinMinutes
import kotlin.math.roundToInt

private val QuickMinutes = listOf(listOf(15, 25, 45, 60), listOf(90, 120, 150, 180))

/**
 * 프리미엄 전용 시간 직접 설정 시트. 슬라이더로 크게, ±1분 버튼으로 세밀하게, 칩으로 자주 쓰는 시간으로 바로 정한다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeSheet(
    minutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var value by rememberSaveable { mutableIntStateOf(minutes.coerceIn(PremiumMinMinutes, PremiumMaxMinutes)) }
    val colors = todoriColors
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.time_sheet_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                stringResource(R.string.time_sheet_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                GlassIconButton(
                    icon = TodoriIcons.Minus,
                    contentDescription = stringResource(R.string.time_minus),
                    onClick = { value = (value - 1).coerceAtLeast(PremiumMinMinutes) },
                    size = 48.dp,
                    enabled = value > PremiumMinMinutes
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(
                        stringResource(R.string.minutes_format, value),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    // 한 시간이 넘으면 "1시간 30분"처럼 풀어서 보여준다. 높이를 비워 두어 숫자가 흔들리지 않게 한다.
                    Text(
                        text = hoursText(value),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.accentDeep,
                        modifier = Modifier.heightIn(min = 24.dp)
                    )
                }
                GlassIconButton(
                    icon = TodoriIcons.Plus,
                    contentDescription = stringResource(R.string.time_plus),
                    onClick = { value = (value + 1).coerceAtMost(PremiumMaxMinutes) },
                    size = 48.dp,
                    enabled = value < PremiumMaxMinutes
                )
            }

            val sliderDescription = stringResource(R.string.time_slider)
            Slider(
                value = value.toFloat(),
                onValueChange = { value = it.roundToInt() },
                valueRange = PremiumMinMinutes.toFloat()..PremiumMaxMinutes.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = colors.accentDeep,
                    activeTrackColor = colors.accentDeep,
                    inactiveTrackColor = colors.accentLight.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = sliderDescription }
            )

            Spacer(Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickMinutes.forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { quick ->
                            QuickChip(
                                text = stringResource(R.string.minutes_chip_format, quick),
                                selected = value == quick,
                                onClick = { value = quick },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            JellyButton(
                text = stringResource(R.string.time_confirm),
                icon = TodoriIcons.Check,
                onClick = { onConfirm(value) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun hoursText(minutes: Int): String = when {
    minutes < 60 -> ""
    minutes % 60 == 0 -> stringResource(R.string.duration_hours, minutes / 60)
    else -> stringResource(R.string.duration_hours_minutes, minutes / 60, minutes % 60)
}

@Composable
private fun QuickChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = todoriColors
    Surface(
        onClick = onClick,
        modifier = modifier.semantics { this.selected = selected },
        shape = RoundedCornerShape(50),
        color = if (selected) colors.accentDeep else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
        border = BorderStroke(1.5.dp, if (selected) colors.accentDeep else colors.accentLight.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth()
        )
    }
}

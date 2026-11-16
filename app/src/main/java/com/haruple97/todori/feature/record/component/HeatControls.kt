package com.haruple97.todori.feature.record.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haruple97.todori.R
import com.haruple97.todori.core.common.HEAT_MAX_LEVEL
import com.haruple97.todori.core.designsystem.theme.todoriColors
import com.haruple97.todori.core.model.HeatStyle

/** 달력 보기 방식: 토마토 스티커 ↔ 농도(잔디). */
@Composable
fun CalendarModeToggle(heatMode: Boolean, onChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    Segmented(
        options = listOf(stringResource(R.string.record_mode_sticker), stringResource(R.string.record_mode_heat)),
        selectedIndex = if (heatMode) 1 else 0,
        onSelect = { onChange(it == 1) },
        modifier = modifier
    )
}

/** 농도 모드 아래의 범례. 오른쪽 칩으로 색 스타일(숙성/핑크)을 바꾼다. */
@Composable
fun HeatLegend(style: HeatStyle, onStyleChange: (HeatStyle) -> Unit, width: Dp, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.width(width).padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        val muted = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
        val label = MaterialTheme.typography.bodyLarge.copy(fontSize = 11.sp)
        Text(stringResource(R.string.record_heat_less), style = label, color = muted)
        (1..HEAT_MAX_LEVEL).forEach { level ->
            Box(Modifier.size(14.dp).background(heatColor(style, level), RoundedCornerShape(4.dp)))
        }
        Text(stringResource(R.string.record_heat_more), style = label, color = muted)
        Box(Modifier.weight(1f))
        Segmented(
            options = listOf(stringResource(R.string.record_style_ripening), stringResource(R.string.record_style_blush)),
            selectedIndex = style.ordinal,
            onSelect = { onStyleChange(HeatStyle.entries[it]) },
            compact = true
        )
    }
}

@Composable
private fun Segmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val outer = RoundedCornerShape(50)
    Row(
        modifier
            .clip(outer)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .border(1.2.dp, todoriColors.accentLight, outer)
            .padding(3.dp)
    ) {
        options.forEachIndexed { index, text ->
            val selected = index == selectedIndex
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = if (compact) 11.sp else 13.sp),
                color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (selected) todoriColors.accentDeep else androidx.compose.ui.graphics.Color.Transparent)
                    .selectable(selected = selected, role = Role.RadioButton, onClick = { onSelect(index) })
                    .padding(horizontal = if (compact) 10.dp else 16.dp, vertical = if (compact) 5.dp else 7.dp)
            )
        }
    }
}

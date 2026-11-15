package com.haruple97.todori.feature.timer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haruple97.todori.R
import com.haruple97.todori.core.designsystem.theme.todoriColors
import com.haruple97.todori.core.model.CycleSettings
import com.haruple97.todori.core.ui.GlassIconButton
import com.haruple97.todori.core.ui.JellyButton
import com.haruple97.todori.core.ui.SoftCard
import com.haruple97.todori.core.ui.icon.TodoriIcons

private const val MIN_ROUNDS = 2
private const val MAX_ROUNDS = 8
private const val MIN_SHORT = 1
private const val MAX_SHORT = 30
private const val MIN_LONG = 5
private const val MAX_LONG = 60
private const val LONG_STEP = 5

/**
 * 사이클 설정 시트. 프리미엄이면 고쳐서 저장하고, 아니면 같은 화면을 흐리게 보여주기만 하며
 * 아래 버튼이 "프리미엄으로 해제"가 되어 안내 시트로 넘어간다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CycleSheet(
    settings: CycleSettings,
    editable: Boolean,
    onDismiss: () -> Unit,
    onSave: (CycleSettings) -> Unit,
    onUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    var draft by rememberSaveable(stateSaver = CycleSettingsSaver) { mutableStateOf(settings) }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                stringResource(R.string.cycle_sheet_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                stringResource(R.string.cycle_sheet_hint),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))

            // 잠겨 있으면 보기만 가능하도록 흐리게 하고 조작을 막는다.
            Column(
                modifier = Modifier.alpha(if (editable) 1f else 0.5f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SwitchRow(
                    title = stringResource(R.string.cycle_enable),
                    hint = null,
                    checked = draft.enabled,
                    enabled = editable,
                    onChange = { draft = draft.copy(enabled = it) }
                )
                StepperRow(
                    title = stringResource(R.string.cycle_rounds),
                    value = stringResource(R.string.cycle_rounds_value, draft.rounds),
                    enabled = editable,
                    canDecrease = draft.rounds > MIN_ROUNDS,
                    canIncrease = draft.rounds < MAX_ROUNDS,
                    onDecrease = { draft = draft.copy(rounds = draft.rounds - 1) },
                    onIncrease = { draft = draft.copy(rounds = draft.rounds + 1) }
                )
                StepperRow(
                    title = stringResource(R.string.cycle_short),
                    value = stringResource(R.string.minutes_format, draft.shortMinutes),
                    enabled = editable,
                    canDecrease = draft.shortMinutes > MIN_SHORT,
                    canIncrease = draft.shortMinutes < MAX_SHORT,
                    onDecrease = { draft = draft.copy(shortMinutes = draft.shortMinutes - 1) },
                    onIncrease = { draft = draft.copy(shortMinutes = draft.shortMinutes + 1) }
                )
                StepperRow(
                    title = stringResource(R.string.cycle_long),
                    value = stringResource(R.string.minutes_format, draft.longMinutes),
                    enabled = editable,
                    canDecrease = draft.longMinutes > MIN_LONG,
                    canIncrease = draft.longMinutes < MAX_LONG,
                    onDecrease = { draft = draft.copy(longMinutes = (draft.longMinutes - LONG_STEP).coerceAtLeast(MIN_LONG)) },
                    onIncrease = { draft = draft.copy(longMinutes = (draft.longMinutes + LONG_STEP).coerceAtMost(MAX_LONG)) }
                )
                SwitchRow(
                    title = stringResource(R.string.cycle_auto),
                    hint = stringResource(R.string.cycle_auto_hint),
                    checked = draft.auto,
                    enabled = editable,
                    onChange = { draft = draft.copy(auto = it) }
                )
            }

            Spacer(Modifier.height(18.dp))
            if (editable) {
                JellyButton(
                    text = stringResource(R.string.cycle_save),
                    icon = TodoriIcons.Check,
                    onClick = {
                        onSave(draft)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                JellyButton(
                    text = stringResource(R.string.customize_unlock),
                    icon = TodoriIcons.Lock,
                    onClick = onUnlock,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SwitchRow(title: String, hint: String?, checked: Boolean, enabled: Boolean, onChange: (Boolean) -> Unit) {
    val colors = todoriColors
    SoftCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (hint != null) {
                    Text(
                        hint,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = colors.accentDeep,
                    checkedThumbColor = Color.White,
                    uncheckedTrackColor = colors.accentLight.copy(alpha = 0.35f),
                    uncheckedBorderColor = colors.accentLight
                )
            )
        }
    }
}

@Composable
private fun StepperRow(
    title: String,
    value: String,
    enabled: Boolean,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    SoftCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            GlassIconButton(
                TodoriIcons.Minus, stringResource(R.string.cycle_less), onDecrease,
                size = 40.dp, enabled = enabled && canDecrease
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(84.dp)
            )
            GlassIconButton(
                TodoriIcons.Plus, stringResource(R.string.cycle_more), onIncrease,
                size = 40.dp, enabled = enabled && canIncrease
            )
        }
    }
}

/** 화면 회전에도 편집 중인 값이 유지되도록 저장하는 방식. */
private val CycleSettingsSaver = listSaver<CycleSettings, Any>(
    save = { listOf(it.enabled, it.rounds, it.shortMinutes, it.longMinutes, it.auto) },
    restore = {
        CycleSettings(
            enabled = it[0] as Boolean,
            rounds = it[1] as Int,
            shortMinutes = it[2] as Int,
            longMinutes = it[3] as Int,
            auto = it[4] as Boolean
        )
    }
)

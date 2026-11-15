package com.haruple97.todori.feature.timer.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haruple97.todori.R
import com.haruple97.todori.core.ui.RoundIconButton

private const val BREAK_STEP = 5
private const val BREAK_MIN = 5
private const val BREAK_MAX = 30

@Composable
fun BreakStepper(
    minutes: Int,
    onMinutesChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.break_label), style = MaterialTheme.typography.bodyLarge)
        RoundIconButton(
            symbol = "−",
            description = stringResource(R.string.break_decrease),
            onClick = { onMinutesChange((minutes - BREAK_STEP).coerceAtLeast(BREAK_MIN)) }
        )
        Text(
            text = stringResource(R.string.minutes_format, minutes),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(56.dp)
        )
        RoundIconButton(
            symbol = "+",
            description = stringResource(R.string.break_increase),
            onClick = { onMinutesChange((minutes + BREAK_STEP).coerceAtMost(BREAK_MAX)) }
        )
    }
}

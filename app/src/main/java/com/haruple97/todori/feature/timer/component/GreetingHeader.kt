package com.haruple97.todori.feature.timer.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.haruple97.todori.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** 시간대에 맞는 인사말과 오늘 날짜를 보여주는 상단 헤더. */
@Composable
fun GreetingHeader(
    horizontalAlignment: Alignment.Horizontal,
    modifier: Modifier = Modifier
) {
    val locale = LocalConfiguration.current.locales[0]
    val datePattern = stringResource(R.string.date_pattern)
    val now = remember { LocalDateTime.now() }
    val greeting = stringResource(
        when (now.hour) {
            in 5..10 -> R.string.greeting_morning
            in 11..16 -> R.string.greeting_afternoon
            in 17..20 -> R.string.greeting_evening
            else -> R.string.greeting_night
        }
    )
    val date = remember(datePattern, locale) { now.format(DateTimeFormatter.ofPattern(datePattern, locale)) }

    Column(modifier.fillMaxWidth(), horizontalAlignment = horizontalAlignment) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = date,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
        )
    }
}

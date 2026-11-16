package com.haruple97.todori.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val VALUE_MAX_SP = 22f
private const val VALUE_MIN_SP = 14f

// 글자가 줄어도 줄 높이는 그대로 두어, 나란한 카드의 높이와 라벨 위치가 어긋나지 않게 한다.
private const val VALUE_LINE_HEIGHT_SP = 28f
private val VALUE_BOX_HEIGHT = 32.dp

/** 큰 숫자와 작은 라벨이 있는 요약 카드. 기록·통계 화면에서 같이 쓴다. */
@Composable
fun StatCard(value: String, label: String, modifier: Modifier = Modifier) {
    SoftCard(modifier) {
        // 값이 카드 폭보다 길면(예: "215시간", "2시간 35분") 한쪽으로 쏠려 잘리지 않도록 글자 크기를 줄여 한 줄에 맞춘다.
        var valueSize by remember(value) { mutableFloatStateOf(VALUE_MAX_SP) }
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 글자 크기가 줄어도 칸 높이는 같게 두고 그 안에서 세로 가운데에 놓아, 나란한 카드의 높이와 라벨 위치를 맞춘다.
            Box(Modifier.fillMaxWidth().height(VALUE_BOX_HEIGHT), contentAlignment = Alignment.Center) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = valueSize.sp, lineHeight = VALUE_LINE_HEIGHT_SP.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    onTextLayout = { if (it.hasVisualOverflow && valueSize > VALUE_MIN_SP) valueSize -= 1f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}


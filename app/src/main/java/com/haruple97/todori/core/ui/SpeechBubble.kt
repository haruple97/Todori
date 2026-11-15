package com.haruple97.todori.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haruple97.todori.core.designsystem.theme.JellyPinkLight

/** 토마토 머리 위에 뜨는 말풍선. 꼬리가 아래(토마토)를 가리킨다. */
@Composable
fun SpeechBubble(text: String, modifier: Modifier = Modifier) {
    val fill = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = fill,
            border = BorderStroke(1.5.dp, JellyPinkLight),
            shadowElevation = 4.dp
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
        Canvas(Modifier.size(width = 20.dp, height = 11.dp).offset(y = (-1.5).dp)) {
            val tail = Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width / 2f, size.height)
                close()
            }
            drawPath(tail, fill)
        }
    }
}

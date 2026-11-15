package com.haruple97.todori.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haruple97.todori.R
import com.haruple97.todori.core.designsystem.theme.todoriColors
import com.haruple97.todori.core.ui.icon.TodoriIcons

/**
 * 유료 기능을 눌렀을 때 뜨는 안내 시트. 지금은 구독 결제가 없어서 "곧 열려요"를 보여주고,
 * 결제를 붙이면 [onSubscribe]가 구독 화면으로 넘어가도록 바꾸면 된다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumGateSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    onSubscribe: (() -> Unit)? = null
) {
    var soon by rememberSaveable { mutableStateOf(false) }
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
                .padding(horizontal = 28.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val colors = todoriColors
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Brush.verticalGradient(listOf(colors.accentLight, colors.accentDeep)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(TodoriIcons.Sparkles, contentDescription = null, tint = Color.White, modifier = Modifier.size(38.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                stringResource(R.string.premium_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.premium_body),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(18.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Benefit(stringResource(R.string.premium_benefit_friends))
                Benefit(stringResource(R.string.premium_benefit_themes))
                Benefit(stringResource(R.string.premium_benefit_time))
            }
            Spacer(Modifier.height(22.dp))
            JellyButton(
                text = stringResource(R.string.premium_subscribe),
                icon = TodoriIcons.Heart,
                onClick = { if (onSubscribe != null) onSubscribe() else soon = true },
                modifier = Modifier.fillMaxWidth()
            )
            if (soon) {
                Spacer(Modifier.height(10.dp))
                Text(
                    stringResource(R.string.premium_soon),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.accentDeep,
                    textAlign = TextAlign.Center
                )
            }
            TextButton(onClick = onDismiss, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)) {
                Text(
                    stringResource(R.string.premium_close),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun Benefit(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(todoriColors.accentLight.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(TodoriIcons.Check, contentDescription = null, tint = todoriColors.accentDeep, modifier = Modifier.size(13.dp))
        }
        Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
    }
}

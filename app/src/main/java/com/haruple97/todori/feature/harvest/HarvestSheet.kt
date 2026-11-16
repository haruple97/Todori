package com.haruple97.todori.feature.harvest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.haruple97.todori.R
import com.haruple97.todori.core.model.HeatStyle
import com.haruple97.todori.core.share.shareBitmap
import com.haruple97.todori.core.ui.ConfettiBurst
import com.haruple97.todori.core.ui.JellyButton
import com.haruple97.todori.core.ui.icon.TodoriIcons
import kotlinx.coroutines.launch

/** 이달의 수확 카드를 보여주고 이미지로 공유하는 시트. 열리면 색종이가 터진다. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestSheet(harvest: Harvest, style: HeatStyle, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val layer = rememberGraphicsLayer()
    val chooserTitle = stringResource(R.string.harvest_share)
    var burst by remember { mutableStateOf(false) }
    var sharing by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { burst = true }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        modifier = modifier
    ) {
        Box(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 420.dp)
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                // 카드만 레이어에 기록해 두었다가 공유할 때 비트맵으로 꺼낸다(시트 배경·버튼은 빠진다).
                HarvestCard(
                    harvest = harvest,
                    style = style,
                    modifier = Modifier.drawWithContent {
                        layer.record { this@drawWithContent.drawContent() }
                        drawLayer(layer)
                    }
                )
                Spacer(Modifier.height(16.dp))
                JellyButton(
                    text = stringResource(R.string.harvest_share),
                    icon = TodoriIcons.Heart,
                    enabled = !sharing,
                    onClick = {
                        sharing = true
                        scope.launch {
                            try {
                                val bitmap = layer.toImageBitmap().asAndroidBitmap()
                                shareBitmap(context, bitmap, "todori_harvest_${harvest.month}.png", chooserTitle)
                            } finally {
                                sharing = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
            }
            // fillMaxSize를 쓰는 효과가 시트를 화면 끝까지 늘리지 않도록 내용 크기에 맞춘다.
            ConfettiBurst(trigger = burst, modifier = Modifier.matchParentSize())
        }
    }
}

package com.haruple97.todori.feature.customize.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.haruple97.todori.R
import com.haruple97.todori.core.designsystem.theme.TodoriTheme
import com.haruple97.todori.core.designsystem.theme.isDarkFor
import com.haruple97.todori.core.designsystem.theme.todoriColors
import com.haruple97.todori.core.model.SkinId
import com.haruple97.todori.core.model.ThemeId
import com.haruple97.todori.core.ui.FloatingDecor
import com.haruple97.todori.core.ui.JellyButton
import com.haruple97.todori.core.ui.TomatoCharacter
import com.haruple97.todori.core.ui.TomatoMood
import com.haruple97.todori.core.ui.icon.TodoriIcons
import com.haruple97.todori.feature.customize.nameRes

/**
 * 고른 캐릭터와 테마를 미리 보여주는 카드. 카드 안은 미리 보는 테마의 색으로 그려져서,
 * 적용하기 전에 캐릭터와 테마의 조합을 확인할 수 있다.
 */
@Composable
fun PreviewCard(skin: SkinId, theme: ThemeId, modifier: Modifier = Modifier) {
    TodoriTheme(theme = theme, skin = skin) {
        val colors = todoriColors
        val shape = RoundedCornerShape(28.dp)
        Box(
            modifier = modifier
                .clip(shape)
                .background(Brush.verticalGradient(listOf(colors.calm.top, colors.calm.bottom)))
                .border(1.5.dp, colors.accentLight, shape)
        ) {
            FloatingDecor()
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val bounce = remember(skin) { Animatable(0.8f) }
                LaunchedEffect(skin) { bounce.animateTo(1f, spring(0.4f, Spring.StiffnessMedium)) }
                TomatoCharacter(
                    mood = TomatoMood.Idle,
                    ripeness = 1f,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .size(116.dp)
                        .graphicsLayer {
                            scaleX = bounce.value
                            scaleY = bounce.value
                        }
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        stringResource(skin.nameRes()),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text("·", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                    Text(
                        stringResource(theme.nameRes()),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                // 이 테마에서 버튼이 어떻게 보이는지 보여주는 작은 견본
                JellyButton(
                    text = stringResource(R.string.btn_start),
                    icon = TodoriIcons.Play,
                    onClick = {},
                    height = 40.dp,
                    enabled = true,
                    modifier = Modifier.fillMaxWidth(0.55f)
                )
            }
        }
    }
}

/** 친구들 / 테마를 오가는 알약형 탭. */
@Composable
fun CustomizeTabs(selectedIndex: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val labels = listOf(stringResource(R.string.customize_tab_friends), stringResource(R.string.customize_tab_themes))
    val colors = todoriColors
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
            .padding(4.dp)
    ) {
        labels.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .then(
                        if (selected) {
                            Modifier.background(Brush.verticalGradient(listOf(colors.accentLight, colors.accentDeep)))
                        } else {
                            Modifier
                        }
                    )
                    .clickable(role = Role.Tab) { onSelect(index) }
                    .semantics { this.selected = selected }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 선택 가능한 한 칸. [applied]는 지금 쓰는 것(테두리 강조 + 체크), [previewed]는 미리 보는 것(은은한 강조),
 * [locked]는 프리미엄이 필요한 것(자물쇠).
 */
@Composable
fun ItemCard(
    name: String,
    applied: Boolean,
    previewed: Boolean,
    locked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val colors = todoriColors
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(24.dp)
    val description = buildString {
        append(name)
        if (applied) append(", ").append(stringResource(R.string.customize_in_use))
        if (locked) append(", ").append(stringResource(R.string.customize_locked))
    }
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = if (previewed) 0.98f else 0.8f),
        border = androidx.compose.foundation.BorderStroke(
            width = if (applied) 2.5.dp else if (previewed) 2.dp else 1.5.dp,
            color = when {
                applied -> colors.accentDeep
                previewed -> colors.accentLight
                else -> colors.accentLight.copy(alpha = 0.45f)
            }
        ),
        modifier = modifier
            .aspectRatio(0.92f)
            .clip(shape)
            .clickable(role = Role.Button) {
                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                onClick()
            }
            .semantics(mergeDescendants = true) { contentDescription = description }
    ) {
        Box(Modifier.fillMaxSize().padding(10.dp)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { content() }
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp, lineHeight = 20.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
            when {
                applied -> Badge(TodoriIcons.Check, colors.accentDeep, Modifier.align(Alignment.TopEnd))
                locked -> Badge(TodoriIcons.Lock, colors.accentLight, Modifier.align(Alignment.TopEnd))
            }
        }
    }
}

@Composable
private fun Badge(icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(24.dp).background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
    }
}

/** 친구 카드 안의 캐릭터. */
@Composable
fun SkinThumb(skin: SkinId, size: Dp = 68.dp) {
    TomatoCharacter(mood = TomatoMood.Idle, ripeness = 1f, skin = skin, modifier = Modifier.size(size))
}

/** 테마 카드 안의 견본: 그 테마의 배경 그라데이션 위에 포인트 색 세 개. */
@Composable
fun ThemeThumb(theme: ThemeId, size: Dp = 64.dp) {
    val colors = todoriColors(theme, isDarkFor(theme))
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.verticalGradient(listOf(colors.calm.top, colors.calm.bottom)))
            .border(2.dp, colors.accentLight, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(colors.accentLight, colors.accentDeep, colors.glow).forEach { dot ->
                Box(Modifier.size(size * 0.17f).background(dot, CircleShape))
            }
        }
    }
}

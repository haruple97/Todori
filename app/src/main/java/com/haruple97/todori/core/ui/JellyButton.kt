package com.haruple97.todori.core.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.haruple97.todori.core.designsystem.theme.JellyPinkDeep
import com.haruple97.todori.core.designsystem.theme.JellyPinkLight

private val ButtonHeight = 60.dp

/** 누르면 말랑하게 눌렸다 튀어오르는 스프링 + 햅틱을 공통으로 처리한다. */
@Composable
private fun Modifier.jellyPress(
    source: MutableInteractionSource,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMedium),
        label = "jellyScale"
    )
    val haptics = LocalHapticFeedback.current
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(interactionSource = source, indication = null, enabled = enabled, role = Role.Button) {
            haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
            onClick()
        }
}

private fun Modifier.glow(shape: Shape): Modifier = shadow(
    elevation = 14.dp,
    shape = shape,
    ambientColor = JellyPinkDeep.copy(alpha = 0.45f),
    spotColor = JellyPinkDeep.copy(alpha = 0.75f)
)

/** 그라데이션 + 상단 광택 + 핑크 글로우의 알약형 메인 버튼. */
@Composable
fun JellyButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val source = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .height(ButtonHeight)
            .jellyPress(source, onClick = onClick)
            .glow(shape)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(JellyPinkLight, JellyPinkDeep)))
            .drawWithContent {
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.6f), Color.Transparent),
                        endY = size.height * 0.55f
                    ),
                    topLeft = Offset(size.width * 0.07f, size.height * 0.07f),
                    size = Size(size.width * 0.86f, size.height * 0.45f),
                    cornerRadius = CornerRadius(size.height * 0.22f)
                )
                drawContent()
            }
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = Color.White)
        }
    }
}

/** 반투명 유리 질감의 원형 보조 버튼. */
@Composable
fun GlassIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = ButtonHeight,
    enabled: Boolean = true
) {
    val source = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(size)
            .alpha(if (enabled) 1f else 0.35f)
            .jellyPress(source, enabled, onClick)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = JellyPinkDeep.copy(alpha = 0.25f),
                spotColor = JellyPinkDeep.copy(alpha = 0.4f)
            )
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
            .border(BorderStroke(1.5.dp, JellyPinkLight), CircleShape)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = JellyPinkDeep, modifier = Modifier.size(size * 0.43f))
    }
}

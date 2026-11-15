package com.haruple97.todori.feature.customize

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.haruple97.todori.R
import com.haruple97.todori.core.designsystem.BackgroundTone
import com.haruple97.todori.core.designsystem.TodoriBackground
import com.haruple97.todori.core.model.SkinId
import com.haruple97.todori.core.model.ThemeId
import com.haruple97.todori.core.ui.FloatingDecor
import com.haruple97.todori.core.ui.GlassIconButton
import com.haruple97.todori.core.ui.JellyButton
import com.haruple97.todori.core.ui.PremiumGateSheet
import com.haruple97.todori.core.ui.icon.TodoriIcons
import com.haruple97.todori.feature.customize.component.CustomizeTabs
import com.haruple97.todori.feature.customize.component.ItemCard
import com.haruple97.todori.feature.customize.component.PreviewCard
import com.haruple97.todori.feature.customize.component.SkinThumb
import com.haruple97.todori.feature.customize.component.ThemeThumb

private val MAX_CONTENT_WIDTH = 520.dp
private const val TAB_FRIENDS = 0

@Composable
fun CustomizeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CustomizeViewModel = hiltViewModel()
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(TAB_FRIENDS) }
    // null이면 아직 아무것도 눌러보지 않은 것이라 지금 쓰는 값을 보여준다.
    var previewSkinId by rememberSaveable { mutableStateOf<String?>(null) }
    var previewThemeId by rememberSaveable { mutableStateOf<String?>(null) }
    var showGate by rememberSaveable { mutableStateOf(false) }

    val previewSkin = previewSkinId?.let(SkinId::fromId) ?: ui.applied.skin
    val previewTheme = previewThemeId?.let(ThemeId::fromId) ?: ui.applied.theme
    val friendsTab = tab == TAB_FRIENDS

    val locked = if (friendsTab) ui.isLocked(previewSkin) else ui.isLocked(previewTheme)
    val inUse = if (friendsTab) previewSkin == ui.applied.skin else previewTheme == ui.applied.theme
    val action = Action(
        text = stringResource(
            when {
                locked -> R.string.customize_unlock
                inUse -> R.string.customize_in_use
                else -> R.string.customize_apply
            }
        ),
        icon = when {
            locked -> TodoriIcons.Lock
            inUse -> TodoriIcons.Check
            else -> TodoriIcons.Heart
        },
        enabled = locked || !inUse,
        onClick = {
            when {
                locked -> showGate = true
                friendsTab -> viewModel.applySkin(previewSkin)
                else -> viewModel.applyTheme(previewTheme)
            }
        }
    )

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val twoPane = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
        !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND)

    val itemGrid: @Composable (Modifier) -> Unit = { gridModifier ->
        ItemGrid(
            friendsTab = friendsTab,
            ui = ui,
            previewSkin = previewSkin,
            previewTheme = previewTheme,
            onSkin = { previewSkinId = it.id },
            onTheme = { previewThemeId = it.id },
            modifier = gridModifier
        )
    }

    TodoriBackground(BackgroundTone.Calm, modifier.fillMaxSize()) {
        FloatingDecor()
        Column(
            Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            if (twoPane) {
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        TopBar(onBack)
                        PreviewCard(previewSkin, previewTheme, Modifier.weight(1f).fillMaxWidth())
                        ActionRow(action, ui, viewModel::setDevPremium)
                    }
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CustomizeTabs(tab, { tab = it }, Modifier.fillMaxWidth())
                        itemGrid(Modifier.weight(1f).fillMaxWidth())
                    }
                }
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    Column(
                        Modifier.fillMaxHeight().widthIn(max = MAX_CONTENT_WIDTH).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TopBar(onBack)
                        PreviewCard(previewSkin, previewTheme, Modifier.fillMaxWidth().heightIn(min = 176.dp, max = 204.dp))
                        CustomizeTabs(tab, { tab = it }, Modifier.fillMaxWidth())
                        itemGrid(Modifier.weight(1f).fillMaxWidth())
                        ActionRow(action, ui, viewModel::setDevPremium)
                    }
                }
            }
        }
    }

    if (showGate) PremiumGateSheet(onDismiss = { showGate = false })
}

private class Action(val text: String, val icon: androidx.compose.ui.graphics.vector.ImageVector, val enabled: Boolean, val onClick: () -> Unit)

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        GlassIconButton(
            icon = TodoriIcons.ChevronLeft,
            contentDescription = stringResource(R.string.record_back),
            onClick = onBack,
            size = 48.dp
        )
        Spacer(Modifier.width(14.dp))
        Text(
            stringResource(R.string.customize_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ItemGrid(
    friendsTab: Boolean,
    ui: CustomizeUiState,
    previewSkin: SkinId,
    previewTheme: ThemeId,
    onSkin: (SkinId) -> Unit,
    onTheme: (ThemeId) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 92.dp),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 4.dp)
    ) {
        if (friendsTab) {
            items(SkinId.entries.toList(), key = { it.id }) { skin ->
                ItemCard(
                    name = stringResource(skin.nameRes()),
                    applied = skin == ui.applied.skin,
                    previewed = skin == previewSkin,
                    locked = ui.isLocked(skin),
                    onClick = { onSkin(skin) }
                ) { SkinThumb(skin) }
            }
        } else {
            items(ThemeId.entries.toList(), key = { it.id }) { theme ->
                ItemCard(
                    name = stringResource(theme.nameRes()),
                    applied = theme == ui.applied.theme,
                    previewed = theme == previewTheme,
                    locked = ui.isLocked(theme),
                    onClick = { onTheme(theme) }
                ) { ThemeThumb(theme) }
            }
        }
    }
}

@Composable
private fun ActionRow(action: Action, ui: CustomizeUiState, onDevPremium: (Boolean) -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        JellyButton(
            text = action.text,
            icon = action.icon,
            onClick = action.onClick,
            enabled = action.enabled,
            modifier = Modifier.fillMaxWidth()
        )
        if (ui.devToggleAvailable) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.customize_dev_premium),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.weight(1f)
                )
                Switch(checked = ui.isPremium, onCheckedChange = onDevPremium)
            }
        }
    }
}

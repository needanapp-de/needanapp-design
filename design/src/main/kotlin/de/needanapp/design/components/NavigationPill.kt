package de.needanapp.design.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.needanapp.design.R
import de.needanapp.design.theme.DesignColors
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.LocalDesignEffects
import de.needanapp.design.theme.emphasizedSpec
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import de.needanapp.design.myzel.ActionPill as MyzelActionPill
import de.needanapp.design.theme.isMyzelDesign
import de.needanapp.design.myzel.ToolbarPill as MyzelToolbarPill
import de.needanapp.design.myzel.NavigationPillWithToolbar as MyzelNavigationPillWithToolbar
import de.needanapp.design.myzel.NavigationPill as MyzelNavigationPill
import de.needanapp.design.myzel.NavigationPillWithMenu as MyzelNavigationPillWithMenu
import de.needanapp.design.myzel.FloatingSurface as MyzelFloatingSurface

/**
 * An entry of the [NavigationPill]; [label] is shown next to the icon when active and read by TalkBack, [badgeCount]
 * shows a counter on the icon (e.g. available updates).
 */
data class PillItem(val icon: ImageVector, val label: String, val badgeCount: Int? = null)

/**
 * A small round button left or right of the pill, e.g. "add" or "options", or an entry of a [ToolbarPill];
 * [badgeCount] shows a counter, [enabled] false greys it out. [value] is a short state at the end of an entry of a
 * [FloatingMenuEntry], e.g. "3 active" or a version. [shortLabel] stands under the icon in a [PillBar], where
 * [contentDescription] would be too long, e.g. "Tab" for "New tab"; TalkBack still reads [contentDescription].
 */
data class PillAction(
    val icon: ImageVector,
    val contentDescription: String,
    val badgeCount: Int? = null,
    val enabled: Boolean = true,
    val value: String? = null,
    val shortLabel: String? = null,
    val onClick: () -> Unit,
)

/**
 * A small round button at the end of a [NavigationPill] that unfolds upwards into a pill of [actions] with their
 * labels, e.g. folder options and settings. [icon] and [label] describe the folded button, which adds up the counters
 * of [actions]; the last of [actions] stands closest to the button.
 */
data class PillMenu(val icon: ImageVector, val label: String, val actions: List<PillAction>)

/**
 * A small button at the start of a [NavigationPill] that turns the pill itself into a bar of actions, e.g. what can be
 * created in the open folder: each an icon above its short label, the [groups] apart by fine lines, and a cross in the
 * button's place that turns it back. [caption] floats above the bar meanwhile, e.g. "New in Download". [icon] and
 * [label] describe the button.
 */
data class PillBar(val icon: ImageVector, val label: String, val groups: List<List<PillAction>>, val caption: String? = null)

/**
 * Hides the pill (or a floating card) while scrolling down and brings it back when scrolling up; attach
 * [nestedScrollConnection], or report [onScroll] from content outside of Compose.
 */
@Stable
class PillScrollBehavior internal constructor() {
    var isVisible by mutableStateOf(true)
        internal set

    // distance scrolled in the current direction; slow drags come in many tiny steps, so single deltas aren't enough
    private var travelled = 0f

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            onScroll(available.y)
            return Offset.Zero
        }
    }

    /**
     * For content that scrolls outside of Compose, e.g. a WebView: [delta] in pixels the content moved, negative while
     * scrolling down (toward the end), as in [nestedScrollConnection].
     */
    fun onScroll(delta: Float) {
        if (delta == 0f) return
        travelled = if (travelled * delta < 0f) delta else travelled + delta
        if (travelled < -SCROLL_THRESHOLD) isVisible = false
        if (travelled > SCROLL_THRESHOLD) isVisible = true
    }

    fun show() {
        isVisible = true
        travelled = 0f
    }

    private companion object {
        const val SCROLL_THRESHOLD = 24f
    }
}

@Composable
fun rememberPillScrollBehavior() = remember { PillScrollBehavior() }

private val PillShape = PillDefaults.shape

/** The active entry and the buttons in the pill, a little less round than the pill around them. */
private val EntryShape = PillDefaults.buttonShape
private val PillHeight = 52.dp
private val ItemHeight = 42.dp
private val PillPadding = 5.dp
private val EntryGap = 4.dp
private val PillGap = 12.dp
private val MenuShape = PillDefaults.shape
private val DividerHeight = 24.dp
private const val DIVIDER_ALPHA = 0.3f
private const val DISABLED_ALPHA = 0.38f
private const val SCRIM_ALPHA = 0.2f
private val BarHeight = 58.dp
private val BarKeyHeight = 48.dp
private val BarKeyPadding = 6.dp
private val BarKeyMaxWidth = 64.dp
private val CaptionShape = RoundedCornerShape(14.dp)

/**
 * The floating navigation pill at the bottom center (Material 3 Expressive style): icons, the active one wider with its label on
 * primaryContainer, optional small buttons on both sides. Translucent with background blur when [hazeState] is given
 * and blur is on, opaque otherwise. Hides itself while the keyboard is open.
 *
 * [trailingMenu] puts a menu button at the end that unfolds upwards into its labelled actions, e.g. folder options and
 * settings. It shows while [trailingAction] is null, so a context such as a selection can put its own button there for
 * a while. Choosing an entry or a page, a tap outside or Back fold the menu, as does hiding the pill.
 *
 * [leadingBar] puts a button at the start that turns the pill into the bar of its actions (see [PillBar]); it shows
 * while [leadingAction] is null. Choosing an action, the cross, Back, another page or hiding the pill turn it back.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NavigationPill(
    items: List<PillItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    leadingAction: PillAction? = null,
    trailingAction: PillAction? = null,
    visible: Boolean = true,
    trailingMenu: PillMenu? = null,
    leadingBar: PillBar? = null,
) {
    val bar = leadingBar.takeIf { leadingAction == null }
    var barOpen by rememberSaveable { mutableStateOf(false) }
    val isKeyboardOpen = WindowInsets.isImeVisible
    LaunchedEffect(bar == null, visible, isKeyboardOpen) { if (bar == null || !visible || isKeyboardOpen) barOpen = false }
    LaunchedEffect(selectedIndex) { barOpen = false }
    if (bar != null) BarPill(bar, barOpen, modifier, hazeState) { barOpen = false }
    // the bar takes the pill's place, which slides away meanwhile
    val leading = leadingAction ?: bar?.let { PillAction(it.icon, it.label) { barOpen = true } }
    val pillVisible = visible && !(bar != null && barOpen)
    if (isMyzelDesign) {
        // the Myzel style has a pill of its own for a menu, without the small buttons
        if (trailingMenu != null && trailingAction == null) {
            return MyzelNavigationPillWithMenu(
                items, selectedIndex, onSelect, trailingMenu.actions, trailingMenu.icon, trailingMenu.label, modifier,
                hazeState, pillVisible,
            )
        }
        return MyzelNavigationPill(
            items, selectedIndex, onSelect, modifier, hazeState, leading, trailingAction, pillVisible,
        )
    }
    val entries: @Composable (fold: () -> Unit) -> Unit = { fold ->
        Row(
            modifier = Modifier.padding(PillPadding).selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(EntryGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                PillEntry(item, isSelected = index == selectedIndex) {
                    fold()
                    onSelect(index)
                }
            }
        }
    }
    if (trailingMenu == null) {
        PillRow(modifier, hazeState, leading, trailingAction, pillVisible) { entries {} }
    } else {
        PillRowWithMenu(modifier, hazeState, leading, trailingAction, trailingMenu, pillVisible, selectedIndex, entries)
    }
}

/**
 * The pill turned into the bar of [bar] while [open]: a cross that turns it back, then the actions, each group apart by
 * a fine line, all as wide as the widest label needs and as the screen allows. The caption floats above.
 */
@Composable
private fun BarPill(bar: PillBar, open: Boolean, modifier: Modifier, hazeState: HazeState?, onClose: () -> Unit) {
    BackHandler(enabled = open, onBack = onClose)
    val groups = bar.groups.filter { it.isNotEmpty() }
    val count = groups.sumOf { it.size }
    val measurer = rememberTextMeasurer()
    val style = MaterialTheme.typography.labelSmall
    val density = LocalDensity.current
    val labels = groups.flatten().map { it.shortLabel ?: it.contentDescription }
    val widestLabel = remember(labels, style, density) {
        with(density) { labels.maxOfOrNull { measurer.measure(it, style, maxLines = 1).size.width.toDp() } ?: 0.dp }
    }
    AnimatedVisibility(
        visible = open,
        enter = slideInVertically(emphasizedSpec()) { it } + fadeIn(emphasizedSpec()),
        exit = slideOutVertically(emphasizedSpec()) { it } + fadeOut(emphasizedSpec()),
        modifier = modifier,
    ) {
        BoxWithConstraints(Modifier.navigationBarsPadding().padding(bottom = 16.dp, start = 16.dp, end = 16.dp)) {
            // all keys alike: room for the widest label, no wider than a share of the screen allows
            val room = (maxWidth - PillPadding * 2 - ItemHeight - (EntryGap * 2 + 1.dp) * groups.size) / count.coerceAtLeast(1)
            val keyWidth = (widestLabel + BarKeyPadding * 2).coerceIn(ItemHeight, BarKeyMaxWidth).coerceAtMost(room)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                bar.caption?.let { caption ->
                    FloatingSurface(CaptionShape, hazeState, Modifier.padding(bottom = 8.dp)) {
                        MarqueeText(
                            caption,
                            Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = LocalDesignColors.current.onPillVariant,
                        )
                    }
                }
                FloatingSurface(PillShape, hazeState, Modifier.height(BarHeight)) {
                    Row(Modifier.padding(horizontal = PillPadding), verticalAlignment = Alignment.CenterVertically) {
                        BarClose(onClose)
                        groups.forEach { group ->
                            PillDivider()
                            group.forEach { action ->
                                BarKey(action, keyWidth) {
                                    onClose()
                                    action.onClick()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** The cross at the start of a [BarPill], on the color of the pill's active entry. */
@Composable
private fun BarClose(onClose: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val label = stringResource(R.string.nd_bar_close)
    Box(
        modifier = Modifier
            .size(ItemHeight)
            .clip(EntryShape)
            .background(colors.primaryContainer)
            .clickable(role = Role.Button, interactionSource = null, indication = ripple()) {
                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                onClose()
            }
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Icon(Icons.Rounded.Close, contentDescription = null, tint = colors.onPrimaryContainer)
    }
}

/** An action of a [BarPill]: its icon above its short label; TalkBack reads the full one. */
@Composable
private fun BarKey(action: PillAction, width: Dp, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val design = LocalDesignColors.current
    val alpha = if (action.enabled) 1f else DISABLED_ALPHA
    Column(
        modifier = Modifier
            .width(width)
            .heightIn(min = BarKeyHeight)
            .clip(EntryShape)
            .clickable(enabled = action.enabled, role = Role.Button, interactionSource = null, indication = ripple()) {
                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                onClick()
            }
            .clearAndSetSemantics { contentDescription = action.contentDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ActionIcon(action, design.onPill.copy(alpha = alpha))
        Spacer(Modifier.height(2.dp))
        Text(
            action.shortLabel ?: action.contentDescription,
            color = design.onPillVariant.copy(alpha = alpha),
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/**
 * The pill in a context without navigation, e.g. while another app picks a file: one main action such as
 * "Save here" in the look of the active entry, or – with [onClick] null – just a hint. Small buttons and behavior as
 * in [NavigationPill]. An action without small buttons floats alone, without the bar around it: the bar would only
 * frame the one button, in some color worlds as a dark edge. A hint keeps the bar, its text needs the surface to stay
 * readable over the content.
 */
@Composable
fun ActionPill(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    hazeState: HazeState? = null,
    leadingAction: PillAction? = null,
    trailingAction: PillAction? = null,
    visible: Boolean = true,
) {
    if (isMyzelDesign) return MyzelActionPill(
        label = label, modifier = modifier, icon = icon, onClick = onClick, hazeState = hazeState,
        leadingAction = leadingAction, trailingAction = trailingAction, visible = visible,
    )
    if (onClick != null && leadingAction == null && trailingAction == null) {
        LoneActionButton(label, icon, onClick, modifier, visible)
        return
    }
    PillRow(modifier, hazeState, leadingAction, trailingAction, visible) {
        val colors = MaterialTheme.colorScheme
        val isAction = onClick != null
        val content = if (isAction) colors.onPrimaryContainer else LocalDesignColors.current.onPillVariant
        val haptics = LocalHapticFeedback.current
        Box(
            modifier = Modifier
                .padding(PillPadding)
                .heightIn(min = ItemHeight)
                .clip(EntryShape)
                .then(
                    if (onClick != null) {
                        Modifier
                            .background(colors.primaryContainer)
                            .clickable(role = Role.Button, interactionSource = null, indication = ripple()) {
                                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                                onClick()
                            }
                    } else {
                        Modifier
                    },
                )
                .animateContentSize(emphasizedSpec()),
            contentAlignment = Alignment.Center,
        ) {
            ActionLabel(label, icon, content, Modifier.padding(horizontal = 16.dp))
        }
    }
}

/**
 * The action of an [ActionPill] alone: the button in the accent container, as tall as the pill and in its shape, with
 * the pill's shadow and its place, sliding away like it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LoneActionButton(label: String, icon: ImageVector?, onClick: () -> Unit, modifier: Modifier, visible: Boolean) {
    val colors = MaterialTheme.colorScheme
    val haptics = LocalHapticFeedback.current
    val shadows = LocalDesignEffects.current.shadows
    AnimatedVisibility(
        visible = visible && !WindowInsets.isImeVisible,
        enter = slideInVertically(emphasizedSpec()) { it } + fadeIn(emphasizedSpec()),
        exit = slideOutVertically(emphasizedSpec()) { it } + fadeOut(emphasizedSpec()),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                .heightIn(min = PillHeight)
                .then(if (shadows) Modifier.shadow(6.dp, PillShape) else Modifier)
                .clip(PillShape)
                .background(colors.primaryContainer)
                .clickable(role = Role.Button, interactionSource = null, indication = ripple()) {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    onClick()
                },
            contentAlignment = Alignment.Center,
        ) {
            ActionLabel(label, icon, colors.onPrimaryContainer, Modifier.padding(horizontal = 20.dp))
        }
    }
}

@Composable
private fun ActionLabel(label: String, icon: ImageVector?, color: Color, modifier: Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        icon?.let {
            Icon(it, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(label, color = color, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

/**
 * A floating pill of plain actions for what is on screen, e.g. edit, share and delete while viewing a picture. Same
 * look, small side buttons and behavior as [NavigationPill], but no entry is ever "active".
 */
@Composable
@Deprecated("Actions for what is on screen float in a FloatingItemCard")
fun ToolbarPill(
    actions: List<PillAction>,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    leadingAction: PillAction? = null,
    trailingAction: PillAction? = null,
    visible: Boolean = true,
) {
    if (isMyzelDesign) return MyzelToolbarPill(
        actions = actions, modifier = modifier, hazeState = hazeState, leadingAction = leadingAction,
        trailingAction = trailingAction, visible = visible,
    )
    PillRow(modifier, hazeState, leadingAction, trailingAction, visible) {
        Row(
            modifier = Modifier.padding(PillPadding),
            horizontalArrangement = Arrangement.spacedBy(EntryGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions.forEach { ToolbarEntry(it) }
        }
    }
}

/**
 * The pill while entries are selected, in place of the [NavigationPill]: a check with [count] on primaryContainer at
 * the start, then [actions] for the selection, e.g. share, copy, delete and "more". TalkBack reads [countDescription]
 * instead of the bare number. Where the pill gets too narrow, the count gives way to the actions (the header usually
 * says it too). Look and behavior as in [ToolbarPill].
 */
@Composable
@Deprecated("Use a SelectionCard")
fun SelectionPill(
    count: Int,
    actions: List<PillAction>,
    modifier: Modifier = Modifier,
    countDescription: String = pluralStringResource(R.plurals.nd_selected_count, count, count),
    hazeState: HazeState? = null,
    visible: Boolean = true,
) {
    // sliding away once the selection is gone, it keeps showing what was selected last
    val last = remember { LastSelection(count, actions, countDescription) }
    if (visible) {
        last.count = count
        last.actions = actions
        last.description = countDescription
    }
    val shownCount = last.count
    val shownActions = last.actions
    val shownDescription = last.description
    PillRow(modifier, hazeState, leadingAction = null, trailingAction = null, visible) {
        BoxWithConstraints {
            val actionsWidth = PillPadding * 2 + ItemHeight * shownActions.size + EntryGap * (shownActions.size - 1)
            val showCount = actionsWidth + EntryGap + CountMinWidth <= maxWidth
            Row(
                modifier = Modifier.padding(PillPadding),
                horizontalArrangement = Arrangement.spacedBy(EntryGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showCount) SelectionCount(shownCount, shownDescription)
                shownActions.forEach { ToolbarEntry(it) }
            }
        }
    }
}

/** What a [SelectionPill] showed while it was visible, for sliding away; read each time the pill composes. */
private class LastSelection(var count: Int, var actions: List<PillAction>, var description: String)

/** The room the count of a [SelectionPill] needs for up to three digits. */
private val CountMinWidth = 76.dp

@Composable
private fun SelectionCount(count: Int, description: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier
            .heightIn(min = ItemHeight)
            .clip(EntryShape)
            .background(colors.primaryContainer)
            .clearAndSetSemantics { contentDescription = description }
            .animateContentSize(emphasizedSpec())
            .padding(start = 12.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.onPrimaryContainer, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(6.dp))
        Text(count.toString(), color = colors.onPrimaryContainer, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

/**
 * The [NavigationPill] with a [ToolbarPill] right next to it, for screens outside of the navigation such as settings.
 * The active entry shows its label only when the longest label of [items] fits next to the toolbar (so it doesn't come
 * and go between the pages); otherwise it is marked by its primaryContainer background alone. Behavior as in
 * [NavigationPill].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
@Deprecated("Use a NavigationPill, and a FloatingItemCard for the actions on screen")
fun NavigationPillWithToolbar(
    items: List<PillItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    actions: List<PillAction>,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    visible: Boolean = true,
) {
    if (isMyzelDesign) return MyzelNavigationPillWithToolbar(
        items = items, selectedIndex = selectedIndex, onSelect = onSelect, actions = actions, modifier = modifier,
        hazeState = hazeState, visible = visible,
    )
    val isKeyboardOpen = WindowInsets.isImeVisible
    AnimatedVisibility(
        visible = visible && !isKeyboardOpen,
        enter = slideInVertically(emphasizedSpec()) { it } + fadeIn(emphasizedSpec()),
        exit = slideOutVertically(emphasizedSpec()) { it } + fadeOut(emphasizedSpec()),
        modifier = modifier,
    ) {
        BoxWithConstraints(Modifier.navigationBarsPadding().padding(bottom = 16.dp, start = 16.dp, end = 16.dp)) {
            val showLabels = labelsFit(items, actions.size, maxWidth)
            Row(
                horizontalArrangement = Arrangement.spacedBy(PillGap),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FloatingSurface(PillShape, hazeState, Modifier.height(PillHeight)) {
                    Row(
                        modifier = Modifier.padding(PillPadding).selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(EntryGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        items.forEachIndexed { index, item ->
                            PillEntry(item, isSelected = index == selectedIndex, showLabel = showLabels) { onSelect(index) }
                        }
                    }
                }
                FloatingSurface(PillShape, hazeState, Modifier.height(PillHeight)) {
                    Row(
                        modifier = Modifier.padding(PillPadding),
                        horizontalArrangement = Arrangement.spacedBy(EntryGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        actions.forEach { ToolbarEntry(it) }
                    }
                }
            }
        }
    }
}

/**
 * The [NavigationPill] in the bottom center with a small round menu button at the end, which unfolds upwards into a
 * pill of [menuActions] with their labels, e.g. for destinations outside the navigation such as settings or about.
 * Choosing an entry, a tap outside or Back fold it again, as does hiding the pill. [menuIcon] and [menuLabel] describe
 * the folded button, which adds up the counters of [menuActions]. The active entry shows its label only when the
 * longest label of [items] fits while the pill stays centered; otherwise it is marked by its primaryContainer
 * background alone. Behavior as in [NavigationPill]; for this plain menu a [NavigationPill] with a trailingMenu
 * ([PillMenu]) does the same.
 *
 * With [myzelMenu] the needanapp look takes the menu of Myzel: the button inside the pill after a fine line, the active
 * entry with a small LED, the menu unfolding above the pill without dimming the screen, [menuHeader] at its top (e.g. a
 * [DeviceDisplay]), the [PillAction.value]s of its entries at their end and [menuButtonContent] in place of the
 * button's icon and the cross (e.g. the app's logo). The Myzel style always has that menu, in its own graphite.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NavigationPillWithMenu(
    items: List<PillItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    menuActions: List<PillAction>,
    menuIcon: ImageVector,
    menuLabel: String,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    visible: Boolean = true,
    menuHeader: (@Composable () -> Unit)? = null,
    menuButtonContent: (@Composable (expanded: Boolean) -> Unit)? = null,
    myzelMenu: Boolean = false,
) {
    if (isMyzelDesign) return MyzelNavigationPillWithMenu(
        items = items, selectedIndex = selectedIndex, onSelect = onSelect, menuActions = menuActions,
        menuIcon = menuIcon, menuLabel = menuLabel, modifier = modifier, hazeState = hazeState, visible = visible,
        menuHeader = menuHeader, menuButtonContent = menuButtonContent,
    )
    if (myzelMenu) {
        return NeedanappPillWithMenu(
            items, selectedIndex, onSelect, menuActions, menuIcon, menuLabel, modifier, hazeState, visible, menuHeader,
            menuButtonContent,
        )
    }
    val isKeyboardOpen = WindowInsets.isImeVisible
    val shown = visible && !isKeyboardOpen
    var expanded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(shown) { if (!shown) expanded = false }
    // another page, chosen in the pill or by swiping, folds the menu
    LaunchedEffect(selectedIndex) { expanded = false }
    MenuOverlay(expanded, onFold = { expanded = false }, modifier) {
        AnimatedVisibility(
            visible = shown,
            enter = slideInVertically(emphasizedSpec()) { it } + fadeIn(emphasizedSpec()),
            exit = slideOutVertically(emphasizedSpec()) { it } + fadeOut(emphasizedSpec()),
        ) {
            BoxWithConstraints(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            ) {
                // the pill stays centered, so the button's room is kept free on both sides
                val showLabels = navigationWidthWithLabel(items) <= maxWidth - (PillHeight + PillGap) * 2
                FloatingSurface(PillShape, hazeState, Modifier.align(Alignment.BottomCenter).height(PillHeight)) {
                    Row(
                        modifier = Modifier.padding(PillPadding).selectableGroup(),
                        horizontalArrangement = Arrangement.spacedBy(EntryGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        items.forEachIndexed { index, item ->
                            PillEntry(item, isSelected = index == selectedIndex, showLabel = showLabels) {
                                expanded = false
                                onSelect(index)
                            }
                        }
                    }
                }
                Column(Modifier.align(Alignment.BottomEnd), horizontalAlignment = Alignment.End) {
                    UnfoldingMenu(expanded, menuActions, hazeState) { expanded = false }
                    FloatingSurface(PillShape, hazeState, Modifier.size(PillHeight)) {
                        Box(Modifier.size(PillHeight), contentAlignment = Alignment.Center) {
                            MenuButton(menuIcon, menuLabel, menuActions, expanded) { expanded = !expanded }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The small buttons and the pill of [NavigationPill] with a menu button at the end ([trailingAction] takes its place
 * while set). The menu unfolds upwards from the button's end edge; meanwhile a scrim over the whole screen folds it at
 * a tap.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PillRowWithMenu(
    modifier: Modifier,
    hazeState: HazeState?,
    leadingAction: PillAction?,
    trailingAction: PillAction?,
    menu: PillMenu,
    visible: Boolean,
    selectedIndex: Int,
    pill: @Composable (fold: () -> Unit) -> Unit,
) {
    val isKeyboardOpen = WindowInsets.isImeVisible
    val shown = visible && !isKeyboardOpen
    val canUnfold = trailingAction == null
    var expanded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(shown, canUnfold) { if (!shown || !canUnfold) expanded = false }
    // another page, chosen in the pill or by swiping, folds the menu
    LaunchedEffect(selectedIndex) { expanded = false }
    val fold = { expanded = false }
    MenuOverlay(expanded, onFold = fold, modifier) {
        AnimatedVisibility(
            visible = shown,
            enter = slideInVertically(emphasizedSpec()) { it } + fadeIn(emphasizedSpec()),
            exit = slideOutVertically(emphasizedSpec()) { it } + fadeOut(emphasizedSpec()),
        ) {
            var rowWidth by remember { mutableIntStateOf(0) }
            BoxWithConstraints(
                Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            ) {
                Row(
                    modifier = Modifier.align(Alignment.BottomCenter).onSizeChanged { rowWidth = it.width },
                    horizontalArrangement = Arrangement.spacedBy(PillGap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FloatingSurface(PillShape, hazeState, Modifier.height(PillHeight)) {
                        Row(Modifier.padding(horizontal = PillPadding), verticalAlignment = Alignment.CenterVertically) {
                            leadingAction?.let { action ->
                                PillActionButton(action.copy(onClick = { fold(); action.onClick() }))
                                PillDivider()
                            }
                            Box(Modifier.weight(1f, fill = false)) { pill(fold) }
                            PillDivider()
                            if (trailingAction != null) {
                                PillActionButton(trailingAction)
                            } else {
                                MenuButton(menu.icon, menu.label, menu.actions, expanded) { expanded = !expanded }
                            }
                        }
                    }
                }
                // the row is centered, so its end, where the button is, lies this far from the edge
                val endInset = with(LocalDensity.current) { ((constraints.maxWidth - rowWidth) / 2).coerceAtLeast(0).toDp() }
                UnfoldingMenu(
                    expanded = expanded,
                    actions = menu.actions,
                    hazeState = hazeState,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = endInset, bottom = PillHeight),
                    onChoose = fold,
                )
            }
        }
    }
}

/** Covers the screen with a scrim while a menu is unfolded; a tap on it or Back calls [onFold]. */
@Composable
private fun MenuOverlay(expanded: Boolean, onFold: () -> Unit, modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    BackHandler(enabled = expanded, onBack = onFold)
    val currentOnFold by rememberUpdatedState(onFold)
    val scrim by animateColorAsState(
        if (expanded) MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA) else Color.Transparent,
        emphasizedSpec(),
        label = "menuScrim",
    )
    // unfolded, the bar covers the screen, so a tap anywhere outside the menu folds it
    Box(
        modifier = modifier
            .then(if (expanded) Modifier.fillMaxSize() else Modifier)
            .background(scrim)
            .then(if (expanded) Modifier.pointerInput(Unit) { detectTapGestures { currentOnFold() } } else Modifier),
        contentAlignment = Alignment.BottomCenter,
        content = content,
    )
}

/** The labelled [actions] of a menu, unfolding upwards out of its button below at the bottom end. */
@Composable
private fun UnfoldingMenu(
    expanded: Boolean,
    actions: List<PillAction>,
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
    onChoose: () -> Unit,
) {
    AnimatedVisibility(
        visible = expanded,
        modifier = modifier,
        enter = expandVertically(emphasizedSpec(), expandFrom = Alignment.Bottom, clip = false) +
            scaleIn(emphasizedSpec(), initialScale = 0.6f, transformOrigin = TransformOrigin(1f, 1f)) +
            fadeIn(emphasizedSpec()),
        exit = shrinkVertically(emphasizedSpec(), shrinkTowards = Alignment.Bottom, clip = false) +
            scaleOut(emphasizedSpec(), targetScale = 0.6f, transformOrigin = TransformOrigin(1f, 1f)) +
            fadeOut(emphasizedSpec()),
    ) {
        FloatingSurface(MenuShape, hazeState, Modifier.padding(bottom = PillGap)) {
            Column(Modifier.padding(PillPadding).width(IntrinsicSize.Max)) {
                actions.forEach { action ->
                    MenuEntry(action) {
                        onChoose()
                        action.onClick()
                    }
                }
            }
        }
    }
}

/** The menu button at the end of the pill: [icon] while folded, a cross while unfolded. */
@Composable
private fun MenuButton(
    icon: ImageVector,
    label: String,
    actions: List<PillAction>,
    expanded: Boolean,
    onToggle: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val closeLabel = stringResource(R.string.nd_menu_close)
    val rotation by animateFloatAsState(if (expanded) 90f else 0f, emphasizedSpec(), label = "menuButton")
    Box(
        modifier = Modifier
            .size(ItemHeight)
            .clip(EntryShape)
            .clickable(role = Role.Button, interactionSource = null, indication = ripple()) {
                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                onToggle()
            }
            .semantics { contentDescription = if (expanded) closeLabel else label },
        contentAlignment = Alignment.Center,
    ) {
        val tint = LocalDesignColors.current.onPill
        val count = actions.sumOf { it.badgeCount ?: 0 }
        Box(Modifier.rotate(rotation)) {
            if (expanded) {
                Icon(Icons.Rounded.Close, contentDescription = null, tint = tint)
            } else if (count > 0) {
                BadgedBox(badge = { Badge { Text(count.toString()) } }) { Icon(icon, contentDescription = null, tint = tint) }
            } else {
                Icon(icon, contentDescription = null, tint = tint)
            }
        }
    }
}

/** An entry of the unfolded menu of [NavigationPillWithMenu]: icon and label, as wide as the widest entry. */
@Composable
private fun MenuEntry(action: PillAction, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val tint = LocalDesignColors.current.onPill.let { if (action.enabled) it else it.copy(alpha = DISABLED_ALPHA) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = ItemHeight)
            .clip(EntryShape)
            .clickable(enabled = action.enabled, role = Role.Button, interactionSource = null, indication = ripple()) {
                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                onClick()
            }
            .padding(start = 12.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ActionIcon(action, tint)
        Spacer(Modifier.width(16.dp))
        Text(action.contentDescription, color = tint, style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

/** Whether both pills of [NavigationPillWithToolbar] fit into [maxWidth] with the longest label of [items] shown. */
@Composable
private fun labelsFit(items: List<PillItem>, actionCount: Int, maxWidth: Dp): Boolean {
    val toolbar = PillPadding * 2 + EntryGap * (actionCount - 1) + ItemHeight * actionCount
    return navigationWidthWithLabel(items) + PillGap + toolbar <= maxWidth
}

/** The width of the navigation pill when its active entry shows the longest label of [items]. */
@Composable
private fun navigationWidthWithLabel(items: List<PillItem>): Dp {
    val measurer = rememberTextMeasurer()
    val style = MaterialTheme.typography.labelLarge
    val density = LocalDensity.current
    val longestLabel = items.maxOfOrNull { item ->
        with(density) { measurer.measure(item.label, style, maxLines = 1).size.width.toDp() }
    } ?: 0.dp
    // the active entry: padding, icon, gap, label, padding; the others: round buttons
    return PillPadding * 2 + EntryGap * (items.size - 1) + ItemHeight * (items.size - 1) +
        14.dp + 24.dp + 8.dp + longestLabel + 14.dp
}

@Composable
private fun ToolbarEntry(action: PillAction) {
    val haptics = LocalHapticFeedback.current
    val tint = LocalDesignColors.current.onPillVariant.let { if (action.enabled) it else it.copy(alpha = DISABLED_ALPHA) }
    Box(
        modifier = Modifier
            .size(ItemHeight)
            .clip(EntryShape)
            .clickable(enabled = action.enabled, role = Role.Button, interactionSource = null, indication = ripple()) {
                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                action.onClick()
            }
            .semantics { contentDescription = action.contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        ActionIcon(action, tint)
    }
}

@Composable
private fun ActionIcon(action: PillAction, tint: Color) {
    val count = action.badgeCount
    if (count != null && count > 0) {
        BadgedBox(badge = { Badge { Text(count.toString()) } }) {
            Icon(action.icon, contentDescription = null, tint = tint)
        }
    } else {
        Icon(action.icon, contentDescription = null, tint = tint)
    }
}

/** Small buttons left and right of a floating pill surface; slides away while hidden or while the keyboard is open. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PillRow(
    modifier: Modifier,
    hazeState: HazeState?,
    leadingAction: PillAction?,
    trailingAction: PillAction?,
    visible: Boolean,
    pill: @Composable () -> Unit,
) {
    val isKeyboardOpen = WindowInsets.isImeVisible
    AnimatedVisibility(
        visible = visible && !isKeyboardOpen,
        enter = slideInVertically(emphasizedSpec()) { it } + fadeIn(emphasizedSpec()),
        exit = slideOutVertically(emphasizedSpec()) { it } + fadeOut(emphasizedSpec()),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.navigationBarsPadding().padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(PillGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FloatingSurface(PillShape, hazeState, Modifier.height(PillHeight)) {
                Row(Modifier.padding(horizontal = PillPadding), verticalAlignment = Alignment.CenterVertically) {
                    leadingAction?.let {
                        PillActionButton(it)
                        PillDivider()
                    }
                    Box(Modifier.weight(1f, fill = false)) { pill() }
                    trailingAction?.let {
                        PillDivider()
                        PillActionButton(it)
                    }
                }
            }
        }
    }
}

@Composable
private fun PillEntry(item: PillItem, isSelected: Boolean, showLabel: Boolean = true, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val background by animateColorAsState(if (isSelected) colors.primaryContainer else Color.Transparent, emphasizedSpec(), label = "pillEntry")
    val content by animateColorAsState(if (isSelected) colors.onPrimaryContainer else LocalDesignColors.current.onPillVariant, emphasizedSpec(), label = "pillContent")
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .heightIn(min = ItemHeight)
            .widthIn(min = ItemHeight)
            .clip(EntryShape)
            .background(background)
            .selectable(
                selected = isSelected,
                role = Role.Tab,
                interactionSource = null,
                indication = ripple(),
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onClick()
            }
            .semantics { contentDescription = item.label }
            .animateContentSize(emphasizedSpec()),
        contentAlignment = Alignment.Center,
    ) {
        val withLabel = isSelected && showLabel
        Row(
            modifier = Modifier.padding(horizontal = if (withLabel) 14.dp else 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ItemIcon(item, content)
            if (withLabel) {
                Spacer(Modifier.width(8.dp))
                Text(item.label, color = content, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ItemIcon(item: PillItem, tint: Color) {
    val count = item.badgeCount
    if (count != null && count > 0) {
        BadgedBox(badge = { Badge { Text(count.toString()) } }) {
            Icon(item.icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        }
    } else {
        Icon(item.icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
    }
}

/** A button at the start or end of the pill, apart from its entries by a [PillDivider]. */
@Composable
private fun PillActionButton(action: PillAction) {
    Box(
        modifier = Modifier
            .size(ItemHeight)
            .clip(EntryShape)
            .clickable(enabled = action.enabled, role = Role.Button, interactionSource = null, indication = ripple(), onClick = action.onClick)
            .semantics { contentDescription = action.contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        val tint = LocalDesignColors.current.onPill
        ActionIcon(action, if (action.enabled) tint else tint.copy(alpha = DISABLED_ALPHA))
    }
}

/** A fine line in the pill between its entries and a button. */
@Composable
private fun PillDivider() {
    Box(
        Modifier
            .padding(horizontal = EntryGap)
            .width(1.dp)
            .height(DividerHeight)
            .background(LocalDesignColors.current.onPillVariant.copy(alpha = DIVIDER_ALPHA)),
    )
}

/**
 * The floating surface of pill and buttons: the pill color of [LocalDesignColors] (surfaceContainerHighest unless a color
 * world brings its own) over a blur of the content behind it, as opaque as the "pill transparency" setting says (55 % by
 * default, at least [DesignColors.pillMinOpacity]), or fully opaque without blur. Shadow unless effects are reduced.
 * What lies on it is drawn in the pill's content color. With [progress] the whole surface is a progress bar that fills in
 * the accent color, what lies on it taking the accent's content colors where the fill reaches. [progressColor] and
 * [onProgressColor] fill it in another color instead, e.g. the error container for deleting.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FloatingSurface(
    shape: Shape,
    hazeState: HazeState?,
    modifier: Modifier = Modifier,
    progress: SurfaceProgress? = null,
    progressColor: Color = Color.Unspecified,
    onProgressColor: Color = Color.Unspecified,
    content: @Composable () -> Unit,
) {
    if (isMyzelDesign) return MyzelFloatingSurface(
        shape = shape, hazeState = hazeState, modifier = modifier, content = content,
    )
    val effects = LocalDesignEffects.current
    val design = LocalDesignColors.current
    val container = design.pill
    val opacity = maxOf(effects.pillOpacity, design.pillMinOpacity)
    val background = MaterialTheme.colorScheme.background
    val useBlur = effects.blur && hazeState != null
    val surfaceModifier = modifier
        .then(if (effects.shadows) Modifier.shadow(6.dp, shape) else Modifier)
        .clip(shape)
        .then(
            if (useBlur) {
                Modifier.hazeBlur(
                    HazeInput.Sources(hazeState!!),
                    HazeBlurStyle {
                        blurRadius(24.dp)
                        // only shows where the blurred source draws nothing; the content's background, not the pill's color
                        backgroundColor(background)
                        colorEffects(listOf(HazeColorEffect.tint(container.copy(alpha = opacity))))
                    },
                )
            } else {
                Modifier.background(container)
            },
        )
    val fillModifier = if (progress != null) {
        val fill = progressColor.takeOrElse { design.signal }
        val onFill = onProgressColor.takeOrElse { design.onSignal }
        val colors = remember(container, design.onPill, design.onPillVariant, fill, onFill) {
            ProgressColors(container, design.onPill, design.onPillVariant, fill, onFill)
        }
        val fromRight = LocalLayoutDirection.current == LayoutDirection.Rtl
        rememberProgressBand(progress)?.let { Modifier.progressFill(it, colors, fromRight) } ?: Modifier.background(colors.tint)
    } else {
        Modifier
    }
    Box(surfaceModifier.then(fillModifier), contentAlignment = Alignment.Center) {
        CompositionLocalProvider(LocalContentColor provides design.onPill) { content() }
    }
}

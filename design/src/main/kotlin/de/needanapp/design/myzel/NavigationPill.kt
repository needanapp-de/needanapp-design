package de.needanapp.design.myzel

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.needanapp.design.R
import de.needanapp.design.theme.DesignType
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.LocalDesignEffects
import de.needanapp.design.theme.emphasizedSpec
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import de.needanapp.design.components.*

private val PillShape = RoundedCornerShape(22.dp)
private val PillHeight = PillBarHeight
private val ItemHeight = 44.dp
private val PillPadding = 4.dp
private val EntryIconSize = 22.dp
private val ActiveEntryPadding = 12.dp
private val EntryLedDiameter = 6.dp
private val EntryGap = 4.dp
private val PillGap = 10.dp
private val MenuShape = RoundedCornerShape(26.dp)
private const val DISABLED_ALPHA = 0.38f
private const val SCRIM_ALPHA = 0.45f

/**
 * The floating navigation bar at the bottom: graphite, the icons in grey, the active entry as a key with an LED and its
 * label, optional round buttons on both sides. Over a blur of the content when [hazeState] is given and blur is on.
 * Hides itself while the keyboard is open.
 */
@Composable
internal fun NavigationPill(
    items: List<PillItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    leadingAction: PillAction? = null,
    trailingAction: PillAction? = null,
    visible: Boolean = true,
) {
    PillRow(modifier, hazeState, leadingAction, trailingAction, visible) {
        Row(
            modifier = Modifier.padding(PillPadding).selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(EntryGap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEachIndexed { index, item ->
                PillEntry(item, isSelected = index == selectedIndex) { onSelect(index) }
            }
        }
    }
}

/**
 * The bar in a context without navigation, e.g. while another app picks a file: one main action such as
 * "Save here" as a key in the signal color, or – with [onClick] null – just a hint. Small buttons and behavior as
 * in [NavigationPill].
 */
@Composable
internal fun ActionPill(
    label: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    hazeState: HazeState? = null,
    leadingAction: PillAction? = null,
    trailingAction: PillAction? = null,
    visible: Boolean = true,
) {
    PillRow(modifier, hazeState, leadingAction, trailingAction, visible) {
        val colors = MaterialTheme.colorScheme
        val design = LocalDesignColors.current
        val isAction = onClick != null
        val content = if (isAction) design.onSignal else design.onGraphiteMuted
        val haptics = LocalHapticFeedback.current
        Box(
            modifier = Modifier
                .padding(PillPadding)
                .heightIn(min = ItemHeight)
                .clip(KeyShape)
                .then(
                    if (onClick != null) {
                        Modifier
                            .background(design.signal)
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
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                icon?.let {
                    Icon(it, contentDescription = null, tint = content, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(label.uppercase(), color = content, style = DesignType.key.copy(fontSize = 12.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/**
 * A floating bar of plain actions for what is on screen, e.g. edit, share and delete while viewing a picture. Same
 * look, small side buttons and behavior as [NavigationPill], but no entry is ever "active".
 */
@Composable
internal fun ToolbarPill(
    actions: List<PillAction>,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    leadingAction: PillAction? = null,
    trailingAction: PillAction? = null,
    visible: Boolean = true,
) {
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
 * The [NavigationPill] with a [ToolbarPill] right next to it, for screens outside of the navigation such as settings.
 * The active entry shows its label only when the longest label of [items] fits next to the toolbar (so it doesn't come
 * and go between the pages); otherwise it is marked by its key alone. Behavior as in [NavigationPill].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NavigationPillWithToolbar(
    items: List<PillItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    actions: List<PillAction>,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    visible: Boolean = true,
) {
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
 * The navigation bar across the bottom with a round menu button at its end, which unfolds upwards into a graphite
 * panel of [menuActions] with their labels, e.g. for destinations outside the navigation such as settings or about.
 * [menuHeader] sits at the top of the panel, e.g. a small display. Choosing an entry, a tap outside or Back fold it
 * again, as does hiding the bar. [menuIcon] and [menuLabel] describe the folded button, which adds up the counters of
 * [menuActions]; [menuButtonContent] replaces its icon (and the cross while unfolded), e.g. by the app's logo.
 * Behavior as in [NavigationPill].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NavigationPillWithMenu(
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
) {
    val isKeyboardOpen = WindowInsets.isImeVisible
    val shown = visible && !isKeyboardOpen
    var expanded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(shown) { if (!shown) expanded = false }
    BackHandler(enabled = expanded) { expanded = false }
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
            .then(if (expanded) Modifier.pointerInput(Unit) { detectTapGestures { expanded = false } } else Modifier),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = shown,
            enter = slideInVertically(emphasizedSpec()) { it } + fadeIn(emphasizedSpec()),
            exit = slideOutVertically(emphasizedSpec()) { it } + fadeOut(emphasizedSpec()),
        ) {
            BoxWithConstraints(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 16.dp, start = 16.dp, end = 16.dp)) {
                // the bar is only as wide as with its longest label active, so it neither wastes room nor changes
                // between the pages; it stands in the middle unless it would come too close to the menu button
                val room = maxWidth - PillHeight - PillGap
                val barWidth = navigationWidthWithLabel(items).coerceAtMost(room)
                val start = minOf((maxWidth - barWidth) / 2, room - barWidth).coerceAtLeast(0.dp)
                FloatingSurface(
                    PillShape,
                    hazeState,
                    Modifier.align(Alignment.BottomStart).offset(x = start).width(barWidth).height(PillHeight),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(PillPadding).selectableGroup(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        items.forEachIndexed { index, item ->
                            PillEntry(item, isSelected = index == selectedIndex) { onSelect(index) }
                        }
                    }
                }
                Column(Modifier.align(Alignment.BottomEnd), horizontalAlignment = Alignment.End) {
                    AnimatedVisibility(
                        visible = expanded,
                        enter = expandVertically(emphasizedSpec(), expandFrom = Alignment.Bottom, clip = false) +
                            scaleIn(emphasizedSpec(), initialScale = 0.6f, transformOrigin = TransformOrigin(1f, 1f)) +
                            fadeIn(emphasizedSpec()),
                        exit = shrinkVertically(emphasizedSpec(), shrinkTowards = Alignment.Bottom, clip = false) +
                            scaleOut(emphasizedSpec(), targetScale = 0.6f, transformOrigin = TransformOrigin(1f, 1f)) +
                            fadeOut(emphasizedSpec()),
                    ) {
                        FloatingSurface(MenuShape, hazeState, Modifier.padding(bottom = PillGap)) {
                            // a fixed size, so the menu stays put while e.g. the display at its top changes
                            Column(Modifier.width(MenuWidth).padding(8.dp)) {
                                menuHeader?.let {
                                    it()
                                    Spacer(Modifier.height(4.dp))
                                }
                                menuActions.forEachIndexed { index, action ->
                                    FloatingMenuEntry(action, divider = index < menuActions.lastIndex) {
                                        expanded = false
                                        action.onClick()
                                    }
                                }
                            }
                        }
                    }
                    MenuButton(menuIcon, menuLabel, menuActions, expanded, hazeState, menuButtonContent) { expanded = !expanded }
                }
            }
        }
    }
}

/** The round graphite button of [NavigationPillWithMenu]: [icon] (or [content]) while folded, pressed in while unfolded. */
@Composable
private fun MenuButton(
    icon: ImageVector,
    label: String,
    actions: List<PillAction>,
    expanded: Boolean,
    hazeState: HazeState?,
    content: (@Composable (expanded: Boolean) -> Unit)?,
    onToggle: () -> Unit,
) {
    val closeLabel = stringResource(R.string.nd_menu_close)
    val tint = LocalDesignColors.current.onGraphite
    FloatingKnob(if (expanded) closeLabel else label, pressed = expanded, hazeState = hazeState, onClick = onToggle) {
        val count = actions.sumOf { it.badgeCount ?: 0 }
        when {
            content != null -> content(expanded)
            expanded -> Icon(Icons.Rounded.Close, contentDescription = null, tint = tint)
            else -> Icon(icon, contentDescription = null, tint = tint)
        }
        if (count > 0 && !expanded) CountBadge(count, Modifier.align(Alignment.TopEnd).offset(x = (-8).dp, y = 8.dp))
    }
}

/** Whether both pills of [NavigationPillWithToolbar] fit into [maxWidth] with the longest label of [items] shown. */
@Composable
private fun labelsFit(items: List<PillItem>, actionCount: Int, maxWidth: Dp): Boolean {
    val toolbar = PillPadding * 2 + EntryGap * (actionCount - 1) + ItemHeight * actionCount
    return navigationWidthWithLabel(items) + PillGap + toolbar <= maxWidth
}

/** The width of the navigation bar when its active entry shows the longest label of [items]. */
@Composable
private fun navigationWidthWithLabel(items: List<PillItem>): Dp {
    val measurer = rememberTextMeasurer()
    val style = DesignType.label
    val density = LocalDensity.current
    val longestLabel = items.maxOfOrNull { item ->
        with(density) { measurer.measure(item.label.uppercase(), style, maxLines = 1).size.width.toDp() }
    } ?: 0.dp
    // the active entry: padding, LED (twice its diameter for the glow), gap, icon, gap, label, padding; the others:
    // square keys; one dp more against rounding, so the label is never cut short
    return PillPadding * 2 + EntryGap * (items.size - 1) + ItemHeight * (items.size - 1) +
        ActiveEntryPadding * 2 + EntryLedDiameter * 2 + 2.dp + EntryIconSize + 8.dp + longestLabel + 1.dp
}

@Composable
private fun ToolbarEntry(action: PillAction) {
    val haptics = LocalHapticFeedback.current
    val tint = LocalDesignColors.current.onGraphite.let { if (action.enabled) it else it.copy(alpha = DISABLED_ALPHA) }
    Box(
        modifier = Modifier
            .size(ItemHeight)
            .clip(CircleShape)
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
    Box {
        Icon(action.icon, contentDescription = null, tint = tint)
        val count = action.badgeCount
        if (count != null && count > 0) CountBadge(count, Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-6).dp))
    }
}

/** Small buttons left and right of a floating bar; slides away while hidden or while the keyboard is open. */
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
            leadingAction?.let { PillActionButton(it, hazeState) }
            FloatingSurface(PillShape, hazeState, Modifier.height(PillHeight).weight(1f, fill = false)) { pill() }
            trailingAction?.let { PillActionButton(it, hazeState) }
        }
    }
}

private val KeyShape = RoundedCornerShape(24.dp)

/** An entry of the bar: a grey icon, or active a graphite key with an LED in the signal color, the icon and its label. */
@Composable
private fun PillEntry(item: PillItem, isSelected: Boolean, showLabel: Boolean = true, onClick: () -> Unit) {
    val design = LocalDesignColors.current
    val background by animateColorAsState(if (isSelected) design.graphiteKey else Color.Transparent, emphasizedSpec(), label = "pillEntry")
    val content by animateColorAsState(if (isSelected) design.onGraphite else design.onGraphiteMuted, emphasizedSpec(), label = "pillContent")
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .heightIn(min = ItemHeight)
            .widthIn(min = ItemHeight)
            .clip(KeyShape)
            .background(background)
            .then(if (isSelected) Modifier.keyEdge(KeyStyle.GRAPHITE) else Modifier)
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
            modifier = Modifier.padding(horizontal = if (withLabel) ActiveEntryPadding else (ItemHeight - EntryIconSize) / 2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (withLabel) {
                Led(design.signal, diameter = EntryLedDiameter)
                Spacer(Modifier.width(2.dp))
            }
            ItemIcon(item, content)
            if (withLabel) {
                Spacer(Modifier.width(8.dp))
                Text(item.label.uppercase(), color = content, style = DesignType.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun ItemIcon(item: PillItem, tint: Color) {
    Box {
        Icon(item.icon, contentDescription = null, tint = tint, modifier = Modifier.size(EntryIconSize))
        val count = item.badgeCount
        if (count != null && count > 0) CountBadge(count, Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-7).dp))
    }
}

@Composable
private fun PillActionButton(action: PillAction, hazeState: HazeState?) {
    FloatingSurface(CircleShape, hazeState, Modifier.size(PillHeight)) {
        Box(
            modifier = Modifier
                .size(PillHeight)
                .clickable(enabled = action.enabled, role = Role.Button, interactionSource = null, indication = ripple(), onClick = action.onClick)
                .semantics { contentDescription = action.contentDescription },
            contentAlignment = Alignment.Center,
        ) {
            val tint = LocalDesignColors.current.onGraphite
            ActionIcon(action, if (action.enabled) tint else tint.copy(alpha = DISABLED_ALPHA))
        }
    }
}

/**
 * The floating surface of bars and round buttons: graphite over a blur of the content behind it, or plain graphite
 * without blur, with a darker lower edge. Content on it takes the light color of graphite. Shadow unless effects are
 * reduced.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FloatingSurface(shape: Shape, hazeState: HazeState?, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val effects = LocalDesignEffects.current
    val design = LocalDesignColors.current
    val container = design.bar
    val background = MaterialTheme.colorScheme.background
    val useBlur = effects.blur && hazeState != null
    val surfaceModifier = modifier
        .then(if (effects.shadows) Modifier.shadow(8.dp, shape, ambientColor = Color.Black, spotColor = Color.Black) else Modifier)
        .clip(shape)
        .then(
            if (useBlur) {
                Modifier.hazeBlur(
                    HazeInput.Sources(hazeState!!),
                    HazeBlurStyle {
                        blurRadius(24.dp)
                        // only shows where the blurred source draws nothing; the content's background, not the bar's color
                        backgroundColor(background)
                        colorEffects(listOf(HazeColorEffect.tint(container.copy(alpha = BAR_OPACITY))))
                    },
                )
            } else {
                Modifier.background(container)
            },
        )
        .then(if (design.isDark) Modifier.border(1.dp, design.barBorder, shape) else Modifier)
        .keyEdge(KeyStyle.GRAPHITE)
    CompositionLocalProvider(LocalContentColor provides design.onGraphite) {
        Box(surfaceModifier, contentAlignment = Alignment.Center) { content() }
    }
}

/** The width of the unfolded menu of [NavigationPillWithMenu]. */
private val MenuWidth = 264.dp

/** An entry of a graphite menu: compact, but still a full touch target. */
private val MenuEntryHeight = 48.dp
private const val BAR_OPACITY = 0.92f

package de.needanapp.design.components

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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import de.needanapp.design.theme.emphasizedSpec
import de.needanapp.design.theme.isNeedanappDesign
import dev.chrisbanes.haze.HazeState

/** Height of the graphite bar and its round buttons; screens keep this much (plus the gap below) free at the bottom. */
val PillBarHeight = 52.dp
/**
 * A round graphite button as high as the navigation bar, e.g. one that unfolds a menu of [FloatingMenuEntry]s above it;
 * [pressed] shows it pressed in while that menu is open.
 */
@Composable
fun FloatingKnob(
    contentDescription: String,
    pressed: Boolean,
    hazeState: HazeState?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    val size = if (isNeedanappDesign) NeedanappKnobSize else PillHeight
    FloatingSurface(CircleShape, hazeState, modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(size)
                .pressedIn(pressed)
                .clickable(role = Role.Button, interactionSource = null, indication = ripple()) {
                    haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                    onClick()
                }
                .semantics { this.contentDescription = contentDescription },
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

/**
 * Pressed into the panel: a shadow inside along the top edge, e.g. a button whose menu is open; in the needanapp look
 * on a slightly deeper fill with a fainter shadow.
 */
@Composable
fun Modifier.pressedIn(pressed: Boolean): Modifier {
    if (!pressed) return this
    if (isNeedanappDesign) {
        val fill = LocalDesignColors.current.pressed
        return drawWithContent {
            drawRect(fill)
            drawContent()
            drawRect(Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.22f), 1f to Color.Transparent, endY = 6.dp.toPx()))
        }
    }
    return drawWithContent {
        drawContent()
        drawRect(Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.55f), 1f to Color.Transparent, endY = size.height * 0.45f))
    }
}

/**
 * An entry of a graphite menu such as the unfolded one of [NavigationPillWithMenu]: its icon on a small round key, the
 * label, a counter or [PillAction.value] at the end and, with [divider], a fine line below.
 */
@Composable
fun FloatingMenuEntry(action: PillAction, divider: Boolean = false, onClick: () -> Unit = action.onClick) {
    if (isNeedanappDesign) {
        NeedanappMenuEntry(action, onClick)
        return
    }
    val haptics = LocalHapticFeedback.current
    val design = LocalDesignColors.current
    val tint = design.onGraphite.let { if (action.enabled) it else it.copy(alpha = DISABLED_ALPHA) }
    val line = design.onGraphite.copy(alpha = 0.1f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MenuEntryHeight)
            .drawWithContent {
                drawContent()
                if (divider) drawRect(line, Offset(0f, size.height - 1.dp.toPx()), Size(size.width, 1.dp.toPx()))
            }
            .clickable(enabled = action.enabled, role = Role.Button, interactionSource = null, indication = ripple()) {
                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                onClick()
            }
            .padding(start = 6.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier.size(34.dp).clip(CircleShape).background(design.graphiteKey).keyEdge(KeyStyle.GRAPHITE),
            contentAlignment = Alignment.Center,
        ) {
            Icon(action.icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(
            action.contentDescription,
            color = tint,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        val count = action.badgeCount ?: 0
        if (count > 0) {
            Spacer(Modifier.width(12.dp))
            CountBadge(count)
        } else if (action.value != null) {
            Spacer(Modifier.width(12.dp))
            Text(action.value.uppercase(), style = DesignType.labelSmall, color = design.onGraphiteMuted, maxLines = 1)
        }
    }
}

/**
 * [FloatingMenuEntry] in the needanapp look: the plain icon, the label, a counter or the value at the end, in the
 * colors of the pill the menu lies on ([FloatingSurface]), also where a color world makes it dark.
 */
@Composable
private fun NeedanappMenuEntry(action: PillAction, onClick: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val design = LocalDesignColors.current
    val tint = design.onPill.let { if (action.enabled) it else it.copy(alpha = DISABLED_ALPHA) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MenuEntryHeight)
            .clickable(enabled = action.enabled, role = Role.Button, interactionSource = null, indication = ripple()) {
                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                onClick()
            }
            .padding(horizontal = NeedanappMenuPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(action.icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Text(
            action.contentDescription,
            color = tint,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        val count = action.badgeCount ?: 0
        if (count > 0) {
            Spacer(Modifier.width(12.dp))
            CountBadge(count)
        } else if (action.value != null) {
            Spacer(Modifier.width(12.dp))
            Text(action.value, style = DesignType.value, color = design.onPillVariant, maxLines = 1)
        }
    }
}

/**
 * A switch in a graphite menu, e.g. "Ignore updates" among [FloatingMenuEntry]s: [title] on the left, the switch at the
 * end, a tap anywhere on the row toggles it; with [divider] a fine line below.
 */
@Composable
fun FloatingMenuSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
    divider: Boolean = false,
) {
    val design = LocalDesignColors.current
    val needanapp = isNeedanappDesign
    // in the needanapp look the menu lies on the pill's color
    val ink = if (needanapp) design.onPill else design.onGraphite
    val tint = if (enabled) ink else ink.copy(alpha = DISABLED_ALPHA)
    val line = design.onGraphite.copy(alpha = 0.1f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MenuEntryHeight)
            .drawWithContent {
                drawContent()
                if (divider && !needanapp) drawRect(line, Offset(0f, size.height - 1.dp.toPx()), Size(size.width, 1.dp.toPx()))
            }
            .toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(start = if (needanapp) NeedanappMenuPadding else 10.dp, end = if (needanapp) 12.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = tint, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = null,
            enabled = enabled,
            colors = if (needanapp) signalSwitchColors() else graphiteSwitchColors(),
        )
    }
}

/** A small capitalized heading between the entries of a graphite menu, e.g. "UPDATES". */
@Composable
fun FloatingMenuLabel(text: String) {
    val design = LocalDesignColors.current
    MonoLabel(
        text,
        color = if (isNeedanappDesign) design.onPillVariant else design.onGraphiteMuted,
        style = DesignType.labelSmall,
        modifier = Modifier.padding(
            start = if (isNeedanappDesign) NeedanappMenuPadding else 10.dp,
            end = 10.dp,
            top = 10.dp,
            bottom = 2.dp,
        ),
    )
}

/** Colors of a switch on graphite: the signal color when on, a dark track when off. */
@Composable
private fun graphiteSwitchColors(): SwitchColors {
    val signal = LocalDesignColors.current.signal
    val thumb = Color(0xFFF3F3F0)
    val off = Color(0xFF3A3A36)
    return SwitchDefaults.colors(
        checkedThumbColor = thumb,
        checkedTrackColor = signal,
        checkedBorderColor = signal,
        checkedIconColor = signal,
        uncheckedThumbColor = thumb,
        uncheckedTrackColor = off,
        uncheckedBorderColor = off,
        disabledCheckedTrackColor = signal.copy(alpha = DISABLED_ALPHA),
        disabledCheckedThumbColor = thumb.copy(alpha = DISABLED_ALPHA),
        disabledUncheckedTrackColor = off.copy(alpha = DISABLED_ALPHA),
        disabledUncheckedThumbColor = thumb.copy(alpha = DISABLED_ALPHA),
        disabledUncheckedBorderColor = off.copy(alpha = DISABLED_ALPHA),
    )
}

/** A small counter in the signal color, e.g. available updates on an icon. */
@Composable
fun CountBadge(count: Int, modifier: Modifier = Modifier) {
    val design = LocalDesignColors.current
    Box(
        modifier = modifier.heightIn(min = 16.dp).widthIn(min = 16.dp).clip(RoundedCornerShape(8.dp)).background(design.signal).padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(count.toString(), style = DesignType.label.copy(fontSize = 10.sp, letterSpacing = 0.sp), color = design.onSignal)
    }
}

/**
 * [NavigationPillWithMenu] in the needanapp look: one raised pill in the middle with the entries, a fine line and the
 * menu button at its end; the menu unfolds above it, flush with its right end, without dimming the screen.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun NeedanappPillWithMenu(
    items: List<PillItem>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    menuActions: List<PillAction>,
    menuIcon: ImageVector,
    menuLabel: String,
    modifier: Modifier,
    hazeState: HazeState?,
    visible: Boolean,
    menuHeader: (@Composable () -> Unit)?,
    menuButtonContent: (@Composable (expanded: Boolean) -> Unit)?,
) {
    val isKeyboardOpen = WindowInsets.isImeVisible
    val shown = visible && !isKeyboardOpen
    var expanded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(shown) { if (!shown) expanded = false }
    BackHandler(enabled = expanded) { expanded = false }
    // unfolded, the bar covers the screen, so a tap anywhere outside the menu folds it
    Box(
        modifier = modifier
            .then(if (expanded) Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { expanded = false } } else Modifier),
        contentAlignment = Alignment.BottomCenter,
    ) {
        AnimatedVisibility(
            visible = shown,
            enter = slideInVertically(emphasizedSpec()) { it } + fadeIn(emphasizedSpec()),
            exit = slideOutVertically(emphasizedSpec()) { it } + fadeOut(emphasizedSpec()),
        ) {
            BoxWithConstraints(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 16.dp, start = 16.dp, end = 16.dp)) {
                // as wide as with its longest label active, so it doesn't change between the pages
                val barWidth = needanappBarWidth(items).coerceAtMost(maxWidth)
                val menuWidth = MenuWidth.coerceAtMost(maxWidth)
                AnimatedVisibility(
                    visible = expanded,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(x = (barWidth - menuWidth) / 2)
                        .padding(bottom = PillHeight + NeedanappMenuGap),
                    enter = expandVertically(emphasizedSpec(), expandFrom = Alignment.Bottom, clip = false) +
                        scaleIn(emphasizedSpec(), initialScale = 0.6f, transformOrigin = TransformOrigin(1f, 1f)) +
                        fadeIn(emphasizedSpec()),
                    exit = shrinkVertically(emphasizedSpec(), shrinkTowards = Alignment.Bottom, clip = false) +
                        scaleOut(emphasizedSpec(), targetScale = 0.6f, transformOrigin = TransformOrigin(1f, 1f)) +
                        fadeOut(emphasizedSpec()),
                ) {
                    FloatingSurface(NeedanappPillShape, hazeState) {
                        // a fixed size, so the menu stays put while e.g. the display at its top changes
                        Column(Modifier.width(menuWidth).padding(vertical = 6.dp)) {
                            menuHeader?.let {
                                Box(Modifier.padding(start = 8.dp, top = 2.dp, end = 8.dp, bottom = 6.dp)) { it() }
                            }
                            menuActions.forEach { action ->
                                FloatingMenuEntry(action) {
                                    expanded = false
                                    action.onClick()
                                }
                            }
                        }
                    }
                }
                FloatingSurface(
                    NeedanappPillShape,
                    hazeState,
                    Modifier.align(Alignment.BottomCenter).width(barWidth).height(PillHeight),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(NeedanappPillPadding),
                        horizontalArrangement = Arrangement.spacedBy(EntryGap),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).selectableGroup(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            items.forEachIndexed { index, item ->
                                NeedanappPillEntry(item, isSelected = index == selectedIndex, showLabel = true) { onSelect(index) }
                            }
                        }
                        Box(Modifier.padding(horizontal = 4.dp).width(1.dp).height(NeedanappDividerHeight).background(LocalDesignColors.current.divider))
                        NeedanappMenuButton(menuIcon, menuLabel, menuActions, expanded, menuButtonContent) { expanded = !expanded }
                    }
                }
            }
        }
    }
}

/** The menu button inside the needanapp pill: [icon] (or [content]) while folded, pressed in while unfolded. */
@Composable
private fun NeedanappMenuButton(
    icon: ImageVector,
    label: String,
    actions: List<PillAction>,
    expanded: Boolean,
    content: (@Composable (expanded: Boolean) -> Unit)?,
    onToggle: () -> Unit,
) {
    val closeLabel = stringResource(R.string.nd_menu_close)
    val design = LocalDesignColors.current
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .size(NeedanappItemHeight)
            .clip(NeedanappEntryShape)
            .pressedIn(expanded)
            .clickable(role = Role.Button, interactionSource = null, indication = ripple()) {
                haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                onToggle()
            }
            .semantics { contentDescription = if (expanded) closeLabel else label },
        contentAlignment = Alignment.Center,
    ) {
        val count = actions.sumOf { it.badgeCount ?: 0 }
        when {
            content != null -> content(expanded)
            expanded -> Icon(Icons.Rounded.Close, contentDescription = null, tint = design.onPill)
            else -> Icon(icon, contentDescription = null, tint = design.onPillVariant)
        }
        if (count > 0 && !expanded) CountBadge(count, Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp))
    }
}

/**
 * An entry of the needanapp pill: an icon in the pill's muted color, or active on the accent's container with a small
 * LED, the icon and its label.
 */
@Composable
private fun NeedanappPillEntry(item: PillItem, isSelected: Boolean, showLabel: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val background by animateColorAsState(if (isSelected) scheme.primaryContainer else Color.Transparent, emphasizedSpec(), label = "pillEntry")
    // inactive in the pill's colors, as in NavigationPill: a color world may make the pill dark
    val inactive = LocalDesignColors.current.onPillVariant
    val content by animateColorAsState(
        if (isSelected) scheme.onPrimaryContainer else inactive, emphasizedSpec(), label = "pillContent",
    )
    val haptics = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .heightIn(min = NeedanappItemHeight)
            .widthIn(min = NeedanappItemHeight)
            .clip(NeedanappEntryShape)
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
        val side = (NeedanappItemHeight - NeedanappIconSize) / 2
        Row(
            modifier = Modifier.padding(start = if (withLabel) NeedanappActiveStart else side, end = if (withLabel) NeedanappActiveEnd else side),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (withLabel) {
                Led(LocalDesignColors.current.signal, diameter = EntryLedDiameter)
                Spacer(Modifier.width(5.dp))
            }
            ItemIcon(item, content, NeedanappIconSize)
            if (withLabel) {
                Spacer(Modifier.width(8.dp))
                Text(item.label, color = content, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** The width of the needanapp pill with its longest label active, the line and the menu button. */
@Composable
private fun needanappBarWidth(items: List<PillItem>): Dp {
    val measurer = rememberTextMeasurer()
    val style = MaterialTheme.typography.labelLarge
    val density = LocalDensity.current
    val longestLabel = items.maxOfOrNull { item ->
        with(density) { measurer.measure(item.label, style, maxLines = 1).size.width.toDp() }
    } ?: 0.dp
    val activeEntry = NeedanappActiveStart + EntryLedDiameter * 2 + 5.dp + NeedanappIconSize + 8.dp + longestLabel + NeedanappActiveEnd
    // the others are square, then the line with its margins and the menu button; one dp more against rounding
    return NeedanappPillPadding * 2 + (NeedanappItemHeight + EntryGap) * (items.size - 1) + activeEntry +
        EntryGap + 9.dp + EntryGap + NeedanappItemHeight + 1.dp
}

@Composable
private fun ItemIcon(item: PillItem, tint: Color, size: Dp = EntryIconSize) {
    Box {
        Icon(item.icon, contentDescription = null, tint = tint, modifier = Modifier.size(size))
        val count = item.badgeCount
        if (count != null && count > 0) CountBadge(count, Modifier.align(Alignment.TopEnd).offset(x = 8.dp, y = (-7).dp))
    }
}

/** An entry of a graphite menu: compact, but still a full touch target. */
private val MenuEntryHeight = 48.dp
/** The width of the unfolded menu of [NavigationPillWithMenu]. */
private val MenuWidth = 264.dp

private val PillHeight = PillBarHeight
private val EntryGap = 4.dp
private val EntryLedDiameter = 6.dp
private val EntryIconSize = 22.dp
private const val DISABLED_ALPHA = 0.38f
private val NeedanappPillShape = RoundedCornerShape(12.dp)
private val NeedanappEntryShape = RoundedCornerShape(8.dp)
private val NeedanappPillPadding = 5.dp
private val NeedanappItemHeight = 42.dp
private val NeedanappIconSize = 24.dp
private val NeedanappActiveStart = 9.dp
private val NeedanappActiveEnd = 14.dp
private val NeedanappDividerHeight = 24.dp
private val NeedanappMenuGap = 12.dp
private val NeedanappMenuPadding = 18.dp
private val NeedanappKnobSize = 56.dp

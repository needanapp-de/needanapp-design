package de.needanapp.design.myzel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.em
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import de.needanapp.design.R
import de.needanapp.design.theme.DesignType
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.LocalDesignShapes
import de.needanapp.design.theme.emphasizedSpec
import de.needanapp.design.components.*

/**
 * A group of settings rows in the expressive style: rounded strongly outside, slightly inside, a small gap between
 * rows. With a [sectionState] and [id] the group is an accordion: its title is a row like the others, with [icon] and an
 * arrow at the end; expanded, the group's rows join it below.
 *
 * Consecutive groups form a block when each gets its [block] position: they join with the small gap between rows, and
 * only the block's top and bottom are rounded strongly. A block should consist of groups without plain titles.
 */
@Composable
internal fun SettingsGroup(
    title: String?,
    modifier: Modifier = Modifier,
    id: String? = null,
    sectionState: SectionState? = null,
    icon: ImageVector? = null,
    block: RowPosition = RowPosition.SINGLE,
    content: SettingsGroupScope.() -> Unit,
) {
    val scope = SettingsGroupScope().apply(content)
    val isCollapsible = id != null && sectionState != null
    if (isCollapsible) sectionState!!.register(id!!)
    val expanded = !isCollapsible || sectionState!!.isExpanded(id!!)
    // the rows continue the header's card, so none of them gets the strongly rounded top
    val hasHeaderRow = isCollapsible && title != null
    val rowGap = LocalDesignShapes.current.rowGap

    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = if (block.joinsAbove) 0.dp else GROUP_SPACING, bottom = if (block.joinsBelow) rowGap else GROUP_SPACING),
    ) {
        if (title != null) {
            if (isCollapsible) {
                SectionHeader(title, icon, expanded, block) { sectionState?.toggle(id!!) }
            } else {
                GroupTitle(title, icon)
            }
        }
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(emphasizedSpec()) + fadeIn(emphasizedSpec()),
            exit = shrinkVertically(emphasizedSpec()) + fadeOut(emphasizedSpec()),
        ) {
            Column(
                modifier = if (hasHeaderRow) Modifier.padding(top = rowGap) else Modifier,
                verticalArrangement = Arrangement.spacedBy(rowGap),
            ) {
                scope.rows.forEachIndexed { index, row ->
                    val isTop = index == 0 && !hasHeaderRow && !block.joinsAbove
                    val isBottom = index == scope.rows.lastIndex && !block.joinsBelow
                    row(RowPosition.of(isTop, isBottom))
                }
            }
        }
    }
}

@Composable
private fun GroupTitle(title: String, icon: ImageVector?) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 36.dp).padding(start = 4.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        MonoLabel(title, modifier = Modifier.weight(1f).semantics { heading() })
    }
}

/**
 * The title of a collapsible group, a row like the group's others: icon, title and an arrow at the end. Collapsed it is
 * rounded like the group would be at its [block] position; expanding turns the arrow down and narrows its lower
 * corners, so the rows below join it.
 */
@Composable
private fun SectionHeader(title: String, icon: ImageVector?, expanded: Boolean, block: RowPosition, onToggle: () -> Unit) {
    val shapes = LocalDesignShapes.current
    val progress by animateFloatAsState(if (expanded) 1f else 0f, emphasizedSpec(), label = "section")
    val top = if (block.joinsAbove) shapes.inner else shapes.group
    val bottom = lerp(if (block.joinsBelow) shapes.inner else shapes.group, shapes.inner, progress)
    val arrowDirection = if (LocalLayoutDirection.current == LayoutDirection.Rtl) 1f else -1f
    val actionLabel = stringResource(if (expanded) R.string.nd_section_expanded else R.string.nd_section_collapsed)
    val headerPosition = RowPosition.of(isTop = !block.joinsAbove, isBottom = !block.joinsBelow && progress < 0.5f)
    Surface(
        shape = RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom),
        color = LocalDesignColors.current.card,
        modifier = Modifier.fillMaxWidth().rowOutline(headerPosition),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClickLabel = actionLabel, onClick = onToggle)
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(16.dp))
            }
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).semantics { heading() })
            Spacer(Modifier.width(12.dp))
            Icon(
                Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(arrowDirection * 90f * (1f - progress)),
            )
        }
    }
}

/** The surface of one row in a [SettingsGroup]. */
@Composable
internal fun GroupRowSurface(
    position: RowPosition,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        shape = position.shape(),
        color = LocalDesignColors.current.card,
        modifier = modifier.fillMaxWidth().rowOutline(position),
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .alpha(if (enabled) 1f else DISABLED_ALPHA),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun RowScope.RowTexts(title: String, summary: String?, icon: ImageVector?) {
    if (icon != null) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(16.dp))
    }
    Column(Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
        if (summary != null) {
            Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** A plain row, clickable when [onClick] is set, with optional [trailing] content. */
@Composable
internal fun ListRow(
    position: RowPosition,
    title: String,
    summary: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    GroupRowSurface(
        position,
        modifier = if (onClick != null) Modifier.clickable(enabled = enabled, onClick = onClick) else Modifier,
        enabled = enabled,
    ) {
        RowTexts(title, summary, icon)
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

@Composable
internal fun SwitchRow(
    position: RowPosition,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    summary: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val haptics = LocalHapticFeedback.current
    GroupRowSurface(
        position,
        enabled = enabled,
        modifier = Modifier.toggleable(value = checked, enabled = enabled, role = Role.Switch) {
            haptics.performHapticFeedback(if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
            onCheckedChange(it)
        },
    ) {
        RowTexts(title, summary, icon)
        Spacer(Modifier.width(12.dp))
        // the whole row toggles, the switch itself is only the indicator
        Switch(checked = checked, onCheckedChange = null, enabled = enabled, colors = deviceSwitchColors())
    }
}

/** A row with a slider below its title; [valueText] shows the current value, e.g. "110 %". */
@Composable
internal fun SliderRow(
    position: RowPosition,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    valueText: String,
    steps: Int = 0,
    summary: String? = null,
    enabled: Boolean = true,
    onValueChangeFinished: (() -> Unit)? = null,
) {
    Surface(shape = position.shape(), color = LocalDesignColors.current.card, modifier = Modifier.fillMaxWidth().rowOutline(position)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp).alpha(if (enabled) 1f else DISABLED_ALPHA)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    if (summary != null) {
                        Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(valueText, style = DesignType.value, color = MaterialTheme.colorScheme.onSurface)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
                enabled = enabled,
                onValueChangeFinished = onValueChangeFinished,
                colors = signalSliderColors(),
            )
        }
    }
}

/** A row with a title (optional) and a segment control, next to the title when it fits there, otherwise below. */
@Composable
internal fun <T> SegmentedRow(
    position: RowPosition,
    title: String?,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    enabled: Boolean = true,
) {
    Surface(shape = position.shape(), color = LocalDesignColors.current.card, modifier = Modifier.fillMaxWidth().rowOutline(position)) {
        TitleBeside(
            title = { if (title != null) Text(title, style = MaterialTheme.typography.bodyLarge) },
            modifier = Modifier.heightIn(min = 56.dp).padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            SegmentControl(options, selected, label, onSelect, enabled = enabled)
        }
    }
}

/**
 * The segment control of the device look: a recessed strip with the options in the monospace font, the selected one
 * as a dark key.
 */
@Composable
internal fun <T> SegmentControl(
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    val design = LocalDesignColors.current
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (design.isDark) design.graphiteKey else scheme.surface)
            .padding(3.dp)
            .selectableGroup()
            .alpha(if (enabled) 1f else DISABLED_ALPHA),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .heightIn(min = 34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) scheme.secondaryContainer else Color.Transparent)
                    .selectable(selected = isSelected, enabled = enabled, role = Role.RadioButton) {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                        onSelect(option)
                    }
                    .padding(horizontal = 10.dp, vertical = 7.dp),
            ) {
                Text(
                    label(option).uppercase(),
                    style = DesignType.label.copy(letterSpacing = 0.08.em),
                    color = if (isSelected) scheme.onSecondaryContainer else scheme.onSurface,
                    maxLines = 1,
                )
            }
        }
    }
}

/** [title] at the start and [content] at the end of one line when both fit, otherwise [content] below [title]. */
@Composable
internal fun TitleBeside(title: @Composable () -> Unit, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(contents = listOf(title, content), modifier = modifier) { (titles, contents), constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val gap = 12.dp.roundToPx()
        val end = contents.map { it.measure(loose) }
        val endWidth = end.maxOfOrNull { it.width } ?: 0
        val besideFits = titles.isEmpty() || titles.all { it.maxIntrinsicWidth(Constraints.Infinity) + gap + endWidth <= constraints.maxWidth }
        if (besideFits) {
            val start = titles.map { it.measure(loose.copy(maxWidth = (constraints.maxWidth - endWidth - gap).coerceAtLeast(0))) }
            val height = maxOf(start.maxOfOrNull { it.height } ?: 0, end.maxOfOrNull { it.height } ?: 0, constraints.minHeight)
            layout(constraints.maxWidth, height) {
                start.forEach { it.placeRelative(0, (height - it.height) / 2) }
                end.forEach { it.placeRelative(constraints.maxWidth - it.width, (height - it.height) / 2) }
            }
        } else {
            val start = titles.map { it.measure(loose) }
            val titleHeight = start.maxOfOrNull { it.height } ?: 0
            val spacing = 8.dp.roundToPx()
            val height = maxOf(titleHeight + spacing + (end.maxOfOrNull { it.height } ?: 0), constraints.minHeight)
            layout(constraints.maxWidth, height) {
                start.forEach { it.placeRelative(0, 0) }
                end.forEach { it.placeRelative(0, titleHeight + spacing) }
            }
        }
    }
}

/**
 * The fine border of a row in a panel: rounded around the panel's ends, straight along the joins; between joined rows a
 * line in the divider color, indented like the content.
 */
@Composable
internal fun Modifier.rowOutline(position: RowPosition): Modifier {
    val outer = LocalDesignShapes.current.group
    val design = LocalDesignColors.current
    return drawWithContent {
        drawContent()
        val stroke = 1.dp.toPx()
        val half = stroke / 2
        val left = half
        val right = size.width - half
        val bottomY = size.height - half
        val radius = (outer.toPx() - half).coerceAtLeast(0f)
        val path = Path()
        val above = position.joinsAbove
        val below = position.joinsBelow
        when {
            !above && !below -> {
                path.moveTo(left, half + radius)
                path.arcTo(Rect(left, half, left + 2 * radius, half + 2 * radius), 180f, 90f, false)
                path.lineTo(right - radius, half)
                path.arcTo(Rect(right - 2 * radius, half, right, half + 2 * radius), 270f, 90f, false)
                path.lineTo(right, bottomY - radius)
                path.arcTo(Rect(right - 2 * radius, bottomY - 2 * radius, right, bottomY), 0f, 90f, false)
                path.lineTo(left + radius, bottomY)
                path.arcTo(Rect(left, bottomY - 2 * radius, left + 2 * radius, bottomY), 90f, 90f, false)
                path.close()
            }
            !above -> {
                path.moveTo(left, size.height)
                path.lineTo(left, half + radius)
                path.arcTo(Rect(left, half, left + 2 * radius, half + 2 * radius), 180f, 90f, false)
                path.lineTo(right - radius, half)
                path.arcTo(Rect(right - 2 * radius, half, right, half + 2 * radius), 270f, 90f, false)
                path.lineTo(right, size.height)
            }
            !below -> {
                path.moveTo(right, 0f)
                path.lineTo(right, bottomY - radius)
                path.arcTo(Rect(right - 2 * radius, bottomY - 2 * radius, right, bottomY), 0f, 90f, false)
                path.lineTo(left + radius, bottomY)
                path.arcTo(Rect(left, bottomY - 2 * radius, left + 2 * radius, bottomY), 90f, 90f, false)
                path.lineTo(left, 0f)
            }
            else -> {
                path.moveTo(left, 0f)
                path.lineTo(left, size.height)
                path.moveTo(right, 0f)
                path.lineTo(right, size.height)
            }
        }
        drawPath(path, design.cardBorder, style = Stroke(stroke))
        if (below) {
            val inset = 16.dp.toPx()
            drawRect(design.divider, Offset(inset, size.height - stroke), Size(size.width - 2 * inset, stroke))
        }
    }
}

/** A row that holds arbitrary content, e.g. a set of chips or color swatches. */
@Composable
internal fun ContentRow(position: RowPosition, title: String?, content: @Composable ColumnScope.() -> Unit) {
    Surface(shape = position.shape(), color = LocalDesignColors.current.card, modifier = Modifier.fillMaxWidth().rowOutline(position)) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 8.dp))
            }
            content()
        }
    }
}

/** Space below the last group, so the floating navigation pill doesn't cover it. */
@Composable
internal fun BottomPillSpacer() = Spacer(Modifier.height(104.dp))

private const val DISABLED_ALPHA = 0.38f

// above and below a group or block
private val GROUP_SPACING = 8.dp

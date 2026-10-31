package de.needanapp.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import de.needanapp.design.R
import de.needanapp.design.theme.LocalDesignShapes
import de.needanapp.design.theme.LocalThemeSettings
import de.needanapp.design.theme.ThemeSettingsStore
import de.needanapp.design.theme.emphasizedSpec
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import de.needanapp.design.myzel.SettingsGroup as MyzelSettingsGroup
import de.needanapp.design.theme.isMyzelDesign
import de.needanapp.design.myzel.GroupRowSurface as MyzelGroupRowSurface
import de.needanapp.design.myzel.ListRow as MyzelListRow
import de.needanapp.design.myzel.SwitchRow as MyzelSwitchRow
import de.needanapp.design.myzel.SliderRow as MyzelSliderRow
import de.needanapp.design.myzel.SegmentedRow as MyzelSegmentedRow
import de.needanapp.design.myzel.ContentRow as MyzelContentRow

/** Scope of a [SettingsGroup]: every [row] gets the group's corner shape by its position. */
class SettingsGroupScope internal constructor() {
    internal val rows = mutableListOf<@Composable (RowPosition) -> Unit>()

    fun row(content: @Composable (RowPosition) -> Unit) {
        rows += content
    }
}

/** Where a row sits in its group (or a group in its block), which decides which corners are rounded strongly. */
enum class RowPosition {
    SINGLE, FIRST, MIDDLE, LAST;

    /** Something lies directly above, joined without a gap. */
    val joinsAbove get() = this == MIDDLE || this == LAST

    /** Something lies directly below, joined without a gap. */
    val joinsBelow get() = this == FIRST || this == MIDDLE

    companion object {
        /** The position of an element that starts or ends its group or block. */
        fun of(isTop: Boolean, isBottom: Boolean) = when {
            isTop && isBottom -> SINGLE
            isTop -> FIRST
            isBottom -> LAST
            else -> MIDDLE
        }
    }
}

/** The expressive shape of a row: the group's outer radius at its ends, small inner radii between rows. */
@Composable
fun RowPosition.shape(): RoundedCornerShape {
    val shapes = LocalDesignShapes.current
    val outer = shapes.group
    val inner = shapes.inner
    return when (this) {
        RowPosition.SINGLE -> RoundedCornerShape(outer)
        RowPosition.FIRST -> RoundedCornerShape(topStart = outer, topEnd = outer, bottomStart = inner, bottomEnd = inner)
        RowPosition.MIDDLE -> RoundedCornerShape(inner)
        RowPosition.LAST -> RoundedCornerShape(topStart = inner, topEnd = inner, bottomStart = outer, bottomEnd = outer)
    }
}

/**
 * Open sections of a screen. All sections start collapsed; the one opened last stays open for the next visit of the
 * screen, also after an app restart (kept in the package's DataStore). With
 * [ThemeSettings.exclusiveSections][de.needanapp.design.theme.ThemeSettings.exclusiveSections] the sections behave as
 * exclusive accordions: expanding one collapses the other sections of the screen.
 */
@Stable
class SectionState internal constructor(
    rememberedOpen: String?,
    isExclusive: Boolean,
    private val screenIds: MutableSet<String>,
    private val onRemember: (screenIds: Set<String>, lastOpen: String?) -> Unit,
) {
    // in the order they were opened, the last one is remembered
    private val open = mutableStateListOf<String>().apply { rememberedOpen?.let(::add) }

    /** Whether only one section may be open at a time. */
    var isExclusive by mutableStateOf(isExclusive)
        private set

    fun isExpanded(id: String) = id in open

    fun toggle(id: String) {
        if (id in open) {
            open.remove(id)
        } else {
            if (isExclusive) open.clear()
            open.add(id)
        }
        save()
    }

    fun collapseAll(ids: Collection<String>) {
        open.removeAll(ids.toSet())
        save()
    }

    fun expandAll(ids: Collection<String>) {
        ids.filterNot(open::contains).forEach(open::add)
        save()
    }

    fun areAllCollapsed(ids: Collection<String>) = ids.none(::isExpanded)

    /** Sections announce themselves, so exclusive expanding also knows those not listed in [rememberSectionState]. */
    internal fun register(id: String) {
        screenIds += id
    }

    /** Switching to exclusive sections keeps only the section opened last. */
    internal fun updateExclusive(exclusive: Boolean) {
        isExclusive = exclusive
        if (exclusive && open.size > 1) open.retainAll(listOf(open.last()))
    }

    private fun save() = onRemember(screenIds.toSet(), open.lastOrNull())
}

/**
 * The state of the collapsible sections of one screen; [ids] are the screen's sections. Sections of a lazy list that
 * were never shown are only known from [ids], so a screen should list them all. Every section starts collapsed except
 * the one the user opened last on this screen.
 */
@Composable
fun rememberSectionState(ids: Collection<String> = emptyList()): SectionState {
    val context = LocalContext.current
    val store = remember { ThemeSettingsStore(context) }
    val exclusive = LocalThemeSettings.current.exclusiveSections
    val scope = rememberCoroutineScope()
    val state = remember {
        // the activity read the theme from the same store when it started, so this answers from memory
        val remembered = runBlocking { store.openSections.first() }.firstOrNull { it in ids }
        SectionState(remembered, exclusive, ids.toMutableSet()) { screenIds, lastOpen ->
            scope.launch { store.rememberOpenSection(screenIds, lastOpen) }
        }
    }
    LaunchedEffect(exclusive) { state.updateExclusive(exclusive) }
    return state
}

/**
 * A group of settings rows in the expressive style: rounded strongly outside, slightly inside, a small gap between
 * rows. With a [sectionState] and [id] the group is an accordion: its title is a row like the others, with [icon] and an
 * arrow at the end; expanded, the group's rows join it below.
 *
 * Consecutive groups form a block when each gets its [block] position: they join with the small gap between rows, and
 * only the block's top and bottom are rounded strongly. A block should consist of groups without plain titles.
 */
@Composable
fun SettingsGroup(
    title: String?,
    modifier: Modifier = Modifier,
    id: String? = null,
    sectionState: SectionState? = null,
    icon: ImageVector? = null,
    block: RowPosition = RowPosition.SINGLE,
    content: SettingsGroupScope.() -> Unit,
) {
    if (isMyzelDesign) return MyzelSettingsGroup(
        title = title, modifier = modifier, id = id, sectionState = sectionState, icon = icon, block = block,
        content = content,
    )
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
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
        }
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
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
    Surface(
        shape = RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .clickable(onClickLabel = actionLabel, onClick = onToggle)
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
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

/**
 * Inside a [SectionCard] the rows lie flat on the card instead of each on its own tile; the value is their horizontal
 * padding (none when the card indents its content anyway). Null outside of a card.
 */
internal val LocalCardRowPadding = staticCompositionLocalOf<Dp?> { null }

/** The tile of one row, or just its place inside a [SectionCard]; [content] gets the padding to keep inside. */
@Composable
private fun RowTile(position: RowPosition, modifier: Modifier = Modifier, content: @Composable (PaddingValues) -> Unit) {
    val cardPadding = LocalCardRowPadding.current
    if (cardPadding == null) {
        Surface(shape = position.shape(), color = MaterialTheme.colorScheme.surfaceContainer, modifier = modifier.fillMaxWidth()) {
            content(PaddingValues(horizontal = 16.dp, vertical = 12.dp))
        }
    } else {
        Box(modifier.fillMaxWidth()) { content(PaddingValues(horizontal = cardPadding, vertical = CARD_ROW_PADDING)) }
    }
}

/**
 * Accessibility actions a wrapper such as [SwipeActionRow] adds to the row inside it; they belong to the row's own
 * node, the one TalkBack focuses.
 */
internal val LocalRowActions = compositionLocalOf { emptyList<CustomAccessibilityAction>() }

/** The surface of one row in a [SettingsGroup]; inside a [SectionCard] the row lies flat on the card. */
@Composable
fun GroupRowSurface(
    position: RowPosition,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    if (isMyzelDesign) return MyzelGroupRowSurface(
        position = position, modifier = modifier, enabled = enabled, content = content,
    )
    val minHeight = if (LocalCardRowPadding.current == null) 56.dp else 48.dp
    val actions = LocalRowActions.current
    RowTile(position, if (actions.isEmpty()) modifier else modifier.semantics { customActions = actions }) { padding ->
        Row(
            modifier = Modifier
                .heightIn(min = minHeight)
                .padding(padding)
                .alpha(if (enabled) 1f else DISABLED_ALPHA),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun RowScope.RowTexts(title: String, summary: String?, icon: ImageVector?) {
    if (icon != null) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
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
fun ListRow(
    position: RowPosition,
    title: String,
    summary: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    if (isMyzelDesign) return MyzelListRow(
        position = position, title = title, summary = summary, icon = icon, enabled = enabled, onClick = onClick,
        trailing = trailing,
    )
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
fun SwitchRow(
    position: RowPosition,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    summary: String? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    if (isMyzelDesign) return MyzelSwitchRow(
        position = position, title = title, checked = checked, onCheckedChange = onCheckedChange, summary = summary,
        icon = icon, enabled = enabled,
    )
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
        Switch(checked = checked, onCheckedChange = null, enabled = enabled, colors = signalSwitchColors())
    }
}

/** A row with a slider below its title; [valueText] shows the current value, e.g. "110 %". */
@Composable
fun SliderRow(
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
    if (isMyzelDesign) return MyzelSliderRow(
        position = position, title = title, value = value, onValueChange = onValueChange, valueRange = valueRange,
        valueText = valueText, steps = steps, summary = summary, enabled = enabled,
        onValueChangeFinished = onValueChangeFinished,
    )
    RowTile(position) { padding ->
        Column(Modifier.padding(padding).alpha(if (enabled) 1f else DISABLED_ALPHA)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    if (summary != null) {
                        Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(valueText, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
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

/** A row with a title (optional) and a single-choice segmented button row below. */
@Composable
fun <T> SegmentedRow(
    position: RowPosition,
    title: String?,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
    enabled: Boolean = true,
) {
    if (isMyzelDesign) return MyzelSegmentedRow(
        position = position, title = title, options = options, selected = selected, label = label, onSelect = onSelect,
        enabled = enabled,
    )
    RowTile(position) { padding ->
        Column(Modifier.padding(padding)) {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 8.dp))
            }
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                options.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = option == selected,
                        onClick = { onSelect(option) },
                        shape = SegmentedButtonDefaults.itemShape(index, options.size),
                        enabled = enabled,
                    ) {
                        Text(label(option), maxLines = 1)
                    }
                }
            }
        }
    }
}

/** A row that holds arbitrary content, e.g. a set of chips or color swatches. */
@Composable
fun ContentRow(position: RowPosition, title: String?, content: @Composable ColumnScope.() -> Unit) {
    if (isMyzelDesign) return MyzelContentRow(position = position, title = title, content = content)
    RowTile(position) { padding ->
        Column(Modifier.padding(padding)) {
            if (title != null) {
                Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 8.dp))
            }
            content()
        }
    }
}

/** Space below the last group, so the floating navigation pill doesn't cover it. */
@Composable
fun BottomPillSpacer() = Spacer(Modifier.height(if (isMyzelDesign) MyzelBottomPillSpace else 96.dp))

/** The pill of the Myzel style stands a little higher. */
private val MyzelBottomPillSpace = 104.dp

private const val DISABLED_ALPHA = 0.38f
private val CARD_ROW_PADDING = 8.dp

// above and below a group or block
private val GROUP_SPACING = 8.dp

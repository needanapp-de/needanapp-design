package de.needanapp.design.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.needanapp.design.R
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.LocalDesignShapes
import de.needanapp.design.theme.emphasizedSpec
import de.needanapp.design.theme.fadedOn
import de.needanapp.design.theme.inks
import de.needanapp.design.theme.readableOn

/**
 * The parts of a [TileCard]: lines of tiles and the small headings between them. A composable function that adds
 * parts for the card (e.g. `fun TileCardScope.detailLines()` with state of its own) must be `@NonRestartableComposable`:
 * otherwise a change it reads redoes only it, adding lines nobody lays out, while the card stays as it was.
 */
class TileCardScope @PublishedApi internal constructor() {
    internal val parts = mutableListOf<TilePart>()

    /** A line of equally wide tiles; each gets the [Modifier] that sizes it and its corner [Shape] in the card. */
    fun line(vararg tiles: @Composable (Modifier, Shape) -> Unit) = line(tiles.toList())

    fun line(tiles: List<@Composable (Modifier, Shape) -> Unit>) {
        if (tiles.isNotEmpty()) parts += TilePart.Line(tiles)
    }

    /** A small heading between lines, e.g. "Direction"; the tiles above and below it round off toward it. */
    fun label(text: String) {
        parts += TilePart.Label(text)
    }
}

internal sealed interface TilePart {
    class Line(val tiles: List<@Composable (Modifier, Shape) -> Unit>) : TilePart
    class Label(val text: String) : TilePart
}

/**
 * A card of tiles: options or destinations as small tinted surfaces instead of rows, in the look of the grouped
 * lists. The [title] sits above in the accent; the tiles lie in lines of equal width (all tiles of a line as high as
 * the highest), with small gaps and small inner radii, and round off at the card's edge and toward the labels between
 * them. Spacing around the card is up to the caller, in sheets and on pages usually [CardSpacing]. An [action] sits at
 * the end of the title.
 */
@Composable
inline fun TileCard(
    title: String?,
    modifier: Modifier = Modifier,
    action: TileCardAction? = null,
    content: @Composable TileCardScope.() -> Unit,
) {
    // the content only lists the parts; composable, so it can read texts and state. Inline, so state it reads
    // recomposes the caller and with it the whole card, not just the content with a list nobody reads again
    val scope = TileCardScope()
    scope.content()
    TileCardLayout(title, modifier, scope, action)
}

/** A small button at the end of a [TileCard]'s title, e.g. "Add" above a list of servers. */
@Immutable
class TileCardAction(val label: String, val icon: ImageVector, val onClick: () -> Unit)

/** The space around the cards of a sheet or page: 16 dp to the sides, 6 dp above and below. */
val CardSpacing = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)

@PublishedApi
@Composable
internal fun TileCardLayout(title: String?, modifier: Modifier, scope: TileCardScope, action: TileCardAction? = null) {
    val shapes = LocalDesignShapes.current
    // nested in the card, so its corners less round by the margin
    val outer = (shapes.group - TILE_CARD_PADDING).coerceAtLeast(shapes.inner)
    Column(
        modifier
            .fillMaxWidth()
            .designCard()
            .padding(TILE_CARD_PADDING),
        verticalArrangement = Arrangement.spacedBy(shapes.rowGap),
    ) {
        if (title != null) TileTitle(title, action)
        scope.parts.forEachIndexed { index, part ->
            when (part) {
                is TilePart.Label -> Text(
                    part.text,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 7.dp, bottom = 3.dp),
                )

                is TilePart.Line -> TileLine(
                    part,
                    joinsAbove = scope.parts.getOrNull(index - 1) is TilePart.Line,
                    joinsBelow = scope.parts.getOrNull(index + 1) is TilePart.Line,
                    outer = outer,
                )
            }
        }
    }
}

/** The title of a [TileCard] or [TileGroup] in the accent, with its [action] at the end. */
@Composable
private fun TileTitle(title: String, action: TileCardAction?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 5.dp)
                .semantics { heading() },
        )
        if (action != null) {
            TextButton(onClick = action.onClick, contentPadding = PaddingValues(horizontal = 12.dp)) {
                Icon(action.icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(action.label)
            }
        }
    }
}

/**
 * Tiles joined like a grouped list, without a card around them: the [title] (with its [action]) above in the accent,
 * the lines of tiles below with small gaps and small inner radii, round at the group's edges. The tiles take the
 * color of cards, as they lie on the screen's background. Compact, e.g. for places to open; spacing to the screen's
 * edges is up to the caller.
 */
@Composable
inline fun TileGroup(
    title: String?,
    modifier: Modifier = Modifier,
    action: TileCardAction? = null,
    content: @Composable TileCardScope.() -> Unit,
) {
    // inline for the same reason as TileCard
    val scope = TileCardScope()
    scope.content()
    TileGroupLayout(title, modifier, scope, action)
}

@PublishedApi
@Composable
internal fun TileGroupLayout(title: String?, modifier: Modifier, scope: TileCardScope, action: TileCardAction?) {
    val shapes = LocalDesignShapes.current
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(shapes.rowGap)) {
        if (title != null) TileTitle(title, action)
        CompositionLocalProvider(LocalTileContainer provides LocalDesignColors.current.card) {
            scope.parts.forEachIndexed { index, part ->
                when (part) {
                    is TilePart.Label -> Text(
                        part.text,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 7.dp, bottom = 3.dp),
                    )

                    is TilePart.Line -> TileLine(
                        part,
                        joinsAbove = scope.parts.getOrNull(index - 1) is TilePart.Line,
                        joinsBelow = scope.parts.getOrNull(index + 1) is TilePart.Line,
                        outer = shapes.group,
                    )
                }
            }
        }
    }
}

/** The color of tiles at rest where it differs from [TileDefaults.containerColor]'s own, e.g. in a [TileGroup]. */
internal val LocalTileContainer = staticCompositionLocalOf<Color?> { null }

/** One line of a [TileCard]: its tiles equally wide and high, round with [outer] where no line joins them. */
@Composable
internal fun TileLine(part: TilePart.Line, joinsAbove: Boolean, joinsBelow: Boolean, outer: Dp) {
    val shapes = LocalDesignShapes.current
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(shapes.rowGap),
    ) {
        part.tiles.forEachIndexed { column, tile ->
            val isStart = column == 0
            val isEnd = column == part.tiles.lastIndex
            val shape = RoundedCornerShape(
                topStart = if (!joinsAbove && isStart) outer else shapes.inner,
                topEnd = if (!joinsAbove && isEnd) outer else shapes.inner,
                bottomStart = if (!joinsBelow && isStart) outer else shapes.inner,
                bottomEnd = if (!joinsBelow && isEnd) outer else shapes.inner,
            )
            tile(Modifier.weight(1f).fillMaxHeight(), shape)
        }
    }
}

/**
 * Tiles on their own, without a card around them: lines of equally wide tiles, each rounded like a card, with room
 * between them. Here [TileCardScope.label] is a section heading in the accent, like the titles of the grouped lists.
 * Spacing to the screen's edges is up to the caller.
 */
@Composable
inline fun TileGrid(modifier: Modifier = Modifier, content: @Composable TileCardScope.() -> Unit) {
    // inline for the same reason as TileCard
    val scope = TileCardScope()
    scope.content()
    TileGridLayout(modifier, scope)
}

@PublishedApi
@Composable
internal fun TileGridLayout(modifier: Modifier, scope: TileCardScope) {
    val shape = RoundedCornerShape(LocalDesignShapes.current.group)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(TILE_GRID_GAP)) {
        scope.parts.forEach { part ->
            when (part) {
                is TilePart.Label -> Text(
                    part.text,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 8.dp, top = 12.dp)
                        .semantics { heading() },
                )

                is TilePart.Line -> Row(
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(TILE_GRID_GAP),
                ) {
                    part.tiles.forEach { tile -> tile(Modifier.weight(1f).fillMaxHeight(), shape) }
                }
            }
        }
    }
}

/** The colors of tiles. */
object TileDefaults {
    /**
     * A tile at rest: the raised surface, tinted a little with the accent so it stands out from its card; in a
     * [TileGroup], lying on the background, the color of cards.
     */
    @Composable
    fun containerColor(): Color {
        LocalTileContainer.current?.let { return it }
        val colors = MaterialTheme.colorScheme
        // the tint may not take away from the text on it
        return colors.primary.copy(alpha = TINT_ALPHA).compositeOver(colors.surfaceContainerHighest)
            .readableOn(colors.inks())
    }

    /** A chosen tile or one switched on: the accent itself. */
    @Composable
    fun selectedContainerColor(): Color = LocalDesignColors.current.signal

    @Composable
    fun selectedContentColor(): Color = LocalDesignColors.current.onSignal

    private const val TINT_ALPHA = 0.08f
}

/** Background and text color of a tile, moving smoothly when it gets chosen or switched. */
@Composable
private fun tileColors(active: Boolean): Pair<Color, Color> {
    val container by animateColorAsState(
        if (active) TileDefaults.selectedContainerColor() else TileDefaults.containerColor(),
        emphasizedSpec(),
        label = "tile",
    )
    val content by animateColorAsState(
        if (active) TileDefaults.selectedContentColor() else MaterialTheme.colorScheme.onSurface,
        emphasizedSpec(),
        label = "tileContent",
    )
    return container to content
}

/**
 * One of several choices, e.g. a sort order. Side by side with others in a line, [stacked] puts the [icon] above the
 * [label] (which gets smaller rather than cut off when the tile is narrow), with [iconInCircle] in a large circle;
 * otherwise the icon sits before it. The chosen tile is in the accent, its circle dark.
 */
@Composable
fun ChoiceTile(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    stacked: Boolean = false,
    enabled: Boolean = true,
    iconInCircle: Boolean = false,
    summary: String? = null,
) {
    val haptics = LocalHapticFeedback.current
    val (container, content) = tileColors(selected)
    val primary = MaterialTheme.colorScheme.primary
    val iconTint = if (selected && !iconInCircle) content else primary
    val circle by animateColorAsState(if (selected) content else primary.copy(alpha = CIRCLE_ALPHA), emphasizedSpec(), label = "tileCircle")
    Box(
        modifier
            .clip(shape)
            .background(container)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton) {
                haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                onClick()
            }
            .heightIn(min = if (stacked) STACKED_MIN_HEIGHT else TILE_MIN_HEIGHT)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .padding(horizontal = if (stacked) 4.dp else 14.dp, vertical = if (stacked && iconInCircle) 12.dp else 8.dp),
        // with an icon, tiles side by side line up their icons and labels at the top, however long their summaries
        contentAlignment = when {
            !stacked -> Alignment.CenterStart
            icon != null -> Alignment.TopCenter
            else -> Alignment.Center
        },
    ) {
        if (stacked) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (icon != null && iconInCircle) {
                    Box(Modifier.size(CHOICE_CIRCLE).clip(CircleShape).background(circle), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(8.dp))
                } else if (icon != null) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = content,
                    maxLines = 1,
                    softWrap = false,
                    autoSize = TextAutoSize.StepBased(minFontSize = STACKED_MIN_TEXT, maxFontSize = MaterialTheme.typography.labelLarge.fontSize),
                    textAlign = TextAlign.Center,
                )
                if (summary != null) {
                    // a short explanation may take a second line
                    Text(
                        summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = content.fadedOn(SUMMARY_ALPHA, container),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null && iconInCircle) {
                    Box(Modifier.size(ROW_CHOICE_CIRCLE).clip(CircleShape).background(circle), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                } else if (icon != null) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                }
                Column {
                    Text(label, style = MaterialTheme.typography.bodyLarge, color = content, maxLines = 2)
                    if (summary != null) {
                        Text(summary, style = MaterialTheme.typography.bodyMedium, color = content.fadedOn(SUMMARY_ALPHA, container))
                    }
                }
            }
        }
    }
}

/**
 * A switch as a compact tile, like the system's quick settings: [icon] at the start, beside it the short [title] and
 * below that "On"/"Off" and an optional [summary]; switched on, the tile is in the accent. The whole tile toggles.
 * [iconInCircle] sets the icon in a circle, for tiles that stand on their own (see [TileGrid]). [description] is read
 * out instead of [title], e.g. the full sentence a short title stands for.
 */
@Composable
fun ToggleTile(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    summary: String? = null,
    enabled: Boolean = true,
    iconInCircle: Boolean = false,
    description: String? = null,
) {
    val haptics = LocalHapticFeedback.current
    val (container, content) = tileColors(checked)
    val colors = MaterialTheme.colorScheme
    val iconTint = if (checked) content else colors.primary
    Row(
        modifier
            .clip(shape)
            .background(container)
            .toggleable(value = checked, enabled = enabled, role = Role.Switch) {
                haptics.performHapticFeedback(if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                onCheckedChange(it)
            }
            .then(if (description != null) Modifier.semantics { contentDescription = description } else Modifier)
            .heightIn(min = TOGGLE_MIN_HEIGHT)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null && iconInCircle) {
            Box(
                Modifier
                    .size(TOGGLE_CIRCLE)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = TOGGLE_CIRCLE_ALPHA)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
        } else if (icon != null) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = content,
                modifier = if (description != null) Modifier.clearAndSetSemantics {} else Modifier,
            )
            // the state is announced by the toggle itself
            Text(
                stringResource(if (checked) R.string.nd_tile_on else R.string.nd_tile_off),
                style = MaterialTheme.typography.bodySmall,
                color = content.fadedOn(SUMMARY_ALPHA, container),
                modifier = Modifier.clearAndSetSemantics {},
            )
            if (summary != null) {
                Text(summary, style = MaterialTheme.typography.bodySmall, color = content.fadedOn(SUMMARY_ALPHA, container))
            }
        }
    }
}

/**
 * A switch as a line on a tile, like [SwitchRow] but among tiles: [icon], [title] and [summary], the switch at the end.
 * The whole tile toggles.
 */
@Composable
fun SwitchTile(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    summary: String? = null,
    enabled: Boolean = true,
) {
    val haptics = LocalHapticFeedback.current
    val colors = MaterialTheme.colorScheme
    Row(
        modifier
            .clip(shape)
            .background(TileDefaults.containerColor())
            .toggleable(value = checked, enabled = enabled, role = Role.Switch) {
                haptics.performHapticFeedback(if (it) HapticFeedbackType.ToggleOn else HapticFeedbackType.ToggleOff)
                onCheckedChange(it)
            }
            .heightIn(min = SWITCH_MIN_HEIGHT)
            .alpha(if (enabled) 1f else DISABLED_ALPHA)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(14.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
            if (summary != null) {
                Text(summary, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
        Spacer(Modifier.width(12.dp))
        // the whole tile toggles, the switch itself is only the indicator
        Switch(checked = checked, onCheckedChange = null, enabled = enabled, colors = signalSwitchColors())
    }
}

/**
 * A tile that does something or opens something: its icon in a circle, [title] and [summary]. [centered] lays it out
 * as a large destination tile, e.g. on a settings page, [inline] as a line with the circle before the text; otherwise
 * it reads from the start like the other tiles. Both together make a button-like tile, the icon and title centered in
 * one line, e.g. "Open" and "Edit" side by side. [destructive] actions are red. Instead of an [icon], [iconContent] can
 * fill the circle with something of its own, e.g. the app icon in its colors; it gets the circle's size. Without
 * [iconInCircle] the icon stands alone. [trailing] goes at the end of an [inline] tile, e.g. a button of its own.
 * [fill] shows how full something is, e.g. a storage, as a bar below the texts (0 = empty, 1 = full). [compact] makes
 * an [inline] tile small enough for three to a line on a phone: smaller type, less room around, the icon without a
 * circle; [rememberCompactTileLines] puts them in lines without breaking a word.
 */
@Composable
fun ActionTile(
    title: String,
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconContent: (@Composable (circle: Dp) -> Unit)? = null,
    summary: String? = null,
    centered: Boolean = false,
    inline: Boolean = false,
    destructive: Boolean = false,
    enabled: Boolean = true,
    iconInCircle: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
    fill: Float? = null,
    compact: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val container = TileDefaults.containerColor()
    val isButton = centered && inline
    val isCompact = compact && inline && !centered
    val circleColor = if (destructive) colors.errorContainer else colors.primary.copy(alpha = CIRCLE_ALPHA).compositeOver(container)
    val iconTint = if (destructive) colors.onErrorContainer else colors.primary
    val circleSize = when {
        centered -> 56.dp
        inline -> 40.dp
        else -> 44.dp
    }
    val hasIcon = icon != null || iconContent != null
    val circle: @Composable () -> Unit = {
        if ((!iconInCircle || isCompact) && icon != null && iconContent == null) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(if (isCompact) COMPACT_ICON else 22.dp))
        } else {
            Box(Modifier.size(circleSize).clip(CircleShape).background(circleColor), contentAlignment = Alignment.Center) {
                if (iconContent != null) {
                    iconContent(circleSize)
                } else if (icon != null) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(circleSize * ICON_SHARE))
                }
            }
        }
    }
    val texts: @Composable (TextAlign) -> Unit = { align ->
        Text(
            title,
            style = when {
                isButton -> MaterialTheme.typography.labelLarge
                centered -> MaterialTheme.typography.titleMedium
                isCompact -> MaterialTheme.typography.titleSmall
                else -> MaterialTheme.typography.bodyLarge
            },
            color = if (destructive) colors.error else colors.onSurface,
            textAlign = align,
        )
        if (summary != null) {
            Text(
                summary,
                style = if (isCompact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                textAlign = align,
                modifier = Modifier.padding(top = if (isCompact) 1.dp else 2.dp),
            )
        }
        if (fill != null) FillBar(fill, Modifier.padding(top = if (isCompact) 6.dp else 8.dp).fillMaxWidth())
    }
    val surface = modifier
        .clip(shape)
        .background(container)
        .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
        .heightIn(
            min = when {
                isButton || isCompact -> BUTTON_MIN_HEIGHT
                inline -> SWITCH_MIN_HEIGHT
                else -> TILE_MIN_HEIGHT
            },
        )
        .alpha(if (enabled) 1f else DISABLED_ALPHA)
    if (inline) {
        Row(
            surface.padding(horizontal = if (isCompact) COMPACT_PADDING else 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isButton) Arrangement.Center else Arrangement.Start,
        ) {
            if (hasIcon) {
                circle()
                Spacer(
                    Modifier.width(
                        when {
                            isCompact -> COMPACT_ICON_GAP
                            iconInCircle -> 14.dp
                            else -> 10.dp
                        },
                    ),
                )
            }
            if (isButton) {
                Column { texts(TextAlign.Center) }
            } else {
                Column(Modifier.weight(1f)) { texts(TextAlign.Start) }
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
    } else {
        Column(
            surface.padding(horizontal = 14.dp, vertical = if (centered) 18.dp else 12.dp),
            horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
            // tiles side by side line up their icons and titles at the top, however long their summaries
            verticalArrangement = Arrangement.Top,
        ) {
            if (hasIcon) {
                circle()
                Spacer(Modifier.height(if (centered) 12.dp else 10.dp))
            }
            texts(if (centered) TextAlign.Center else TextAlign.Start)
        }
    }
}

/**
 * [items] as lines of [compact][ActionTile] tiles with an icon, in their order, at most [most] to a line in [width]:
 * each line only holds as many as leave room for the longest word of their title and summary from [texts], so they
 * wrap only between words and never within one (words joined by non-breaking spaces count as one, e.g. "81 GB").
 * As few lines as can be, as evenly filled as can be, the longer ones last, e.g. 2 + 3 rather than 3 + 1 + 1.
 */
@Composable
fun <T> rememberCompactTileLines(items: List<T>, width: Dp, most: Int, texts: (T) -> Pair<String, String?>): List<List<T>> {
    val measurer = rememberTextMeasurer()
    val typography = MaterialTheme.typography
    val gap = LocalDesignShapes.current.rowGap
    val density = LocalDensity.current
    val pairs = items.map(texts)
    val sizes = remember(pairs, width, most, typography, gap, density) {
        // how many tiles side by side each one still fits into
        val fits = pairs.map { (title, summary) ->
            val widest = maxOf(measurer.widestWord(title, typography.titleSmall), summary?.let { measurer.widestWord(it, typography.bodySmall) } ?: 0)
            (most downTo 2).firstOrNull { columns ->
                val tile = (width - gap * (columns - 1)) / columns
                with(density) { (tile - COMPACT_CHROME).toPx() } >= widest
            } ?: 1
        }
        lineSizes(fits, most)
    }
    var start = 0
    return sizes.map { size -> items.subList(start, start + size).also { start += size } }
}

/**
 * How many of the entries go into each line, in order, when each fits into lines of at most its [fits] (and [most]):
 * the fewest lines, then the most even ones, then the shorter ones first.
 */
internal fun lineSizes(fits: List<Int>, most: Int): List<Int> {
    class Plan(val lines: Int, val evenness: Int, val first: Int, val sizes: List<Int>)
    val best = arrayOfNulls<Plan>(fits.size + 1)
    best[fits.size] = Plan(0, 0, 0, emptyList())
    for (start in fits.indices.reversed()) {
        var roomInLine = most
        for (size in 1..minOf(most, fits.size - start)) {
            roomInLine = minOf(roomInLine, fits[start + size - 1])
            if (size > roomInLine) break
            val rest = best[start + size]!!
            val plan = Plan(rest.lines + 1, rest.evenness + size * size, size, listOf(size) + rest.sizes)
            val current = best[start]
            if (current == null || compareValuesBy(plan, current, { it.lines }, { it.evenness }, { it.first }) < 0) best[start] = plan
        }
    }
    return best[0]!!.sizes
}

/** The width of the longest word in [text], on one line. */
private fun TextMeasurer.widestWord(text: String, style: TextStyle): Int = text.split(' ', '\n')
    .filter { it.isNotEmpty() }
    .maxOfOrNull { measure(it, style, softWrap = false, maxLines = 1).size.width } ?: 0

/** How full something is, as a thin bar in the accent over a faint track. */
@Composable
private fun FillBar(fill: Float, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val share = fill.coerceIn(0f, 1f)
    Box(
        modifier
            .height(6.dp)
            .clip(CircleShape)
            .background(colors.onSurface.copy(alpha = FILL_TRACK_ALPHA))
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(share, 0f..1f) },
    ) {
        Box(Modifier.fillMaxWidth(share).fillMaxHeight().clip(CircleShape).background(LocalDesignColors.current.signal))
    }
}

/**
 * A value changed in steps, e.g. the size of grid cells: a smaller button, the [value] and a bigger button. A button
 * without its action is off (at the end of the range); [decreaseLabel] and [increaseLabel] name the buttons for
 * TalkBack.
 */
@Composable
fun StepperTile(
    value: String,
    onDecrease: (() -> Unit)?,
    onIncrease: (() -> Unit)?,
    decreaseLabel: String,
    increaseLabel: String,
    shape: Shape,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier
            .clip(shape)
            .background(TileDefaults.containerColor())
            .heightIn(min = STACKED_MIN_HEIGHT)
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onDecrease?.invoke() }, enabled = onDecrease != null) {
            Icon(Icons.Outlined.Remove, contentDescription = decreaseLabel, tint = if (onDecrease != null) colors.primary else colors.onSurfaceVariant)
        }
        Text(
            value,
            style = MaterialTheme.typography.labelLarge,
            color = colors.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = { onIncrease?.invoke() }, enabled = onIncrease != null) {
            Icon(Icons.Outlined.Add, contentDescription = increaseLabel, tint = if (onIncrease != null) colors.primary else colors.onSurfaceVariant)
        }
    }
}

private val TILE_CARD_PADDING: Dp = 6.dp
private val TILE_GRID_GAP = 8.dp
private val CHOICE_CIRCLE = 48.dp
private val SWITCH_MIN_HEIGHT = 56.dp
private val TOGGLE_CIRCLE = 36.dp
private val ROW_CHOICE_CIRCLE = 40.dp
private val TOGGLE_MIN_HEIGHT = 64.dp
private const val TOGGLE_CIRCLE_ALPHA = 0.16f
private val TILE_MIN_HEIGHT = 52.dp
private val BUTTON_MIN_HEIGHT = 48.dp
private val COMPACT_PADDING = 10.dp
private val COMPACT_ICON = 20.dp
private val COMPACT_ICON_GAP = 8.dp

/** What a compact tile takes beside its texts: the padding on both sides, the icon and the gap after it. */
private val COMPACT_CHROME = COMPACT_PADDING * 2 + COMPACT_ICON + COMPACT_ICON_GAP
private val STACKED_MIN_HEIGHT = 64.dp
private val STACKED_MIN_TEXT = 11.sp
private const val DISABLED_ALPHA = 0.38f
private const val SUMMARY_ALPHA = 0.78f
private const val CIRCLE_ALPHA = 0.2f
private const val ICON_SHARE = 0.5f
private const val FILL_TRACK_ALPHA = 0.14f

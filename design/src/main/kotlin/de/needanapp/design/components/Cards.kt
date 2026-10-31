package de.needanapp.design.components

import android.os.SystemClock
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.LocalDesignEffects
import de.needanapp.design.theme.LocalDesignShapes
import de.needanapp.design.theme.mixedWith
import de.needanapp.design.theme.readableOn
import kotlinx.coroutines.delay
import de.needanapp.design.myzel.designCard as myzelDesignCard
import de.needanapp.design.theme.isMyzelDesign
import de.needanapp.design.myzel.colors as myzelColors
import de.needanapp.design.myzel.SectionCard as MyzelSectionCard
import de.needanapp.design.myzel.StatusBand as MyzelStatusBand
import de.needanapp.design.myzel.LabeledRow as MyzelLabeledRow
import de.needanapp.design.myzel.TagPill as MyzelTagPill
import de.needanapp.design.myzel.PillButton as MyzelPillButton
import de.needanapp.design.myzel.ActionBar as MyzelActionBar
import de.needanapp.design.myzel.ActionBarButton as myzelActionBarButton
import de.needanapp.design.myzel.ActionBarSlot as myzelActionBarSlot

/**
 * The card look: the card fill of [LocalDesignColors], a fine border and, unless effects are reduced, a soft shadow
 * below; clipped to [shape], by default the corner radius of the appearance settings. [elevated] false leaves out the
 * shadow, e.g. inside a carousel that would cut it off.
 */
@Composable
fun Modifier.designCard(
    shape: Shape = RoundedCornerShape(LocalDesignShapes.current.group),
    elevated: Boolean = true,
): Modifier {
    if (isMyzelDesign) return myzelDesignCard(shape = shape, elevated = elevated)
    val colors = LocalDesignColors.current
    val shadowed = if (elevated && LocalDesignEffects.current.shadows) {
        dropShadow(shape, if (colors.isDark) DarkCardShadow else LightCardShadow)
    } else {
        this
    }
    return shadowed.clip(shape).background(colors.card).border(colors.cardBorderWidth, colors.cardBorder, shape)
}

/** How a [StatusTile] colors its state. */
enum class StatusTone {
    /** Everything fine, e.g. "up to date": green. */
    POSITIVE,

    /** Something to do, e.g. "update available": the secondary container. */
    ACCENT,

    /** Neither, e.g. "not installed": grey. */
    NEUTRAL,
    ERROR,
}

/**
 * A state for a [StatusTile], e.g. "Up to date" with a check mark. The display of the Myzel style ([DeviceDisplay])
 * also shows [detail] quietly at its end, e.g. "FROM 2.4.1", and with [progress] (0 to 1) a row of dots that fills up.
 */
@Immutable
data class CardStatus(
    val text: String,
    val icon: ImageVector,
    val tone: StatusTone,
    val progress: Float? = null,
    val detail: String? = null,
)

/** Background and content color of [StatusTone]. */
@Composable
fun StatusTone.colors(): Pair<Color, Color> {
    if (isMyzelDesign) return myzelColors()
    val scheme = MaterialTheme.colorScheme
    val design = LocalDesignColors.current
    return when (this) {
        StatusTone.POSITIVE -> design.positive to design.onPositive
        StatusTone.ACCENT -> scheme.secondaryContainer to scheme.onSecondaryContainer
        StatusTone.NEUTRAL -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
        StatusTone.ERROR -> scheme.errorContainer to scheme.onErrorContainer
    }
}

/**
 * A card with a small capitalized [title] and an optional [hint] on the right (e.g. a date), then its content. A
 * [status] shows as a band across the card's top; with [watermark] its icon also sits large and faint in the bottom
 * right corner. The content is indented like the title unless [fullWidthContent] (e.g. a row of pictures that brings its
 * own padding, or rows that react to touch up to the card's edges). Rows of the grouped list ([ListRow], [SwitchRow],
 * [SegmentedRow], [SliderRow], [ContentRow]) lie flat on the card, in line with the title; their position doesn't matter
 * here. The card grows and shrinks smoothly with its content. Spacing around the card is up to the caller. While
 * [loading], a [LoadingBar] runs along the bottom of the status band (only with a [status]).
 */
@Composable
@Deprecated("Cards are TileCards now: a StatusTile for the state, DetailTile or detailLines for facts, ActionTile for actions")
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    status: CardStatus? = null,
    watermark: Boolean = false,
    fullWidthContent: Boolean = false,
    loading: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (isMyzelDesign) return MyzelSectionCard(
        title = title, modifier = modifier, hint = hint, status = status, watermark = watermark,
        fullWidthContent = fullWidthContent, content = content,
    )
    Column(modifier.fillMaxWidth().designCard().animateContentSize()) {
        @Suppress("DEPRECATION")
        status?.let { StatusBand(it, loading = loading) }
        Box(Modifier.padding(top = CardPaddingTop, bottom = CardPaddingBottom)) {
            if (watermark && status != null) {
                Icon(
                    status.icon,
                    contentDescription = null,
                    tint = status.tone.colors().second.copy(alpha = WATERMARK_ALPHA),
                    modifier = Modifier.align(Alignment.BottomEnd).padding(end = CardPaddingHorizontal).size(WatermarkSize),
                )
            }
            Column(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = CardPaddingHorizontal)) {
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.5.sp,
                            lineHeight = (13.5f * 1.3f).sp,
                            letterSpacing = 0.5.sp,
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics { heading() },
                    )
                    Spacer(Modifier.weight(1f))
                    if (hint != null) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.labelSmall,
                            color = LocalDesignColors.current.hint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                // rows of the grouped list (ListRow, SwitchRow, …) lie flat on the card, as far in as the title
                CompositionLocalProvider(LocalCardRowPadding provides if (fullWidthContent) CardPaddingHorizontal else 0.dp) {
                    Column(if (fullWidthContent) Modifier else Modifier.padding(horizontal = CardPaddingHorizontal), content = content)
                }
            }
        }
    }
}

/**
 * A band with the text of [status] and its icon at the end, in the colors of its tone. While [loading] (e.g. the data
 * behind the status is being fetched), a [LoadingBar] in the band's text color runs along its bottom edge.
 */
@Composable
@Deprecated("Use a StatusTile in a TileCard")
fun StatusBand(status: CardStatus, modifier: Modifier = Modifier, loading: Boolean = false) {
    if (isMyzelDesign) return MyzelStatusBand(status = status, modifier = modifier)
    val (background, color) = status.tone.colors()
    Box(modifier.fillMaxWidth().background(background)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = CardPaddingHorizontal, vertical = 9.dp),
        ) {
            Text(
                text = status.text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = color,
                modifier = Modifier.weight(1f),
            )
            Icon(status.icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
        }
        LoadingBar(loading, Modifier.align(Alignment.BottomCenter), color = color)
    }
}

/**
 * A thin running bar across the full width that shows that something is loading. It fades in and out and, once shown,
 * stays for at least [minimumMillis], so a quick load doesn't just flicker. Takes no space while hidden.
 */
@Composable
fun LoadingBar(
    loading: Boolean,
    modifier: Modifier = Modifier,
    color: Color = LocalDesignColors.current.signal,
    minimumMillis: Long = 700,
) {
    var shown by remember { mutableStateOf(loading) }
    var since by remember { mutableLongStateOf(if (loading) SystemClock.uptimeMillis() else 0L) }
    LaunchedEffect(loading) {
        if (loading) {
            if (!shown) {
                since = SystemClock.uptimeMillis()
                shown = true
            }
        } else if (shown) {
            delay((since + minimumMillis - SystemClock.uptimeMillis()).coerceAtLeast(0))
            shown = false
        }
    }
    AnimatedVisibility(shown, modifier = modifier.fillMaxWidth(), enter = fadeIn(), exit = fadeOut()) {
        LinearProgressIndicator(
            modifier = Modifier.fillMaxWidth().height(4.dp),
            color = color,
            trackColor = color.copy(alpha = 0.18f),
            gapSize = 0.dp,
        )
    }
}

/**
 * A row of a card: [label] on the left in a column of [labelWidth], [value] next to it. With [alignFirstLines] the label
 * sits on the first line of a value that wraps (e.g. a path), otherwise it is centered beside the value.
 */
@Composable
@Deprecated("Use a DetailTile in a TileCard")
fun LabeledRow(
    label: String,
    modifier: Modifier = Modifier,
    labelWidth: Dp = 92.dp,
    gap: Dp = 8.dp,
    alignFirstLines: Boolean = false,
    value: @Composable RowScope.() -> Unit,
) {
    if (isMyzelDesign) return MyzelLabeledRow(
        label = label, modifier = modifier, labelWidth = labelWidth, gap = gap, value = value,
    )
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(labelWidth).then(if (alignFirstLines) Modifier.alignByBaseline() else Modifier),
        )
        Spacer(Modifier.width(gap))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).then(if (alignFirstLines) Modifier.alignByBaseline() else Modifier),
            content = value,
        )
    }
}

/**
 * A small pill with a word, e.g. a category or "New". By default the primary color shows faintly through the highest
 * container. [compact] makes it smaller, e.g. next to a version in a small card. [led] puts a small dot of that color in
 * front of the word.
 */
@Composable
fun TagPill(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    compact: Boolean = false,
    led: Color = Color.Unspecified,
) {
    if (isMyzelDesign) return MyzelTagPill(
        text = text, modifier = modifier, containerColor = containerColor, contentColor = contentColor,
        compact = compact, led = led,
    )
    val scheme = MaterialTheme.colorScheme
    val style = MaterialTheme.typography.labelSmall
    val textStyle = if (compact) style.copy(fontSize = 10.sp, lineHeight = 13.sp, letterSpacing = 0.3.sp) else style
    val container = containerColor.takeOrElse { scheme.surfaceContainerHighest.mixedWith(scheme.primary, TAG_ACCENT) }
    val textColor = contentColor.takeOrElse { scheme.onSurface.readableOn(container) }
    val pill = modifier
        .clip(CircleShape)
        .background(container)
        .padding(horizontal = if (compact) 7.dp else 12.dp, vertical = if (compact) 1.dp else 4.dp)
    if (led == Color.Unspecified) {
        Text(text = text, style = textStyle, color = textColor, maxLines = 1, modifier = pill)
        return
    }
    Row(pill, verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(if (compact) 5.dp else 6.dp).clip(CircleShape).background(led))
        Spacer(Modifier.width(if (compact) 4.dp else 6.dp))
        Text(text = text, style = textStyle, color = textColor, maxLines = 1)
    }
}

/**
 * The main action of a screen as a wide pill in the primary color; disabled it turns grey, e.g. while loading. Another
 * [style] takes the colors of that [DeviceKey], e.g. red for a destructive action.
 */
@Composable
@Deprecated("Use an ActionTile in a TileCard, or a Button")
fun PillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, style: KeyStyle = KeyStyle.SIGNAL) {
    if (isMyzelDesign) return MyzelPillButton(
        text = text, onClick = onClick, modifier = modifier, enabled = enabled, style = style,
    )
    if (style != KeyStyle.SIGNAL) {
        val (container, content) = keyColors(style, enabled)
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxWidth()
                .height(PillButtonHeight)
                .clip(CircleShape)
                .background(container)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp),
        ) {
            Text(text = text, style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        return
    }
    val scheme = MaterialTheme.colorScheme
    val design = LocalDesignColors.current
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(PillButtonHeight)
            .clip(CircleShape)
            .background(if (enabled) design.signal else scheme.onSurface.copy(alpha = DISABLED_CONTAINER_ALPHA))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) design.onSignal else scheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * A bar of actions at the bottom of a screen, e.g. on a detail page: rounded at the top, with a fine line along its
 * top edge and a shadow upward; it keeps clear of the navigation bar. Fill it with [ActionBarButton] and
 * [ActionBarSlot], which share its width evenly.
 */
@Composable
@Deprecated("Actions for one item float in a FloatingItemCard, for several selected items in a SelectionCard")
fun ActionBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    if (isMyzelDesign) return MyzelActionBar(modifier = modifier, content = content)
    val colors = LocalDesignColors.current
    val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val shadowed = if (LocalDesignEffects.current.shadows) {
        modifier.dropShadow(shape, if (colors.isDark) DarkBarShadow else LightBarShadow)
    } else {
        modifier
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = shadowed
            .fillMaxWidth()
            .clip(shape)
            .background(colors.bar)
            .drawBehind { drawLine(colors.barBorder, Offset.Zero, Offset(size.width, 0f), 1.dp.toPx()) }
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 8.dp),
        content = content,
    )
}

/** An icon button of an [ActionBar], in the primary color. */
@Composable
@Deprecated("Actions for one item float in a FloatingItemCard, for several selected items in a SelectionCard")
fun RowScope.ActionBarButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    if (isMyzelDesign) return myzelActionBarButton(
        icon = icon, contentDescription = contentDescription, onClick = onClick,
    )
    @Suppress("DEPRECATION")
    ActionBarSlot {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = contentDescription, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

/** A place in an [ActionBar] for own content, e.g. a toggle button; as wide as the others. */
@Composable
@Deprecated("Actions for one item float in a FloatingItemCard, for several selected items in a SelectionCard")
fun RowScope.ActionBarSlot(content: @Composable BoxScope.() -> Unit) {
    if (isMyzelDesign) return myzelActionBarSlot(content = content)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f), content = content)
}

/** How far a [SectionCard] indents its title and content, e.g. for own content in a card with full-width content. */
val SectionCardPadding = 20.dp

private val CardPaddingHorizontal = SectionCardPadding
private val CardPaddingTop = 10.dp
private val CardPaddingBottom = 12.dp
private val WatermarkSize = 52.dp
private val PillButtonHeight = 52.dp
private const val WATERMARK_ALPHA = 28 / 255f
private const val TAG_ACCENT = 0.12f
private const val DISABLED_CONTAINER_ALPHA = 31 / 255f
private const val DISABLED_CONTENT_ALPHA = 97 / 255f
private val DarkCardShadow = Shadow(16.dp, Color.Black.copy(alpha = 180 / 255f), offset = DpOffset(0.dp, 4.dp))
private val LightCardShadow = Shadow(12.dp, Color.Black.copy(alpha = 40 / 255f), offset = DpOffset(0.dp, 2.dp))
private val DarkBarShadow = Shadow(18.dp, Color.Black.copy(alpha = 130 / 255f), offset = DpOffset(0.dp, (-3).dp))
private val LightBarShadow = Shadow(12.dp, Color.Black.copy(alpha = 40 / 255f), offset = DpOffset(0.dp, (-2).dp))

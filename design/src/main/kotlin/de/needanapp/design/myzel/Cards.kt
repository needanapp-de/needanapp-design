package de.needanapp.design.myzel

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.needanapp.design.theme.DesignType
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.LocalDesignShapes
import de.needanapp.design.components.*

/**
 * The panel look: the card fill of [LocalDesignColors] with a fine border, clipped to [shape], by default the corner
 * radius of the appearance settings. Flat like a device front; [elevated] is kept for callers and has no effect.
 */
@Composable
internal fun Modifier.designCard(
    shape: Shape = RoundedCornerShape(LocalDesignShapes.current.group),
    @Suppress("UNUSED_PARAMETER") elevated: Boolean = true,
): Modifier {
    val colors = LocalDesignColors.current
    return clip(shape).background(colors.card).border(1.dp, colors.cardBorder, shape)
}

/** Background and content color of [StatusTone] outside of a display, e.g. for a pill. */
@Composable
internal fun StatusTone.colors(): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    val design = LocalDesignColors.current
    return when (this) {
        StatusTone.POSITIVE -> design.positive to design.onPositive
        StatusTone.ACCENT -> design.signal to design.onSignal
        StatusTone.NEUTRAL -> scheme.surfaceContainerHighest to scheme.onSurfaceVariant
        StatusTone.ERROR -> scheme.errorContainer to scheme.onErrorContainer
    }
}

/**
 * A panel with a small capitalized [title] and an optional [hint] on the right (e.g. a date), then its content. A
 * [status] shows as a display at the panel's top. The content is indented like the title unless [fullWidthContent]
 * (e.g. a row of pictures that brings its own padding). The panel grows and shrinks smoothly with its content. Spacing
 * around it is up to the caller. [watermark] is kept for callers and has no effect.
 */
@Composable
internal fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    status: CardStatus? = null,
    @Suppress("UNUSED_PARAMETER") watermark: Boolean = false,
    fullWidthContent: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth().designCard().animateContentSize()) {
        status?.let { StatusBand(it, Modifier.padding(start = DisplayInset, top = DisplayInset, end = DisplayInset)) }
        Column(Modifier.fillMaxWidth().padding(top = if (status != null) 12.dp else CardPaddingTop, bottom = CardPaddingBottom)) {
            if (title.isNotEmpty() || hint != null) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = CardPaddingHorizontal)) {
                    MonoLabel(title, modifier = Modifier.semantics { heading() })
                    Spacer(Modifier.weight(1f))
                    if (hint != null) {
                        Text(
                            text = hint,
                            style = DesignType.label.copy(letterSpacing = 0.4.sp),
                            color = LocalDesignColors.current.hint,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            Column(if (fullWidthContent) Modifier else Modifier.padding(horizontal = CardPaddingHorizontal), content = content)
        }
    }
}

/** The display of [status]: see [DeviceDisplay]. */
@Composable
internal fun StatusBand(status: CardStatus, modifier: Modifier = Modifier) {
    DeviceDisplay(status.text, status.tone, modifier, detail = status.detail, progress = status.progress)
}

/** A row of a panel: [label] small and capitalized on the left in a column of [labelWidth], [value] next to it. */
@Composable
internal fun LabeledRow(
    label: String,
    modifier: Modifier = Modifier,
    labelWidth: Dp = 92.dp,
    gap: Dp = 8.dp,
    value: @Composable RowScope.() -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier.fillMaxWidth()) {
        MonoLabel(label, modifier = Modifier.width(labelWidth), style = DesignType.label.copy(fontSize = 10.5.sp))
        Spacer(Modifier.width(gap))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f), content = value)
    }
}

/**
 * A small tag with a capitalized word, e.g. a category or "NEW": outlined by default, filled with [containerColor] when
 * given, with an LED dot of [led] in front when given. [compact] makes it smaller, e.g. next to a version.
 */
@Composable
internal fun TagPill(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    compact: Boolean = false,
    led: Color = Color.Unspecified,
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(if (compact) 5.dp else 7.dp)
    val filled = containerColor != Color.Unspecified
    val style = DesignType.label.copy(
        fontSize = if (compact) 9.5.sp else 10.5.sp,
        lineHeight = if (compact) 12.sp else 14.sp,
        letterSpacing = if (compact) 0.6.sp else 1.sp,
        fontWeight = if (filled) androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Medium,
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(shape)
            .then(if (filled) Modifier.background(containerColor) else Modifier.border(1.dp, scheme.outline.copy(alpha = 0.6f), shape))
            .padding(horizontal = if (compact) 5.dp else 8.dp, vertical = if (compact) 1.dp else 3.dp),
    ) {
        if (led != Color.Unspecified) {
            Box(Modifier.size(if (compact) 5.dp else 6.dp).clip(CircleShape).background(led))
            Spacer(Modifier.width(if (compact) 4.dp else 6.dp))
        }
        Text(text = text.uppercase(), style = style, color = contentColor.takeOrElse { scheme.onSurface }, maxLines = 1)
    }
}

/** The main action of a screen as a wide key in the signal color; disabled it turns grey, e.g. while loading. */
@Composable
internal fun PillButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, style: KeyStyle = KeyStyle.SIGNAL) {
    DeviceKey(text, onClick, modifier.fillMaxWidth(), style = style, enabled = enabled)
}

/**
 * A bar of actions at the bottom of a screen, e.g. on a detail page: graphite, rounded at the top; it keeps clear of
 * the navigation bar. Fill it with [ActionBarButton] and [ActionBarSlot], which share its width evenly.
 */
@Composable
internal fun ActionBar(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val colors = LocalDesignColors.current
    val shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.bar)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 8.dp),
        content = content,
    )
}

/** An icon button of an [ActionBar]. */
@Composable
internal fun RowScope.ActionBarButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
    ActionBarSlot {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = contentDescription, tint = LocalDesignColors.current.onGraphite)
        }
    }
}

/** A place in an [ActionBar] for own content, e.g. a toggle button; as wide as the others. */
@Composable
internal fun RowScope.ActionBarSlot(content: @Composable BoxScope.() -> Unit) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f), content = content)
}

private val CardPaddingHorizontal = 16.dp
private val CardPaddingTop = 14.dp
private val CardPaddingBottom = 14.dp
private val DisplayInset = 10.dp

package de.needanapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import de.needanapp.design.myzel.DetailRow as MyzelDetailRow
import de.needanapp.design.theme.isMyzelDesign

/**
 * One fact for a [DetailTile]: its [label] and [value]. [isWarning] paints the value in the error color, [isHighlighted]
 * in the main color (e.g. a matching checksum), [isCode] sets it in a fixed font (fingerprints, checksums).
 */
@Immutable
data class Detail(
    val label: String,
    val value: String,
    val isWarning: Boolean = false,
    val isCode: Boolean = false,
    val isHighlighted: Boolean = false,
)

/**
 * One [detail] as a tile of a [TileCard]: the label small above, the value below, which can be selected and copied.
 * With [onClick] the whole tile is a button instead, e.g. a place that opens a map: [actionIcon] in a circle at its end
 * shows what a tap does, [actionLabel] says it to TalkBack.
 */
@Composable
fun DetailTile(
    detail: Detail,
    shape: Shape,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    actionIcon: ImageVector = Icons.AutoMirrored.Outlined.OpenInNew,
    actionLabel: String? = null,
) {
    val typography = MaterialTheme.typography
    val colors = MaterialTheme.colorScheme
    val style = if (detail.isCode) typography.bodyMedium.copy(fontFamily = FontFamily.Monospace) else typography.bodyLarge
    val color = when {
        detail.isWarning -> colors.error
        detail.isHighlighted -> colors.primary
        else -> colors.onSurface
    }
    val container = TileDefaults.containerColor()
    if (onClick == null) {
        Column(modifier.clip(shape).background(container).padding(horizontal = 14.dp, vertical = 10.dp)) {
            Text(detail.label, style = typography.labelMedium, color = colors.onSurfaceVariant)
            SelectionContainer { Text(detail.value, style = style, color = color) }
        }
        return
    }
    Row(
        modifier
            .clip(shape)
            .background(container)
            .clickable(onClickLabel = actionLabel, role = Role.Button, onClick = onClick)
            .padding(start = 14.dp, top = 10.dp, end = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // a tap anywhere opens, so the value is not selectable here
        Column(Modifier.weight(1f)) {
            Text(detail.label, style = typography.labelMedium, color = colors.onSurfaceVariant)
            Text(detail.value, style = style, color = color)
        }
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier.size(40.dp).clip(CircleShape).background(colors.primary.copy(alpha = ACTION_CIRCLE_ALPHA).compositeOver(container)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(actionIcon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(22.dp))
        }
    }
}

/** [details] as lines of [DetailTile]s; two short ones in a row share a line, e.g. type and size. */
fun TileCardScope.detailLines(details: List<Detail>) {
    var index = 0
    while (index < details.size) {
        val detail = details[index]
        val next = details.getOrNull(index + 1)
        if (next != null && detail.isShort() && next.isShort()) {
            line({ m, shape -> DetailTile(detail, shape, m) }, { m, shape -> DetailTile(next, shape, m) })
            index += 2
        } else {
            line({ m, shape -> DetailTile(detail, shape, m) })
            index++
        }
    }
}

private fun Detail.isShort() = !isCode && value.length <= SHORT_DETAIL && label.length <= SHORT_DETAIL

/** A state as a band among tiles, e.g. how well an archive is protected, in the colors of its tone. */
@Composable
fun StatusTile(status: CardStatus, shape: Shape, modifier: Modifier = Modifier) {
    val (background, color) = status.tone.colors()
    Row(
        modifier
            .clip(shape)
            .background(background)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(status.icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(status.text, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

private const val SHORT_DETAIL = 16

// as the circle of an ActionTile
private const val ACTION_CIRCLE_ALPHA = 0.2f

/**
 * One fact as a small label above its value, e.g. inside a [ContentDialog] or a [GroupRowSurface]. [isCode] sets the
 * value in a monospace font (fingerprints, checksums); [selectable] lets the user copy it. [valueColor] can point out
 * a problem, e.g. with the error color.
 */
@Deprecated("Facts are tiles now: a DetailTile in a TileCard, or detailLines for several")
@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Unspecified,
    isCode: Boolean = false,
    selectable: Boolean = false,
) {
    if (isMyzelDesign) return MyzelDetailRow(
        label = label, value = value, modifier = modifier, valueColor = valueColor, isCode = isCode,
        selectable = selectable,
    )
    Column(modifier.fillMaxWidth().padding(vertical = 6.dp).semantics(mergeDescendants = true) { contentDescription = "$label: $value" }) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        val style = if (isCode) MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace) else MaterialTheme.typography.bodyLarge
        if (selectable) {
            SelectionContainer { Text(value, style = style, color = valueColor) }
        } else {
            Text(value, style = style, color = valueColor)
        }
    }
}

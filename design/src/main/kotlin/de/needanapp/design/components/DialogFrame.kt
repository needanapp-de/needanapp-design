package de.needanapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import de.needanapp.design.theme.LocalDesignColors

/** What a dialog is about, which colors its band: a destructive question, a question or input, or a note. */
internal enum class DialogTone { ACCENT, DESTRUCTIVE, QUIET }

/** A key at the bottom of a dialog; the [isMain] one is filled in the color of the dialog's band when there are more. */
@Immutable
internal class DialogKey(
    val label: String,
    val icon: ImageVector?,
    val onClick: () -> Unit,
    val isMain: Boolean = false,
    val enabled: Boolean = true,
)

/**
 * The frame of the needanapp dialogs: at the top a band in the dialog's [tone] with [icon] and [title], below it the
 * [content], at the bottom the [keys], which share the whole width between them, separated by fine lines; of two or
 * more keys the main one is filled. Labels are never cut short: keys that don't fit side by side leave out their
 * symbols, and if that is not enough either, they lie one above the other. The content scrolls when it gets long; a
 * dialog without keys shows something under way.
 */
@Composable
internal fun DialogFrame(
    title: String,
    icon: ImageVector?,
    tone: DialogTone,
    onDismiss: () -> Unit,
    keys: List<DialogKey>,
    content: (@Composable ColumnScope.() -> Unit)?,
) {
    val colors = MaterialTheme.colorScheme
    val design = LocalDesignColors.current
    val (band, onBand) = when (tone) {
        DialogTone.DESTRUCTIVE -> colors.errorContainer to colors.onErrorContainer
        DialogTone.ACCENT -> design.signal to design.onSignal
        DialogTone.QUIET -> colors.secondaryContainer to colors.onSecondaryContainer
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(DIALOG_RADIUS),
            color = colors.surfaceContainerHigh,
            contentColor = colors.onSurface,
            shadowElevation = 6.dp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp).widthIn(min = 280.dp, max = 400.dp).fillMaxWidth(),
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth().background(band).padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (icon != null) {
                        Icon(icon, contentDescription = null, tint = onBand, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(title, style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, lineHeight = 24.sp), color = onBand)
                }
                if (content != null) {
                    // the text style and color Material's dialogs give their text, which the content may rely on
                    CompositionLocalProvider(LocalContentColor provides colors.onSurfaceVariant) {
                        ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
                            Column(
                                Modifier.weight(1f, fill = false).verticalScrollWithBar().padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 16.dp),
                                content = content,
                            )
                        }
                    }
                }
                if (keys.isNotEmpty()) KeyBar(keys, band, onBand)
            }
        }
    }
}

/** The keys side by side, without their symbols where those don't fit, or one above the other, the main one on top. */
@Composable
private fun KeyBar(keys: List<DialogKey>, mainColor: Color, onMain: Color) {
    // a single key is just the way out, nothing to choose between
    val fillsMain = keys.size > 1
    val colors = MaterialTheme.colorScheme
    val divider = LocalDesignColors.current.divider
    val style = MaterialTheme.typography.labelLarge
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val labels = keys.map { it.label }
    val labelWidths = remember(labels, style, density) { labels.map { measurer.measure(it, style).size.width } }
    Column(Modifier.fillMaxWidth().background(colors.surfaceContainer)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(divider))
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val padding = with(density) { (KEY_PADDING * 2).roundToPx() } + LABEL_SLACK_PX
            val iconRoom = with(density) { (KEY_ICON + KEY_ICON_GAP).roundToPx() }
            val lines = with(density) { 1.dp.roundToPx() } * (keys.size - 1)
            val needWithIcons = keys.mapIndexed { i, key -> labelWidths[i] + padding + if (key.icon != null) iconRoom else 0 }
            val needWithout = keys.mapIndexed { i, _ -> labelWidths[i] + padding }
            val need = when {
                needWithIcons.sum() + lines <= constraints.maxWidth -> needWithIcons
                needWithout.sum() + lines <= constraints.maxWidth -> needWithout
                else -> null
            }
            if (need != null) {
                val showIcons = need === needWithIcons
                // equal shares where every label fits in one, otherwise shares by the room each label needs
                val equal = need.max() * keys.size + lines <= constraints.maxWidth
                // as high as a label in a large font needs
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                    keys.forEachIndexed { i, key ->
                        if (i > 0) Box(Modifier.width(1.dp).fillMaxHeight().background(divider))
                        val share = Modifier.weight(if (equal) 1f else need[i].toFloat())
                        Key(key, fillsMain, showIcons, mainColor, onMain, share.heightIn(min = KEY_HEIGHT).fillMaxHeight())
                    }
                }
            } else {
                Column {
                    keys.asReversed().forEachIndexed { i, key ->
                        if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(divider))
                        Key(key, fillsMain, showIcon = true, mainColor, onMain, Modifier.fillMaxWidth().heightIn(min = STACKED_KEY_HEIGHT))
                    }
                }
            }
        }
    }
}

@Composable
private fun Key(key: DialogKey, fillsMain: Boolean, showIcon: Boolean, mainColor: Color, onMain: Color, modifier: Modifier) {
    val isFilled = key.isMain && fillsMain
    val content = if (isFilled) onMain else MaterialTheme.colorScheme.onSurface
    Row(
        modifier
            .alpha(if (key.enabled) 1f else DISABLED_ALPHA)
            .background(if (isFilled) mainColor else Color.Transparent)
            .clickable(enabled = key.enabled, role = Role.Button, onClick = key.onClick)
            .padding(horizontal = KEY_PADDING),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showIcon && key.icon != null) {
            Icon(key.icon, contentDescription = null, tint = content, modifier = Modifier.size(KEY_ICON))
            Spacer(Modifier.width(KEY_ICON_GAP))
        }
        Text(key.label, style = MaterialTheme.typography.labelLarge, color = content, maxLines = 1, softWrap = false)
    }
}

private val DIALOG_RADIUS = 28.dp
private val KEY_HEIGHT = 56.dp
private val STACKED_KEY_HEIGHT = 52.dp
private val KEY_PADDING = 12.dp
private val KEY_ICON = 19.dp
private val KEY_ICON_GAP = 8.dp

/** A little more room than the measured label, so rounding never cuts its last letter. */
private const val LABEL_SLACK_PX = 4
private const val DISABLED_ALPHA = 0.38f

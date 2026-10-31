package de.needanapp.design.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.LocalDesignShapes
import de.needanapp.design.theme.NeedanappMotion
import de.needanapp.design.theme.emphasizedSpec

/**
 * One entry of a list as a card of its own, light against the background like the design's cards: [leading] (e.g.
 * an icon or a [SelectionMark]), the [title] running through when long, [subtitle] below and [trailing] at the end.
 * Given a [menu] the card is open: its [CardActionRow] unfolds below, and with [expanded] its groups ("more"). A
 * [selected] card lies on the selection color. A long press runs [onLongClick] with a short vibration, e.g. to open
 * the card or start selecting.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    selected: Boolean = false,
    menu: CardMenu? = null,
    expanded: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val designColors = LocalDesignColors.current
    val shapes = LocalDesignShapes.current
    val haptics = LocalHapticFeedback.current
    // the last actions stay while the card closes
    val last = remember { LastMenu() }
    if (menu != null) last.menu = menu
    val contentColor = if (selected) colors.onSecondaryContainer else colors.onSurface
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = shapes.rowGap)
            .designCard(RoundedCornerShape(shapes.group))
            .background(if (selected) colors.secondaryContainer else Color.Transparent),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick?.let { longClick ->
                        {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            longClick()
                        }
                    },
                )
                .heightIn(min = ROW_HEIGHT)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                MarqueeText(title, style = MaterialTheme.typography.bodyLarge, color = contentColor)
                if (!subtitle.isNullOrEmpty()) {
                    // the details on a selected card in its own text color, faded as on the others
                    val detailColor = if (selected) designColors.onSelectionVariant else colors.onSurfaceVariant
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = detailColor, maxLines = 1)
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(12.dp))
                trailing()
            }
        }
        AnimatedVisibility(
            visible = menu != null,
            enter = expandVertically(emphasizedSpec()) + fadeIn(emphasizedSpec()),
            exit = shrinkVertically(emphasizedSpec(NeedanappMotion.SHORT_DURATION)) + fadeOut(emphasizedSpec(NeedanappMotion.SHORT_DURATION)),
        ) {
            val shown = last.menu ?: return@AnimatedVisibility
            Column {
                CardActionRow(shown.actions, contentColor)
                AnimatedVisibility(
                    visible = menu != null && expanded,
                    enter = expandVertically(emphasizedSpec()) + fadeIn(emphasizedSpec()),
                    exit = shrinkVertically(emphasizedSpec(NeedanappMotion.SHORT_DURATION)) + fadeOut(emphasizedSpec(NeedanappMotion.SHORT_DURATION)),
                ) {
                    CardActionGroups(shown.groups, contentColor, if (selected) colors.secondaryContainer else designColors.card)
                }
            }
        }
    }
}

private class LastMenu {
    var menu: CardMenu? = null
}

private val ROW_HEIGHT = 52.dp

package de.needanapp.design.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Chips side by side in a line that scrolls sideways, e.g. filters, places or the sheets of a table, each made by
 * [chip] (usually a FilterChip, rounded by the theme). The line starts at [initialIndex], e.g. the chosen place far
 * to the right, and runs to the screen's edges while its first and last chip keep the usual 16 dp.
 */
@Composable
fun <T> ChipRow(entries: List<T>, modifier: Modifier = Modifier, initialIndex: Int = 0, chip: @Composable (T) -> Unit) {
    LazyRow(
        state = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex.coerceIn(0, maxOf(entries.size - 1, 0))),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(entries) { chip(it) }
    }
}

/**
 * A tab or place as a chip, e.g. in a line of [ChipRow]: [label] with an optional [icon], the [selected] one in the
 * look of a chosen FilterChip. Beside [onClick], a long press runs [onLongClick], e.g. a card with more about the tab;
 * [onClickLabel] and [onLongClickLabel] name them for TalkBack, e.g. when a tap on the shown tab opens that card.
 */
@Composable
fun TabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    onLongClick: (() -> Unit)? = null,
    onLongClickLabel: String? = null,
    onClickLabel: String? = null,
) {
    val haptics = LocalHapticFeedback.current
    // the chip shows the presses, the layer above it takes them, long ones included
    val interactions = remember { MutableInteractionSource() }
    Box(modifier) {
        FilterChip(
            selected = selected,
            onClick = onClick,
            label = { Text(label, maxLines = 1) },
            leadingIcon = icon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) } },
            interactionSource = interactions,
            modifier = Modifier.clearAndSetSemantics { },
        )
        Box(
            Modifier
                .matchParentSize()
                .combinedClickable(
                    interactionSource = interactions,
                    indication = null,
                    role = Role.Tab,
                    onClickLabel = onClickLabel,
                    onLongClickLabel = onLongClickLabel,
                    onLongClick = onLongClick?.let {
                        {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            it()
                        }
                    },
                    onClick = onClick,
                )
                .semantics {
                    contentDescription = label
                    this.selected = selected
                },
        )
    }
}

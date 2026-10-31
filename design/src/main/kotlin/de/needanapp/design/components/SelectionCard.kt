package de.needanapp.design.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.needanapp.design.R
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.LocalDesignEffects
import de.needanapp.design.theme.LocalDesignShapes
import de.needanapp.design.theme.NeedanappMotion
import de.needanapp.design.theme.emphasizedSpec

/**
 * The actions for several selected entries, floating at the bottom in place of the pill: a card in the selected
 * entries' color with [count] in a badge, [summary] (e.g. "3 items · 330 kB") over the [names] running through, select
 * all at the end, then the [menu]'s [CardActionRow]; with [expanded] its groups unfold below as in an opened card, and
 * back folds them. While [visible] turns false it slides away showing what was selected last; it also steps aside for
 * the keyboard. [onHeightChange] tells how much of the screen it takes, e.g. to move a bar above it.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectionCard(
    count: Int,
    summary: String,
    names: String,
    menu: CardMenu,
    expanded: Boolean,
    onFold: () -> Unit,
    onSelectAll: () -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier,
    onHeightChange: (Dp) -> Unit = {},
) {
    val last = remember { ShownSelection() }
    if (visible) {
        last.count = count
        last.summary = summary
        last.names = names
        last.menu = menu
    }
    val shown = visible && !WindowInsets.isImeVisible
    if (shown && expanded) BackHandler(onBack = onFold)
    val density = LocalDensity.current
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(LocalDesignShapes.current.group)
    val maxHeight = LocalConfiguration.current.screenHeightDp.dp * MAX_HEIGHT_SHARE
    AnimatedVisibility(
        visible = shown,
        enter = slideInVertically(emphasizedSpec()) { it } + fadeIn(emphasizedSpec()),
        exit = slideOutVertically(emphasizedSpec()) { it } + fadeOut(emphasizedSpec()),
        modifier = modifier,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Column(
                Modifier
                    .widthIn(max = MAX_WIDTH)
                    .fillMaxWidth()
                    .onSizeChanged { onHeightChange(with(density) { it.height.toDp() }) }
                    .then(if (LocalDesignEffects.current.shadows) Modifier.shadow(6.dp, shape) else Modifier)
                    .designCard(shape)
                    .background(colors.secondaryContainer)
                    .heightIn(max = maxHeight)
                    .verticalScrollWithBar(),
            ) {
                SelectionHead(last.count, last.summary, last.names, onSelectAll)
                CardActionRow(last.menu.actions, colors.onSecondaryContainer)
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(emphasizedSpec()) + fadeIn(emphasizedSpec()),
                    exit = shrinkVertically(emphasizedSpec(NeedanappMotion.SHORT_DURATION)) + fadeOut(emphasizedSpec(NeedanappMotion.SHORT_DURATION)),
                ) {
                    CardActionGroups(last.menu.groups, colors.onSecondaryContainer, colors.secondaryContainer)
                }
            }
        }
    }
}

/**
 * The count in a badge, the summary over the names, select all at the end. The badge takes the check mark's colors
 * ([SelectionMark]), made to stand out on the selected entries' color; the accent may not, e.g. yellow on yellow.
 */
@Composable
private fun SelectionHead(count: Int, summary: String, names: String, onSelectAll: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val design = LocalDesignColors.current
    Row(Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .widthIn(min = BADGE_SIZE)
                .height(BADGE_SIZE)
                .background(design.mark, CircleShape)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(count.toString(), style = MaterialTheme.typography.labelLarge, color = design.onMark, maxLines = 1)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(summary, style = MaterialTheme.typography.bodyLarge, color = colors.onSecondaryContainer, maxLines = 1)
            MarqueeText(names, style = MaterialTheme.typography.bodySmall, color = design.onSelectionVariant)
        }
        IconButton(onClick = onSelectAll) {
            Icon(Icons.Outlined.SelectAll, stringResource(R.string.nd_select_all), tint = colors.onSecondaryContainer)
        }
    }
}

/** What a [SelectionCard] showed while it was visible, for sliding away. */
private class ShownSelection {
    var count = 0
    var summary = ""
    var names = ""
    var menu = CardMenu(emptyList(), emptyList())
}

private val BADGE_SIZE = 32.dp
private val MAX_WIDTH = 560.dp
private const val MAX_HEIGHT_SHARE = 0.7f

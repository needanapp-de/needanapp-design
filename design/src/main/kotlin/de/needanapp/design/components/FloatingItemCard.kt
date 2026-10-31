package de.needanapp.design.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.LocalDesignEffects
import de.needanapp.design.theme.LocalDesignShapes
import de.needanapp.design.theme.NeedanappMotion
import de.needanapp.design.theme.emphasizedSpec

/**
 * The actions for one item, floating at the bottom in place of the pill, e.g. for the picture a viewer shows: a card
 * in the card color like an opened card in a list, with [title] running through and [subtitle] below it (e.g. size
 * and date), then the [menu]'s [CardActionRow]; with [expanded] its groups unfold below, and back folds them. Given
 * [details] (e.g. what is known about the picture, as [CardTileGroups]) unfold there instead of the groups, and back
 * folds them as well. It slides away while [visible] is false, e.g. in full screen, and steps aside for the keyboard.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FloatingItemCard(
    title: String,
    subtitle: String?,
    menu: CardMenu,
    expanded: Boolean,
    onFold: () -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier,
    details: (@Composable () -> Unit)? = null,
) {
    val shown = visible && !WindowInsets.isImeVisible
    if (shown && (expanded || details != null)) BackHandler(onBack = onFold)
    // the last details stay while they fold away
    val lastDetails = remember { LastDetails() }
    if (details != null) lastDetails.content = details
    val colors = MaterialTheme.colorScheme
    val cardColor = LocalDesignColors.current.card
    val shape = RoundedCornerShape(LocalDesignShapes.current.group)
    val maxHeight = LocalConfiguration.current.screenHeightDp.dp * MAX_HEIGHT_SHARE
    // slides in also when it comes along with the screen or the request that shows it
    val appearance = remember { MutableTransitionState(false) }
    appearance.targetState = shown
    AnimatedVisibility(
        visibleState = appearance,
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
                    .then(if (LocalDesignEffects.current.shadows) Modifier.shadow(6.dp, shape) else Modifier)
                    .designCard(shape)
                    .background(cardColor)
                    .heightIn(max = maxHeight)
                    .verticalScrollWithBar(),
            ) {
                Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)) {
                    MarqueeText(title, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
                    if (!subtitle.isNullOrEmpty()) {
                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1)
                    }
                }
                CardActionRow(menu.actions, colors.onSurface)
                AnimatedVisibility(
                    visible = expanded && details == null,
                    enter = expandVertically(emphasizedSpec()) + fadeIn(emphasizedSpec()),
                    exit = shrinkVertically(emphasizedSpec(NeedanappMotion.SHORT_DURATION)) + fadeOut(emphasizedSpec(NeedanappMotion.SHORT_DURATION)),
                ) {
                    CardActionGroups(menu.groups, colors.onSurface, cardColor)
                }
                AnimatedVisibility(
                    visible = details != null,
                    enter = expandVertically(emphasizedSpec()) + fadeIn(emphasizedSpec()),
                    exit = shrinkVertically(emphasizedSpec(NeedanappMotion.SHORT_DURATION)) + fadeOut(emphasizedSpec(NeedanappMotion.SHORT_DURATION)),
                ) {
                    lastDetails.content?.invoke()
                }
            }
        }
    }
}

/** What goes with a [FloatingItemCard]. */
object FloatingItemCardDefaults {
    /**
     * About the folded card's height: kept free below scrolling content (added to the bottom inset), so its end can
     * be read above the card.
     */
    val SpaceBelowContent = 140.dp
}

private class LastDetails {
    var content: (@Composable () -> Unit)? = null
}

private val MAX_WIDTH = 560.dp
private const val MAX_HEIGHT_SHARE = 0.7f

package de.needanapp.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.LocalDesignShapes
import de.needanapp.design.theme.contrastRatio
import de.needanapp.design.theme.readsLight

/** One button of an opened card; [isDestructive] ones (delete) lie on red, an [isActive] one (more, unfolded) is marked. */
class CardAction(
    val icon: ImageVector,
    val label: String,
    val enabled: Boolean = true,
    val isDestructive: Boolean = false,
    val isActive: Boolean = false,
    val onClick: () -> Unit,
)

/** A titled set of the further actions an opened card unfolds. */
class CardActionGroup(val title: String, val actions: List<CardAction>)

/** What an opened card offers: its row of [actions], and the [groups] "more" unfolds below them. */
class CardMenu(val actions: List<CardAction>, val groups: List<CardActionGroup>)

/**
 * The actions inside an opened card, side by side, each its symbol over a short label in [contentColor], the card's
 * text color. Destructive ones always lie on red; long labels get a little smaller instead of breaking apart.
 */
@Composable
fun CardActionRow(actions: List<CardAction>, contentColor: Color, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        actions.forEach { action ->
            val color = if (action.isDestructive) colors.onErrorContainer else contentColor
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when {
                            action.isDestructive -> colors.errorContainer
                            action.isActive -> contentColor.copy(alpha = ACTIVE_ACTION_ALPHA)
                            else -> Color.Transparent
                        },
                    )
                    .clickable(enabled = action.enabled, onClick = action.onClick)
                    .heightIn(min = 56.dp)
                    .padding(vertical = 6.dp)
                    .alpha(if (action.enabled) 1f else DISABLED_ACTION_ALPHA),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(action.icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                Spacer(Modifier.height(4.dp))
                Text(
                    action.label,
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                    color = color,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    softWrap = false,
                    autoSize = TextAutoSize.StepBased(minFontSize = LABEL_MIN_SIZE, maxFontSize = MaterialTheme.typography.labelSmall.fontSize),
                    modifier = Modifier.padding(horizontal = 2.dp),
                )
            }
        }
    }
}

/**
 * The groups "more" unfolds below a card's [CardActionRow], each in a box of its own with up to four buttons to a line,
 * outer corners round and inner ones small as in a grouped list. The boxes sink into [cardColor] toward the background;
 * where the card's [contentColor] would sink with them (a bright card in the dark, a deep one in the light), they sink
 * deeper and take the background's text color instead. The group titles are in the accent where it reads on the box,
 * otherwise in the buttons' color.
 */
@Composable
fun CardActionGroups(groups: List<CardActionGroup>, contentColor: Color, cardColor: Color, modifier: Modifier = Modifier) {
    GroupBoxes(groups.map { it.title }, contentColor, cardColor, modifier, PaddingValues(start = 2.dp, end = 2.dp, top = 8.dp, bottom = 2.dp)) { index, boxContent ->
        groups[index].actions.chunked(GROUP_COLUMNS).forEach { line ->
            Row(Modifier.fillMaxWidth()) {
                line.forEach { action -> GroupActionButton(action, boxContent, Modifier.weight(1f)) }
                repeat(GROUP_COLUMNS - line.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/**
 * Facts unfolded below a card's [CardActionRow] in place of its groups, e.g. what is known about a picture: each
 * [TileCardScope.label] starts a box like those of [CardActionGroups] with the label as its title, the lines of tiles
 * in it lie as in a [TileCard]. Lines before the first label get a box without a title. The colors are those of a
 * card ([FloatingItemCard]) unless given.
 */
@Composable
inline fun CardTileGroups(
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    cardColor: Color = LocalDesignColors.current.card,
    modifier: Modifier = Modifier,
    content: @Composable TileCardScope.() -> Unit,
) {
    // inline for the same reason as TileCard
    val scope = TileCardScope()
    scope.content()
    CardTileGroupsLayout(contentColor, cardColor, modifier, scope)
}

@PublishedApi
@Composable
internal fun CardTileGroupsLayout(contentColor: Color, cardColor: Color, modifier: Modifier, scope: TileCardScope) {
    val sections = mutableListOf<Pair<String?, MutableList<TilePart.Line>>>()
    scope.parts.forEach { part ->
        when (part) {
            is TilePart.Label -> sections += part.text to mutableListOf()
            is TilePart.Line -> {
                if (sections.isEmpty()) sections += null to mutableListOf()
                sections.last().second += part
            }
        }
    }
    val shapes = LocalDesignShapes.current
    // nested in the box, which is nested in the card
    val outer = (shapes.group - GROUP_MARGIN - TILE_BOX_PADDING).coerceAtLeast(shapes.inner)
    GroupBoxes(sections.map { it.first }, contentColor, cardColor, modifier, PaddingValues(TILE_BOX_PADDING)) { index, _ ->
        val lines = sections[index].second
        Column(
            Modifier.padding(top = if (sections[index].first != null) 6.dp else 0.dp),
            verticalArrangement = Arrangement.spacedBy(shapes.rowGap),
        ) {
            lines.forEachIndexed { line, part -> TileLine(part, joinsAbove = line > 0, joinsBelow = line < lines.lastIndex, outer = outer) }
        }
    }
}

/**
 * The boxes of [CardActionGroups] and [CardTileGroups], one per title (null: without one) with [padding] inside, outer
 * corners round and inner ones small as in a grouped list. The boxes sink into [cardColor] toward the background;
 * where the card's [contentColor] would sink with them, they sink deeper and hand their [content] the background's
 * text color instead. The titles are in the accent where it reads on the box, otherwise in that color.
 */
@Composable
private fun GroupBoxes(
    titles: List<String?>,
    contentColor: Color,
    cardColor: Color,
    modifier: Modifier,
    padding: PaddingValues,
    content: @Composable (index: Int, boxContent: Color) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val shapes = LocalDesignShapes.current
    val turns = contentColor.readsLight() != colors.onSurface.readsLight()
    val box = colors.surface.copy(alpha = if (turns) GROUP_BOX_ALPHA_DEEP else GROUP_BOX_ALPHA)
    val boxBehind = box.compositeOver(cardColor)
    val boxContent = if (turns) colors.onSurface else contentColor
    val titleColor = if (contrastRatio(colors.primary, boxBehind) >= MIN_TITLE_CONTRAST) colors.primary else boxContent
    // nested in the card, so its corners less round by the margin
    val outer = (shapes.group - GROUP_MARGIN).coerceAtLeast(shapes.inner)
    Column(
        modifier
            .fillMaxWidth()
            .padding(start = GROUP_MARGIN, end = GROUP_MARGIN, bottom = GROUP_MARGIN),
        verticalArrangement = Arrangement.spacedBy(shapes.rowGap),
    ) {
        titles.forEachIndexed { index, title ->
            val top = if (index == 0) outer else shapes.inner
            val bottom = if (index == titles.lastIndex) outer else shapes.inner
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom))
                    .background(box)
                    .padding(padding),
            ) {
                if (title != null) {
                    Text(
                        title,
                        style = MaterialTheme.typography.labelMedium,
                        color = titleColor,
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .semantics { heading() },
                    )
                }
                content(index, boxContent)
            }
        }
    }
}

/** A button among the unfolded ones: its symbol over a label of up to two lines. */
@Composable
private fun GroupActionButton(action: CardAction, contentColor: Color, modifier: Modifier) {
    Column(
        modifier
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(12.dp))
            // an active one (e.g. a mode that is on) is marked like in the row above
            .background(if (action.isActive) contentColor.copy(alpha = ACTIVE_ACTION_ALPHA) else Color.Transparent)
            .clickable(enabled = action.enabled, onClick = action.onClick)
            .semantics { selected = action.isActive }
            .heightIn(min = 56.dp)
            .padding(vertical = 6.dp)
            .alpha(if (action.enabled) 1f else DISABLED_ACTION_ALPHA),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(action.icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp))
        Spacer(Modifier.height(4.dp))
        Text(
            action.label,
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp, hyphens = Hyphens.Auto),
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = 2.dp),
        )
    }
}

/**
 * The check mark in place of a selected entry's icon: a circle in the color world's [mark][de.needanapp.design.theme.DesignColors.mark]
 * color, which stands out on selected cards, with the tick on it. It fills the size [modifier] gives it.
 */
@Composable
fun SelectionMark(modifier: Modifier = Modifier) {
    val design = LocalDesignColors.current
    Box(modifier.background(design.mark, CircleShape), contentAlignment = Alignment.Center) {
        Icon(Icons.Filled.Check, contentDescription = null, tint = design.onMark)
    }
}

private const val DISABLED_ACTION_ALPHA = 0.38f
private const val ACTIVE_ACTION_ALPHA = 0.12f
private const val GROUP_BOX_ALPHA = 0.4f

/** Where the boxes take the background's text color, enough of the background for it to read well. */
private const val GROUP_BOX_ALPHA_DEEP = 0.65f

/** What a group title in the accent needs against its box to read as small text; below, it takes the buttons' color. */
private const val MIN_TITLE_CONTRAST = 4.5f
private const val GROUP_COLUMNS = 4
private val GROUP_MARGIN = 8.dp
private val TILE_BOX_PADDING = 4.dp
private val LABEL_MIN_SIZE = 8.sp

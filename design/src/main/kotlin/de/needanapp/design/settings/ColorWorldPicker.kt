package de.needanapp.design.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.SurfaceColors

/** One choosable color world: its name and the colors its preview shows, with the accent it would have. */
internal class WorldChoice(val name: String, val colors: SurfaceColors, val accent: Color)

/**
 * The color worlds as small previews: background, a card with two lines of text and the accent. A tap chooses one; when
 * its theme is the one on screen, the whole app changes at once. [compact] tiles fill rows of their own without names
 * (TalkBack reads them), e.g. where an introduction has little room.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun ColorWorldPicker(choices: List<WorldChoice>, selected: Int, compact: Boolean = false, onSelect: (Int) -> Unit) {
    if (compact) {
        Column(verticalArrangement = Arrangement.spacedBy(COMPACT_GAP)) {
            choices.chunked(COMPACT_COLUMNS).forEachIndexed { row, chunk ->
                Row(horizontalArrangement = Arrangement.spacedBy(COMPACT_GAP)) {
                    chunk.forEachIndexed { column, choice ->
                        val index = row * COMPACT_COLUMNS + column
                        WorldTile(choice, selected = index == selected, onClick = { onSelect(index) }, compact = true, modifier = Modifier.weight(1f))
                    }
                    // a shorter last row keeps the width of the tiles above
                    repeat(COMPACT_COLUMNS - chunk.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        return
    }
    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        choices.forEachIndexed { index, choice ->
            WorldTile(choice, selected = index == selected, onClick = { onSelect(index) })
        }
    }
}

@Composable
private fun WorldTile(choice: WorldChoice, selected: Boolean, onClick: () -> Unit, compact: Boolean = false, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val design = LocalDesignColors.current
    val colors = choice.colors
    val shape = RoundedCornerShape(12.dp)
    val cardShape = RoundedCornerShape(6.dp)
    Column(
        modifier
            .then(if (compact) Modifier.semantics { contentDescription = choice.name } else Modifier.width(TILE_WIDTH))
            .clip(RoundedCornerShape(14.dp))
            .selectable(selected, role = Role.RadioButton, onClick = onClick)
            .padding(bottom = if (compact) 0.dp else 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(if (compact) COMPACT_TILE_HEIGHT else TILE_HEIGHT)
                .clip(shape)
                .background(colors.background)
                .border(if (selected) 3.dp else 1.dp, if (selected) scheme.primary else scheme.outlineVariant, shape)
                .padding(if (compact) 5.dp else 8.dp),
        ) {
            Column(
                Modifier.fillMaxWidth().clip(cardShape).background(colors.card).border(1.dp, colors.line, cardShape).padding(if (compact) 4.dp else 6.dp),
                verticalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 5.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(if (compact) 3.dp else 5.dp)) {
                    Box(Modifier.size(if (compact) 6.dp else 9.dp).clip(CircleShape).background(choice.accent))
                    Box(Modifier.height(4.dp).weight(1f).clip(CircleShape).background(colors.text))
                }
                Box(Modifier.height(3.dp).fillMaxWidth(0.7f).clip(CircleShape).background(colors.textVariant))
            }
            if (selected && !compact) {
                Box(Modifier.align(Alignment.BottomEnd).size(18.dp).clip(CircleShape).background(design.signal), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = design.onSignal, modifier = Modifier.size(13.dp))
                }
            }
        }
        if (!compact) {
            Text(choice.name, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

private val TILE_WIDTH = 84.dp
private val TILE_HEIGHT = 64.dp
private val COMPACT_TILE_HEIGHT = 40.dp
private val COMPACT_GAP = 8.dp
private const val COMPACT_COLUMNS = 6

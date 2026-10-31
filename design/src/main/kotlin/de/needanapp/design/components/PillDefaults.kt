package de.needanapp.design.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Colors for buttons lying on a [FloatingSurface], e.g. a bar floating in place of the pill: the main action as a tonal
 * button in the accent's container color, which stands out on the pill color in every color world (the plain tonal
 * secondary container often almost matches it). Further actions stay plain icon buttons in the pill's content color.
 * Surface and buttons take the pill's corners, [shape] and [buttonShape].
 */
object PillDefaults {
    /** The corners of the pill and of bars floating with it, e.g. one showing a copy's progress. */
    val shape: Shape = RoundedCornerShape(12.dp)

    /** The corners of the active entry and of buttons on such a surface, a little less round than it. */
    val buttonShape: Shape = RoundedCornerShape(8.dp)

    @Composable
    fun tonalButtonColors(): ButtonColors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )

    @Composable
    fun tonalIconButtonColors(): IconButtonColors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )
}

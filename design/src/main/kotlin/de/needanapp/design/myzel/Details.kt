package de.needanapp.design.myzel

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.sp
import de.needanapp.design.theme.DesignType
import androidx.compose.ui.unit.dp
import de.needanapp.design.components.*

/**
 * One fact as a small label above its value, e.g. inside a [ContentDialog] or a [GroupRowSurface]. [isCode] sets the
 * value in a monospace font (fingerprints, checksums); [selectable] lets the user copy it. [valueColor] can point out
 * a problem, e.g. with the error color.
 */
@Composable
internal fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.Unspecified,
    isCode: Boolean = false,
    selectable: Boolean = false,
) {
    Column(modifier.fillMaxWidth().padding(vertical = 6.dp).semantics(mergeDescendants = true) { contentDescription = "$label: $value" }) {
        MonoLabel(label, style = DesignType.label.copy(fontSize = 10.5.sp))
        Spacer(Modifier.height(3.dp))
        val style = if (isCode) DesignType.value else MaterialTheme.typography.bodyLarge
        if (selectable) {
            SelectionContainer { Text(value, style = style, color = valueColor) }
        } else {
            Text(value, style = style, color = valueColor)
        }
    }
}

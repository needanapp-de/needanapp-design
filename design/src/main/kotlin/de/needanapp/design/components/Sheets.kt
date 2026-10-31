package de.needanapp.design.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.needanapp.design.myzel.OptionsSheet as MyzelOptionsSheet
import de.needanapp.design.theme.isMyzelDesign
import de.needanapp.design.myzel.SheetAction as MyzelSheetAction
import de.needanapp.design.myzel.EmptyState as MyzelEmptyState

/**
 * A bottom sheet for options and actions instead of a dialog: full width, the screen stays visible above it.
 * [content] typically holds [SheetAction]s or [SettingsGroup]s.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsSheet(
    onDismiss: () -> Unit,
    title: String? = null,
    skipPartiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (isMyzelDesign) return MyzelOptionsSheet(
        onDismiss = onDismiss, title = title, skipPartiallyExpanded = skipPartiallyExpanded, content = content,
    )
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded),
        sheetMaxWidth = Dp.Unspecified,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(bottom = 16.dp)) {
            if (title != null) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).semantics { heading() },
                )
            }
            content()
        }
    }
}

/** One action row of an [OptionsSheet]. */
@Composable
@Deprecated("Actions in a sheet are tiles now: a TileCard of ActionTiles, centered two in a line, inline when alone")
fun SheetAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    summary: String? = null,
    tint: Color = Color.Unspecified,
    enabled: Boolean = true,
) {
    // the Myzel style draws the icon in the color of the text
    if (isMyzelDesign) return MyzelSheetAction(
        icon = icon, label = label, onClick = onClick, summary = summary,
        tint = tint.takeOrElse { MaterialTheme.colorScheme.onSurface }, enabled = enabled,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = tint.takeOrElse { MaterialTheme.colorScheme.onSurfaceVariant }, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(20.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (summary != null) {
                Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * A calm placeholder while something loads, e.g. a folder on a server: [icon] in a turning ring and [title] below,
 * e.g. "Opening NAS…". It fades in after a moment, so quick loads don't flash it.
 */
@Composable
fun LoadingState(title: String, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) { alpha.animateTo(1f, tween(LOADING_FADE_MS, delayMillis = LOADING_APPEAR_DELAY_MS)) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
            .graphicsLayer { this.alpha = alpha.value }
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
            SignalCircularProgressIndicator(Modifier.fillMaxSize(), strokeWidth = 3.dp)
            if (icon != null) Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

private const val LOADING_APPEAR_DELAY_MS = 150
private const val LOADING_FADE_MS = 200

/** A calm placeholder for empty lists: icon, title, explanation and an optional [action]. */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    if (isMyzelDesign) return MyzelEmptyState(
        icon = icon, title = title, modifier = modifier, message = message, action = action,
    )
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (message != null) {
            Spacer(Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
        if (action != null) {
            Spacer(Modifier.height(16.dp))
            action()
        }
    }
}

package de.needanapp.design.components

import androidx.compose.animation.core.animate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.needanapp.design.theme.LocalDesignShapes
import de.needanapp.design.theme.emphasizedSpec
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * A row that a red button with [actionLabel] pushes in from its end when swiped towards its start; only a tap on that
 * button runs [onAction]. Meant for removing entries without a button on the row that a second tap could hit by
 * mistake. The row gets narrower as the button comes in and keeps the height it has at rest, whatever its content
 * would need at that width. While the row is open, a tap on it closes it again. TalkBack offers the action on the row
 * as [accessibilityLabel].
 *
 * [content] is the row itself, e.g. a [ListRow] at the same [position]. Rows in a list should be keyed by their entry,
 * so an open row doesn't pass its state on to the next one when its entry goes away. A row of another shape, e.g. a
 * tile in a [TileCard], passes its [shape].
 */
@Composable
fun SwipeActionRow(
    position: RowPosition,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    actionIcon: ImageVector = Icons.Outlined.Delete,
    accessibilityLabel: String = actionLabel,
    enabled: Boolean = true,
    shape: Shape? = null,
    content: @Composable () -> Unit,
) {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val spec = emphasizedSpec<Float>()
    val shapes = LocalDesignShapes.current
    val gap = with(LocalDensity.current) { shapes.rowGap.toPx() }
    val currentOnAction by rememberUpdatedState(onAction)
    // sizes the layout finds out; plain fields, they never need to recompose anything
    val measured = remember { Measured() }
    // how far the button has pushed in; 0 is closed, its width + gap open
    var push by remember { mutableFloatStateOf(0f) }
    val isOpen by remember { derivedStateOf { push > 0f } }
    var settling by remember { mutableStateOf<Job?>(null) }

    fun openWidth() = measured.buttonWidth + gap

    fun settle(open: Boolean) {
        settling?.cancel()
        settling = scope.launch {
            animate(push, if (open) openWidth() else 0f, animationSpec = spec) { value, _ -> push = value }
        }
    }

    fun act() {
        settling?.cancel()
        push = 0f
        currentOnAction()
    }

    LaunchedEffect(enabled) { if (!enabled) settle(open = false) }

    // on a card the row lies flat; a tile keeps its shape, which also rounds the button's outer corners
    val rowShape = shape ?: if (LocalCardRowPadding.current != null) RoundedCornerShape(shapes.inner) else position.shape()
    Layout(
        content = {
            Box {
                val actions = remember(enabled, accessibilityLabel) {
                    if (enabled) listOf(CustomAccessibilityAction(accessibilityLabel) { act(); true }) else emptyList()
                }
                CompositionLocalProvider(LocalRowActions provides actions) { content() }
                // while open, a tap on the row only closes it
                if (isOpen) {
                    Box(Modifier.matchParentSize().pointerInput(Unit) { detectTapGestures { settle(open = false) } })
                }
            }
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(shapes.inner))
                    .background(MaterialTheme.colorScheme.error)
                    .clickable(enabled = enabled && isOpen) { act() }
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(actionIcon, contentDescription = null, tint = MaterialTheme.colorScheme.onError, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onError, maxLines = 1)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clip(rowShape)
            .draggable(
                state = rememberDraggableState { delta ->
                    push = (push + if (isRtl) delta else -delta).coerceIn(0f, openWidth())
                },
                orientation = Orientation.Horizontal,
                enabled = enabled,
                onDragStarted = { settling?.cancel() },
                onDragStopped = { velocity ->
                    val towardsStart = if (isRtl) velocity else -velocity
                    val open = when {
                        towardsStart > FLING_VELOCITY -> true
                        towardsStart < -FLING_VELOCITY -> false
                        else -> push > openWidth() / 2
                    }
                    if (open) haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                    settle(open)
                },
            ),
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val pushed = push.roundToInt().coerceIn(0, width)
        val rowWidth = width - pushed
        // squeezed, the row keeps its height at rest; its content may be cut off meanwhile
        val row = measurables[0].measure(
            if (pushed > 0 && measured.restingHeight > 0) {
                Constraints.fixed(rowWidth, measured.restingHeight)
            } else {
                constraints.copy(minWidth = rowWidth, maxWidth = rowWidth)
            },
        )
        if (pushed == 0) measured.restingHeight = row.height
        val button = measurables[1].measure(Constraints.fixedHeight(row.height))
        measured.buttonWidth = button.width
        layout(width, row.height) {
            row.placeRelative(0, 0)
            // closed, the button waits just past the end, outside of the clip
            button.placeRelative(rowWidth + gap.roundToInt(), 0)
        }
    }
}

private class Measured {
    var restingHeight = 0
    var buttonWidth = 0
}

/** Pixels per second from which a swipe opens or closes the row whatever its distance. */
private const val FLING_VELOCITY = 800f

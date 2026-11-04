package de.needanapp.design.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

/**
 * A thin bar at the end edge of a scrolling list showing where it stands and how much of it is in view. It shows while
 * the list scrolls and fades out a second after; it can't be grabbed. [contentPadding] is the list's own, so the bar
 * runs between a header the list scrolls under and the pill below it. Nothing shows while everything fits. With
 * [reverseLayout] as given to the list (e.g. a chat with the newest message at the bottom), the bar stands as the list
 * shows it, not by its order of items.
 */
@Composable
fun Modifier.scrollbar(state: LazyListState, contentPadding: PaddingValues = PaddingValues(), reverseLayout: Boolean = false): Modifier =
    scrollbar(remember(state, reverseLayout) { LazyListSource(state, reverseLayout) }, contentPadding)

/** [scrollbar] for a grid. */
@Composable
fun Modifier.scrollbar(state: LazyGridState, contentPadding: PaddingValues = PaddingValues()): Modifier =
    scrollbar(remember(state) { LazyGridSource(state) }, contentPadding)

/** [scrollbar] for a column scrolled with [Modifier.verticalScroll][androidx.compose.foundation.verticalScroll]; goes before it. */
@Composable
fun Modifier.scrollbar(state: ScrollState, contentPadding: PaddingValues = PaddingValues()): Modifier =
    scrollbar(remember(state) { ScrollStateSource(state) }, contentPadding)

@Composable
private fun Modifier.scrollbar(source: ScrollbarSource, contentPadding: PaddingValues): Modifier {
    val shown by rememberShown({ source.isScrolling }, BAR_HIDE_DELAY_MS)
    val color = ScrollbarDefaults.barColor()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    return drawWithContent {
        drawContent()
        val alpha = shown
        if (alpha <= 0f) return@drawWithContent
        val span = source.span() ?: return@drawWithContent
        val top = contentPadding.calculateTopPadding().toPx() + TRACK_INSET.toPx()
        val track = size.height - contentPadding.calculateBottomPadding().toPx() - TRACK_INSET.toPx() - top
        if (track <= 0f) return@drawWithContent
        val length = maxOf(track * span.visible, MIN_BAR_LENGTH.toPx()).coerceAtMost(track)
        val width = BAR_WIDTH.toPx()
        val x = if (isRtl) EDGE_GAP.toPx() else size.width - EDGE_GAP.toPx() - width
        drawRoundRect(
            color = color.copy(alpha = color.alpha * alpha),
            topLeft = Offset(x, top + (track - length) * span.progress),
            size = Size(width, length),
            cornerRadius = CornerRadius(width / 2),
        )
    }
}

/** Scrolls a column like [verticalScroll] and shows the thin [scrollbar] while it does. */
@Composable
fun Modifier.verticalScrollWithBar(state: ScrollState = rememberScrollState()): Modifier = scrollbar(state).verticalScroll(state)

/**
 * Scrolling through a long list fast: while it scrolls, a handle shows at the end edge that can be grabbed; dragging
 * it moves through the whole list, and a bubble beside it names where it is, given by [label] for the key of the
 * topmost item in view (e.g. the first letter, a date or a size; null skips an item such as a header). A list
 * shorter than two screens only gets the thin bar of [scrollbar]. Lies over the list in the same box, e.g. with
 * [Modifier.matchParentSize][androidx.compose.foundation.layout.BoxScope.matchParentSize]; [contentPadding] is the
 * list's own. Hidden from TalkBack, which scrolls by itself.
 */
@Composable
fun FastScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    label: ((key: Any) -> String?)? = null,
) = FastScrollbar(state, modifier, contentPadding, reverseLayout = false, label)

/**
 * [FastScrollbar] for a list with [reverseLayout] as given to it: handle, dragging and the bubble then follow what is
 * on screen, the topmost item being the one furthest from the start of the list. An overload of its own, so that
 * [label] stays the last parameter (a trailing lambda) and the fourth one of the variant above.
 */
@Composable
fun FastScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    reverseLayout: Boolean,
    label: ((key: Any) -> String?)? = null,
) = FastScrollbar(remember(state, reverseLayout) { LazyListSource(state, reverseLayout) }, modifier, contentPadding, label)

/** [FastScrollbar] for a grid. */
@Composable
fun FastScrollbar(
    state: LazyGridState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    label: ((key: Any) -> String?)? = null,
) = FastScrollbar(remember(state) { LazyGridSource(state) }, modifier, contentPadding, label)

@Composable
private fun FastScrollbar(source: ScrollbarSource, modifier: Modifier, contentPadding: PaddingValues, label: ((Any) -> String?)?) {
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    var dragging by remember { mutableStateOf(false) }
    // where the handle's top is while it is dragged, from the top of the track
    var dragTop by remember { mutableFloatStateOf(0f) }
    var dragFraction by remember { mutableFloatStateOf(0f) }
    val isLong by remember(source) { derivedStateOf { (source.span()?.visible ?: 1f) <= 1f / FAST_MIN_SCREENS } }
    val shown by rememberShown({ source.isScrolling || dragging }, HANDLE_HIDE_DELAY_MS)

    if (!isLong) {
        // short lists: only the thin bar
        Box(modifier.fillMaxSize().scrollbar(source, contentPadding).clearAndSetSemantics { })
        return
    }
    LaunchedEffect(source, dragging) {
        if (dragging) snapshotFlow { dragFraction }.collectLatest { source.scrollTo(it) }
    }
    BoxWithConstraints(
        modifier
            .fillMaxSize()
            .padding(top = contentPadding.calculateTopPadding() + TRACK_INSET, bottom = contentPadding.calculateBottomPadding() + TRACK_INSET)
            .clearAndSetSemantics { },
    ) {
        val handleHeight = with(density) { HANDLE_LENGTH.toPx() }
        val room = (constraints.maxHeight - handleHeight).coerceAtLeast(0f)
        val handleTop by remember(room, source) {
            derivedStateOf { if (dragging) dragTop else room * (source.span()?.progress ?: 0f) }
        }
        if (shown <= 0f && !dragging) return@BoxWithConstraints

        val text = if (dragging && label != null) source.topKeys().firstNotNullOfOrNull(label) else null
        LaunchedEffect(text) {
            if (text != null) haptics.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
        }
        if (text != null) {
            Bubble(
                text,
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = EDGE_GAP + HANDLE_WIDTH_DRAGGED + BUBBLE_GAP)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        layout(placeable.width, placeable.height) {
                            val y = handleTop + handleHeight / 2 - placeable.height / 2
                            placeable.place(0, y.roundToInt().coerceIn(-placeable.height / 2, constraints.maxHeight))
                        }
                    },
            )
        }
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset { IntOffset(0, handleTop.roundToInt()) }
                .size(TOUCH_WIDTH, HANDLE_LENGTH)
                .alpha(if (dragging) 1f else shown)
                .pointerInput(source, room) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            dragTop = room * (source.span()?.progress ?: 0f)
                            dragFraction = if (room > 0f) dragTop / room else 0f
                            dragging = true
                            haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                        },
                        onDragEnd = { dragging = false },
                        onDragCancel = { dragging = false },
                    ) { change, amount ->
                        change.consume()
                        dragTop = (dragTop + amount).coerceIn(0f, room)
                        dragFraction = if (room > 0f) dragTop / room else 0f
                    }
                },
            contentAlignment = Alignment.CenterEnd,
        ) {
            Box(
                Modifier
                    .padding(end = EDGE_GAP)
                    .size(if (dragging) HANDLE_WIDTH_DRAGGED else HANDLE_WIDTH, HANDLE_LENGTH)
                    .background(ScrollbarDefaults.handleColor(), CircleShape),
            )
        }
    }
}

/** The bubble beside the dragged handle: round for a letter, wider for a date or a size; its corner points to the handle. */
@Composable
private fun Bubble(text: String, modifier: Modifier) {
    val colors = MaterialTheme.colorScheme
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val corner = BUBBLE_HEIGHT / 2
    val shape = if (isRtl) {
        RoundedCornerShape(topStart = corner, topEnd = corner, bottomEnd = corner, bottomStart = BUBBLE_TIP)
    } else {
        RoundedCornerShape(topStart = corner, topEnd = corner, bottomEnd = BUBBLE_TIP, bottomStart = corner)
    }
    Box(
        modifier
            .heightIn(min = BUBBLE_HEIGHT)
            .widthIn(min = BUBBLE_HEIGHT)
            .background(colors.secondaryContainer, shape)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = if (text.length <= 2) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
            color = colors.onSecondaryContainer,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

/** The colors of the bar and the handle. */
object ScrollbarDefaults {
    @Composable
    fun barColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = BAR_ALPHA)

    @Composable
    fun handleColor(): Color = MaterialTheme.colorScheme.primary
}

/** 1 while [isActive], then after [hideDelay] fading out to 0. [isActive] may change, e.g. with a new list state. */
@Composable
private fun rememberShown(isActive: () -> Boolean, hideDelay: Long): State<Float> {
    val alpha = remember { Animatable(0f) }
    val currentIsActive by rememberUpdatedState(isActive)
    val active by remember { derivedStateOf { currentIsActive() } }
    LaunchedEffect(active) {
        if (active) {
            alpha.animateTo(1f, tween(FADE_IN_MS))
        } else {
            delay(hideDelay)
            alpha.animateTo(0f, tween(FADE_OUT_MS))
        }
    }
    return alpha.asState()
}

/** How far a list has scrolled ([progress], 0 at the top, 1 at the end) and which part of it is in view ([visible]). */
private class ScrollbarSpan(val progress: Float, val visible: Float)

/** What a bar needs to know about a scrolling list, and how to move it. */
private interface ScrollbarSource {
    val isScrolling: Boolean

    /** Where the list stands; null while everything fits. Reads the list's state, so callers update with it. */
    fun span(): ScrollbarSpan?

    /** The keys of the items in view, the topmost first; those mostly under the list's top padding are left out. */
    fun topKeys(): List<Any>

    /** Scrolls to [fraction] of the whole list. */
    suspend fun scrollTo(fraction: Float)
}

/**
 * Estimated from the items in view: their average size stands for the rest. [reversed] for a list with reverseLayout:
 * its first item is at the bottom, so progress and jumps are mirrored. Offsets of the items count from the start of
 * the list either way, at the bottom when reversed; the top edge on screen is then the end of the viewport.
 */
private class LazyListSource(private val state: LazyListState, private val reversed: Boolean = false) : ScrollbarSource {
    override val isScrolling get() = state.isScrollInProgress

    override fun span(): ScrollbarSpan? {
        val info = state.layoutInfo
        val items = info.visibleItemsInfo
        if (items.isEmpty() || !(state.canScrollForward || state.canScrollBackward)) return null
        val extent = items.sumOf { it.size }.toFloat() / items.size + info.mainAxisItemSpacing
        val viewport = info.viewportSize.height.toFloat()
        val total = info.beforeContentPadding + info.afterContentPadding + extent * info.totalItemsCount
        val scrolled = extent * state.firstVisibleItemIndex + state.firstVisibleItemScrollOffset
        val progress = when {
            !state.canScrollBackward -> 0f
            !state.canScrollForward -> 1f
            else -> (scrolled / (total - viewport).coerceAtLeast(1f)).coerceIn(0f, 1f)
        }
        return ScrollbarSpan(if (reversed) 1f - progress else progress, (viewport / total).coerceIn(0f, 1f))
    }

    // items mostly hidden under a header the list scrolls below don't count
    override fun topKeys(): List<Any> {
        val info = state.layoutInfo
        if (!reversed) return info.visibleItemsInfo.filter { it.offset + it.size / 2 >= 0 }.map { it.key }
        val top = info.viewportEndOffset - info.afterContentPadding
        return info.visibleItemsInfo.filter { it.offset + it.size / 2 <= top }.asReversed().map { it.key }
    }

    override suspend fun scrollTo(fraction: Float) {
        val count = state.layoutInfo.totalItemsCount
        if (count > 0) state.scrollToItem(((if (reversed) 1f - fraction else fraction) * (count - 1)).roundToInt())
    }
}

/** Estimated by rows: the rows in view stand for the rest, with as many columns as the widest row in view. */
private class LazyGridSource(private val state: LazyGridState) : ScrollbarSource {
    override val isScrolling get() = state.isScrollInProgress

    override fun span(): ScrollbarSpan? {
        val info = state.layoutInfo
        val items = info.visibleItemsInfo
        if (items.isEmpty() || !(state.canScrollForward || state.canScrollBackward)) return null
        val rows = items.groupBy { it.row }
        val columns = items.maxOf { it.column } + 1
        val extent = rows.values.sumOf { row -> row.maxOf { it.size.height } }.toFloat() / rows.size + info.mainAxisItemSpacing
        val last = items.last()
        val totalRows = last.row + 1 + (info.totalItemsCount - 1 - last.index + columns - 1) / columns
        val viewport = info.viewportSize.height.toFloat()
        val total = info.beforeContentPadding + info.afterContentPadding + extent * totalRows
        val scrolled = extent * items.first().row + state.firstVisibleItemScrollOffset
        val progress = when {
            !state.canScrollBackward -> 0f
            !state.canScrollForward -> 1f
            else -> (scrolled / (total - viewport).coerceAtLeast(1f)).coerceIn(0f, 1f)
        }
        return ScrollbarSpan(progress, (viewport / total).coerceIn(0f, 1f))
    }

    override fun topKeys() = state.layoutInfo.visibleItemsInfo.filter { it.offset.y + it.size.height / 2 >= 0 }.map { it.key }

    override suspend fun scrollTo(fraction: Float) {
        val count = state.layoutInfo.totalItemsCount
        if (count > 0) state.scrollToItem((fraction * (count - 1)).roundToInt())
    }
}

private class ScrollStateSource(private val state: ScrollState) : ScrollbarSource {
    override val isScrolling get() = state.isScrollInProgress

    override fun span(): ScrollbarSpan? {
        val max = state.maxValue
        val viewport = state.viewportSize
        if (max <= 0 || max == Int.MAX_VALUE || viewport <= 0) return null
        return ScrollbarSpan(state.value.toFloat() / max, viewport.toFloat() / (viewport + max))
    }

    override fun topKeys() = emptyList<Any>()

    override suspend fun scrollTo(fraction: Float) {
        state.scrollTo((fraction * state.maxValue).roundToInt())
    }
}

private const val BAR_ALPHA = 0.55f
private const val BAR_HIDE_DELAY_MS = 1000L
private const val HANDLE_HIDE_DELAY_MS = 1500L
private const val FADE_IN_MS = 150
private const val FADE_OUT_MS = 300
private const val FAST_MIN_SCREENS = 2f
private val BAR_WIDTH = 4.dp
private val MIN_BAR_LENGTH = 32.dp
private val TRACK_INSET = 4.dp
private val EDGE_GAP = 2.dp
private val HANDLE_WIDTH = 8.dp
private val HANDLE_WIDTH_DRAGGED = 10.dp
private val HANDLE_LENGTH = 52.dp
private val TOUCH_WIDTH = 48.dp
private val BUBBLE_HEIGHT = 64.dp
private val BUBBLE_TIP = 4.dp
private val BUBBLE_GAP = 12.dp

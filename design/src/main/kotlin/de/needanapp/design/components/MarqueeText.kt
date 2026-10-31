package de.needanapp.design.components

import androidx.compose.foundation.MarqueeSpacing
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.needanapp.design.theme.LocalDesignEffects

/**
 * One line of text that is never cut off: when it doesn't fit, it scrolls through, pauses at the start and repeats,
 * e.g. a long file name. Without motion (system animations off or effects reduced) it wraps onto further lines instead,
 * or, with [wrapWithoutMotion] false (e.g. in a bar of fixed height), it can be scrolled by hand. While it fits it
 * stands as [textAlign] says.
 */
@Composable
fun MarqueeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    wrapWithoutMotion: Boolean = true,
) {
    val moves = LocalDesignEffects.current.richMotion
    if (!moves && wrapWithoutMotion) {
        Text(text, modifier, color = color, style = style, textAlign = textAlign)
        return
    }
    val alignment = when (textAlign) {
        TextAlign.Center -> Alignment.Center
        TextAlign.End, TextAlign.Right -> Alignment.CenterEnd
        else -> Alignment.CenterStart
    }
    val scrolling = if (moves) {
        Modifier.basicMarquee(
            iterations = Int.MAX_VALUE,
            repeatDelayMillis = MARQUEE_PAUSE_MILLIS,
            initialDelayMillis = MARQUEE_PAUSE_MILLIS,
            spacing = MarqueeSpacing.fractionOfContainer(MARQUEE_GAP),
        )
    } else {
        Modifier.horizontalScroll(rememberScrollState())
    }
    var boxWidth by remember { mutableIntStateOf(0) }
    var textWidth by remember { mutableIntStateOf(0) }
    val overflows = textWidth > boxWidth && boxWidth > 0
    Box(
        modifier
            .onSizeChanged { boxWidth = it.width }
            // fades out at the end while the text doesn't fit, so it doesn't look cut off
            .then(if (overflows) Modifier.fadingEnd() else Modifier),
        contentAlignment = alignment,
    ) {
        Text(
            text,
            scrolling,
            color = color,
            style = style,
            maxLines = 1,
            softWrap = false,
            onTextLayout = { textWidth = it.size.width },
        )
    }
}

private fun Modifier.fadingEnd() = graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
    .drawWithContent {
        drawContent()
        val fade = MARQUEE_FADE.toPx().coerceAtMost(size.width / 2)
        val start = if (layoutDirection == LayoutDirection.Ltr) size.width - fade else fade
        val end = if (layoutDirection == LayoutDirection.Ltr) size.width else 0f
        drawRect(Brush.horizontalGradient(listOf(Color.Black, Color.Transparent), startX = start, endX = end), blendMode = BlendMode.DstIn)
    }

private const val MARQUEE_PAUSE_MILLIS = 3000
private const val MARQUEE_GAP = 1f / 4f
private val MARQUEE_FADE = 24.dp

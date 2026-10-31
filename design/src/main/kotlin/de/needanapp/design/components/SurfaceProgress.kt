package de.needanapp.design.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import de.needanapp.design.theme.LocalDesignEffects
import de.needanapp.design.theme.contrastRatio
import kotlin.math.abs

/**
 * How far the work a [FloatingSurface] shows has come, e.g. a copy: the whole surface is its progress bar and fills
 * from its start in the accent color; where the fill reaches, what lies on the surface takes the colors on the accent.
 */
@Immutable
sealed interface SurfaceProgress {
    /** [value] of the work is done, from 0 to 1. */
    @Immutable
    data class Fraction(val value: Float) : SurfaceProgress

    /** Work of unknown length: a band runs across the surface (without motion it is tinted throughout). */
    data object Unknown : SurfaceProgress
}

/** Where the fill of a surface lies while drawing: from [start] over [width], both shares of the surface's width. */
internal class ProgressBand(val start: () -> Float, val width: () -> Float) {
    /** The band in a surface of [size]; beyond its edges the surface's clip cuts it. */
    fun rect(size: Size, fromRight: Boolean): Rect {
        val bandWidth = size.width * width()
        val left = if (fromRight) size.width * (1f - start()) - bandWidth else size.width * start()
        return Rect(Offset(left, 0f), Size(bandWidth, size.height))
    }
}

/** The band [progress] fills, moving with it; null when it has none and tints the surface throughout instead. */
@Composable
internal fun rememberProgressBand(progress: SurfaceProgress): ProgressBand? {
    val richMotion = LocalDesignEffects.current.richMotion
    return when (progress) {
        is SurfaceProgress.Fraction -> {
            val shown = animateFloatAsState(
                progress.value.coerceIn(0f, 1f),
                if (richMotion) tween(FILL_MS) else snap(),
                label = "progress",
            )
            // read while drawing, so the fill moves without composing anew
            remember(shown) { ProgressBand({ 0f }, { shown.value }) }
        }

        SurfaceProgress.Unknown -> if (richMotion) {
            val start = rememberInfiniteTransition(label = "progress").animateFloat(
                -BAND_WIDTH,
                1f,
                infiniteRepeatable(tween(BAND_MS, easing = LinearEasing)),
                label = "band",
            )
            remember(start) { ProgressBand({ start.value }, { BAND_WIDTH }) }
        } else {
            null
        }
    }
}

/**
 * The colors of a surface in [pill] with [text] on it once its progress fills it in [fill] with [onFill] on that. On
 * the fill, what lies on the surface is drawn through [filter]: [pill] turns into [fill], [text] into [onFill] and what
 * lies between, e.g. fainter text or a button, in between by its brightness, so a button stays a button.
 */
@Immutable
internal class ProgressColors(pill: Color, text: Color, textVariant: Color, val fill: Color, onFill: Color) {
    val filter = recolorFilter(pill, text, fill, onFill)

    /** Without a band to fill: as much [fill] over [pill] as keeps [text] and the fainter [textVariant] readable. */
    val tint = tintColor(fill, pill, text, textVariant)
}

/** Fills [band] in [colors] and draws what lies on it there in their colors; from the right in right-to-left layouts. */
internal fun Modifier.progressFill(band: ProgressBand, colors: ProgressColors, fromRight: Boolean) = drawWithCache {
    val recolor = Paint().apply { colorFilter = colors.filter }
    onDrawWithContent {
        val rect = band.rect(size, fromRight)
        if (rect.width <= 0f) {
            drawContent()
        } else {
            drawRect(colors.fill, rect.topLeft, rect.size)
            clipRect(rect.left, rect.top, rect.right, rect.bottom, ClipOp.Difference) { this@onDrawWithContent.drawContent() }
            clipRect(rect.left, rect.top, rect.right, rect.bottom) {
                drawIntoCanvas { it.saveLayer(rect, recolor) }
                this@onDrawWithContent.drawContent()
                drawIntoCanvas { it.restore() }
            }
        }
    }
}

/** Maps colors by their brightness from the line [pill]–[text] onto the line [fill]–[onFill], keeping their alpha. */
private fun recolorFilter(pill: Color, text: Color, fill: Color, onFill: Color): ColorFilter {
    val from = brightness(pill)
    val span = (brightness(text) - from).let { if (abs(it) >= MIN_SPAN) it else if (it < 0f) -MIN_SPAN else MIN_SPAN }
    val values = FloatArray(MATRIX_SIZE)
    listOf(fill.red to onFill.red, fill.green to onFill.green, fill.blue to onFill.blue).forEachIndexed { row, (start, end) ->
        val scale = (end - start) / span
        values[row * MATRIX_COLUMNS] = scale * RED_WEIGHT
        values[row * MATRIX_COLUMNS + 1] = scale * GREEN_WEIGHT
        values[row * MATRIX_COLUMNS + 2] = scale * BLUE_WEIGHT
        // the matrix works on channels from 0 to 255
        values[row * MATRIX_COLUMNS + 4] = (start - scale * from) * CHANNEL_MAX
    }
    values[ALPHA_INDEX] = 1f
    return ColorFilter.colorMatrix(ColorMatrix(values))
}

private fun brightness(color: Color) = RED_WEIGHT * color.red + GREEN_WEIGHT * color.green + BLUE_WEIGHT * color.blue

private fun tintColor(accent: Color, pill: Color, text: Color, textVariant: Color): Color {
    var alpha = MAX_TINT_ALPHA
    while (alpha > MIN_TINT_ALPHA) {
        val tint = accent.copy(alpha = alpha).compositeOver(pill)
        if (contrastRatio(text, tint) >= TEXT_CONTRAST && contrastRatio(textVariant.compositeOver(tint), tint) >= VARIANT_CONTRAST) return tint
        alpha -= TINT_ALPHA_STEP
    }
    return accent.copy(alpha = MIN_TINT_ALPHA).compositeOver(pill)
}

private const val RED_WEIGHT = 0.2126f
private const val GREEN_WEIGHT = 0.7152f
private const val BLUE_WEIGHT = 0.0722f
private const val MIN_SPAN = 0.1f
private const val MATRIX_SIZE = 20
private const val MATRIX_COLUMNS = 5
private const val ALPHA_INDEX = 18
private const val CHANNEL_MAX = 255f
private const val MAX_TINT_ALPHA = 0.5f
private const val MIN_TINT_ALPHA = 0.2f
private const val TINT_ALPHA_STEP = 0.05f
private const val TEXT_CONTRAST = 4.5f
private const val VARIANT_CONTRAST = 3f
private const val FILL_MS = 300
private const val BAND_MS = 1400
private const val BAND_WIDTH = 0.35f

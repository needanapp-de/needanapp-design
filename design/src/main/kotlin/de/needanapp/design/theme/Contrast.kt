package de.needanapp.design.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.hct.Hct
import com.materialkolor.utils.ColorUtils
import kotlin.math.abs

/** The contrast ratio of two colors as WCAG counts it, from 1 (the same) to 21 (black on white); 4.5 reads as small text. */
fun contrastRatio(a: Color, b: Color): Float {
    val la = a.luminance()
    val lb = b.luminance()
    return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
}

/** Whether this is a light color: lighter than where black and white on it read equally well, so dark text belongs on it. */
fun Color.readsLight(): Boolean = luminance() > EVEN_CONTRAST_LUMINANCE

/** The least contrast of text, as WCAG asks for normal sizes. */
const val TEXT_CONTRAST = 4.5f

/** The least contrast of icons and other graphics. */
const val ICON_CONTRAST = 3f

/**
 * This color as it is when it reads at [min] on each of [backgrounds], otherwise with its tone moved as little as
 * needed, lighter or darker, keeping hue and chroma as far as the tone allows. When no tone reads on all of them, black
 * or white, whichever reads better on the worst of them. Its alpha stays; [backgrounds] count as opaque.
 */
fun Color.readableOn(backgrounds: List<Color>, min: Float = TEXT_CONTRAST): Color {
    val opaque = copy(alpha = 1f)
    if (backgrounds.all { contrastRatio(opaque, it) >= min }) return this
    val luminances = backgrounds.map { it.luminance() }
    // as light as it must be to stand out lighter than all of them, as dark as it must be to stand out darker
    val lighter = luminances.maxOf { min * (it + 0.05f) - 0.05f }
    val darker = luminances.minOf { (it + 0.05f) / min - 0.05f }
    val hct = Hct.fromInt(opaque.toArgb())
    val tones = buildList {
        if (lighter <= 1f) add(ColorUtils.lstarFromY(lighter * 100.0) to TONE_STEP)
        if (darker >= 0f) add(ColorUtils.lstarFromY(darker * 100.0) to -TONE_STEP)
    }
    val (tone, step) = tones.minByOrNull { (tone, _) -> abs(tone - hct.tone) }
        ?: return listOf(Color.White, Color.Black)
            .maxBy { extreme -> backgrounds.minOf { contrastRatio(extreme, it) } }
            .copy(alpha = alpha)
    // the 8 bits of each channel round a little; a step further where they fall short
    var target = tone + step
    var result = Color(Hct.from(hct.hue, hct.chroma, target.coerceIn(0.0, 100.0)).toInt())
    while (backgrounds.any { contrastRatio(result, it) < min } && target in 0.0..100.0) {
        target += step
        result = Color(Hct.from(hct.hue, hct.chroma, target.coerceIn(0.0, 100.0)).toInt())
    }
    return result.copy(alpha = alpha)
}

/** [readableOn] for one background. */
fun Color.readableOn(background: Color, min: Float = TEXT_CONTRAST): Color = readableOn(listOf(background), min)

/**
 * This color at [alpha] when that reads at [min] on each of [backgrounds], otherwise just as much more opaque as it
 * needs, fully opaque at most. For faint text such as hints, which stays fainter than the color itself.
 */
fun Color.fadedOn(alpha: Float, backgrounds: List<Color>, min: Float = TEXT_CONTRAST): Color {
    fun reads(value: Float) = backgrounds.all { contrastRatio(copy(alpha = value).compositeOver(it), it) >= min }
    if (reads(alpha)) return copy(alpha = alpha)
    if (!reads(1f)) return copy(alpha = 1f)
    var low = alpha
    var high = 1f
    repeat(ALPHA_STEPS) {
        val middle = (low + high) / 2
        if (reads(middle)) high = middle else low = middle
    }
    return copy(alpha = high)
}

/** [fadedOn] for one background. */
fun Color.fadedOn(alpha: Float, background: Color, min: Float = TEXT_CONTRAST): Color = fadedOn(alpha, listOf(background), min)

private const val EVEN_CONTRAST_LUMINANCE = 0.18f
private const val TONE_STEP = 0.5
private const val ALPHA_STEPS = 12

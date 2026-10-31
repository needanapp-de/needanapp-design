package de.needanapp.design.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.needanapp.design.theme.DesignType
import de.needanapp.design.theme.LocalDesignColors
import de.needanapp.design.theme.LocalDesignEffects
import de.needanapp.design.theme.NeedanappMotion
import de.needanapp.design.theme.isNeedanappDesign
import de.needanapp.design.theme.labelCase
import kotlin.math.floor
import kotlin.math.roundToInt

/** The lit color of [StatusTone] on a display and of its LED; null for an LED that is off. */
@Composable
fun StatusTone.displayColor(): Color {
    val design = LocalDesignColors.current
    return when (this) {
        StatusTone.POSITIVE -> design.ledGreen
        StatusTone.ACCENT -> design.displayLit
        StatusTone.NEUTRAL -> design.displayDim
        StatusTone.ERROR -> if (isNeedanappDesign) Color.White else design.onGraphite
    }
}

@Composable
private fun StatusTone.ledColor(): Color? {
    val design = LocalDesignColors.current
    return when (this) {
        StatusTone.POSITIVE -> design.ledGreen
        StatusTone.ACCENT -> design.displayLit
        StatusTone.NEUTRAL -> null
        StatusTone.ERROR -> design.ledRed
    }
}

/** A small capitalized label in the monospace font, e.g. the title of a panel or the name of a value. */
@Composable
fun MonoLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    style: TextStyle = DesignType.label,
) {
    Text(text.labelCase(), style = style, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = modifier)
}

/**
 * The black display of the device look: an LED and [text] in the dot matrix, lit in the color of [tone] (an error lights
 * the text white with a red LED and a red rim), [detail] quietly at the end. Text that doesn't fit gets smaller. With
 * [progress] (0 to 1) a row of square dots below fills up, its last lit dot blinking, e.g. while downloading. When
 * [contentKey] changes, the old line fades out upwards and the new one comes in from below, e.g. when the display
 * cycles through several messages; the glass stays. [compact] makes it lower with smaller type, e.g. at the top of a
 * menu. The LED blinks with [progress], or with [blinking] also without its row of dots.
 */
@Composable
fun DeviceDisplay(
    text: String,
    tone: StatusTone,
    modifier: Modifier = Modifier,
    detail: String? = null,
    progress: Float? = null,
    footer: (@Composable () -> Unit)? = null,
    compact: Boolean = false,
    contentKey: Any? = null,
    blinking: Boolean = progress != null,
) {
    val design = LocalDesignColors.current
    val needanapp = isNeedanappDesign
    val shape = RoundedCornerShape(
        when {
            !needanapp -> DisplayRadius
            compact -> NeedanappCompactDisplayRadius
            else -> NeedanappDisplayRadius
        },
    )
    val lit = tone.displayColor()
    val height = when {
        needanapp && compact -> NeedanappCompactDisplayHeight
        needanapp -> NeedanappDisplayHeight
        compact -> CompactDisplayHeight
        else -> DisplayHeight
    }
    val textStyle = when {
        needanapp && compact -> DesignType.display.copy(fontSize = 16.sp, lineHeight = 20.sp)
        compact -> DesignType.display.copy(fontSize = 18.sp, lineHeight = 22.sp)
        else -> DesignType.display
    }
    val rim = if (tone == StatusTone.ERROR) Modifier.border(1.dp, design.ledRed, shape) else Modifier
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(design.display)
            .then(rim)
            .drawWithContent {
                drawContent()
                // the glass sits a little deeper than the panel around it
                drawRect(
                    Brush.verticalGradient(0f to Color.Black.copy(alpha = 0.45f), 1f to Color.Transparent, endY = 6.dp.toPx()),
                    size = Size(size.width, 6.dp.toPx()),
                )
            }
            .heightIn(min = height)
            .padding(horizontal = if (compact) (if (needanapp) 12.dp else 14.dp) else 16.dp, vertical = if (progress != null || footer != null) 14.dp else 0.dp)
            .then(if (progress != null) Modifier.semantics(mergeDescendants = true) { progressBarRangeInfo = ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f) } else Modifier),
        verticalArrangement = Arrangement.Center,
    ) {
        val richMotion = LocalDesignEffects.current.richMotion
        AnimatedContent(
            targetState = DisplayLine(text, tone, detail, contentKey),
            contentKey = { it.key },
            transitionSpec = { if (richMotion) displaySwitch() else EnterTransition.None togetherWith ExitTransition.None },
            modifier = Modifier.fillMaxWidth(),
            label = "display",
        ) { line ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().heightIn(min = if (progress != null || footer != null) 0.dp else height),
            ) {
                Led(line.tone.ledColor(), diameter = if (needanapp && compact) 7.dp else 9.dp, blinking = blinking)
                Spacer(Modifier.width(if (needanapp && compact) 9.dp else 12.dp))
                FitText(line.text.uppercase(), textStyle, line.tone.displayColor(), Modifier.weight(1f))
                if (line.detail != null) {
                    Spacer(Modifier.width(10.dp))
                    // a long detail (e.g. an app name) is cut short rather than squeezing the text
                    Text(
                        line.detail.uppercase(),
                        style = DesignType.value.copy(fontSize = if (needanapp) (if (compact) 10.5.sp else 11.sp) else 12.sp),
                        color = design.displayDim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 150.dp),
                    )
                }
            }
        }
        if (progress != null) {
            Spacer(Modifier.height(12.dp))
            DotProgress(progress, lit)
        }
        if (footer != null) {
            Spacer(Modifier.height(10.dp))
            footer()
        }
    }
}

/** What one line of a [DeviceDisplay] shows; a new [key] switches to it with [displaySwitch]. */
@Immutable
private data class DisplayLine(val text: String, val tone: StatusTone, val detail: String?, val key: Any?)

/** The old line fades out upwards, the new one comes in from below, like a ticker. */
private fun displaySwitch(): ContentTransform {
    val out = tween<Float>(DISPLAY_SWITCH_MILLIS / 2, easing = NeedanappMotion.EmphasizedAccelerate)
    val inn = tween<Float>(DISPLAY_SWITCH_MILLIS, delayMillis = DISPLAY_SWITCH_MILLIS / 3, easing = NeedanappMotion.EmphasizedDecelerate)
    return (fadeIn(inn) + slideInVertically(tween(DISPLAY_SWITCH_MILLIS, easing = NeedanappMotion.EmphasizedDecelerate)) { it / 3 }) togetherWith
        (fadeOut(out) + slideOutVertically(tween(DISPLAY_SWITCH_MILLIS / 2, easing = NeedanappMotion.EmphasizedAccelerate)) { -it / 3 })
}

/** A round LED of [color] with a soft glow; null draws an LED that is off. [blinking] makes it flash, e.g. while busy. */
@Composable
fun Led(color: Color?, modifier: Modifier = Modifier, diameter: Dp = 9.dp, blinking: Boolean = false) {
    val design = LocalDesignColors.current
    val alpha = if (blinking && color != null && LocalDesignEffects.current.richMotion) {
        val transition = rememberInfiniteTransition(label = "led")
        val value by transition.animateFloat(1f, 0.2f, infiniteRepeatable(tween(BLINK_MILLIS, easing = LinearEasing), RepeatMode.Reverse), label = "ledAlpha")
        value
    } else {
        1f
    }
    Canvas(modifier.size(diameter * 2).alpha(alpha)) {
        val radius = diameter.toPx() / 2
        if (color == null) {
            drawCircle(design.displayOff, radius)
        } else {
            drawCircle(Brush.radialGradient(listOf(color.copy(alpha = 0.55f), Color.Transparent), radius = radius * 2.2f), radius * 2.2f)
            drawCircle(color, radius)
        }
    }
}

/**
 * A row of square dots as progress bar on a display: as many dots as fit, [progress] (0 to 1) of them lit in [color],
 * the last lit one blinking while there is something left.
 */
@Composable
fun DotProgress(progress: Float, color: Color, modifier: Modifier = Modifier) {
    val design = LocalDesignColors.current
    val fraction by animateFloatAsState(progress.coerceIn(0f, 1f), label = "dots")
    val blink = if (LocalDesignEffects.current.richMotion && fraction < 1f) {
        val transition = rememberInfiniteTransition(label = "dot")
        val value by transition.animateFloat(1f, 0.15f, infiniteRepeatable(tween(BLINK_MILLIS, easing = LinearEasing), RepeatMode.Reverse), label = "dotAlpha")
        value
    } else {
        1f
    }
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Canvas(modifier.fillMaxWidth().height(DotSize)) {
        val dot = DotSize.toPx()
        val gap = DotGap.toPx()
        val count = floor((size.width + gap) / (dot + gap)).toInt().coerceAtLeast(1)
        val litCount = (fraction * count).roundToInt()
        val corner = CornerRadius(1.5.dp.toPx())
        for (i in 0 until count) {
            val x = if (rtl) size.width - dot - i * (dot + gap) else i * (dot + gap)
            val isLit = i < litCount
            val alpha = if (isLit && i == litCount - 1 && fraction < 1f) blink else 1f
            drawRoundRect(if (isLit) color.copy(alpha = color.alpha * alpha) else design.displayOff, Offset(x, 0f), Size(dot, dot), corner)
        }
    }
}

/**
 * One line of [text] in [style] that shrinks in steps until it fits, at least to [MIN_FIT_SIZE] sp, then ends in "…".
 * Uses the auto size of BasicText rather than BoxWithConstraints, so it also works inside layouts that ask for
 * intrinsic sizes, e.g. a menu as wide as its widest entry.
 */
@Composable
private fun FitText(text: String, style: TextStyle, color: Color, modifier: Modifier) {
    BasicText(
        text,
        modifier,
        style = style.copy(color = color, lineHeight = 1.18.em),
        overflow = TextOverflow.Ellipsis,
        softWrap = false,
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(minFontSize = MIN_FIT_SIZE.sp, maxFontSize = style.fontSize, stepSize = 1.sp),
    )
}

/** The look of a [DeviceKey]: the signal color for the main action, graphite, aluminium or red for destructive ones. */
enum class KeyStyle { SIGNAL, GRAPHITE, ALU, DANGER }

/**
 * A key of the device look: rounded, with a slightly darker lower edge, capitalized lettering in the monospace font and
 * an optional [icon] in front. Disabled it turns grey. Width is up to the caller.
 */
@Composable
fun DeviceKey(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: KeyStyle = KeyStyle.SIGNAL,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = KeyHeight,
) {
    val (container, content) = keyColors(style, enabled)
    val design = LocalDesignColors.current
    val needanapp = isNeedanappDesign
    val shape = RoundedCornerShape(if (needanapp) NeedanappKeyRadius else KeyRadius)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(if (needanapp && height == KeyHeight) NeedanappKeyHeight else height)
            .clip(shape)
            .background(container)
            .then(if (style == KeyStyle.ALU && enabled && !needanapp) Modifier.border(1.dp, design.aluKeyBorder, shape) else Modifier)
            .keyEdge(style, enabled)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
            }
            Text(
                text = text.labelCase(),
                style = DesignType.key,
                color = content,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * A round key with an icon, e.g. Back in a header: aluminium by default, [diameter] wide inside a touch area of at
 * least 48 dp.
 */
@Composable
fun RoundKey(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: KeyStyle = KeyStyle.ALU,
    enabled: Boolean = true,
    diameter: Dp = 40.dp,
) {
    val (fill, tint) = keyColors(style, enabled)
    val design = LocalDesignColors.current
    val needanapp = isNeedanappDesign
    // the needanapp look has plain icon buttons where the device look has aluminium keys
    val plain = needanapp && style == KeyStyle.ALU
    val container = if (plain) Color.Transparent else fill
    val content = if (plain) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else DISABLED_ICON_ALPHA) else tint
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                interactionSource = null,
                indication = ripple(bounded = false, radius = diameter / 2),
                onClick = onClick,
            )
            .semantics { this.contentDescription = contentDescription },
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(diameter)
                .clip(CircleShape)
                .background(container)
                .then(if (style == KeyStyle.ALU && enabled && !needanapp) Modifier.border(1.dp, design.aluKeyBorder, CircleShape) else Modifier)
                .keyEdge(style, enabled),
        ) {
            Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(if (plain) 22.dp else 20.dp))
        }
    }
}

/** The colors of a key of [style]. */
@Composable
fun keyColors(style: KeyStyle, enabled: Boolean = true): Pair<Color, Color> {
    val scheme = MaterialTheme.colorScheme
    val design = LocalDesignColors.current
    if (isNeedanappDesign) {
        if (!enabled) return scheme.onSurface.copy(alpha = 0.1f) to scheme.onSurface.copy(alpha = DISABLED_ICON_ALPHA)
        return when (style) {
            KeyStyle.SIGNAL -> design.signal to design.onSignal
            KeyStyle.GRAPHITE -> scheme.primaryContainer to scheme.onPrimaryContainer
            KeyStyle.ALU -> design.tile to scheme.onSurface
            KeyStyle.DANGER -> scheme.errorContainer to scheme.onErrorContainer
        }
    }
    if (!enabled) return scheme.outlineVariant to scheme.onSurfaceVariant
    return when (style) {
        KeyStyle.SIGNAL -> design.signal to design.onSignal
        KeyStyle.GRAPHITE -> (if (design.isDark) design.graphiteKey else scheme.inverseSurface) to design.onGraphite
        KeyStyle.ALU -> design.aluKey to scheme.onSurface
        KeyStyle.DANGER -> design.ledRed to Color.White
    }
}

/** The slightly darker lower edge of a key, as if it stood out of the panel a little. */
@Composable
fun Modifier.keyEdge(style: KeyStyle = KeyStyle.SIGNAL, enabled: Boolean = true): Modifier {
    // the needanapp look is flat
    if (isNeedanappDesign) return this
    val design = LocalDesignColors.current
    val edge = when {
        !enabled -> Color.Black.copy(alpha = 0.06f)
        style == KeyStyle.ALU -> design.aluKeyEdge
        style == KeyStyle.GRAPHITE -> Color.Black
        else -> Color.Black.copy(alpha = 0.18f)
    }
    return drawBehind {
        val h = 3.dp.toPx()
        drawRect(edge, Offset(0f, size.height - h), Size(size.width, h))
    }
}

private val DisplayRadius = 14.dp
private val DisplayHeight = 60.dp
private val CompactDisplayHeight = 46.dp
private val DotSize = 7.dp
private val DotGap = 3.dp
private val KeyHeight = 56.dp
private val KeyRadius = 18.dp
private const val MIN_FIT_SIZE = 14f
private const val DISABLED_ICON_ALPHA = 0.38f

// the needanapp look: rounder displays and keys a little lower, a soft shadow under cards
private val NeedanappDisplayRadius = 10.dp
private val NeedanappCompactDisplayRadius = 8.dp
private val NeedanappDisplayHeight = 56.dp
private val NeedanappCompactDisplayHeight = 38.dp
private val NeedanappKeyHeight = 52.dp
private val NeedanappKeyRadius = 12.dp
private const val BLINK_MILLIS = 450
private const val DISPLAY_SWITCH_MILLIS = 420

/** The switch in the signal color, see [signalSwitchColors]; the same, kept for the apps that use it. */
@Composable
fun deviceSwitchColors(): SwitchColors = signalSwitchColors()

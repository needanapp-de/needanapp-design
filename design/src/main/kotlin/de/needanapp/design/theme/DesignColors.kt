package de.needanapp.design.theme

import androidx.compose.foundation.background
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Colors of the needanapp look that Material's [ColorScheme] doesn't name: the background with its glow of the accent
 * at the top, cards with their fine border, bars at the screen's edge and the green of a good state, and the displays,
 * LEDs and keys of the Myzel style, which the needanapp look draws as dark displays and fields of a card.
 * [NeedanappTheme] derives them from its color scheme and provides them as [LocalDesignColors].
 */
@Immutable
data class DesignColors(
    val isDark: Boolean,
    /** The screen background from top to bottom, at the stops [BACKGROUND_STOPS]; see [designBackground]. */
    val background: List<Color>,
    /** Fill of cards, a little deeper than the container they are made of. */
    val card: Color,
    val cardBorder: Color,
    /** Faint secondary text, e.g. a date next to a card's title. */
    val hint: Color,
    /** A good state, e.g. "up to date": background and the text and icon on it. */
    val positive: Color,
    val onPositive: Color,
    /** Bars at the edge of the screen, e.g. [de.needanapp.design.components.ActionBar]. */
    val bar: Color,
    val barBorder: Color,
    /** The floating pill and its buttons, with its icons and text, the inactive ones in [onPillVariant]. */
    val pill: Color = bar,
    val onPill: Color = Color.Black,
    val onPillVariant: Color = onPill,
    /** The least opacity of the pill over its blur; a pill much darker or lighter than the screen stays legible. */
    val pillMinOpacity: Float = 0f,
    /** The check mark of a selected entry: its circle and the tick on it; see [de.needanapp.design.components.SelectionMark]. */
    val mark: Color = Color.Black,
    val onMark: Color = Color.White,
    /**
     * The accent as it is, for what it fills or lights: keys, LEDs, the pill's buttons, counters and progress, with
     * [onSignal] on it. Text and icons in the accent on the page take the scheme's primary instead, a tone of the
     * accent that reads there, so a bright signal color stays bright where it is a surface.
     */
    val signal: Color = mark,
    val onSignal: Color = onMark,
    /** Secondary text on a selected entry (secondaryContainer), e.g. its size and date: its text color, a little faded. */
    val onSelectionVariant: Color = Color.Black.copy(alpha = SELECTION_VARIANT_ALPHA),
    /** The width of the fine border around cards. */
    val cardBorderWidth: Dp = 1.dp,
    /** Lines between the rows of a panel. */
    val divider: Color = cardBorder,
    /** The dark glass of a display, e.g. [de.needanapp.design.components.DeviceDisplay]; its lit text is [displayLit]. */
    val display: Color = Color(0xFF1B2130),
    /** Dots of a display that are off, e.g. the rest of a progress row. */
    val displayOff: Color = display.mixedWith(Color.White, DISPLAY_OFF_LIFT),
    /** Quiet text on a display, e.g. "FROM 2.4.1". */
    val displayDim: Color = Color(0xFF9AA3B2),
    /** The lettering and LED of the signal color on a display. */
    val displayLit: Color = Color.Unspecified,
    /** The green LED and lettering of a good state on a display. */
    val ledGreen: Color = LedGreen,
    /** The red LED and border of a problem on a display. */
    val ledRed: Color = LedRed,
    /** A dark key, e.g. inside a bar, and the text on it: graphite in the Myzel style. */
    val graphiteKey: Color = bar,
    val onGraphite: Color = onPill,
    val onGraphiteMuted: Color = onPillVariant,
    /** A key on the page, e.g. "back" or "cancel": fill, border and its lower edge; aluminium in the Myzel style. */
    val aluKey: Color = card,
    val aluKeyBorder: Color = cardBorder,
    val aluKeyEdge: Color = Color.Transparent,
    /** Fields inside a card, e.g. a fact or an action tile. */
    val tile: Color = aluKey,
    /** A button pressed in while its menu is open. */
    val pressed: Color = graphiteKey,
) {
    /**
     * [background] as a vertical gradient over [height] in pixels, by default over the area it is drawn in. A bar at
     * the top of the screen passes the screen's height, so it continues the background seamlessly.
     */
    fun backgroundBrush(height: Float = Float.POSITIVE_INFINITY): Brush =
        Brush.verticalGradient(*BACKGROUND_STOPS.zip(background).toTypedArray(), startY = 0f, endY = height)

    companion object {
        /** Where the colors of [background] sit: the glow fades out by a little more than a third of the height. */
        val BACKGROUND_STOPS = listOf(0f, 0.38f, 0.72f, 1f)
    }
}

/**
 * The [DesignColors] of the needanapp look for [scheme]. [accentGlow] scales the glow of the primary color at the top
 * of the background (0 = none, 1 = full); with [pureBlack] the background stays black. With [fixedWorld] the scheme carries a color world
 * ([withWorld]): cards and bars are its card and raised colors as they are. A [world] given along brings its pill and
 * card border, its [accent] (when it is in use) the check mark. [seed] lights the displays; [signal] is the accent as it
 * is, before [buildColorScheme] made primary read as text, with [onSignal] on it.
 */
fun designColors(
    scheme: ColorScheme,
    isDark: Boolean,
    pureBlack: Boolean = false,
    accentGlow: Float = 1f,
    fixedWorld: Boolean = false,
    world: SurfaceColors? = null,
    accent: WorldAccent? = null,
    seed: Color = scheme.primary,
    signal: Color = accent?.color ?: scheme.primary,
    onSignal: Color = accent?.onColor ?: scheme.onPrimary,
): DesignColors {
    val hasWorld = fixedWorld || world != null
    val surface = scheme.surface
    val glow = if (pureBlack) 0f else accentGlow.coerceIn(0f, 1f)
    // what text lies on reads under it, however deep the look draws it (see readableOn)
    fun Color.underText() = readableOn(scheme.inks())
    val card = when {
        // a color world sets its cards itself (ColorScheme.surfaceContainer), only the brightness settings move them
        hasWorld -> scheme.surfaceContainer
        isDark -> scheme.surfaceContainerHighest.mixedWith(Color.Black, DARK_CARD_DEPTH)
        else -> scheme.surfaceContainer.mixedWith(Color.Black, LIGHT_CARD_DEPTH)
    }.underText()
    val background = glowingBackground(scheme, signal, isDark, glow).map { it.underText() }
    val display = if (isDark) surface.mixedWith(Color.Black, DARK_DISPLAY_DEPTH) else scheme.onSurface
    val tile = (if (isDark) scheme.surfaceContainerHigh else surface.mixedWith(scheme.surfaceContainer, LIGHT_TILE_LIFT)).underText()
    val positive = if (isDark) card.mixedWith(PositiveGreen, DARK_POSITIVE_STRENGTH) else PositiveLight
    val pill = world?.pill ?: scheme.surfaceContainerHighest
    val pillMinOpacity = if (world?.pill != null) WORLD_PILL_MIN_OPACITY else 0f
    // the pill over the page, as see-through as the settings let it be, and opaque
    val pillOpacity = maxOf(1f - NeedanappDefaults.MAX_PILL_TRANSPARENCY, pillMinOpacity)
    val pillGrounds = (background + card).map { pill.copy(alpha = pillOpacity).compositeOver(it) } + pill
    val onPill = (world?.onPill ?: scheme.onSurface).readableOn(pillGrounds)
    val mark = accent?.mark ?: signal
    val displayDim = if (isDark) scheme.onSurfaceVariant.mixedWith(display, DARK_DISPLAY_DIM) else scheme.onSurfaceVariant.mixedWith(Color.White, LIGHT_DISPLAY_DIM)
    return DesignColors(
        isDark = isDark,
        background = background,
        card = card,
        cardBorder = scheme.outlineVariant,
        hint = scheme.onSurfaceVariant.fadedOn(HINT_ALPHA, listOf(card, background.last())),
        positive = positive,
        onPositive = (if (isDark) OnPositiveDark else OnPositiveLight).readableOn(positive),
        bar = (if (isDark || hasWorld) scheme.surfaceContainerHigh else scheme.surfaceContainerHighest).underText(),
        barBorder = scheme.outlineVariant.copy(alpha = if (isDark) DARK_BAR_BORDER_ALPHA else LIGHT_BAR_BORDER_ALPHA),
        pill = pill,
        onPill = onPill,
        onPillVariant = if (world?.onPill != null) {
            onPill.fadedOn(PILL_VARIANT_ALPHA, pillGrounds)
        } else {
            scheme.onSurfaceVariant.readableOn(pillGrounds)
        },
        pillMinOpacity = pillMinOpacity,
        mark = mark,
        onMark = (accent?.onMark ?: onSignal).readableOn(mark),
        signal = signal,
        onSignal = onSignal.readableOn(signal),
        onSelectionVariant = scheme.onSecondaryContainer.fadedOn(SELECTION_VARIANT_ALPHA, scheme.secondaryContainer),
        cardBorderWidth = world?.lineWidth ?: 1.dp,
        // displays, keys and tiles: a dark display on every world, the keys are fields of the card
        divider = scheme.outlineVariant,
        display = display,
        displayDim = displayDim.readableOn(display),
        displayLit = litOn(display, seed.mixedWith(Color.White, DISPLAY_LIT_LIFT), accent),
        ledGreen = LedGreen.readableOn(display),
        ledRed = LedRed.readableOn(display, min = ICON_CONTRAST),
        graphiteKey = tile,
        onGraphite = scheme.onSurface,
        onGraphiteMuted = scheme.onSurfaceVariant,
        aluKey = tile,
        aluKeyBorder = scheme.outlineVariant,
        tile = tile,
        pressed = (if (isDark) scheme.surfaceContainerLow else scheme.surfaceContainerHighest).underText(),
    )
}

/** What the building blocks write on the surfaces of [this]: text, secondary text, errors and the accent's two colors. */
internal fun ColorScheme.inks(): List<Color> = listOf(onSurface, onSurfaceVariant, error, primary, secondary)

/**
 * The lit color of a display on [display]: [lit] when it reads there, else the signal color of the world's [accent]
 * when that does (e.g. the yellow of the high-visibility world, whose accent itself is graphite), else [lit] as much
 * lighter or darker as it needs.
 */
private fun litOn(display: Color, lit: Color, accent: WorldAccent?): Color =
    listOfNotNull(lit, accent?.container, accent?.mark, accent?.onColor).firstOrNull { contrastRatio(it, display) >= TEXT_CONTRAST }
        ?: lit.readableOn(display)

/** The background of [scheme] from top to bottom with the glow of [signal] at the top, [glow] 0 = none. */
private fun glowingBackground(scheme: ColorScheme, signal: Color, isDark: Boolean, glow: Float): List<Color> {
    val surface = scheme.surface
    return listOf(
        surface.mixedWith(signal, glow * if (isDark) DARK_GLOW_TOP else LIGHT_GLOW_TOP),
        surface.mixedWith(signal, glow * if (isDark) DARK_GLOW_MIDDLE else LIGHT_GLOW_MIDDLE),
        surface,
        surface,
    )
}

/**
 * The fixed colors of the Myzel style: aluminium (light) and graphite (dark) as in [deviceColorScheme], and the dark
 * parts that stay the same in every color world: bars, menus, displays and graphite keys.
 */
internal object Device {
    val Alu = Color(0xFFE4E4E0)
    val AluPanel = Color(0xFFF3F3F0)
    val AluBorder = Color(0xFFC9C9C3)
    val AluDivider = Color(0xFFD6D6D0)
    val AluKeyBorder = Color(0xFFC3C3BC)
    val AluKeyEdge = Color(0xFFCFCFC9)
    val Ink = Color(0xFF151515)
    val Label = Color(0xFF55554F)
    val Muted = Color(0xFF9A9A93)
    val Graphite = Color(0xFF151515)
    val GraphiteKey = Color(0xFF2A2A2A)
    val Display = Color(0xFF141414)
    val DisplayOff = Color(0xFF3A3A36)
    val Green = Color(0xFF2F9E5B)
    val Positive = Color(0xFFCDEBD8)
    val OnPositive = Color(0xFF0F3D22)

    val Night = Color(0xFF111111)
    val NightPanel = Color(0xFF1C1C1B)
    val NightBorder = Color(0xFF2E2E2C)
    val NightKey = Color(0xFF262624)
    val NightInk = Color(0xFFEDEDEA)
    val NightLabel = Color(0xFFA3A39C)
    val NightDisplay = Color(0xFF050505)
    val NightDisplayOff = Color(0xFF2A2A28)
    val NightDisplayDim = Color(0xFF7A7A74)
}

/** The text on the signal color in the Myzel style's own worlds: dark, as on a lit key. */
internal fun deviceOnSignal(isDark: Boolean): Color = if (isDark) Device.Night else Device.Ink

/**
 * The Material scheme of aluminium and graphite in the Myzel style with [accent] as the signal color, which it takes as
 * it is; [pureBlack] makes the dark background black.
 */
internal fun deviceColorScheme(accent: Color, isDark: Boolean, pureBlack: Boolean = false): ColorScheme = if (isDark) {
    val night = if (pureBlack) Color.Black else Device.Night
    darkColorScheme(
        primary = accent,
        onPrimary = deviceOnSignal(isDark = true),
        primaryContainer = accent.mixedWith(Device.Night, 0.72f),
        onPrimaryContainer = accent.mixedWith(Color.White, 0.6f),
        inversePrimary = accent,
        secondary = Device.NightLabel,
        onSecondary = Device.Night,
        secondaryContainer = Device.NightInk,
        onSecondaryContainer = Device.Night,
        tertiary = LedGreen,
        onTertiary = Device.Night,
        tertiaryContainer = LedGreen.mixedWith(Device.Night, 0.7f),
        onTertiaryContainer = LedGreen,
        background = night,
        onBackground = Device.NightInk,
        surface = night,
        onSurface = Device.NightInk,
        surfaceVariant = Device.NightKey,
        onSurfaceVariant = Device.NightLabel,
        surfaceTint = night,
        inverseSurface = Device.NightInk,
        inverseOnSurface = Device.Night,
        error = Color(0xFFFF6B5E),
        onError = Device.Night,
        errorContainer = Color(0xFF4A1410),
        onErrorContainer = Color(0xFFFFDAD5),
        outline = Color(0xFF6E6E68),
        outlineVariant = Device.NightBorder,
        scrim = Color.Black,
        surfaceBright = Color(0xFF2C2C2A),
        surfaceDim = night,
        surfaceContainerLowest = if (pureBlack) Color.Black else Color(0xFF0A0A0A),
        surfaceContainerLow = Color(0xFF161615),
        surfaceContainer = Device.NightPanel,
        surfaceContainerHigh = Color(0xFF222221),
        surfaceContainerHighest = Device.NightKey,
    )
} else {
    lightColorScheme(
        primary = accent,
        onPrimary = deviceOnSignal(isDark = false),
        primaryContainer = accent.mixedWith(Device.AluPanel, 0.78f),
        onPrimaryContainer = accent.mixedWith(Device.Ink, 0.7f),
        inversePrimary = accent,
        secondary = Device.Label,
        onSecondary = Device.AluPanel,
        secondaryContainer = Device.Graphite,
        onSecondaryContainer = Device.AluPanel,
        tertiary = Device.Green,
        onTertiary = Device.Ink,
        tertiaryContainer = Device.Positive,
        onTertiaryContainer = Device.OnPositive,
        background = Device.Alu,
        onBackground = Device.Ink,
        surface = Device.Alu,
        onSurface = Device.Ink,
        surfaceVariant = Device.AluDivider,
        onSurfaceVariant = Device.Label,
        surfaceTint = Device.Alu,
        inverseSurface = Device.Graphite,
        inverseOnSurface = Device.AluPanel,
        error = Color(0xFFD13A2E),
        onError = Device.AluPanel,
        errorContainer = Color(0xFFFFDAD5),
        onErrorContainer = Color(0xFF5C0F0A),
        outline = Device.Muted,
        outlineVariant = Device.AluBorder,
        scrim = Color.Black,
        surfaceBright = Color(0xFFFAFAF8),
        surfaceDim = Color(0xFFD6D6D0),
        surfaceContainerLowest = Color(0xFFFAFAF8),
        surfaceContainerLow = Color(0xFFEDEDE9),
        surfaceContainer = Device.AluPanel,
        surfaceContainerHigh = Color(0xFFDCDCD6),
        surfaceContainerHighest = Device.AluDivider,
    )
}

/**
 * The [DesignColors] of the Myzel style for [scheme]: background, panels, lines and the keys on the page follow the
 * scheme and so the color world and the brightness settings, while bars, menus, displays and graphite keys stay dark in
 * every world. [deviceWorld] (aluminium or graphite) keeps the borders and edges of its keys as they are made;
 * [accentGlow] and [pureBlack] as in [designColors].
 */
internal fun myzelDesignColors(
    scheme: ColorScheme,
    isDark: Boolean,
    deviceWorld: Boolean,
    accentGlow: Float = 0f,
    pureBlack: Boolean = false,
    accent: WorldAccent? = null,
    signal: Color = accent?.color ?: scheme.primary,
    onSignal: Color = accent?.onColor ?: scheme.onPrimary,
): DesignColors {
    val card = scheme.surfaceContainer
    val readableOnSignal = onSignal.readableOn(signal)
    val background = glowingBackground(scheme, signal, isDark, if (pureBlack) 0f else accentGlow.coerceIn(0f, 1f))
        .map { it.readableOn(scheme.inks()) }
    val hint = scheme.onSurfaceVariant.fadedOn(MYZEL_HINT_ALPHA, listOf(card, background.last()))
    val onSelectionVariant = scheme.onSecondaryContainer.fadedOn(SELECTION_VARIANT_ALPHA, scheme.secondaryContainer)
    return if (isDark) {
        val positive = LedGreen.mixedWith(card, MYZEL_DARK_POSITIVE_DEPTH)
        DesignColors(
            isDark = true,
            background = background,
            card = card,
            cardBorder = scheme.outlineVariant,
            hint = hint,
            positive = positive,
            onPositive = LedGreen.readableOn(positive),
            bar = Device.NightPanel,
            barBorder = Device.NightBorder,
            pill = Device.NightPanel,
            onPill = Device.NightInk,
            onPillVariant = Device.NightLabel,
            mark = signal,
            onMark = readableOnSignal,
            signal = signal,
            onSignal = readableOnSignal,
            onSelectionVariant = onSelectionVariant,
            divider = scheme.outlineVariant,
            display = Device.NightDisplay,
            displayOff = Device.NightDisplayOff,
            displayDim = Device.NightDisplayDim,
            displayLit = litOn(Device.NightDisplay, signal, accent),
            graphiteKey = Device.NightKey,
            onGraphite = Device.NightInk,
            onGraphiteMuted = Device.NightLabel,
            aluKey = scheme.surfaceContainerHighest,
            aluKeyBorder = scheme.outlineVariant,
            aluKeyEdge = Color.Black,
        )
    } else {
        DesignColors(
            isDark = false,
            background = background,
            card = card,
            cardBorder = scheme.outlineVariant,
            hint = hint,
            positive = Device.Positive,
            onPositive = Device.OnPositive,
            bar = Device.Graphite,
            barBorder = Device.Graphite,
            pill = Device.Graphite,
            onPill = Device.AluPanel,
            onPillVariant = Device.Muted,
            mark = signal,
            onMark = readableOnSignal,
            signal = signal,
            onSignal = readableOnSignal,
            onSelectionVariant = onSelectionVariant,
            divider = scheme.surfaceVariant,
            display = Device.Display,
            displayOff = Device.DisplayOff,
            displayDim = Device.Muted,
            displayLit = litOn(Device.Display, signal, accent),
            graphiteKey = Device.GraphiteKey,
            onGraphite = Device.AluPanel,
            onGraphiteMuted = Device.Muted,
            aluKey = scheme.surface,
            aluKeyBorder = if (deviceWorld) Device.AluKeyBorder else scheme.outlineVariant,
            aluKeyEdge = if (deviceWorld) Device.AluKeyEdge else scheme.outlineVariant.mixedWith(scheme.surface, MYZEL_KEY_EDGE_FADE),
        )
    }
}

val LocalDesignColors = staticCompositionLocalOf { designColors(lightColorScheme(), isDark = false) }

/** The screen background of the needanapp look ([DesignColors.background]); it stays in place while content scrolls. */
fun Modifier.designBackground(colors: DesignColors): Modifier = background(colors.backgroundBrush())

/**
 * Mixes linearly in the sRGB channels, [amount] 0 = this, 1 = [other]. Unlike Compose's `lerp`, which mixes in Oklab,
 * this keeps tints of dark colors subtle.
 */
fun Color.mixedWith(other: Color, amount: Float): Color = Color(
    red = red + (other.red - red) * amount,
    green = green + (other.green - green) * amount,
    blue = blue + (other.blue - blue) * amount,
    alpha = alpha + (other.alpha - alpha) * amount,
)

private const val DARK_CARD_DEPTH = 0.055f
private const val LIGHT_CARD_DEPTH = 0.045f
private const val DARK_GLOW_TOP = 0.22f
private const val DARK_GLOW_MIDDLE = 0.12f
private const val LIGHT_GLOW_TOP = 0.16f
private const val LIGHT_GLOW_MIDDLE = 0.08f
private const val HINT_ALPHA = 130 / 255f
private const val DARK_POSITIVE_STRENGTH = 60 / 255f
private const val DARK_BAR_BORDER_ALPHA = 140 / 255f
private const val LIGHT_BAR_BORDER_ALPHA = 70 / 255f
private const val PILL_VARIANT_ALPHA = 0.78f
private const val SELECTION_VARIANT_ALPHA = 0.78f
private const val WORLD_PILL_MIN_OPACITY = 0.88f
private const val DARK_DISPLAY_DEPTH = 0.25f
private const val LIGHT_TILE_LIFT = 0.2f
private const val DISPLAY_OFF_LIFT = 0.16f
private const val DARK_DISPLAY_DIM = 0.15f
private const val LIGHT_DISPLAY_DIM = 0.4f
private const val DISPLAY_LIT_LIFT = 0.15f
private const val MYZEL_HINT_ALPHA = 170 / 255f
private const val MYZEL_DARK_POSITIVE_DEPTH = 0.8f
private const val MYZEL_KEY_EDGE_FADE = 0.4f
private val LedGreen = Color(0xFF3DDC97)
private val LedRed = Color(0xFFFF453A)
private val PositiveGreen = Color(0xFF2E7D32)
private val PositiveLight = Color(0xFFC8E6C9)
private val OnPositiveDark = Color(0xFFA5D6A7)
private val OnPositiveLight = Color(0xFF1B5E20)

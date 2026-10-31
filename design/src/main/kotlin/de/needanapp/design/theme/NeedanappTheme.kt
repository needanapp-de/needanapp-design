package de.needanapp.design.theme

import android.app.Activity
import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.provider.Settings
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.materialkolor.dynamicColorScheme
import com.materialkolor.hct.Hct
import java.io.File
import com.materialkolor.PaletteStyle as KolorStyle

/** Which of the costly or decorative effects are on; everything is off with "reduce effects". */
@Immutable
data class DesignEffects(
    val blur: Boolean,
    val shadows: Boolean,
    /** Elaborate transitions; also off when the system setting "remove animations" is on. */
    val richMotion: Boolean,
    /** Opacity of the floating pill and its buttons over the blur (1 - [ThemeSettings.pillTransparency]). */
    val pillOpacity: Float = 1f - NeedanappDefaults.PillTransparency,
)

/** Radii and spacing of the grouped expressive lists, derived from the corner radius setting. */
@Immutable
data class DesignShapes(val group: Dp, val inner: Dp, val rowGap: Dp)

val LocalDesignEffects = staticCompositionLocalOf { DesignEffects(blur = true, shadows = true, richMotion = true) }
val LocalDesignShapes = staticCompositionLocalOf {
    DesignShapes(group = NeedanappDefaults.CornerRadius.dp, inner = 4.dp, rowGap = 3.dp)
}
val LocalThemeSettings = staticCompositionLocalOf { ThemeSettings() }

/** True when the current theme is dark, whatever made it dark (setting or system). */
val LocalIsDarkTheme = staticCompositionLocalOf { false }

/** The look of the current theme, [ThemeSettings.design]. */
val LocalDesignStyle = staticCompositionLocalOf { DesignStyle.NEEDANAPP }

/** Whether the current theme has the needanapp look. */
val isNeedanappDesign: Boolean
    @Composable @ReadOnlyComposable get() = LocalDesignStyle.current == DesignStyle.NEEDANAPP

/** Whether the current theme has the Myzel style. */
val isMyzelDesign: Boolean
    @Composable @ReadOnlyComposable get() = LocalDesignStyle.current == DesignStyle.MYZEL

/**
 * A label or the lettering of a key as the look writes it: capitalized in the Myzel style, as it is in the needanapp
 * look. The dot matrix of a display stays capitalized in both.
 */
@Composable
@ReadOnlyComposable
fun String.labelCase(): String = if (isMyzelDesign) uppercase() else this

/**
 * The needanapp theme: Material 3 colors from [settings] (mode, accent, palette style, color world, tint), the colors of
 * the card look ([LocalDesignColors]), shapes from the corner radius, the optional custom font, UI scaling and the
 * effect switches ([LocalDesignEffects]). In the Myzel style ([ThemeSettings.design], [LocalDesignStyle]) the colors
 * come from its own color worlds, the corners are fixed and the text is set in Geist ([DesignFonts]).
 *
 * @param defaultAccent the app's own accent for [AccentSource.DEFAULT]
 */
@Composable
fun NeedanappTheme(
    settings: ThemeSettings,
    defaultAccent: Color = NeedanappDefaults.Accent,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val isSystemDark by rememberSystemDark()
    val isDark = when (settings.mode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val colorScheme = remember(settings, defaultAccent, isDark) { buildColorScheme(context, settings, defaultAccent, isDark) }
    val style = settings.design
    val isMyzel = style == DesignStyle.MYZEL
    val designColors = remember(colorScheme, settings, defaultAccent, isDark) {
        buildDesignColors(context, settings, colorScheme, defaultAccent, isDark)
    }
    val effects = remember(settings, isDark) {
        val full = !settings.reduceEffects
        DesignEffects(
            blur = full && settings.progressiveBlur,
            shadows = full,
            richMotion = full && !areSystemAnimationsOff(context),
            pillOpacity = 1f - settings.pillTransparency.coerceIn(0f, NeedanappDefaults.MAX_PILL_TRANSPARENCY),
        )
    }
    val radius = settings.cornerRadius.coerceIn(NeedanappDefaults.MIN_CORNER_RADIUS, NeedanappDefaults.MAX_CORNER_RADIUS)
    val shapes = remember(radius, isMyzel) { if (isMyzel) myzelShapes() else buildShapes(radius) }
    val designShapes = remember(radius, isMyzel) {
        // the rows of a group in the Myzel style form one panel, divided by fine lines
        if (isMyzel) DesignShapes(group = NeedanappDefaults.MyzelCornerRadius.dp, inner = 0.dp, rowGap = 0.dp)
        else DesignShapes(group = radius.dp, inner = minOf(radius, 4f).dp, rowGap = 3.dp)
    }
    // the labels, keys and displays of the Myzel style depend on the proportions of Geist, an imported font stays out
    val typography = remember(settings.customFontPath, isMyzel) { if (isMyzel) designTypography() else buildTypography(settings.customFontPath) }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !isDark
                isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    val density = LocalDensity.current
    val scale = settings.uiScale.coerceIn(NeedanappDefaults.MIN_UI_SCALE, NeedanappDefaults.MAX_UI_SCALE)
    CompositionLocalProvider(
        LocalDensity provides Density(density.density * scale, density.fontScale),
        LocalDesignEffects provides effects,
        LocalDesignShapes provides designShapes,
        LocalThemeSettings provides settings,
        LocalIsDarkTheme provides isDark,
        LocalDesignStyle provides style,
        LocalDesignColors provides designColors,
    ) {
        MaterialTheme(colorScheme = colorScheme, shapes = shapes, typography = typography, content = content)
    }
}

/**
 * The seed color [settings] ask for; for Material You the system's main accent, for "Standard" the accent of the color
 * world of the [isDark] theme when it brings one, else the style's ([styleAccent]).
 */
fun resolveAccent(
    context: Context,
    settings: ThemeSettings,
    defaultAccent: Color,
    isDark: Boolean = isDarkTheme(context, settings),
): Color = when (settings.accentSource) {
    AccentSource.DEFAULT -> settings.worldAccent(isDark)?.color ?: settings.styleAccent(defaultAccent)
    AccentSource.DYNAMIC -> Color(context.getColor(android.R.color.system_accent1_500))
    AccentSource.CUSTOM -> settings.customAccent
    AccentSource.PRESET -> settings.preset.color
}

/** Whether [settings] ask for a dark theme right now; outside of Compose, e.g. to color screens still built with Views. */
@Suppress("UNUSED_PARAMETER")
fun isDarkTheme(context: Context, settings: ThemeSettings): Boolean = when (settings.mode) {
    ThemeMode.SYSTEM -> isSystemDark()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

/**
 * Whether the system itself is dark, followed while shown. Not the activity's configuration: AppCompat keeps an
 * activity on the night mode it was created with ([NeedanappViewTheme]), so after switching from Dark or Light to
 * System it would neither say nor follow what the system is; the application hears every change.
 */
@Composable
private fun rememberSystemDark(): State<Boolean> {
    val application = LocalContext.current.applicationContext
    val isDark = remember { mutableStateOf(isSystemDark()) }
    DisposableEffect(application) {
        val callbacks = object : ComponentCallbacks {
            override fun onConfigurationChanged(newConfig: Configuration) {
                isDark.value = newConfig.isNight()
            }

            @Deprecated("Deprecated in Java")
            override fun onLowMemory() = Unit
        }
        application.registerComponentCallbacks(callbacks)
        // a change while nothing was listening
        isDark.value = isSystemDark()
        onDispose { application.unregisterComponentCallbacks(callbacks) }
    }
    return isDark
}

private fun isSystemDark() = Resources.getSystem().configuration.isNight()

private fun Configuration.isNight() = uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

/** The corners of [radius] in dp for Material's shapes. */
private fun buildShapes(radius: Float) = Shapes(
    extraSmall = RoundedCornerShape(minOf(radius, 4f).dp),
    // chips and other small surfaces, rounded like the inner corners of the file manager's chips
    small = RoundedCornerShape(minOf(radius, 12f).dp),
    medium = RoundedCornerShape((radius * 0.6f).dp),
    large = RoundedCornerShape(radius.dp),
    extraLarge = RoundedCornerShape((radius * 1.4f).dp),
)

/** The fixed corners of the Myzel style; a radius set for the needanapp look doesn't count there. */
private fun myzelShapes(): Shapes {
    val radius = NeedanappDefaults.MyzelCornerRadius
    return Shapes(
        extraSmall = RoundedCornerShape(minOf(radius, 6f).dp),
        small = RoundedCornerShape(minOf(radius, 12f).dp),
        medium = RoundedCornerShape((radius * 0.72f).dp),
        large = RoundedCornerShape(radius.dp),
        extraLarge = RoundedCornerShape((radius * 1.36f).dp),
    )
}

/**
 * The color scheme of [settings], the same one [NeedanappTheme] uses; usable outside of Compose. The Myzel style draws
 * aluminium and graphite with the fixed colors of [deviceColorScheme] and the signal color as it is; tint and
 * brightness still apply.
 */
fun buildColorScheme(context: Context, settings: ThemeSettings, defaultAccent: Color, isDark: Boolean): ColorScheme {
    val isAmoled = isDark && settings.pureBlack
    if (settings.isMyzelDeviceWorld(isDark)) {
        val device = deviceColorScheme(resolveAccent(context, settings, defaultAccent, isDark), isDark, isAmoled)
        val lightened = if (isDark && !isAmoled) device.withDarkBrightness(settings.darkBrightness, keepAccents = true) else device
        return lightened.withSurfaceTint(settings.surfaceTint).withCardBrightness(settings.cardBrightness, isDark, keepAccents = true)
            .withReadableColors()
    }
    val base = if (settings.accentSource == AccentSource.DYNAMIC && settings.paletteStyle == PaletteStyle.TONAL_SPOT) {
        // the system scheme as it is, it's the exact Material You of the device
        val system = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        if (isAmoled) system.withPureBlack() else system
    } else {
        dynamicColorScheme(
            seedColor = resolveAccent(context, settings, defaultAccent, isDark),
            isDark = isDark,
            isAmoled = isAmoled,
            style = settings.paletteStyle.toKolorStyle(),
        )
    }
    val accented = base.withAccentSurfaces(isDark, isAmoled).withWorld(settings.worldColors(isDark), isDark, settings.worldAccent(isDark))
        .let { if (isAmoled) it.withPureBlack() else it }
    // a world's own accent colors are made as they are, the brightness settings move only its surfaces
    val keepAccents = settings.worldAccent(isDark) != null
    val lightened = if (isDark && !isAmoled) accented.withDarkBrightness(settings.darkBrightness, keepAccents) else accented
    return lightened.withSurfaceTint(settings.surfaceTint).withCardBrightness(settings.cardBrightness, isDark, keepAccents)
        .withReadableColors()
}

/**
 * Makes sure what lies on each role reads, whatever accent, world and brightness settings made of it: text, the error
 * color and the accent's primary and secondary as text and icons on every surface, each "on" color on its role
 * ([TEXT_CONTRAST]). A bright accent so becomes a deeper tone as primary; [DesignColors.signal] keeps it as it is. Only what doesn't read moves, by as
 * little tone as needed ([readableOn]): the text first, and where no tone of it reads on all surfaces (e.g. very light
 * cards in the dark theme), it reads on background and surface, and the other surfaces move until it reads on them.
 */
private fun ColorScheme.withReadableColors(): ColorScheme {
    val surfaces = listOf(
        background, surface, surfaceDim, surfaceBright, surfaceContainerLowest, surfaceContainerLow, surfaceContainer,
        surfaceContainerHigh, surfaceContainerHighest, surfaceVariant,
    )
    fun Color.ink(): Color {
        val onAll = readableOn(surfaces)
        return if (surfaces.all { contrastRatio(onAll, it) >= TEXT_CONTRAST }) onAll else readableOn(listOf(background, surface))
    }
    val text = onSurface.ink()
    val textVariant = onSurfaceVariant.ink()
    val errorText = error.ink()
    // the accent and its second color as text and icons; DesignColors.signal keeps the accent as it is for surfaces
    val accentText = primary.ink()
    val secondaryText = secondary.ink()
    fun Color.underInk() = readableOn(listOf(text, textVariant, errorText, accentText, secondaryText))
    return copy(
        background = background.underInk(),
        onBackground = onBackground.readableOn(background.underInk()),
        surface = surface.underInk(),
        onSurface = text,
        surfaceVariant = surfaceVariant.underInk(),
        onSurfaceVariant = textVariant,
        surfaceDim = surfaceDim.underInk(),
        surfaceBright = surfaceBright.underInk(),
        surfaceContainerLowest = surfaceContainerLowest.underInk(),
        surfaceContainerLow = surfaceContainerLow.underInk(),
        surfaceContainer = surfaceContainer.underInk(),
        surfaceContainerHigh = surfaceContainerHigh.underInk(),
        surfaceContainerHighest = surfaceContainerHighest.underInk(),
        error = errorText,
        onError = onError.readableOn(errorText),
        primary = accentText,
        onPrimary = onPrimary.readableOn(accentText),
        secondary = secondaryText,
        onPrimaryContainer = onPrimaryContainer.readableOn(primaryContainer),
        onSecondary = onSecondary.readableOn(secondaryText),
        onSecondaryContainer = onSecondaryContainer.readableOn(secondaryContainer),
        onTertiary = onTertiary.readableOn(tertiary),
        onTertiaryContainer = onTertiaryContainer.readableOn(tertiaryContainer),
        onErrorContainer = onErrorContainer.readableOn(errorContainer),
        inverseOnSurface = inverseOnSurface.readableOn(inverseSurface),
        inversePrimary = inversePrimary.readableOn(inverseSurface),
    )
}

/** The [DesignColors] of [settings] for [scheme] from [buildColorScheme], the same ones [NeedanappTheme] provides. */
internal fun buildDesignColors(context: Context, settings: ThemeSettings, scheme: ColorScheme, defaultAccent: Color, isDark: Boolean): DesignColors {
    val glow = if (settings.backgroundGlow) settings.surfaceTint else 0f
    val pureBlack = isDark && settings.pureBlack
    val accent = settings.worldAccent(isDark)
    val seed = resolveAccent(context, settings, defaultAccent, isDark)
    // the accent as the scheme had it before primary was made to read as text (withReadableColors)
    val deviceWorld = settings.isMyzelDeviceWorld(isDark)
    val signal = if (deviceWorld) seed else accent?.color ?: scheme.primary
    val onSignal = if (deviceWorld) deviceOnSignal(isDark) else accent?.onColor ?: scheme.onPrimary
    if (settings.design == DesignStyle.MYZEL) {
        return myzelDesignColors(
            scheme, isDark, deviceWorld, accentGlow = glow, pureBlack = pureBlack, accent = accent,
            signal = signal, onSignal = onSignal,
        )
    }
    return designColors(
        scheme, isDark, pureBlack = pureBlack, accentGlow = glow, world = settings.worldColors(isDark), accent = accent,
        seed = seed, signal = signal, onSignal = onSignal,
    )
}

/**
 * Pulls surfaces and outlines toward the primary color, so the accent's hue shows on cards and backgrounds. Dark: all
 * surface roles by 12 % and the outlines by 18 %, then the whole scheme a few steps toward black, so the colors don't
 * glare. Light: only the containers that cards and bars are made of (18 %) and the fine outline (28 %). With pure black
 * the black roles stay black.
 */
private fun ColorScheme.withAccentSurfaces(isDark: Boolean, isAmoled: Boolean): ColorScheme {
    if (!isDark) {
        val highest = surfaceContainerHighest.mixedWith(primary, LIGHT_SURFACE_ACCENT)
        return copy(
            background = surface,
            surfaceContainer = surfaceContainer.mixedWith(primary, LIGHT_SURFACE_ACCENT),
            surfaceContainerHigh = surfaceContainerHigh.mixedWith(primary, LIGHT_SURFACE_ACCENT),
            surfaceContainerHighest = highest,
            surfaceVariant = highest,
            outlineVariant = outlineVariant.mixedWith(primary, LIGHT_OUTLINE_ACCENT),
        )
    }
    fun Color.darker(amount: Float) = mixedWith(Color.Black, amount)
    fun Color.accented(depth: Float = SURFACE_DEPTH) = mixedWith(primary, DARK_SURFACE_ACCENT).darker(depth)
    val darkSurface = surface.accented()
    val darkHighest = surfaceContainerHighest.accented()
    val accented = copy(
        primary = primary.darker(COLOR_DEPTH),
        primaryContainer = primaryContainer.darker(CONTAINER_DEPTH),
        secondary = secondary.darker(COLOR_DEPTH),
        secondaryContainer = secondaryContainer.darker(CONTAINER_DEPTH),
        tertiary = tertiary.darker(COLOR_DEPTH),
        tertiaryContainer = tertiaryContainer.darker(CONTAINER_DEPTH),
        background = darkSurface,
        surface = darkSurface,
        surfaceDim = surfaceDim.accented(),
        surfaceBright = surfaceBright.accented(BRIGHT_SURFACE_DEPTH),
        surfaceContainerLowest = surfaceContainerLowest.accented(),
        surfaceContainerLow = surfaceContainerLow.accented(),
        surfaceContainer = surfaceContainer.accented(),
        surfaceContainerHigh = surfaceContainerHigh.accented(),
        surfaceContainerHighest = darkHighest,
        surfaceVariant = darkHighest,
        outline = outline.mixedWith(primary, DARK_OUTLINE_ACCENT).darker(OUTLINE_DEPTH),
        outlineVariant = outlineVariant.mixedWith(primary, DARK_OUTLINE_ACCENT).darker(OUTLINE_VARIANT_DEPTH),
        surfaceTint = surfaceTint.darker(TINT_DEPTH),
    )
    return if (isAmoled) accented.withPureBlack() else accented
}

private const val LIGHT_SURFACE_ACCENT = 0.18f
private const val LIGHT_OUTLINE_ACCENT = 0.28f
private const val DARK_SURFACE_ACCENT = 0.12f
private const val DARK_OUTLINE_ACCENT = 0.18f
private const val COLOR_DEPTH = 0.08f
private const val CONTAINER_DEPTH = 0.12f
private const val SURFACE_DEPTH = 0.14f
private const val BRIGHT_SURFACE_DEPTH = 0.12f
private const val OUTLINE_DEPTH = 0.07f
private const val OUTLINE_VARIANT_DEPTH = 0.09f
private const val TINT_DEPTH = 0.06f

/**
 * Raises the tones of background and surfaces in the dark theme by up to the given amounts ([amount] 1 = full), so
 * the app looks less deep black and cards stand out lighter. Text and icons keep their light tones, so contrast stays
 * sufficient (on the lightest surfaces still about 4.5:1 for secondary text). With [keepAccents] the containers keep the
 * colors a color world brought for them.
 */
private fun ColorScheme.withDarkBrightness(amount: Float, keepAccents: Boolean = false): ColorScheme {
    val factor = amount.coerceIn(0f, NeedanappDefaults.MAX_DARK_BRIGHTNESS).toDouble()
    if (factor == 0.0) return this
    fun Color.lighter(tones: Double): Color {
        val hct = Hct.fromInt(toArgb())
        return Color(Hct.from(hct.hue, hct.chroma, (hct.tone + tones * factor).coerceAtMost(100.0)).toInt())
    }
    return copy(
        background = background.lighter(10.0),
        surface = surface.lighter(10.0),
        surfaceDim = surfaceDim.lighter(10.0),
        surfaceBright = surfaceBright.lighter(14.0),
        surfaceContainerLowest = surfaceContainerLowest.lighter(8.0),
        surfaceContainerLow = surfaceContainerLow.lighter(12.0),
        surfaceContainer = surfaceContainer.lighter(16.0),
        surfaceContainerHigh = surfaceContainerHigh.lighter(15.0),
        surfaceContainerHighest = surfaceContainerHighest.lighter(14.0),
        surfaceVariant = surfaceVariant.lighter(6.0),
        outlineVariant = outlineVariant.lighter(8.0),
        // selected chips and segments, the pill's active entry: they must stay apart from the lighter cards
        primaryContainer = if (keepAccents) primaryContainer else primaryContainer.lighter(8.0),
        secondaryContainer = if (keepAccents) secondaryContainer else secondaryContainer.lighter(8.0),
        tertiaryContainer = tertiaryContainer.lighter(8.0),
    )
}

/**
 * Shifts the tones of the containers that cards, groups and bars are made of ([amount] -1 = darker, 1 = lighter), while
 * background and surface stay as they are. Dark: up to [DARK_CARD_LIGHTER] tones up and [DARK_CARD_DARKER] down, the
 * accent containers go half the way up, so selected entries stay apart from lighter cards. Light: up to
 * [LIGHT_CARD_LIGHTER] tones up (cards become almost white) and [LIGHT_CARD_DARKER] down. The lowest container stays, it
 * is black with pure black. With [keepAccents] the accent containers stay, as a color world brought them.
 */
private fun ColorScheme.withCardBrightness(amount: Float, isDark: Boolean, keepAccents: Boolean = false): ColorScheme {
    val value = amount.coerceIn(-NeedanappDefaults.MAX_CARD_BRIGHTNESS, NeedanappDefaults.MAX_CARD_BRIGHTNESS).toDouble()
    if (value == 0.0) return this
    val tones = value * when {
        isDark && value > 0 -> DARK_CARD_LIGHTER
        isDark -> DARK_CARD_DARKER
        value > 0 -> LIGHT_CARD_LIGHTER
        else -> LIGHT_CARD_DARKER
    }
    fun Color.shifted(by: Double): Color {
        val hct = Hct.fromInt(toArgb())
        return Color(Hct.from(hct.hue, hct.chroma, (hct.tone + by).coerceIn(0.0, 100.0)).toInt())
    }
    val accents = if (isDark && tones > 0 && !keepAccents) tones / 2 else 0.0
    return copy(
        surfaceContainerLow = surfaceContainerLow.shifted(tones),
        surfaceContainer = surfaceContainer.shifted(tones),
        surfaceContainerHigh = surfaceContainerHigh.shifted(tones),
        surfaceContainerHighest = surfaceContainerHighest.shifted(tones),
        surfaceVariant = surfaceVariant.shifted(tones),
        primaryContainer = primaryContainer.shifted(accents),
        secondaryContainer = secondaryContainer.shifted(accents),
        tertiaryContainer = tertiaryContainer.shifted(accents),
    )
}

private const val DARK_CARD_LIGHTER = 14.0
private const val DARK_CARD_DARKER = 8.0
private const val LIGHT_CARD_LIGHTER = 16.0
private const val LIGHT_CARD_DARKER = 6.0

private fun ColorScheme.withPureBlack() = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceDim = Color.Black,
    surfaceContainerLowest = Color.Black,
)

/**
 * Scales the chroma (colorfulness) of the surface roles: 0 makes them neutral grey, 1 keeps the scheme, 2 tints them
 * strongly with the accent's hue. Tones stay the same, so contrast doesn't change.
 */
private fun ColorScheme.withSurfaceTint(strength: Float): ColorScheme {
    val factor = strength.coerceIn(0f, NeedanappDefaults.MAX_SURFACE_TINT).let { if (it <= 1f) it else 1f + (it - 1f) * STRONG_TINT_GAIN }
    if (factor == 1f) return this
    val hue = Hct.fromInt(primary.toArgb()).hue
    fun Color.tinted(): Color {
        val hct = Hct.fromInt(toArgb())
        // grey surfaces have no hue of their own, the accent's hue is used when tinting them up
        val chroma = if (hct.chroma < 1.0) MIN_TINT_CHROMA * (factor - 1f).coerceAtLeast(0f) else hct.chroma * factor
        return Color(Hct.from(if (hct.chroma < 1.0) hue else hct.hue, chroma, hct.tone).toInt())
    }
    return copy(
        background = background.tinted(),
        surface = surface.tinted(),
        surfaceVariant = surfaceVariant.tinted(),
        surfaceDim = surfaceDim.tinted(),
        surfaceBright = surfaceBright.tinted(),
        surfaceContainerLowest = surfaceContainerLowest.tinted(),
        surfaceContainerLow = surfaceContainerLow.tinted(),
        surfaceContainer = surfaceContainer.tinted(),
        surfaceContainerHigh = surfaceContainerHigh.tinted(),
        surfaceContainerHighest = surfaceContainerHighest.tinted(),
    )
}

private const val STRONG_TINT_GAIN = 3f
private const val MIN_TINT_CHROMA = 6.0

private fun PaletteStyle.toKolorStyle() = when (this) {
    PaletteStyle.TONAL_SPOT -> KolorStyle.TonalSpot
    PaletteStyle.NEUTRAL -> KolorStyle.Neutral
    PaletteStyle.VIBRANT -> KolorStyle.Vibrant
    PaletteStyle.EXPRESSIVE -> KolorStyle.Expressive
    PaletteStyle.RAINBOW -> KolorStyle.Rainbow
    PaletteStyle.FRUIT_SALAD -> KolorStyle.FruitSalad
    PaletteStyle.MONOCHROME -> KolorStyle.Monochrome
    PaletteStyle.FIDELITY -> KolorStyle.Fidelity
    PaletteStyle.CONTENT -> KolorStyle.Content
}

private fun buildTypography(fontPath: String?): Typography {
    val default = Typography()
    val file = fontPath?.let(::File)?.takeIf { it.isFile } ?: return default
    val family = try {
        FontFamily(Font(file))
    } catch (e: Exception) {
        return default
    }
    fun TextStyle.withFont() = copy(fontFamily = family)
    return Typography(
        displayLarge = default.displayLarge.withFont(),
        displayMedium = default.displayMedium.withFont(),
        displaySmall = default.displaySmall.withFont(),
        headlineLarge = default.headlineLarge.withFont(),
        headlineMedium = default.headlineMedium.withFont(),
        headlineSmall = default.headlineSmall.withFont(),
        titleLarge = default.titleLarge.withFont(),
        titleMedium = default.titleMedium.withFont(),
        titleSmall = default.titleSmall.withFont(),
        bodyLarge = default.bodyLarge.withFont(),
        bodyMedium = default.bodyMedium.withFont(),
        bodySmall = default.bodySmall.withFont(),
        labelLarge = default.labelLarge.withFont(),
        labelMedium = default.labelMedium.withFont(),
        labelSmall = default.labelSmall.withFont(),
    )
}

/** The system setting "remove animations" (developer options or accessibility) sets the animator scale to 0. */
fun areSystemAnimationsOff(context: Context): Boolean =
    Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

package de.needanapp.design.theme

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import de.needanapp.design.R

enum class ThemeMode(@StringRes val label: Int) {
    SYSTEM(R.string.nd_theme_mode_system),
    LIGHT(R.string.nd_theme_mode_light),
    DARK(R.string.nd_theme_mode_dark),
}

/**
 * The look of the whole app. [NEEDANAPP] is the card look of needanapp and the default of every app. [MYZEL] is the
 * look of a device: aluminium (light) or graphite (dark), tactile keys, black displays with a dot matrix, graphite bars
 * and menus, set in Geist. Accent, color worlds and effects work in both; the Myzel style keeps a color world of its
 * own ([ThemeSettings.myzelLightWorld], [ThemeSettings.myzelDarkWorld]), fixed corners and its fonts.
 */
enum class DesignStyle(@StringRes val label: Int) {
    NEEDANAPP(R.string.nd_design_needanapp),
    MYZEL(R.string.nd_design_myzel),
}

/** Where the accent (seed) color comes from. */
enum class AccentSource(@StringRes val label: Int) {
    /** The app's own color, [NeedanappDefaults.Accent] unless the app passes another one. */
    DEFAULT(R.string.nd_accent_default),

    /** Material You: the color the system derives from the wallpaper. */
    DYNAMIC(R.string.nd_accent_dynamic),
    CUSTOM(R.string.nd_accent_custom),
    PRESET(R.string.nd_accent_preset),
}

/** How the color scheme is derived from the accent, the variants of Material's dynamic color. */
enum class PaletteStyle(@StringRes val label: Int) {
    TONAL_SPOT(R.string.nd_style_tonal_spot),
    NEUTRAL(R.string.nd_style_neutral),
    VIBRANT(R.string.nd_style_vibrant),
    EXPRESSIVE(R.string.nd_style_expressive),
    RAINBOW(R.string.nd_style_rainbow),
    FRUIT_SALAD(R.string.nd_style_fruit_salad),
    MONOCHROME(R.string.nd_style_monochrome),
    FIDELITY(R.string.nd_style_fidelity),
    CONTENT(R.string.nd_style_content),
}

/** Ready-made accent colors; one with a [style] is offered only in that style. */
enum class AccentPreset(val color: Color, @StringRes val label: Int, val style: DesignStyle? = null) {
    SAPPHIRE(Color(0xFF1E5AA8), R.string.nd_preset_sapphire),
    EMERALD(Color(0xFF1E8A5A), R.string.nd_preset_emerald),
    AMBER(Color(0xFFC98A12), R.string.nd_preset_amber),
    VIOLET(Color(0xFF6E4BC4), R.string.nd_preset_violet),
    CORAL(Color(0xFFE0624A), R.string.nd_preset_coral),
    PETROL(Color(0xFF0E7C86), R.string.nd_preset_petrol),
    LIME(Color(0xFF6F9E1C), R.string.nd_preset_lime),
    ROSE(Color(0xFFC94A7A), R.string.nd_preset_rose),
    SLATE(Color(0xFF56606E), R.string.nd_preset_slate),

    /** The signal colors of the Myzel style. */
    ORANGE(Color(0xFFFF5A1F), R.string.nd_preset_orange, DesignStyle.MYZEL),
    YELLOW(Color(0xFFFFC400), R.string.nd_preset_yellow, DesignStyle.MYZEL),
    GREEN(Color(0xFF3DDC97), R.string.nd_preset_green, DesignStyle.MYZEL),
}

/** The presets [style] offers, its own first. */
fun accentPresets(style: DesignStyle): List<AccentPreset> =
    AccentPreset.entries.filter { it.style == style } + AccentPreset.entries.filter { it.style == null }

/**
 * Everything the "Appearance" settings control. Stored by [ThemeSettingsStore]; [NeedanappTheme] turns it into colors,
 * shapes, type and effects.
 */
data class ThemeSettings(
    val design: DesignStyle = DesignStyle.NEEDANAPP,
    val mode: ThemeMode = ThemeMode.SYSTEM,
    /** Pure black backgrounds in dark mode (AMOLED). */
    val pureBlack: Boolean = false,
    /**
     * Lightens background and surfaces of the dark theme: 0 = Material's deep dark tones, 1 = clearly lighter grey
     * surfaces and cards. Not with [pureBlack].
     */
    val darkBrightness: Float = 0f,
    val accentSource: AccentSource = AccentSource.DEFAULT,
    val customAccent: Color = NeedanappDefaults.Accent,
    val savedColors: List<Color> = emptyList(),
    val preset: AccentPreset = AccentPreset.SAPPHIRE,
    val paletteStyle: PaletteStyle = PaletteStyle.FIDELITY,
    /** Fixed background, card, line and text colors of the light theme ("color world"); [LightWorld.TINTED] = tinted by the accent. */
    val lightWorld: LightWorld = LightWorld.CLOUD,
    /** The same for the dark theme; [pureBlack] keeps the background black. */
    val darkWorld: DarkWorld = DarkWorld.NORD_NIGHT,
    /**
     * The color worlds of the Myzel style, kept apart from those of the needanapp look so each style keeps its own:
     * aluminium and graphite by default, which the style draws with its own fixed colors.
     */
    val myzelLightWorld: LightWorld = LightWorld.ALU,
    val myzelDarkWorld: DarkWorld = DarkWorld.GRAPHITE,
    /** How strongly the accent tints cards and surfaces: 0 = neutral grey, 1 = Material default, 2 = strong. */
    val surfaceTint: Float = 1f,
    /** The glow of the accent at the top of the background; off (the default) = a plain background. Dark mode with [pureBlack] has none. */
    val backgroundGlow: Boolean = false,
    /**
     * Brightness of cards, groups and bars against the background: -1 = darker, 0 = the scheme as it is, 1 = lighter.
     * The background itself stays as it is.
     */
    val cardBrightness: Float = 0f,
    val progressiveBlur: Boolean = true,
    /** How much of the blurred content shows through the floating pill and its buttons: 0 = opaque. Needs blur. */
    val pillTransparency: Float = NeedanappDefaults.PillTransparency,
    /** Master switch: no blur, shadows or elaborate transitions. */
    val reduceEffects: Boolean = false,
    /** Scales all dp and sp sizes, 1 = 100 %. */
    val uiScale: Float = 1f,
    /** Corner radius of cards and groups in dp. */
    val cornerRadius: Float = NeedanappDefaults.CornerRadius,
    /** Absolute path of an imported .ttf/.otf file, null for the default font. */
    val customFontPath: String? = null,
    /** Collapsible sections as exclusive accordions: opening one closes the others on the same screen. */
    val exclusiveSections: Boolean = true,
)

object NeedanappDefaults {
    /** needanapp blue, the accent of [AccentSource.DEFAULT] unless an app brings its own. */
    val Accent = Color(0xFF3D63D8)

    /** Signal orange, the accent of Myzel. */
    val MyzelAccent = Color(0xFFFF5A1F)
    const val CornerRadius = 14f

    /** The fixed corners of cards and groups in the Myzel style. */
    const val MyzelCornerRadius = 22f
    const val MIN_CORNER_RADIUS = 0f
    const val MAX_CORNER_RADIUS = 32f
    const val MIN_UI_SCALE = 0.85f
    const val MAX_UI_SCALE = 1.15f
    const val MAX_SURFACE_TINT = 2f
    const val MAX_DARK_BRIGHTNESS = 1f
    /** [ThemeSettings.cardBrightness] goes from minus this to this. */
    const val MAX_CARD_BRIGHTNESS = 1f
    const val PillTransparency = 0.45f

    /** Above this the icons get hard to read over bright content. */
    const val MAX_PILL_TRANSPARENCY = 0.8f
    const val MAX_SAVED_COLORS = 12
}

/**
 * The accent of [AccentSource.DEFAULT] when the color world brings none: the app's own [appAccent], or in the Myzel
 * style its signal orange ([NeedanappDefaults.MyzelAccent]).
 */
fun ThemeSettings.styleAccent(appAccent: Color): Color =
    if (design == DesignStyle.MYZEL) NeedanappDefaults.MyzelAccent else appAccent

/** The light color world of the active style: [ThemeSettings.lightWorld], or [ThemeSettings.myzelLightWorld] in the Myzel style. */
val ThemeSettings.activeLightWorld: LightWorld
    get() = if (design == DesignStyle.MYZEL) myzelLightWorld else lightWorld

/** The dark color world of the active style: [ThemeSettings.darkWorld], or [ThemeSettings.myzelDarkWorld] in the Myzel style. */
val ThemeSettings.activeDarkWorld: DarkWorld
    get() = if (design == DesignStyle.MYZEL) myzelDarkWorld else darkWorld

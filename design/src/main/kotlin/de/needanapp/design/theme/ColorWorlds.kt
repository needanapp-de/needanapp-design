package de.needanapp.design.theme

import androidx.annotation.StringRes
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.materialkolor.hct.Hct
import de.needanapp.design.R

/**
 * The fixed colors of a color world: background, cards, raised parts (pill, bars), fine lines and text. They replace
 * the surfaces tinted by the accent; the accent itself comes from the accent settings, unless the world brings its own
 * [accent] and the accent is set to "Standard".
 */
@Immutable
data class SurfaceColors(
    val background: Color,
    val card: Color,
    /** The pill, bars and other parts lying on top; in the light worlds the same as the cards or one step deeper. */
    val raised: Color,
    val line: Color,
    val text: Color,
    val textVariant: Color,
    /** The world's own accent, made to suit its colors; it takes the place of the app's accent for "Standard". */
    val accent: WorldAccent? = null,
    /** The floating pill and its buttons, with the color of what's on them; null = the usual raised surface. */
    val pill: Color? = null,
    val onPill: Color? = null,
    /** The width of the fine border around cards; the graphic worlds draw it firmer. */
    val lineWidth: Dp = 1.dp,
)

/**
 * The accent a color world brings along: the color itself and its container (the active pill entry), each with the
 * color on top of it. Selected entries lie on [selection], their check mark is [mark], destructive actions such as
 * deleting lie on [danger] (null = Material's error container).
 */
@Immutable
data class WorldAccent(
    val color: Color,
    val onColor: Color,
    val container: Color,
    val onContainer: Color,
    val selection: Color = container,
    val onSelection: Color = onContainer,
    val mark: Color = color,
    val onMark: Color = onColor,
    val danger: Color? = null,
    val onDanger: Color? = null,
)

/**
 * The color world of the light theme; [TINTED] = the surfaces tinted by the accent, the default before 0.30. A world
 * with a [style] is offered only in that style.
 */
enum class LightWorld(@StringRes val label: Int, val colors: SurfaceColors?, val style: DesignStyle? = null) {
    TINTED(R.string.nd_world_tinted, null),

    /** Cool light grey with white cards, like grouped lists on iOS; the default since 0.30. */
    CLOUD(R.string.nd_world_cloud, SurfaceColors(Color(0xFFF1F3F6), Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFFE0E4EA), Color(0xFF1B2130), Color(0xFF596273))),

    /** Warm off-white, less glaring than pure white. */
    PAPER(R.string.nd_world_paper, SurfaceColors(Color(0xFFF5F2EC), Color(0xFFFFFDF8), Color(0xFFFFFDF8), Color(0xFFE6E0D4), Color(0xFF29261F), Color(0xFF665E51))),

    /** Blue grey after the Nord palette ("Snow Storm"). */
    NORD_SNOW(R.string.nd_world_nord_snow, SurfaceColors(Color(0xFFECEFF4), Color(0xFFF8F9FB), Color(0xFFF8F9FB), Color(0xFFD8DEE9), Color(0xFF2E3440), Color(0xFF4C566A))),

    /** Soft pastel after Catppuccin Latte; the cards are a little deeper than the background. */
    LATTE(R.string.nd_world_latte, SurfaceColors(Color(0xFFEFF1F5), Color(0xFFE6E9EF), Color(0xFFDCE0E8), Color(0xFFD3D8E2), Color(0xFF4C4F69), Color(0xFF5C5F77))),

    /** Graphic: black, white and signal orange, cards with a firm black border, a black pill. */
    SIGNAL(
        R.string.nd_world_signal,
        SurfaceColors(
            Color(0xFFEFEFEC), Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFF111111), Color(0xFF0A0A0A), Color(0xFF4A4A4A),
            WorldAccent(
                Color(0xFFFF4F00), Color(0xFF000000), Color(0xFFFF4F00), Color(0xFF000000),
                selection = Color(0xFF0A0A0A), onSelection = Color(0xFFFFFFFF),
                mark = Color(0xFFFF4F00), onMark = Color(0xFF000000),
                danger = Color(0xFFE00000), onDanger = Color(0xFFFFFFFF),
            ),
            pill = Color(0xFF0A0A0A), onPill = Color(0xFFFFFFFF),
            lineWidth = 1.5.dp,
        ),
    ),
    /** Electric violet with neon lime. */
    ULTRAVIOLET(
        R.string.nd_world_ultraviolet,
        SurfaceColors(
            Color(0xFFF3F2F8), Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFFD4CDEC), Color(0xFF140A33), Color(0xFF4B3F78),
            WorldAccent(
                Color(0xFF5A2DE0), Color(0xFFFFFFFF), Color(0xFFA8E600), Color(0xFF140A33),
                selection = Color(0xFF5A2DE0), onSelection = Color(0xFFFFFFFF),
                mark = Color(0xFFA8E600), onMark = Color(0xFF140A33),
                danger = Color(0xFFE01E37), onDanger = Color(0xFFFFFFFF),
            ),
            pill = Color(0xFF140A33), onPill = Color(0xFFFFFFFF),
        ),
    ),
    /** Graphite with neon yellow like a high-visibility vest; selected entries turn yellow. */
    HI_VIS(
        R.string.nd_world_hi_vis,
        SurfaceColors(
            Color(0xFFECECE9), Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFFCFD1D5), Color(0xFF16181D), Color(0xFF4A4E57),
            WorldAccent(
                Color(0xFF16181D), Color(0xFFFFE600), Color(0xFFFFE600), Color(0xFF16181D),
                selection = Color(0xFFFFE600), onSelection = Color(0xFF16181D),
                mark = Color(0xFF16181D), onMark = Color(0xFFFFE600),
                danger = Color(0xFFC80000), onDanger = Color(0xFFFFFFFF),
            ),
            pill = Color(0xFF16181D), onPill = Color(0xFFFFFFFF),
        ),
    ),
    /** Deep petrol with glowing copper. */
    COPPER(
        R.string.nd_world_copper,
        SurfaceColors(
            Color(0xFFF3F1EE), Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFFD8CEC2), Color(0xFF0B2A33), Color(0xFF45606A),
            WorldAccent(
                Color(0xFF0E5C6B), Color(0xFFFFFFFF), Color(0xFFF08A2E), Color(0xFF1A0A00),
                selection = Color(0xFF0E5C6B), onSelection = Color(0xFFFFFFFF),
                mark = Color(0xFFF08A2E), onMark = Color(0xFF1A0A00),
                danger = Color(0xFFC1121F), onDanger = Color(0xFFFFFFFF),
            ),
            pill = Color(0xFF0B2A33), onPill = Color(0xFFFFFFFF),
        ),
    ),
    /** Graphic: graphite and neon pink, cards with a firm dark border. */
    NEON_PINK(
        R.string.nd_world_neon_pink,
        SurfaceColors(
            Color(0xFFEDEDEF), Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFF1E2026), Color(0xFF15161A), Color(0xFF4A4C55),
            WorldAccent(
                Color(0xFFFF2D87), Color(0xFF15161A), Color(0xFFFF2D87), Color(0xFF15161A),
                selection = Color(0xFF15161A), onSelection = Color(0xFFFFFFFF),
                mark = Color(0xFFFF2D87), onMark = Color(0xFF15161A),
                danger = Color(0xFFE00000), onDanger = Color(0xFFFFFFFF),
            ),
            pill = Color(0xFF15161A), onPill = Color(0xFFFFFFFF),
            lineWidth = 1.5.dp,
        ),
    ),
    /** Strong olive with clear sky blue. */
    OLIVE(
        R.string.nd_world_olive,
        SurfaceColors(
            Color(0xFFF2F3EC), Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFFD0D4C0), Color(0xFF1E240C), Color(0xFF4D5538),
            WorldAccent(
                Color(0xFF4A5A1C), Color(0xFFFFFFFF), Color(0xFF00A2FF), Color(0xFF001A2B),
                selection = Color(0xFF4A5A1C), onSelection = Color(0xFFFFFFFF),
                mark = Color(0xFF00A2FF), onMark = Color(0xFF001A2B),
                danger = Color(0xFFD7263D), onDanger = Color(0xFFFFFFFF),
            ),
            pill = Color(0xFF1E240C), onPill = Color(0xFFFFFFFF),
        ),
    ),

    /**
     * Brushed aluminium with lighter panels, the default of the Myzel style, which draws it with its own fixed colors
     * ([isMyzelDeviceWorld]).
     */
    ALU(
        R.string.nd_world_alu,
        SurfaceColors(Color(0xFFE4E4E0), Color(0xFFF3F3F0), Color(0xFFF3F3F0), Color(0xFFC9C9C3), Color(0xFF151515), Color(0xFF55554F)),
        DesignStyle.MYZEL,
    ),
}

/** The color world of the dark theme; [TINTED] = the surfaces tinted by the accent, the default before 0.30. */
enum class DarkWorld(@StringRes val label: Int, val colors: SurfaceColors?) {
    TINTED(R.string.nd_world_tinted, null),

    /**
     * Almost neutral dark grey as Material recommends, instead of black; in the Myzel style its graphite, the default
     * there ([isMyzelDeviceWorld]).
     */
    GRAPHITE(R.string.nd_world_graphite, SurfaceColors(Color(0xFF121316), Color(0xFF1C1E22), Color(0xFF23262B), Color(0xFF2B2E34), Color(0xFFE3E5E9), Color(0xFFA0A6B1))),

    /** Muted blue grey after the Nord palette ("Polar Night"); the default since 0.30. */
    NORD_NIGHT(R.string.nd_world_nord_night, SurfaceColors(Color(0xFF242933), Color(0xFF2E3440), Color(0xFF353C4A), Color(0xFF3B4252), Color(0xFFECEFF4), Color(0xFFAEB7C7))),

    /** Deep indigo after Catppuccin Mocha. */
    MIDNIGHT(R.string.nd_world_midnight, SurfaceColors(Color(0xFF1E1E2E), Color(0xFF282A3C), Color(0xFF313244), Color(0xFF3A3C52), Color(0xFFCDD6F4), Color(0xFFA6ADC8))),

    /** Soft slate grey with little contrast, after GitHub's "dark dimmed". */
    DIMMED(R.string.nd_world_dimmed, SurfaceColors(Color(0xFF22272E), Color(0xFF2D333B), Color(0xFF373E47), Color(0xFF444C56), Color(0xFFADBAC7), Color(0xFF909BA8))),

    /** Black with signal orange; selected entries turn orange. */
    SIGNAL(
        R.string.nd_world_signal,
        SurfaceColors(
            Color(0xFF000000), Color(0xFF171717), Color(0xFF1F1F1F), Color(0xFF3A3A3A), Color(0xFFFFFFFF), Color(0xFFB5B5B5),
            WorldAccent(
                Color(0xFFFF5A1F), Color(0xFF000000), Color(0xFFFF5A1F), Color(0xFF000000),
                selection = Color(0xFFFF5A1F), onSelection = Color(0xFF000000),
                mark = Color(0xFF000000), onMark = Color(0xFFFF5A1F),
                danger = Color(0xFF8E0000), onDanger = Color(0xFFFFFFFF),
            ),
            pill = Color(0xFF1F1F1F), onPill = Color(0xFFFFFFFF),
        ),
    ),
    /** Deep violet night with neon lime. */
    ULTRAVIOLET(
        R.string.nd_world_ultraviolet,
        SurfaceColors(
            Color(0xFF0B051E), Color(0xFF190F3E), Color(0xFF23154F), Color(0xFF35246F), Color(0xFFFFFFFF), Color(0xFFBDB0F5),
            WorldAccent(
                Color(0xFFB6F500), Color(0xFF142000), Color(0xFFB6F500), Color(0xFF142000),
                selection = Color(0xFF5A2DE0), onSelection = Color(0xFFFFFFFF),
                mark = Color(0xFFB6F500), onMark = Color(0xFF142000),
                danger = Color(0xFFFF4D5E), onDanger = Color(0xFF1A0005),
            ),
            pill = Color(0xFF23154F), onPill = Color(0xFFFFFFFF),
        ),
    ),
    /** Graphite with neon yellow; selected entries turn yellow. */
    HI_VIS(
        R.string.nd_world_hi_vis,
        SurfaceColors(
            Color(0xFF111215), Color(0xFF1D1F24), Color(0xFF25282E), Color(0xFF30333A), Color(0xFFF5F5F2), Color(0xFFA9ADB5),
            WorldAccent(
                Color(0xFFFFE600), Color(0xFF16181D), Color(0xFFFFE600), Color(0xFF16181D),
                selection = Color(0xFFFFE600), onSelection = Color(0xFF16181D),
                mark = Color(0xFF16181D), onMark = Color(0xFFFFE600),
                danger = Color(0xFFB00000), onDanger = Color(0xFFFFFFFF),
            ),
            pill = Color(0xFF25282E), onPill = Color(0xFFFFFFFF),
        ),
    ),
    /** Deep petrol with glowing copper. */
    COPPER(
        R.string.nd_world_copper,
        SurfaceColors(
            Color(0xFF02181E), Color(0xFF0A2D35), Color(0xFF0E3B46), Color(0xFF165061), Color(0xFFF2FBFD), Color(0xFF9EC3CC),
            WorldAccent(
                Color(0xFFF08A2E), Color(0xFF1A0A00), Color(0xFFF08A2E), Color(0xFF1A0A00),
                selection = Color(0xFF0E7C8C), onSelection = Color(0xFFFFFFFF),
                mark = Color(0xFFF08A2E), onMark = Color(0xFF1A0A00),
                danger = Color(0xFFD32F2F), onDanger = Color(0xFFFFFFFF),
            ),
            pill = Color(0xFF0E3B46), onPill = Color(0xFFFFFFFF),
        ),
    ),
    /** Graphite with neon pink; selected entries turn pink. */
    NEON_PINK(
        R.string.nd_world_neon_pink,
        SurfaceColors(
            Color(0xFF0E0F12), Color(0xFF1B1C21), Color(0xFF23252B), Color(0xFF33353D), Color(0xFFFFFFFF), Color(0xFFB4B6BF),
            WorldAccent(
                Color(0xFFFF2D87), Color(0xFF15161A), Color(0xFFFF2D87), Color(0xFF15161A),
                selection = Color(0xFFFF2D87), onSelection = Color(0xFF15161A),
                mark = Color(0xFF15161A), onMark = Color(0xFFFF2D87),
                danger = Color(0xFF8E0014), onDanger = Color(0xFFFFFFFF),
            ),
            pill = Color(0xFF23252B), onPill = Color(0xFFFFFFFF),
        ),
    ),
    /** Almost black olive with clear sky blue. */
    OLIVE(
        R.string.nd_world_olive,
        SurfaceColors(
            Color(0xFF0D1007), Color(0xFF1A1F0E), Color(0xFF232A13), Color(0xFF333C1C), Color(0xFFF7FAEE), Color(0xFFB9C2A0),
            WorldAccent(
                Color(0xFF33B5FF), Color(0xFF001A2B), Color(0xFF33B5FF), Color(0xFF001A2B),
                selection = Color(0xFF5C6F22), onSelection = Color(0xFFFFFFFF),
                mark = Color(0xFF33B5FF), onMark = Color(0xFF001A2B),
                danger = Color(0xFFD32F2F), onDanger = Color(0xFFFFFFFF),
            ),
            pill = Color(0xFF232A13), onPill = Color(0xFFFFFFFF),
        ),
    ),
}

/** The colors of the world [settings] choose for the light or dark theme; null = tinted by the accent. */
fun ThemeSettings.worldColors(isDark: Boolean): SurfaceColors? = if (isDark) activeDarkWorld.colors else activeLightWorld.colors

/**
 * Whether the Myzel style shows one of its own worlds, aluminium or graphite, in the light or dark theme: it draws them
 * with the fixed colors of a device ([deviceColorScheme]) rather than from the world's [SurfaceColors].
 */
fun ThemeSettings.isMyzelDeviceWorld(isDark: Boolean): Boolean =
    design == DesignStyle.MYZEL && if (isDark) myzelDarkWorld == DarkWorld.GRAPHITE else myzelLightWorld == LightWorld.ALU

/** The light worlds [style] offers, its own first. */
fun lightWorlds(style: DesignStyle): List<LightWorld> =
    LightWorld.entries.filter { it.style == style } + LightWorld.entries.filter { it.style == null }

/** The dark worlds [style] offers; the Myzel style has its graphite first. */
fun darkWorlds(style: DesignStyle): List<DarkWorld> =
    if (style == DesignStyle.MYZEL) listOf(DarkWorld.GRAPHITE) + (DarkWorld.entries - DarkWorld.GRAPHITE) else DarkWorld.entries

/** The accent of the chosen world when it brings one and the accent is set to "Standard"; otherwise null. */
fun ThemeSettings.worldAccent(isDark: Boolean): WorldAccent? = worldColors(isDark)?.accent?.takeIf { accentSource == AccentSource.DEFAULT }

/**
 * Puts the colors of [world] in place of the surface, line and text roles. The selected entries (primary and secondary
 * containers) become soft tones of the accent's hue, like the light container of a tonal palette, so they suit every
 * world whatever palette style is set. With the world's own [accent] its colors are taken as they are, so they stay as
 * strong as the world was made with.
 */
internal fun ColorScheme.withWorld(world: SurfaceColors?, isDark: Boolean, accent: WorldAccent? = null): ColorScheme {
    world ?: return this
    val hue = Hct.fromInt(primary.toArgb()).hue
    fun tone(chroma: Double, tone: Double) = Color(Hct.from(hue, chroma, tone).toInt())
    val container = if (isDark) tone(DARK_CONTAINER_CHROMA, DARK_CONTAINER_TONE) else tone(LIGHT_CONTAINER_CHROMA, LIGHT_CONTAINER_TONE)
    val onContainer = if (isDark) tone(DARK_ON_CONTAINER_CHROMA, DARK_ON_CONTAINER_TONE) else tone(LIGHT_ON_CONTAINER_CHROMA, LIGHT_ON_CONTAINER_TONE)
    // the graphic worlds draw their lines black; their deeper surfaces take a light grey of their text instead
    val shade = if (world.line.readsLight()) world.line else world.background.mixedWith(world.textVariant, GRAPHIC_SHADE)
    val highest = if (isDark) world.raised.mixedWith(world.textVariant, DARK_HIGHEST_LIFT) else world.background.mixedWith(shade, LIGHT_HIGHEST_DEPTH)
    return copy(
        background = world.background,
        onBackground = world.text,
        surface = world.background,
        onSurface = world.text,
        surfaceVariant = highest,
        onSurfaceVariant = world.textVariant,
        surfaceDim = if (isDark) world.background.mixedWith(Color.Black, DARK_DIM_DEPTH) else world.background.mixedWith(shade, LIGHT_DIM_DEPTH),
        surfaceBright = world.raised,
        surfaceContainerLowest = if (isDark) world.background.mixedWith(Color.Black, DARK_LOWEST_DEPTH) else world.card.mixedWith(Color.White, LIGHT_LOWEST_LIFT),
        surfaceContainerLow = world.background.mixedWith(world.card, LOW_BETWEEN),
        surfaceContainer = world.card,
        surfaceContainerHigh = world.raised,
        surfaceContainerHighest = highest,
        outline = world.textVariant.mixedWith(world.background, OUTLINE_FADE),
        outlineVariant = world.line,
        primary = accent?.color ?: primary,
        onPrimary = accent?.onColor ?: onPrimary,
        surfaceTint = accent?.color ?: surfaceTint,
        primaryContainer = accent?.container ?: container,
        onPrimaryContainer = accent?.onContainer ?: onContainer,
        secondaryContainer = accent?.selection ?: container,
        onSecondaryContainer = accent?.onSelection ?: onContainer,
        errorContainer = accent?.danger ?: errorContainer,
        onErrorContainer = accent?.onDanger ?: onErrorContainer,
    )
}

private const val LIGHT_CONTAINER_CHROMA = 24.0
private const val LIGHT_CONTAINER_TONE = 90.0
private const val LIGHT_ON_CONTAINER_CHROMA = 48.0
private const val LIGHT_ON_CONTAINER_TONE = 25.0
private const val DARK_CONTAINER_CHROMA = 28.0
private const val DARK_CONTAINER_TONE = 28.0
private const val DARK_ON_CONTAINER_CHROMA = 16.0
private const val DARK_ON_CONTAINER_TONE = 90.0
private const val DARK_HIGHEST_LIFT = 0.08f
private const val LIGHT_HIGHEST_DEPTH = 0.6f
private const val GRAPHIC_SHADE = 0.2f
private const val DARK_DIM_DEPTH = 0.2f
private const val LIGHT_DIM_DEPTH = 0.4f
private const val DARK_LOWEST_DEPTH = 0.25f
private const val LIGHT_LOWEST_LIFT = 0.5f
private const val LOW_BETWEEN = 0.5f
private const val OUTLINE_FADE = 0.35f

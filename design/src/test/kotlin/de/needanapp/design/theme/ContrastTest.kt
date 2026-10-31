package de.needanapp.design.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.toArgb
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Builds the colors of both styles in every color world, light and dark, with every accent and the brightness settings
 * at their ends, as [NeedanappTheme] does, and checks that text and icons read on what the building blocks put them on:
 * at least [TEXT] for text, [ICON] for icons. A failure lists each pair with its worst theme.
 */
class ContrastTest {
    private val context = mockk<Context>(relaxed = true)

    @Test
    fun textAndIconsReadInEveryTheme() {
        val failures = mutableListOf<Failure>()
        val themes = themes()
        themes.forEach { theme -> check(theme, failures) }
        val report = failures.groupBy { it.pair }.entries.sortedBy { it.key }.joinToString("\n") { (pair, list) ->
            val worst = list.minBy { it.ratio }
            val worlds = list.groupBy { it.theme.world() }.entries
                .sortedBy { (_, it) -> it.minOf { failure -> failure.ratio } }
                .take(WORLDS_SHOWN)
                .joinToString { (world, it) -> "$world ${"%.2f".format(it.minOf { failure -> failure.ratio })}" }
            "$pair (needs ${worst.min}): ${list.size} themes, worst ${"%.2f".format(worst.ratio)} in ${worst.theme}\n    $worlds"
        }
        println("${themes.size} themes, ${failures.size} pairs too faint\n$report")
        assertTrue("${failures.size} pairs too faint:\n$report", failures.isEmpty())
    }

    private fun check(theme: Theme, failures: MutableList<Failure>) {
        val settings = theme.settings
        val scheme = buildColorScheme(context, settings, theme.defaultAccent, theme.isDark)
        val design = buildDesignColors(context, settings, scheme, theme.defaultAccent, theme.isDark)
        val isMyzel = settings.design == DesignStyle.MYZEL
        fun need(pair: String, content: Color, background: Color, min: Float) {
            val ground = background.compositeOver(design.background.last())
            val ratio = contrastRatio(content.compositeOver(ground), ground)
            if (ratio < min) failures += Failure(pair, ratio, min, theme)
        }

        // displays: the dot matrix in every tone, the detail at its end
        need("displayLit on display", design.displayLit, design.display, TEXT)
        need("ledGreen on display", design.ledGreen, design.display, TEXT)
        need("displayDim on display", design.displayDim, design.display, TEXT)
        need("error text on display", if (isMyzel) design.onGraphite else Color.White, design.display, TEXT)

        // the floating pill and its menus, opaque and as see-through as the settings allow over the page
        if (isMyzel) {
            for (opacity in listOf(1f, MYZEL_BAR_OPACITY)) {
                for (ground in listOf(design.background.first(), design.background.last(), design.card)) {
                    val pill = design.bar.copy(alpha = opacity).compositeOver(ground)
                    need("onGraphite on pill", design.onGraphite, pill, TEXT)
                    need("onGraphiteMuted on pill", design.onGraphiteMuted, pill, TEXT)
                }
            }
        } else {
            val leastOpacity = maxOf(1f - NeedanappDefaults.MAX_PILL_TRANSPARENCY, design.pillMinOpacity)
            val defaultOpacity = maxOf(1f - NeedanappDefaults.PillTransparency, design.pillMinOpacity)
            for (opacity in listOf(1f, defaultOpacity, leastOpacity)) {
                for (ground in listOf(design.background.first(), design.background.last(), design.card)) {
                    val pill = design.pill.copy(alpha = opacity).compositeOver(ground)
                    need("onPill on pill", design.onPill, pill, TEXT)
                    need("onPillVariant on pill", design.onPillVariant, pill, TEXT)
                }
            }
            need("onSurface on pressed", scheme.onSurface, design.pressed, TEXT)
        }
        need("onPrimaryContainer on primaryContainer", scheme.onPrimaryContainer, scheme.primaryContainer, TEXT)
        need("onPrimary on primary", scheme.onPrimary, scheme.primary, TEXT)
        need("onSignal on signal", design.onSignal, design.signal, TEXT)
        need("signal lit on display", design.displayLit, design.display, TEXT)
        need("onPrimary 78% on primary", scheme.onPrimary.fadedOn(SUMMARY_ALPHA, scheme.primary), scheme.primary, TEXT)

        // keys and bars
        need("onGraphite on graphiteKey", design.onGraphite, design.graphiteKey, TEXT)
        need("onGraphiteMuted on graphiteKey", design.onGraphiteMuted, design.graphiteKey, TEXT)
        if (isMyzel) need("onGraphite on bar", design.onGraphite, design.bar, TEXT)

        // text, icons and the accent on the page, on cards and the fields on them
        val tile = scheme.primary.copy(alpha = TILE_TINT_ALPHA).compositeOver(scheme.surfaceContainerHighest)
            .readableOn(scheme.inks())
        val surfaces = buildList {
            add("background top" to design.background.first())
            add("background" to design.background.last())
            add("card" to design.card)
            add("surfaceContainerLow" to scheme.surfaceContainerLow)
            add("surfaceContainer" to scheme.surfaceContainer)
            add("surfaceContainerHigh" to scheme.surfaceContainerHigh)
            add("surfaceContainerHighest" to scheme.surfaceContainerHighest)
            add("tile" to design.tile)
            add("aluKey" to design.aluKey)
            add("tile container" to tile)
            if (!isMyzel) add("bar" to design.bar)
        }
        for ((name, surface) in surfaces) {
            need("onSurface on $name", scheme.onSurface, surface, TEXT)
            need("onSurfaceVariant on $name", scheme.onSurfaceVariant, surface, TEXT)
            need("primary icon on $name", scheme.primary, surface, ICON)
            need("primary text on $name", scheme.primary, surface, TEXT)
            need("error on $name", scheme.error, surface, TEXT)
            need("secondary on $name", scheme.secondary, surface, TEXT)
        }
        need("onSurface 78% on tile container", scheme.onSurface.fadedOn(SUMMARY_ALPHA, tile), tile, TEXT)
        need("onSurface 86% on card", scheme.onSurface.fadedOn(DIALOG_TEXT_ALPHA, design.card), design.card, TEXT)
        need("hint on card", design.hint, design.card, TEXT)
        val tag = scheme.surfaceContainerHighest.mixedWith(scheme.primary, TAG_ACCENT)
        need("onSurface on tag", scheme.onSurface.readableOn(tag), tag, TEXT)

        // selected entries, marks and states
        need("onSecondaryContainer on secondaryContainer", scheme.onSecondaryContainer, scheme.secondaryContainer, TEXT)
        need("onSelectionVariant on secondaryContainer", design.onSelectionVariant, scheme.secondaryContainer, TEXT)
        need("onMark on mark", design.onMark, design.mark, TEXT)
        need("onPositive on positive", design.onPositive, design.positive, TEXT)
        need("onErrorContainer on errorContainer", scheme.onErrorContainer, scheme.errorContainer, TEXT)
        need("onError on error", scheme.onError, scheme.error, TEXT)
        need("onSecondary on secondary", scheme.onSecondary, scheme.secondary, TEXT)
        need("onTertiary on tertiary", scheme.onTertiary, scheme.tertiary, TEXT)
        need("onTertiaryContainer on tertiaryContainer", scheme.onTertiaryContainer, scheme.tertiaryContainer, TEXT)
        need("inverseOnSurface on inverseSurface", scheme.inverseOnSurface, scheme.inverseSurface, TEXT)
    }

    /** Every theme to check: style, light and dark, world, accent, then the palette styles and the settings' ends. */
    private fun themes(): List<Theme> = buildList {
        for (style in DesignStyle.entries) {
            for (isDark in listOf(false, true)) {
                val worlds: List<Enum<*>> = if (isDark) darkWorlds(style) else lightWorlds(style)
                for (world in worlds) {
                    for ((accent, defaultAccent) in accents(style)) {
                        val base = accent.copy(design = style, mode = if (isDark) ThemeMode.DARK else ThemeMode.LIGHT).withWorld(world)
                        val variants = PaletteStyle.entries.map { base.copy(paletteStyle = it) } + listOf(
                            base.copy(surfaceTint = 0f),
                            base.copy(surfaceTint = NeedanappDefaults.MAX_SURFACE_TINT),
                            base.copy(cardBrightness = -NeedanappDefaults.MAX_CARD_BRIGHTNESS),
                            base.copy(cardBrightness = NeedanappDefaults.MAX_CARD_BRIGHTNESS),
                            base.copy(backgroundGlow = true, surfaceTint = NeedanappDefaults.MAX_SURFACE_TINT),
                        ) + if (isDark) {
                            listOf(
                                base.copy(pureBlack = true),
                                base.copy(darkBrightness = NeedanappDefaults.MAX_DARK_BRIGHTNESS),
                                base.copy(
                                    darkBrightness = NeedanappDefaults.MAX_DARK_BRIGHTNESS,
                                    cardBrightness = NeedanappDefaults.MAX_CARD_BRIGHTNESS,
                                ),
                            )
                        } else {
                            emptyList()
                        }
                        variants.forEach { add(Theme(it, defaultAccent, isDark)) }
                    }
                }
            }
        }
    }

    /** The accents of [style]: "Standard" with the app accents of needanapp and Myzel, the presets and odd own colors. */
    private fun accents(style: DesignStyle): List<Pair<ThemeSettings, Color>> = buildList {
        for (appAccent in listOf(NeedanappDefaults.Accent, NeedanappDefaults.MyzelAccent)) {
            add(ThemeSettings(accentSource = AccentSource.DEFAULT) to appAccent)
        }
        for (preset in accentPresets(style)) {
            add(ThemeSettings(accentSource = AccentSource.PRESET, preset = preset) to NeedanappDefaults.Accent)
        }
        for (color in CUSTOM_ACCENTS) {
            add(ThemeSettings(accentSource = AccentSource.CUSTOM, customAccent = color) to NeedanappDefaults.Accent)
        }
    }

    private fun ThemeSettings.withWorld(world: Enum<*>): ThemeSettings = when {
        world is LightWorld && design == DesignStyle.MYZEL -> copy(myzelLightWorld = world)
        world is LightWorld -> copy(lightWorld = world)
        world is DarkWorld && design == DesignStyle.MYZEL -> copy(myzelDarkWorld = world)
        else -> copy(darkWorld = world as DarkWorld)
    }

    private data class Theme(val settings: ThemeSettings, val defaultAccent: Color, val isDark: Boolean) {
        fun world(): String {
            val world = if (isDark) settings.activeDarkWorld else settings.activeLightWorld
            return "${settings.design}/${if (isDark) "dark" else "light"}/$world"
        }

        override fun toString(): String = buildString {
            append(world())
            append(", accent ")
            append(
                when (settings.accentSource) {
                    AccentSource.DEFAULT -> "standard (app ${defaultAccent.hex()})"
                    AccentSource.PRESET -> settings.preset.name
                    AccentSource.CUSTOM -> settings.customAccent.hex()
                    AccentSource.DYNAMIC -> "dynamic"
                },
            )
            append(", ${settings.paletteStyle}")
            if (settings.pureBlack) append(", pure black")
            if (settings.darkBrightness != 0f) append(", dark brightness ${settings.darkBrightness}")
            if (settings.cardBrightness != 0f) append(", card brightness ${settings.cardBrightness}")
            if (settings.surfaceTint != 1f) append(", tint ${settings.surfaceTint}")
            if (settings.backgroundGlow) append(", glow")
        }
    }

    private data class Failure(val pair: String, val ratio: Float, val min: Float, val theme: Theme)

    private companion object {
        /** Text, as WCAG asks for normal sizes. */
        const val TEXT = 4.5f

        /** Icons and other graphics. */
        const val ICON = 3f

        const val WORLDS_SHOWN = 6

        // as the building blocks draw them
        const val MYZEL_BAR_OPACITY = 0.92f
        const val SUMMARY_ALPHA = 0.78f
        const val DIALOG_TEXT_ALPHA = 0.86f
        const val TILE_TINT_ALPHA = 0.08f
        const val TAG_ACCENT = 0.12f

        /** Own colors at the edges of what the color picker allows: black, white, greys and pure hues. */
        val CUSTOM_ACCENTS = listOf(
            Color(0xFF000000), Color(0xFF111111), Color(0xFF333333), Color(0xFF808080), Color(0xFFCCCCCC),
            Color(0xFFFFFFFF), Color(0xFFFFFF00), Color(0xFF00FFFF), Color(0xFF00FF00), Color(0xFFFF0000),
            Color(0xFF0000FF), Color(0xFF0A0A40), Color(0xFFC8772E),
        )

        fun Color.hex(): String = "#%06X".format(toArgb() and 0xFFFFFF)
    }
}

package de.needanapp.design.settings

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FontDownload
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import de.needanapp.design.R
import de.needanapp.design.components.ColorPicker
import de.needanapp.design.components.ColorSwatch
import de.needanapp.design.components.ContentRow
import de.needanapp.design.components.ListRow
import de.needanapp.design.components.OptionsSheet
import de.needanapp.design.components.RowPosition
import de.needanapp.design.components.SegmentedRow
import de.needanapp.design.components.SettingsGroup
import de.needanapp.design.components.SliderRow
import de.needanapp.design.components.SwitchRow
import de.needanapp.design.components.rememberSectionState
import de.needanapp.design.components.scrollbar
import de.needanapp.design.components.toHex
import de.needanapp.design.theme.AccentSource
import de.needanapp.design.theme.DarkWorld
import de.needanapp.design.theme.DesignStyle
import de.needanapp.design.theme.Device
import de.needanapp.design.theme.FONT_FOLDER
import de.needanapp.design.theme.LightWorld
import de.needanapp.design.theme.LocalIsDarkTheme
import de.needanapp.design.theme.NeedanappDefaults
import de.needanapp.design.theme.PaletteStyle
import de.needanapp.design.theme.SurfaceColors
import de.needanapp.design.theme.ThemeMode
import de.needanapp.design.theme.ThemeSettings
import de.needanapp.design.theme.ThemeSettingsStore
import de.needanapp.design.theme.accentPresets
import de.needanapp.design.theme.activeDarkWorld
import de.needanapp.design.theme.activeLightWorld
import de.needanapp.design.theme.buildColorScheme
import de.needanapp.design.theme.darkWorlds
import de.needanapp.design.theme.designColors
import de.needanapp.design.theme.isMyzelDeviceWorld
import de.needanapp.design.theme.lightWorlds
import de.needanapp.design.theme.styleAccent
import de.needanapp.design.theme.worldAccent
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale
import kotlin.math.roundToInt

private const val LANGUAGE_SECTION = "appearance_language"
private const val FONT_SECTION = "appearance_font"
private val SECTION_IDS = listOf(
    "appearance_mode", "appearance_accent", "appearance_style", "appearance_surfaces", "appearance_effects", "appearance_size",
    FONT_SECTION, "appearance_behavior", LANGUAGE_SECTION,
)

/**
 * The complete "Appearance" settings of the package, ready to be shown as a screen: mode, pure black, brightness of the
 * dark theme, accent source with presets and custom colors, palette style, surface tint, background gradient, card
 * brightness, blur, pill transparency, reduce effects, UI scale, corner radius, custom font, exclusive sections and
 * language.
 * Changes are stored right away and apply to [de.needanapp.design.theme.NeedanappTheme]. The Myzel style has fixed
 * corners and fonts, so it leaves those out, and offers its own color worlds and signal colors first.
 *
 * @param languages the app's languages as BCP 47 tags; "System" is always offered in addition. With at most one
 *   language there is nothing to choose, so the language section is left out.
 * @param defaultAccent the app's own accent, shown for "Standard"; the Myzel style has its own (orange)
 * @param styles the styles the app lets choose from ([ThemeSettings.design]); with fewer than two there is no choice
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppearanceSettings(
    settings: ThemeSettings,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    listState: LazyListState = rememberLazyListState(),
    nestedScrollConnection: NestedScrollConnection? = null,
    languages: List<String> = listOf("en", "de"),
    defaultAccent: Color = NeedanappDefaults.Accent,
    styles: List<DesignStyle> = emptyList(),
) {
    val context = LocalContext.current
    val store = remember { ThemeSettingsStore(context) }
    val scope = rememberCoroutineScope()
    val isMyzel = settings.design == DesignStyle.MYZEL
    val sectionIds = SECTION_IDS.filter { (languages.size > 1 || it != LANGUAGE_SECTION) && !(isMyzel && it == FONT_SECTION) }
    val sections = rememberSectionState(sectionIds)
    // all sections form one block
    fun block(id: String) = RowPosition.of(isTop = id == sectionIds.first(), isBottom = id == sectionIds.last())
    fun update(transform: (ThemeSettings) -> ThemeSettings) {
        scope.launch { store.update(transform) }
    }

    var showColorPicker by remember { mutableStateOf(false) }
    // the color the picker starts with when it isn't the custom color, e.g. the app's own one
    var pickerStart by remember { mutableStateOf<Color?>(null) }
    val isDarkShown = LocalIsDarkTheme.current
    val previews = remember(settings, defaultAccent) { WorldPreviews(context, settings, defaultAccent) }
    // slider values move locally and are stored when the finger lifts, so DataStore isn't written on every frame
    var surfaceTint by remember(settings.surfaceTint) { mutableStateOf(settings.surfaceTint) }
    var darkBrightness by remember(settings.darkBrightness) { mutableStateOf(settings.darkBrightness) }
    var cardBrightness by remember(settings.cardBrightness) { mutableStateOf(settings.cardBrightness) }
    var uiScale by remember(settings.uiScale) { mutableStateOf(settings.uiScale) }
    var cornerRadius by remember(settings.cornerRadius) { mutableStateOf(settings.cornerRadius) }
    var pillTransparency by remember(settings.pillTransparency) { mutableStateOf(settings.pillTransparency) }
    val fontPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = importFont(context, uri)
                if (path != null) store.update { it.copy(customFontPath = path) }
            }
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = contentPadding,
        modifier = modifier
            .fillMaxWidth()
            .scrollbar(listState, contentPadding)
            .then(if (nestedScrollConnection != null) Modifier.nestedScroll(nestedScrollConnection) else Modifier),
    ) {
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.End) {
                val allCollapsed = sections.areAllCollapsed(sectionIds)
                if (sections.isExclusive) {
                    // only one section may be open, so there is nothing to expand all at once
                    TextButton(onClick = { sections.collapseAll(sectionIds) }, enabled = !allCollapsed) {
                        Text(stringResource(R.string.nd_collapse_all))
                    }
                } else {
                    TextButton(onClick = { if (allCollapsed) sections.expandAll(sectionIds) else sections.collapseAll(sectionIds) }) {
                        Text(stringResource(if (allCollapsed) R.string.nd_expand_all else R.string.nd_collapse_all))
                    }
                }
            }
        }

        item {
            SettingsGroup(stringResource(R.string.nd_section_mode), id = SECTION_IDS[0], sectionState = sections, icon = Icons.Outlined.DarkMode, block = block(SECTION_IDS[0])) {
                if (styles.size > 1) {
                    row { position ->
                        SegmentedRow(
                            position,
                            stringResource(R.string.nd_design),
                            styles,
                            settings.design,
                            label = { stringResource(it.label) },
                            onSelect = { design -> update { it.copy(design = design) } },
                        )
                    }
                }
                row { position ->
                    SegmentedRow(
                        position,
                        stringResource(R.string.nd_theme_mode),
                        ThemeMode.entries,
                        settings.mode,
                        label = { stringResource(it.label) },
                        onSelect = { mode -> update { it.copy(mode = mode) } },
                    )
                }
                // the world of the theme on screen first: a tap there changes the app at once
                listOf(isDarkShown, !isDarkShown).forEach { isDark ->
                    row { position -> WorldRow(position, settings, isDark, isShown = isDark == isDarkShown, previews, ::update) }
                }
                row { position ->
                    SwitchRow(
                        position,
                        stringResource(R.string.nd_pure_black),
                        settings.pureBlack,
                        onCheckedChange = { value -> update { it.copy(pureBlack = value) } },
                        summary = stringResource(R.string.nd_pure_black_summary),
                        enabled = settings.mode != ThemeMode.LIGHT,
                    )
                }
                row { position ->
                    SliderRow(
                        position,
                        stringResource(R.string.nd_dark_brightness),
                        darkBrightness,
                        onValueChange = { darkBrightness = it },
                        valueRange = 0f..NeedanappDefaults.MAX_DARK_BRIGHTNESS,
                        valueText = stringResource(R.string.nd_percent, (darkBrightness * 100).roundToInt()),
                        steps = 9,
                        summary = stringResource(if (settings.pureBlack) R.string.nd_dark_brightness_pure_black else R.string.nd_dark_brightness_summary),
                        enabled = settings.mode != ThemeMode.LIGHT && !settings.pureBlack,
                        onValueChangeFinished = { update { it.copy(darkBrightness = darkBrightness) } },
                    )
                }
            }
        }

        item {
            SettingsGroup(stringResource(R.string.nd_section_accent), id = SECTION_IDS[1], sectionState = sections, icon = Icons.Outlined.Palette, block = block(SECTION_IDS[1])) {
                row { position ->
                    ContentRow(position, stringResource(R.string.nd_accent_source)) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AccentSource.entries.forEach { source ->
                                SelectChip(stringResource(source.label), settings.accentSource == source) {
                                    update { it.copy(accentSource = source) }
                                }
                            }
                        }
                    }
                }
                when (settings.accentSource) {
                    AccentSource.DEFAULT -> row { position ->
                        // the color world on screen may bring its own accent instead of the app's one
                        val worldAccent = settings.worldAccent(isDarkShown)?.color
                        val styleAccent = settings.styleAccent(defaultAccent)
                        val shownAccent = worldAccent ?: styleAccent
                        // the app's own color can't be changed itself: a tap starts a custom color from it
                        val pickFromDefault = {
                            pickerStart = shownAccent
                            showColorPicker = true
                        }
                        val summary = if (worldAccent != null) {
                            val world = if (isDarkShown) settings.activeDarkWorld.label else settings.activeLightWorld.label
                            stringResource(R.string.nd_accent_world_summary, stringResource(world))
                        } else {
                            stringResource(R.string.nd_accent_default_summary, styleAccent.toHex())
                        }
                        ListRow(
                            position,
                            stringResource(R.string.nd_accent_default),
                            summary = summary,
                            onClick = pickFromDefault,
                            trailing = {
                                ColorSwatch(shownAccent, stringResource(R.string.nd_accent_default), selected = true, onClick = pickFromDefault, size = 40.dp)
                            },
                        )
                    }

                    AccentSource.DYNAMIC -> row { position ->
                        ListRow(position, stringResource(R.string.nd_accent_dynamic), summary = stringResource(R.string.nd_accent_dynamic_summary))
                    }

                    AccentSource.PRESET -> row { position ->
                        ContentRow(position, null) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                accentPresets(settings.design).forEach { preset ->
                                    ColorSwatch(preset.color, stringResource(preset.label), settings.preset == preset, onClick = {
                                        update { it.copy(preset = preset) }
                                    })
                                }
                            }
                            Text(
                                stringResource(settings.preset.label),
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }

                    AccentSource.CUSTOM -> {
                        row { position ->
                            ListRow(
                                position,
                                stringResource(R.string.nd_accent_pick),
                                summary = settings.customAccent.toHex(),
                                onClick = { showColorPicker = true },
                                trailing = { ColorSwatch(settings.customAccent, settings.customAccent.toHex(), selected = true, onClick = { showColorPicker = true }, size = 40.dp) },
                            )
                        }
                        row { position ->
                            ContentRow(position, stringResource(R.string.nd_saved_colors)) {
                                if (settings.savedColors.isEmpty()) {
                                    Text(stringResource(R.string.nd_saved_colors_empty))
                                } else {
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        settings.savedColors.forEach { color ->
                                            ColorSwatch(
                                                color,
                                                color.toHex(),
                                                selected = color == settings.customAccent,
                                                onClick = { update { it.copy(customAccent = color) } },
                                                onLongClick = { update { it.copy(savedColors = it.savedColors - color) } },
                                            )
                                        }
                                    }
                                    Text(stringResource(R.string.nd_saved_colors_hint), modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            SettingsGroup(stringResource(R.string.nd_section_style), id = SECTION_IDS[2], sectionState = sections, icon = Icons.Outlined.Style, block = block(SECTION_IDS[2])) {
                row { position ->
                    ContentRow(position, stringResource(R.string.nd_palette_style)) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            PaletteStyle.entries.forEach { style ->
                                SelectChip(stringResource(style.label), settings.paletteStyle == style) {
                                    update { it.copy(paletteStyle = style) }
                                }
                            }
                        }
                        if (settings.isMyzelDeviceWorld(isDarkShown)) {
                            Text(
                                stringResource(R.string.nd_palette_style_device),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                    }
                }
            }
        }

        item {
            SettingsGroup(stringResource(R.string.nd_section_surfaces), id = SECTION_IDS[3], sectionState = sections, icon = Icons.Outlined.Layers, block = block(SECTION_IDS[3])) {
                row { position ->
                    SliderRow(
                        position,
                        stringResource(R.string.nd_surface_tint),
                        surfaceTint,
                        onValueChange = { surfaceTint = it },
                        valueRange = 0f..NeedanappDefaults.MAX_SURFACE_TINT,
                        valueText = "%.1f".format(Locale.getDefault(), surfaceTint),
                        steps = 19,
                        summary = stringResource(R.string.nd_surface_tint_summary),
                        onValueChangeFinished = { update { it.copy(surfaceTint = surfaceTint) } },
                    )
                }
                row { position ->
                    // pure black has no glow in dark mode; in "System" it still counts while the system is light
                    val pureBlackDark = settings.pureBlack && settings.mode != ThemeMode.LIGHT
                    SwitchRow(
                        position,
                        stringResource(R.string.nd_background_glow),
                        settings.backgroundGlow && !(settings.pureBlack && settings.mode == ThemeMode.DARK),
                        onCheckedChange = { value -> update { it.copy(backgroundGlow = value) } },
                        summary = stringResource(if (pureBlackDark) R.string.nd_background_glow_pure_black else R.string.nd_background_glow_summary),
                        enabled = !(settings.pureBlack && settings.mode == ThemeMode.DARK),
                    )
                }
                row { position ->
                    val percent = (cardBrightness * 100).roundToInt()
                    SliderRow(
                        position,
                        stringResource(R.string.nd_card_brightness),
                        cardBrightness,
                        onValueChange = { cardBrightness = it },
                        valueRange = -NeedanappDefaults.MAX_CARD_BRIGHTNESS..NeedanappDefaults.MAX_CARD_BRIGHTNESS,
                        valueText = stringResource(if (percent == 0) R.string.nd_percent else R.string.nd_signed_percent, percent),
                        steps = 19,
                        summary = stringResource(R.string.nd_card_brightness_summary),
                        onValueChangeFinished = { update { it.copy(cardBrightness = cardBrightness) } },
                    )
                }
            }
        }

        item {
            SettingsGroup(stringResource(R.string.nd_section_effects), id = SECTION_IDS[4], sectionState = sections, icon = Icons.Outlined.AutoAwesome, block = block(SECTION_IDS[4])) {
                row { position ->
                    SwitchRow(
                        position,
                        stringResource(R.string.nd_progressive_blur),
                        settings.progressiveBlur && !settings.reduceEffects,
                        onCheckedChange = { value -> update { it.copy(progressiveBlur = value) } },
                        summary = stringResource(R.string.nd_progressive_blur_summary),
                        enabled = !settings.reduceEffects,
                    )
                }
                row { position ->
                    val blurOn = settings.progressiveBlur && !settings.reduceEffects
                    SliderRow(
                        position,
                        stringResource(R.string.nd_pill_transparency),
                        pillTransparency,
                        onValueChange = { pillTransparency = it },
                        valueRange = 0f..NeedanappDefaults.MAX_PILL_TRANSPARENCY,
                        valueText = stringResource(R.string.nd_percent, (pillTransparency * 100).roundToInt()),
                        steps = 15,
                        summary = stringResource(if (blurOn) R.string.nd_pill_transparency_summary else R.string.nd_pill_transparency_needs_blur),
                        enabled = blurOn,
                        onValueChangeFinished = { update { it.copy(pillTransparency = pillTransparency) } },
                    )
                }
                row { position ->
                    SwitchRow(
                        position,
                        stringResource(R.string.nd_reduce_effects),
                        settings.reduceEffects,
                        onCheckedChange = { value -> update { it.copy(reduceEffects = value) } },
                        summary = stringResource(R.string.nd_reduce_effects_summary),
                    )
                }
            }
        }

        item {
            SettingsGroup(stringResource(R.string.nd_section_size), id = SECTION_IDS[5], sectionState = sections, icon = Icons.Outlined.FormatSize, block = block(SECTION_IDS[5])) {
                row { position ->
                    SliderRow(
                        position,
                        stringResource(R.string.nd_ui_scale),
                        uiScale,
                        onValueChange = { uiScale = it },
                        valueRange = NeedanappDefaults.MIN_UI_SCALE..NeedanappDefaults.MAX_UI_SCALE,
                        valueText = stringResource(R.string.nd_percent, (uiScale * 100).roundToInt()),
                        steps = 5,
                        onValueChangeFinished = { update { it.copy(uiScale = uiScale) } },
                    )
                }
                if (!isMyzel) row { position ->
                    SliderRow(
                        position,
                        stringResource(R.string.nd_corner_radius),
                        cornerRadius,
                        onValueChange = { cornerRadius = it },
                        valueRange = NeedanappDefaults.MIN_CORNER_RADIUS..NeedanappDefaults.MAX_CORNER_RADIUS,
                        valueText = stringResource(R.string.nd_dp, cornerRadius.roundToInt()),
                        steps = 7,
                        onValueChangeFinished = { update { it.copy(cornerRadius = cornerRadius) } },
                    )
                }
            }
        }

        if (!isMyzel) item {
            SettingsGroup(stringResource(R.string.nd_section_font), id = FONT_SECTION, sectionState = sections, icon = Icons.Outlined.FontDownload, block = block(FONT_SECTION)) {
                row { position ->
                    ListRow(
                        position,
                        stringResource(R.string.nd_custom_font),
                        summary = settings.customFontPath?.let { File(it).name } ?: stringResource(R.string.nd_custom_font_none),
                        onClick = { fontPicker.launch(FONT_MIME_TYPES) },
                        trailing = { Icon(Icons.Filled.Add, contentDescription = null) },
                    )
                }
                if (settings.customFontPath != null) {
                    row { position ->
                        ListRow(position, stringResource(R.string.nd_custom_font_reset), onClick = {
                            settings.customFontPath?.let { File(it).delete() }
                            update { it.copy(customFontPath = null) }
                        })
                    }
                }
            }
        }

        item {
            SettingsGroup(stringResource(R.string.nd_section_behavior), id = SECTION_IDS[7], sectionState = sections, icon = Icons.Outlined.TouchApp, block = block(SECTION_IDS[7])) {
                row { position ->
                    SwitchRow(
                        position,
                        stringResource(R.string.nd_exclusive_sections),
                        settings.exclusiveSections,
                        onCheckedChange = { value -> update { it.copy(exclusiveSections = value) } },
                        summary = stringResource(R.string.nd_exclusive_sections_summary),
                    )
                }
            }
        }

        if (languages.size > 1) item {
            SettingsGroup(stringResource(R.string.nd_section_language), id = LANGUAGE_SECTION, sectionState = sections, icon = Icons.Outlined.Language, block = block(LANGUAGE_SECTION)) {
                val current = AppCompatDelegate.getApplicationLocales().toLanguageTags()
                (listOf("") + languages).forEach { tag ->
                    row { position ->
                        val label = if (tag.isEmpty()) stringResource(R.string.nd_language_system) else displayName(tag)
                        ListRow(position, label, onClick = {
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
                        }, trailing = {
                            RadioButton(selected = current == tag || (tag.isNotEmpty() && current.startsWith(tag)), onClick = null)
                        })
                    }
                }
            }
        }
    }

    if (showColorPicker) {
        OptionsSheet(onDismiss = {
            showColorPicker = false
            pickerStart = null
        }, title = stringResource(R.string.nd_accent_pick), skipPartiallyExpanded = true) {
            ColorPicker(pickerStart ?: settings.customAccent, onPick = { color ->
                showColorPicker = false
                pickerStart = null
                update {
                    val saved = (listOf(color) + it.savedColors.filter { saved -> saved != color }).take(NeedanappDefaults.MAX_SAVED_COLORS)
                    it.copy(accentSource = AccentSource.CUSTOM, customAccent = color, savedColors = saved)
                }
            })
        }
    }
}

/**
 * What the tiles of the color worlds show: the tinted look of today's settings and the accent, for light and dark; the
 * accent as it is without a world that brings its own.
 */
internal class WorldPreviews(context: Context, settings: ThemeSettings, defaultAccent: Color) {
    private val tinted = settings.copy(
        lightWorld = LightWorld.TINTED,
        darkWorld = DarkWorld.TINTED,
        myzelLightWorld = LightWorld.TINTED,
        myzelDarkWorld = DarkWorld.TINTED,
    )
    private val isMyzel = settings.design == DesignStyle.MYZEL
    private val light = buildColorScheme(context, tinted, defaultAccent, isDark = false)
    private val dark = buildColorScheme(context, tinted, defaultAccent, isDark = true)
    private val lightCard = if (isMyzel) light.surfaceContainer else designColors(light, isDark = false).card
    private val darkCard = if (isMyzel) dark.surfaceContainer else designColors(dark, isDark = true, pureBlack = settings.pureBlack).card

    fun accent(isDark: Boolean) = if (isDark) dark.primary else light.primary

    fun tinted(isDark: Boolean): SurfaceColors {
        val scheme = if (isDark) dark else light
        return SurfaceColors(
            background = scheme.background,
            card = if (isDark) darkCard else lightCard,
            raised = scheme.surfaceContainerHigh,
            line = scheme.outlineVariant,
            text = scheme.onSurface,
            textVariant = scheme.onSurfaceVariant,
        )
    }
}

/** The color worlds of the light or the dark theme; [isShown] = that theme is on screen now. */
@Composable
private fun WorldRow(
    position: RowPosition,
    settings: ThemeSettings,
    isDark: Boolean,
    isShown: Boolean,
    previews: WorldPreviews,
    update: ((ThemeSettings) -> ThemeSettings) -> Unit,
) {
    val (choices, selected) = worldChoices(settings, isDark, previews)
    ContentRow(position, stringResource(if (isDark) R.string.nd_world_dark else R.string.nd_world_light)) {
        val hint = when {
            isDark && settings.pureBlack -> R.string.nd_world_pure_black
            isShown -> R.string.nd_world_shown
            isDark -> R.string.nd_world_dark_hidden
            else -> R.string.nd_world_light_hidden
        }
        Text(
            stringResource(hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 10.dp),
        )
        ColorWorldPicker(choices, selected) { index -> update { it.withWorldAt(isDark, index) } }
    }
}

/** The color worlds of the light or the dark theme in the active style as tiles show them, and which one is chosen. */
@Composable
internal fun worldChoices(settings: ThemeSettings, isDark: Boolean, previews: WorldPreviews): Pair<List<WorldChoice>, Int> {
    val style = settings.design
    val darkWorlds = darkWorlds(style)
    val lightWorlds = lightWorlds(style)
    val labels = if (isDark) darkWorlds.map { it.label } else lightWorlds.map { it.label }
    // the Myzel style draws its graphite with the colors of a device
    val colors = if (isDark) darkWorlds.map { if (style == DesignStyle.MYZEL && it == DarkWorld.GRAPHITE) MyzelGraphite else it.colors } else lightWorlds.map { it.colors }
    val choices = labels.zip(colors).map { (label, world) ->
        val accent = world?.accent?.color?.takeIf { settings.accentSource == AccentSource.DEFAULT } ?: previews.accent(isDark)
        WorldChoice(stringResource(label), world ?: previews.tinted(isDark), accent)
    }
    val selected = if (isDark) darkWorlds.indexOf(settings.activeDarkWorld) else lightWorlds.indexOf(settings.activeLightWorld)
    return choices to selected
}

/** These settings with the world at [index] of [worldChoices] chosen for the light or the dark theme of the active style. */
internal fun ThemeSettings.withWorldAt(isDark: Boolean, index: Int): ThemeSettings = when {
    design == DesignStyle.MYZEL && isDark -> copy(myzelDarkWorld = darkWorlds(design)[index])
    design == DesignStyle.MYZEL -> copy(myzelLightWorld = lightWorlds(design)[index])
    isDark -> copy(darkWorld = darkWorlds(design)[index])
    else -> copy(lightWorld = lightWorlds(design)[index])
}

/** The graphite of the Myzel style as its tile shows it. */
private val MyzelGraphite = SurfaceColors(Device.Night, Device.NightPanel, Device.NightKey, Device.NightBorder, Device.NightInk, Device.NightLabel)

@Composable
private fun SelectChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.padding(0.dp)) }
        } else {
            null
        },
        colors = FilterChipDefaults.filterChipColors(),
    )
}

/** The language's own name, e.g. "Deutsch" for "de". */
private fun displayName(tag: String): String {
    val locale = Locale.forLanguageTag(tag)
    return locale.getDisplayName(locale).replaceFirstChar { it.titlecase(locale) }
}

private val FONT_MIME_TYPES = arrayOf("font/*", "application/x-font-ttf", "application/x-font-otf", "application/font-sfnt", "application/octet-stream")

/** Copies the font behind [uri] into the app's files and returns its path, null if it isn't a usable font. */
private suspend fun importFont(context: Context, uri: Uri): String? = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
    val folder = File(context.filesDir, FONT_FOLDER).apply { mkdirs() }
    val name = uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.endsWith(".ttf", true) || it.endsWith(".otf", true) } ?: "custom.ttf"
    val target = File(folder, name)
    try {
        context.contentResolver.openInputStream(uri)?.use { input -> target.outputStream().use { input.copyTo(it) } } ?: return@withContext null
        // createFromFile throws for broken files
        Typeface.createFromFile(target)
        folder.listFiles()?.filter { it != target }?.forEach { it.delete() }
        target.absolutePath
    } catch (e: Exception) {
        target.delete()
        null
    }
}

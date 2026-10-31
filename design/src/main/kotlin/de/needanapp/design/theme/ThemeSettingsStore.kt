package de.needanapp.design.theme

import android.content.Context
import android.graphics.Typeface
import android.util.Base64
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

private val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "needanapp_theme")

/** Where imported fonts are kept, inside the app's files. */
internal const val FONT_FOLDER = "needanapp/fonts"

/**
 * Stores [ThemeSettings] and the open sections of grouped settings in a DataStore of the package. Values that were
 * never stored take those of [defaults].
 */
class ThemeSettingsStore(context: Context) {
    private val appContext = context.applicationContext
    private val dataStore = appContext.themeDataStore

    val settings: Flow<ThemeSettings> = dataStore.data.map { prefs -> prefs.toSettings().also { cached = it } }

    /**
     * The settings this process read last, null before the first read. As the initial value for collecting [settings]
     * it lets the first frame show the app instead of waiting for the read, also when an activity is recreated;
     * [preload] reads them early.
     */
    val cachedSettings: ThemeSettings? get() = cached

    /**
     * Reads the settings in the background, e.g. in `Application.onCreate`, so [cachedSettings] holds them by the time
     * the first screen is composed.
     */
    fun preload() {
        preloadScope.launch { runCatching { settings.first() } }
    }

    /** The section opened last on each screen (see [de.needanapp.design.components.rememberSectionState]). */
    val openSections: Flow<Set<String>> = dataStore.data.map { it[Keys.OPEN_SECTIONS].orEmpty() }

    suspend fun update(transform: (ThemeSettings) -> ThemeSettings) {
        dataStore.edit { prefs -> prefs.write(transform(prefs.toSettings())) }
    }

    /** Remembers [lastOpen] as the open section of the screen made of [screenIds]; null when all of them are closed. */
    suspend fun rememberOpenSection(screenIds: Collection<String>, lastOpen: String?) {
        dataStore.edit { prefs ->
            prefs[Keys.OPEN_SECTIONS] = prefs[Keys.OPEN_SECTIONS].orEmpty() - screenIds.toSet() + listOfNotNull(lastOpen)
            // until 0.16 every collapsed and expanded section was kept
            prefs.remove(Keys.OLD_COLLAPSED_SECTIONS)
            prefs.remove(Keys.OLD_EXPANDED_SECTIONS)
        }
    }

    /**
     * The appearance as JSON for an app's backup: all [ThemeSettings], an imported font (as Base64) and the app's own
     * language. Which sections are open stays out, that belongs to the device. The app decides how the backup is
     * stored and protected; [importBackup] reads it back.
     */
    suspend fun exportBackup(): JSONObject {
        val current = settings.first()
        val json = JSONObject()
            .put(Backup.FORMAT, Backup.FORMAT_VERSION)
            .put(Backup.LOOK, CURRENT_LOOK)
            .put(Backup.DESIGN, current.design.name)
            .put(Backup.MODE, current.mode.name)
            .put(Backup.PURE_BLACK, current.pureBlack)
            .put(Backup.DARK_BRIGHTNESS, current.darkBrightness.toDouble())
            .put(Backup.ACCENT_SOURCE, current.accentSource.name)
            .put(Backup.CUSTOM_ACCENT, current.customAccent.toArgb())
            .put(Backup.SAVED_COLORS, JSONArray(current.savedColors.map { it.toArgb() }))
            .put(Backup.PRESET, current.preset.name)
            .put(Backup.PALETTE_STYLE, current.paletteStyle.name)
            .put(Backup.LIGHT_WORLD, current.lightWorld.name)
            .put(Backup.DARK_WORLD, current.darkWorld.name)
            .put(Backup.MYZEL_LIGHT_WORLD, current.myzelLightWorld.name)
            .put(Backup.MYZEL_DARK_WORLD, current.myzelDarkWorld.name)
            .put(Backup.SURFACE_TINT, current.surfaceTint.toDouble())
            .put(Backup.BACKGROUND_GLOW, current.backgroundGlow)
            .put(Backup.CARD_BRIGHTNESS, current.cardBrightness.toDouble())
            .put(Backup.BLUR, current.progressiveBlur)
            .put(Backup.PILL_TRANSPARENCY, current.pillTransparency.toDouble())
            .put(Backup.REDUCE_EFFECTS, current.reduceEffects)
            .put(Backup.UI_SCALE, current.uiScale.toDouble())
            .put(Backup.CORNER_RADIUS, current.cornerRadius.toDouble())
            .put(Backup.EXCLUSIVE_SECTIONS, current.exclusiveSections)
            .put(Backup.LANGUAGE, withContext(Dispatchers.Main) { AppCompatDelegate.getApplicationLocales().toLanguageTags() })
        val font = current.customFontPath?.let(::File)?.takeIf { it.isFile && it.length() <= Backup.MAX_FONT_SIZE }
        if (font != null) {
            val data = withContext(Dispatchers.IO) { font.readBytes() }
            json.put(Backup.FONT, JSONObject().put(Backup.FONT_NAME, font.name).put(Backup.FONT_DATA, Base64.encodeToString(data, Base64.NO_WRAP)))
        }
        return json
    }

    /**
     * Replaces the appearance with one saved by [exportBackup]; values it lacks or doesn't know get their defaults.
     * A font in it is stored like an imported one. The language changes too, which recreates the app's activities.
     */
    suspend fun importBackup(json: JSONObject) {
        val fontPath = json.optJSONObject(Backup.FONT)?.let { restoreFont(it) }
        val look = json.optInt(Backup.LOOK, 0)
        val oldLook = look < CARD_LOOK
        val restored = ThemeSettings(
            design = designValue(json.optString(Backup.DESIGN)),
            mode = enumValue(json.optString(Backup.MODE), defaults.mode),
            pureBlack = json.optBoolean(Backup.PURE_BLACK, defaults.pureBlack),
            darkBrightness = json.optFloat(Backup.DARK_BRIGHTNESS, defaults.darkBrightness, 0f, NeedanappDefaults.MAX_DARK_BRIGHTNESS),
            accentSource = enumValue(json.optString(Backup.ACCENT_SOURCE), defaults.accentSource),
            customAccent = if (json.has(Backup.CUSTOM_ACCENT)) Color(json.getInt(Backup.CUSTOM_ACCENT)) else defaults.customAccent,
            savedColors = json.optJSONArray(Backup.SAVED_COLORS)?.let { colors -> (0 until colors.length()).map { Color(colors.getInt(it)) } }
                ?: defaults.savedColors,
            preset = enumValue(json.optString(Backup.PRESET), defaults.preset),
            paletteStyle = enumValue(json.optString(Backup.PALETTE_STYLE), defaults.paletteStyle).migrated(oldLook),
            lightWorld = enumValue(json.optString(Backup.LIGHT_WORLD), defaults.lightWorld).migratedWorld(look, defaults.lightWorld),
            darkWorld = enumValue(json.optString(Backup.DARK_WORLD), defaults.darkWorld).migratedWorld(look, defaults.darkWorld),
            myzelLightWorld = enumValue(json.optString(Backup.MYZEL_LIGHT_WORLD), defaults.myzelLightWorld),
            myzelDarkWorld = enumValue(json.optString(Backup.MYZEL_DARK_WORLD), defaults.myzelDarkWorld),
            surfaceTint = json.optFloat(Backup.SURFACE_TINT, defaults.surfaceTint, 0f, NeedanappDefaults.MAX_SURFACE_TINT),
            backgroundGlow = json.optBoolean(Backup.BACKGROUND_GLOW, defaults.backgroundGlow).migratedGlow(look),
            cardBrightness = json.optFloat(Backup.CARD_BRIGHTNESS, defaults.cardBrightness, -NeedanappDefaults.MAX_CARD_BRIGHTNESS, NeedanappDefaults.MAX_CARD_BRIGHTNESS),
            progressiveBlur = json.optBoolean(Backup.BLUR, defaults.progressiveBlur),
            pillTransparency = json.optFloat(Backup.PILL_TRANSPARENCY, defaults.pillTransparency, 0f, NeedanappDefaults.MAX_PILL_TRANSPARENCY),
            reduceEffects = json.optBoolean(Backup.REDUCE_EFFECTS, defaults.reduceEffects),
            uiScale = json.optFloat(Backup.UI_SCALE, defaults.uiScale, NeedanappDefaults.MIN_UI_SCALE, NeedanappDefaults.MAX_UI_SCALE),
            cornerRadius = json.optFloat(Backup.CORNER_RADIUS, defaults.cornerRadius, NeedanappDefaults.MIN_CORNER_RADIUS, NeedanappDefaults.MAX_CORNER_RADIUS)
                .migratedRadius(oldLook),
            customFontPath = fontPath,
            exclusiveSections = json.optBoolean(Backup.EXCLUSIVE_SECTIONS, defaults.exclusiveSections).migratedExclusive(look),
        )
        dataStore.edit { prefs -> prefs.write(restored) }
        if (fontPath == null) withContext(Dispatchers.IO) { fontFolder().listFiles()?.forEach { it.delete() } }
        val language = json.optString(Backup.LANGUAGE)
        withContext(Dispatchers.Main) { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language)) }
    }

    /** Writes the backed-up font next to imported ones, replacing them; null if it isn't a usable font. */
    private suspend fun restoreFont(font: JSONObject): String? = withContext(Dispatchers.IO) {
        val name = font.optString(Backup.FONT_NAME).substringAfterLast('/').takeIf { it.endsWith(".ttf", true) || it.endsWith(".otf", true) } ?: "custom.ttf"
        val data = runCatching { Base64.decode(font.optString(Backup.FONT_DATA), Base64.NO_WRAP) }.getOrNull()
        if (data == null || data.isEmpty() || data.size > Backup.MAX_FONT_SIZE) return@withContext null
        val folder = fontFolder()
        val target = File(folder, name)
        try {
            target.writeBytes(data)
            // createFromFile throws for broken files
            Typeface.createFromFile(target)
            folder.listFiles()?.filter { it != target }?.forEach { it.delete() }
            target.absolutePath
        } catch (e: Exception) {
            target.delete()
            null
        }
    }

    private fun fontFolder() = File(appContext.filesDir, FONT_FOLDER).apply { mkdirs() }

    private fun JSONObject.optFloat(name: String, default: Float, min: Float, max: Float): Float =
        optDouble(name, default.toDouble()).toFloat().takeIf { !it.isNaN() }?.coerceIn(min, max) ?: default

    private fun Preferences.toSettings(): ThemeSettings {
        val look = this[Keys.LOOK] ?: 0
        val oldLook = look < CARD_LOOK
        return ThemeSettings(
            design = designValue(this[Keys.DESIGN]),
            mode = enumValue(this[Keys.MODE], defaults.mode),
            pureBlack = this[Keys.PURE_BLACK] ?: defaults.pureBlack,
            darkBrightness = this[Keys.DARK_BRIGHTNESS] ?: defaults.darkBrightness,
            accentSource = enumValue(this[Keys.ACCENT_SOURCE], defaults.accentSource),
            customAccent = this[Keys.CUSTOM_ACCENT]?.let { Color(it) } ?: defaults.customAccent,
            savedColors = this[Keys.SAVED_COLORS]?.split(',')?.mapNotNull { it.toIntOrNull()?.let(::Color) } ?: defaults.savedColors,
            preset = enumValue(this[Keys.PRESET], defaults.preset),
            paletteStyle = enumValue(this[Keys.PALETTE_STYLE], defaults.paletteStyle).migrated(oldLook),
            lightWorld = enumValue(this[Keys.LIGHT_WORLD], defaults.lightWorld).migratedWorld(look, defaults.lightWorld),
            darkWorld = enumValue(this[Keys.DARK_WORLD], defaults.darkWorld).migratedWorld(look, defaults.darkWorld),
            myzelLightWorld = enumValue(this[Keys.MYZEL_LIGHT_WORLD], defaults.myzelLightWorld),
            myzelDarkWorld = enumValue(this[Keys.MYZEL_DARK_WORLD], defaults.myzelDarkWorld),
            surfaceTint = this[Keys.SURFACE_TINT] ?: defaults.surfaceTint,
            backgroundGlow = this[Keys.BACKGROUND_GLOW]?.migratedGlow(look) ?: defaults.backgroundGlow,
            cardBrightness = this[Keys.CARD_BRIGHTNESS] ?: defaults.cardBrightness,
            progressiveBlur = this[Keys.BLUR] ?: defaults.progressiveBlur,
            pillTransparency = this[Keys.PILL_TRANSPARENCY] ?: defaults.pillTransparency,
            reduceEffects = this[Keys.REDUCE_EFFECTS] ?: defaults.reduceEffects,
            uiScale = this[Keys.UI_SCALE] ?: defaults.uiScale,
            cornerRadius = this[Keys.CORNER_RADIUS]?.migratedRadius(oldLook) ?: defaults.cornerRadius,
            customFontPath = this[Keys.FONT_PATH],
            exclusiveSections = this[Keys.EXCLUSIVE_SECTIONS]?.migratedExclusive(look) ?: defaults.exclusiveSections,
        )
    }

    private fun MutablePreferences.write(settings: ThemeSettings) {
        this[Keys.LOOK] = CURRENT_LOOK
        this[Keys.DESIGN] = settings.design.name
        this[Keys.MODE] = settings.mode.name
        this[Keys.PURE_BLACK] = settings.pureBlack
        this[Keys.DARK_BRIGHTNESS] = settings.darkBrightness
        this[Keys.ACCENT_SOURCE] = settings.accentSource.name
        this[Keys.CUSTOM_ACCENT] = settings.customAccent.toArgb()
        this[Keys.SAVED_COLORS] = settings.savedColors.joinToString(",") { it.toArgb().toString() }
        this[Keys.PRESET] = settings.preset.name
        this[Keys.PALETTE_STYLE] = settings.paletteStyle.name
        this[Keys.LIGHT_WORLD] = settings.lightWorld.name
        this[Keys.DARK_WORLD] = settings.darkWorld.name
        this[Keys.MYZEL_LIGHT_WORLD] = settings.myzelLightWorld.name
        this[Keys.MYZEL_DARK_WORLD] = settings.myzelDarkWorld.name
        this[Keys.SURFACE_TINT] = settings.surfaceTint
        this[Keys.BACKGROUND_GLOW] = settings.backgroundGlow
        this[Keys.CARD_BRIGHTNESS] = settings.cardBrightness
        // settings that no longer exist: gradient background (until 0.9.0), background transparency and its blur
        // (0.10.0 to 0.14.0)
        remove(Keys.OLD_GRADIENT)
        remove(Keys.OLD_WALLPAPER_BLUR)
        remove(Keys.OLD_BACKGROUND_BLUR)
        remove(Keys.OLD_BACKGROUND_TRANSPARENCY)
        this[Keys.BLUR] = settings.progressiveBlur
        this[Keys.PILL_TRANSPARENCY] = settings.pillTransparency
        this[Keys.REDUCE_EFFECTS] = settings.reduceEffects
        this[Keys.UI_SCALE] = settings.uiScale
        this[Keys.CORNER_RADIUS] = settings.cornerRadius
        if (settings.customFontPath != null) this[Keys.FONT_PATH] = settings.customFontPath else remove(Keys.FONT_PATH)
        this[Keys.EXCLUSIVE_SECTIONS] = settings.exclusiveSections
    }

    /**
     * Until 0.19 the defaults were Tonal Spot and 20 dp, and every change of the appearance stored all values. Values
     * saved before the card look (0.20) that are still at those old defaults move to the new defaults once; what the
     * user chooses afterwards is kept.
     */
    private fun PaletteStyle.migrated(oldLook: Boolean) =
        if (oldLook && this == PaletteStyle.TONAL_SPOT) ThemeSettings().paletteStyle else this

    /** The stored style; "DEVICE" is the name of the Myzel style before it came to the main line of the package. */
    private fun designValue(name: String?): DesignStyle =
        if (name == OLD_DEVICE_DESIGN) DesignStyle.MYZEL else enumValue(name, defaults.design)

    private fun Float.migratedRadius(oldLook: Boolean) =
        if (oldLook && this == OLD_CORNER_RADIUS) NeedanappDefaults.CornerRadius else this

    /**
     * Until 0.26 "only one section open" was off by default, and every change stored it. Saved before it became the
     * default, "off" moves to "on" once; what the user chooses afterwards is kept.
     */
    private fun Boolean.migratedExclusive(look: Int) = this || look < EXCLUSIVE_LOOK

    /**
     * Until 0.27 the background gradient was on by default, and every change stored it. Saved before the plain
     * background became the default, "on" moves to "off" once; what the user chooses afterwards is kept.
     */
    private fun Boolean.migratedGlow(look: Int) = this && look >= SOLID_LOOK

    /**
     * In 0.29 the color worlds were "tinted" by default, and every change stored that. Saved before Cloud and Nord Night
     * became the defaults, "tinted" moves to the new default once; what the user chooses afterwards is kept.
     */
    private fun <T : Enum<T>> T.migratedWorld(look: Int, default: T) = if (look < WORLD_LOOK && name == TINTED) default else this

    private inline fun <reified T : Enum<T>> enumValue(name: String?, default: T): T =
        name?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: default

    private object Backup {
        const val FORMAT = "format"
        const val FORMAT_VERSION = 1
        const val LOOK = "look"
        const val DESIGN = "design"
        const val MODE = "mode"
        const val PURE_BLACK = "pureBlack"
        const val DARK_BRIGHTNESS = "darkBrightness"
        const val ACCENT_SOURCE = "accentSource"
        const val CUSTOM_ACCENT = "customAccent"
        const val SAVED_COLORS = "savedColors"
        const val PRESET = "preset"
        const val PALETTE_STYLE = "paletteStyle"
        const val LIGHT_WORLD = "lightWorld"
        const val DARK_WORLD = "darkWorld"
        const val MYZEL_LIGHT_WORLD = "myzelLightWorld"
        const val MYZEL_DARK_WORLD = "myzelDarkWorld"
        const val SURFACE_TINT = "surfaceTint"
        const val BACKGROUND_GLOW = "backgroundGlow"
        const val CARD_BRIGHTNESS = "cardBrightness"
        const val BLUR = "progressiveBlur"
        const val PILL_TRANSPARENCY = "pillTransparency"
        const val REDUCE_EFFECTS = "reduceEffects"
        const val UI_SCALE = "uiScale"
        const val CORNER_RADIUS = "cornerRadius"
        const val EXCLUSIVE_SECTIONS = "exclusiveSections"
        const val LANGUAGE = "language"
        const val FONT = "font"
        const val FONT_NAME = "name"
        const val FONT_DATA = "data"
        const val MAX_FONT_SIZE = 20L * 1024 * 1024
    }

    private object Keys {
        val DESIGN = stringPreferencesKey("design")
        val MODE = stringPreferencesKey("mode")
        val PURE_BLACK = booleanPreferencesKey("pure_black")
        val DARK_BRIGHTNESS = floatPreferencesKey("dark_brightness")
        val ACCENT_SOURCE = stringPreferencesKey("accent_source")
        val CUSTOM_ACCENT = intPreferencesKey("custom_accent")
        val SAVED_COLORS = stringPreferencesKey("saved_colors")
        val PRESET = stringPreferencesKey("preset")
        val PALETTE_STYLE = stringPreferencesKey("palette_style")
        val LIGHT_WORLD = stringPreferencesKey("light_world")
        val DARK_WORLD = stringPreferencesKey("dark_world")
        val MYZEL_LIGHT_WORLD = stringPreferencesKey("myzel_light_world")
        val MYZEL_DARK_WORLD = stringPreferencesKey("myzel_dark_world")
        val SURFACE_TINT = floatPreferencesKey("surface_tint")

        // not "gradient_background": that one was the plain gradient until 0.9.0 and is removed when settings are saved
        val BACKGROUND_GLOW = booleanPreferencesKey("background_glow")
        val CARD_BRIGHTNESS = floatPreferencesKey("card_brightness")
        val OLD_BACKGROUND_TRANSPARENCY = floatPreferencesKey("background_transparency")
        val OLD_BACKGROUND_BLUR = booleanPreferencesKey("background_blur")
        val OLD_WALLPAPER_BLUR = booleanPreferencesKey("wallpaper_blur")
        val OLD_GRADIENT = booleanPreferencesKey("gradient_background")
        val BLUR = booleanPreferencesKey("progressive_blur")
        val PILL_TRANSPARENCY = floatPreferencesKey("pill_transparency")
        val REDUCE_EFFECTS = booleanPreferencesKey("reduce_effects")
        val UI_SCALE = floatPreferencesKey("ui_scale")
        val CORNER_RADIUS = floatPreferencesKey("corner_radius")
        val FONT_PATH = stringPreferencesKey("font_path")
        val OPEN_SECTIONS = stringSetPreferencesKey("open_sections")
        val OLD_COLLAPSED_SECTIONS = stringSetPreferencesKey("collapsed_sections")
        val OLD_EXPANDED_SECTIONS = stringSetPreferencesKey("expanded_sections")
        val EXCLUSIVE_SECTIONS = booleanPreferencesKey("exclusive_sections")
        val LOOK = intPreferencesKey("look")
    }

    companion object {
        /**
         * The settings of an app before anything is stored, e.g. the Myzel style for Myzel; [ThemeSettings] by default.
         * Set it once in `Application.onCreate`, before the first read.
         */
        @Volatile
        var defaults: ThemeSettings = ThemeSettings()

        @Volatile
        private var cached: ThemeSettings? = null
        private val preloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /**
         * Version of the defaults the stored values were saved with: 0 before, [CARD_LOOK] since the card look (0.20),
         * [EXCLUSIVE_LOOK] since "only one section open" is on by default (0.27, on the 0.21.x branch 0.21.2),
         * [SOLID_LOOK] since the plain background without gradient is the default (0.28), [WORLD_LOOK] since the color
         * worlds Cloud and Nord Night are the default (0.30).
         */
        private const val CURRENT_LOOK = 4
        private const val CARD_LOOK = 1
        private const val EXCLUSIVE_LOOK = 2
        private const val SOLID_LOOK = 3
        private const val WORLD_LOOK = 4
        private const val TINTED = "TINTED"
        private const val OLD_CORNER_RADIUS = 20f
        private const val OLD_DEVICE_DESIGN = "DEVICE"
    }
}

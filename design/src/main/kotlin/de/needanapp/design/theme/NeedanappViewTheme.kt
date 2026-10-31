package de.needanapp.design.theme

import android.app.Activity
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.DynamicColorsOptions
import de.needanapp.design.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Bridge for screens still built with Views (theme [R.style.Theme_Needanapp]): the same mode, accent and pure black as
 * the Compose theme. Views get Material's content-based scheme of the accent, palette style and surface tint are
 * Compose-only.
 */
object NeedanappViewTheme {
    /** Call in [Activity.onCreate] before `super.onCreate`. Reads the settings synchronously once. */
    fun apply(activity: Activity, defaultAccent: Color = NeedanappDefaults.Accent) {
        val settings = runBlocking { ThemeSettingsStore(activity).settings.first() }
        apply(activity, settings, defaultAccent)
    }

    fun apply(activity: Activity, settings: ThemeSettings, defaultAccent: Color = NeedanappDefaults.Accent) {
        AppCompatDelegate.setDefaultNightMode(
            when (settings.mode) {
                ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            },
        )

        val options = if (settings.accentSource == AccentSource.DYNAMIC) {
            DynamicColorsOptions.Builder().build()
        } else {
            DynamicColorsOptions.Builder().setContentBasedSource(resolveAccent(activity, settings, defaultAccent).toArgb()).build()
        }
        DynamicColors.applyToActivityIfAvailable(activity, options)

        val isSystemDark = activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val isDark = settings.mode == ThemeMode.DARK || settings.mode == ThemeMode.SYSTEM && isSystemDark
        if (isDark && settings.pureBlack) {
            activity.theme.applyStyle(R.style.ThemeOverlay_Needanapp_PureBlack, true)
        }
    }
}

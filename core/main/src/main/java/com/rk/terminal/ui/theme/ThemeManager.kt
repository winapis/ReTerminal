package com.rk.terminal.ui.theme

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.rk.libcommons.isDarkMode
import com.rk.settings.Settings
import com.rk.terminal.R

/**
 * Legacy Kotlin ThemeManager - Now delegates to the new Java ThemeManager.
 * Kept for backward compatibility.
 *
 * @author Aquiles Trindade (trindadedev).
 */
object LegacyThemeManager {

    /**
     * Applies the theme based on user settings.
     * Now delegates to the new Java ThemeManager for unified theme management.
     *
     * @param activity An instance of an Activity.
     */
    fun apply(activity: Activity) {
        // Delegate to the new Java ThemeManager
        val javaThemeManager = ThemeManager.getInstance(activity)
        javaThemeManager.applyTheme(activity)
    }

    /**
     * Returns the current theme.
     *
     * @param ctx The context from which to get the theme.
     */
    fun getCurrentTheme(ctx: Context): Resources.Theme? = ctx.theme
}

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
 * Enhanced theme manager for applying themes universally across the app and terminal.
 * Supports 20 curated modern themes with both UI and terminal color synchronization.
 *
 * @author Aquiles Trindade (trindadedev), Enhanced by AI Assistant
 */
object ThemeManager {

    /**
     * Applies the theme based on user settings.
     * This should be called before setContentView() in activities.
     *
     * @param activity An instance of an Activity.
     */
    fun apply(activity: Activity) {
        val nightMode = Settings.default_night_mode

        // Set theme mode
        if (nightMode != AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }

        // Apply OLED theme if dark mode and OLED setting is enabled
        if (isDarkMode(activity) && Settings.amoled) {
            if (Settings.monet) {
                activity.setTheme(R.style.Theme_Karbon_Oled_Monet)
                return
            }
            activity.setTheme(R.style.Theme_Karbon_Oled)
            return
        }
        
        // Apply Monet (dynamic colors) if enabled and no custom theme is selected
        if (Settings.monet && Settings.color_scheme == 0) {
            DynamicColors.applyToActivityIfAvailable(activity)
        }
    }

    /**
     * Sets the theme and saves the selection in SharedPreferences.
     * This triggers a theme change across the app.
     *
     * @param context The context to save preferences.
     * @param themeId The theme ID to set (0 = default, 1-20 = curated themes).
     */
    fun setTheme(context: Context, themeId: Int) {
        Settings.color_scheme = themeId
        
        // Update terminal colors to match the new theme
        updateTerminalColors(themeId)
    }

    /**
     * Gets the current theme ID.
     *
     * @return The current theme ID.
     */
    fun getCurrentThemeId(): Int {
        return Settings.color_scheme
    }

    /**
     * Gets the display name for the current theme.
     *
     * @return The human-readable theme name.
     */
    fun getCurrentThemeName(): String {
        return ThemeHelper.getThemeName(Settings.color_scheme)
    }

    /**
     * Checks if the current theme is a dark theme.
     *
     * @return True if the current theme is dark, false otherwise.
     */
    fun isCurrentThemeDark(): Boolean {
        return ThemeHelper.isDarkTheme(Settings.color_scheme)
    }

    /**
     * Updates terminal emulator colors to match the selected theme.
     * This ensures consistency between UI and terminal appearance.
     *
     * @param themeId The theme ID to get colors for.
     */
    private fun updateTerminalColors(themeId: Int) {
        val terminalColors = ThemeHelper.getTerminalColors(themeId)
        
        // Update the terminal color scheme
        // This will be called by the terminal when it needs to refresh colors
        try {
            val colorScheme = com.termux.terminal.TerminalColors.COLOR_SCHEME
            // Update the default colors array
            System.arraycopy(terminalColors, 0, colorScheme.mDefaultColors, 0, 
                            minOf(terminalColors.size, colorScheme.mDefaultColors.size))
        } catch (e: Exception) {
            // Silently handle if terminal classes aren't available during theme setting
        }
    }

    /**
     * Gets terminal colors for the current theme.
     * Used by terminal components to synchronize with UI theme.
     *
     * @return Array of ANSI colors for terminal emulator.
     */
    fun getCurrentTerminalColors(): IntArray {
        return ThemeHelper.getTerminalColors(Settings.color_scheme)
    }

    /**
     * Applies theme to activity with proper timing.
     * Call this in onCreate() before setContentView().
     *
     * @param activity The activity to apply theme to.
     */
    fun applyTheme(activity: Activity) {
        apply(activity)
    }

    /**
     * Returns the current theme resources.
     *
     * @param ctx The context from which to get the theme.
     * @return The current theme resources.
     */
    fun getCurrentTheme(ctx: Context): Resources.Theme? = ctx.theme

    /**
     * Gets all available themes grouped by category.
     *
     * @return Map of theme categories to theme lists.
     */
    fun getAllThemes(): Map<String, List<Pair<Int, String>>> {
        return ThemeHelper.getAllThemes()
    }

    /**
     * Checks if a theme ID is valid.
     *
     * @param themeId The theme ID to validate.
     * @return True if the theme ID is valid, false otherwise.
     */
    fun isValidThemeId(themeId: Int): Boolean {
        return themeId in 0..21
    }

    /**
     * Gets the default theme ID based on system dark mode.
     *
     * @param context The context to check dark mode.
     * @return Default theme ID (0 for system default).
     */
    fun getDefaultThemeId(context: Context): Int {
        return 0 // Always return 0 for system default
    }

    /**
     * Resets theme to system default.
     *
     * @param context The context to reset theme for.
     */
    fun resetToDefault(context: Context) {
        setTheme(context, 0)
    }
}

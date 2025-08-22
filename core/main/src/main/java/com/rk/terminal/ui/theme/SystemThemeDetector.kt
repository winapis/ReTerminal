package com.rk.terminal.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.rk.settings.Settings

/**
 * System theme detection and management utilities.
 * Handles automatic theme switching based on system settings.
 */
object SystemThemeDetector {
    
    /**
     * Check if the system is currently in dark mode.
     * 
     * @param context The context to check dark mode for
     * @return True if system is in dark mode, false otherwise
     */
    fun isSystemInDarkMode(context: Context): Boolean {
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            else -> false // Default to light if unknown
        }
    }
    
    /**
     * Check if the app should follow system theme.
     * 
     * @return True if app is set to follow system theme
     */
    fun shouldFollowSystemTheme(): Boolean {
        return Settings.default_night_mode == AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    }
    
    /**
     * Get the appropriate theme based on system settings and user preferences.
     * 
     * @param context The context to check system state
     * @return The theme ID to apply
     */
    fun getSystemAppropriateTheme(context: Context): Int {
        // If user has selected a specific theme, use it
        if (Settings.color_scheme != 0) {
            return Settings.color_scheme
        }
        
        // If following system theme, choose appropriate default
        if (shouldFollowSystemTheme()) {
            return if (isSystemInDarkMode(context)) {
                // Default to One Dark for dark mode
                ThemeHelper.ThemeIds.ONE_DARK
            } else {
                // Default to GitHub Light for light mode
                ThemeHelper.ThemeIds.GITHUB_LIGHT
            }
        }
        
        // Default theme
        return 0
    }
    
    /**
     * Apply theme based on system settings.
     * Call this when the app starts or when system theme changes.
     * 
     * @param context The context to apply theme for
     */
    fun applySystemTheme(context: Context) {
        val themeId = getSystemAppropriateTheme(context)
        if (themeId != Settings.color_scheme) {
            ThemeManager.setTheme(context, themeId)
        }
    }
    
    /**
     * Check if WCAG contrast requirements are met for a theme.
     * 
     * @param themeId The theme ID to check
     * @return True if theme meets accessibility standards
     */
    fun isThemeAccessible(themeId: Int): Boolean {
        val terminalColors = ThemeHelper.getTerminalColors(themeId)
        if (terminalColors.size < 8) return false
        
        val backgroundColor = terminalColors[0]
        val foregroundColor = terminalColors[7]
        
        return calculateContrastRatio(backgroundColor, foregroundColor) >= 4.5
    }
    
    /**
     * Calculate WCAG contrast ratio between two colors.
     * 
     * @param color1 First color as integer
     * @param color2 Second color as integer
     * @return Contrast ratio (1.0 to 21.0)
     */
    private fun calculateContrastRatio(color1: Int, color2: Int): Double {
        val luminance1 = getRelativeLuminance(color1)
        val luminance2 = getRelativeLuminance(color2)
        
        val lighter = maxOf(luminance1, luminance2)
        val darker = minOf(luminance1, luminance2)
        
        return (lighter + 0.05) / (darker + 0.05)
    }
    
    /**
     * Calculate relative luminance of a color according to WCAG.
     * 
     * @param color Color as integer
     * @return Relative luminance (0.0 to 1.0)
     */
    private fun getRelativeLuminance(color: Int): Double {
        val r = ((color shr 16) and 0xFF) / 255.0
        val g = ((color shr 8) and 0xFF) / 255.0
        val b = (color and 0xFF) / 255.0
        
        fun gammaCorrect(c: Double): Double {
            return if (c <= 0.03928) {
                c / 12.92
            } else {
                Math.pow((c + 0.055) / 1.055, 2.4)
            }
        }
        
        val rLin = gammaCorrect(r)
        val gLin = gammaCorrect(g)
        val bLin = gammaCorrect(b)
        
        return 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin
    }
    
    /**
     * Get accessibility information for all themes.
     * 
     * @return Map of theme ID to accessibility status
     */
    fun getThemeAccessibilityInfo(): Map<Int, Boolean> {
        val result = mutableMapOf<Int, Boolean>()
        
        // Check all theme IDs from 1 to 21
        for (themeId in 1..21) {
            result[themeId] = isThemeAccessible(themeId)
        }
        
        return result
    }
    
    /**
     * Get recommended themes based on accessibility and system settings.
     * 
     * @param context The context to check system state
     * @return List of recommended theme IDs
     */
    fun getRecommendedThemes(context: Context): List<Int> {
        val isDark = isSystemInDarkMode(context)
        val accessibleThemes = getThemeAccessibilityInfo()
        
        return if (isDark) {
            // Recommend accessible dark themes
            listOf(
                ThemeHelper.ThemeIds.ONE_DARK,
                ThemeHelper.ThemeIds.DRACULA,
                ThemeHelper.ThemeIds.GITHUB_DARK,
                ThemeHelper.ThemeIds.SOLARIZED_DARK,
                ThemeHelper.ThemeIds.GRUVBOX_DARK
            ).filter { accessibleThemes[it] == true }
        } else {
            // Recommend accessible light themes
            listOf(
                ThemeHelper.ThemeIds.GITHUB_LIGHT,
                ThemeHelper.ThemeIds.SOLARIZED_LIGHT,
                ThemeHelper.ThemeIds.MATERIAL_LIGHT,
                ThemeHelper.ThemeIds.ATOM_ONE_LIGHT,
                ThemeHelper.ThemeIds.GRUVBOX_LIGHT
            ).filter { accessibleThemes[it] == true }
        }
    }
}
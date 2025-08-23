package com.rk.terminal.ui.theme

import androidx.compose.runtime.mutableIntStateOf
import com.rk.settings.SettingsManager

/**
 * Global theme state manager for reactive theme changes
 * Updated to use the new organized settings structure
 */
object ThemeState {
    // Reactive theme state that triggers recomposition
    var currentTheme = mutableIntStateOf(SettingsManager.Appearance.colorScheme)
        private set
    
    /**
     * Update the current theme and trigger recomposition
     */
    fun updateTheme(themeId: Int) {
        SettingsManager.Appearance.colorScheme = themeId
        currentTheme.intValue = themeId
    }
    
    /**
     * Get current theme ID
     */
    fun getCurrentTheme(): Int = currentTheme.intValue
    
    /**
     * Initialize theme state from Settings
     */
    fun initialize() {
        currentTheme.intValue = SettingsManager.Appearance.colorScheme
    }
    
    /**
     * Check if dark mode is enabled based on theme variant setting
     */
    fun isDarkModeEnabled(): Boolean {
        return when (SettingsManager.Appearance.themeVariant) {
            1 -> false // Force light
            2 -> true  // Force dark
            else -> {
                // Auto mode - check if current theme is a dark theme (1-10)
                val themeId = getCurrentTheme()
                themeId in 1..10
            }
        }
    }
    
    /**
     * Check if AMOLED mode is enabled
     */
    fun isAmoledEnabled(): Boolean = SettingsManager.Appearance.amoled
    
    /**
     * Check if dynamic theming (Monet) is enabled
     */
    fun isDynamicThemingEnabled(): Boolean = SettingsManager.Appearance.monet
}
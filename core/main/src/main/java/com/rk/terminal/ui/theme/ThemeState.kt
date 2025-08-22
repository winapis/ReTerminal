package com.rk.terminal.ui.theme

import androidx.compose.runtime.mutableIntStateOf
import com.rk.settings.Settings

/**
 * Global theme state manager for reactive theme changes
 */
object ThemeState {
    // Reactive theme state that triggers recomposition
    var currentTheme = mutableIntStateOf(Settings.color_scheme)
        private set
    
    /**
     * Update the current theme and trigger recomposition
     */
    fun updateTheme(themeId: Int) {
        Settings.color_scheme = themeId
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
        currentTheme.intValue = Settings.color_scheme
    }
}
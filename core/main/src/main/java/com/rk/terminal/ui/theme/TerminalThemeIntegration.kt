package com.rk.terminal.ui.theme

import com.termux.terminal.TerminalColors
import com.termux.terminal.TextStyle

/**
 * Terminal theme integration utility.
 * Synchronizes UI themes with terminal emulator colors.
 */
object TerminalThemeIntegration {
    
    /**
     * Apply theme colors to the terminal color scheme.
     * This should be called when the theme is changed.
     * 
     * @param themeId The theme ID to apply
     */
    fun applyThemeToTerminal(themeId: Int) {
        val terminalColors = ThemeHelper.getTerminalColors(themeId)
        
        try {
            val colorScheme = TerminalColors.COLOR_SCHEME
            
            // Update the first 16 ANSI colors (standard + bright)
            for (i in 0 until minOf(16, terminalColors.size)) {
                colorScheme.mDefaultColors[i] = terminalColors[i]
            }
            
            // Set special colors based on theme
            when (themeId) {
                ThemeHelper.ThemeIds.DRACULA -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFFF8F8F2.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFF282A36.toInt()
                }
                ThemeHelper.ThemeIds.ONE_DARK -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFFABB2BF.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFF282C34.toInt()
                }
                ThemeHelper.ThemeIds.NORD -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFFECEFF4.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFF2E3440.toInt()
                }
                ThemeHelper.ThemeIds.TOKYO_NIGHT -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFFC0CAF5.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFF1A1B26.toInt()
                }
                ThemeHelper.ThemeIds.SOLARIZED_DARK -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFF839496.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFF002B36.toInt()
                }
                ThemeHelper.ThemeIds.MONOKAI_PRO -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFFFCFCFA.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFF2D2A2E.toInt()
                }
                ThemeHelper.ThemeIds.GITHUB_DARK -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFFF0F6FC.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFF0D1117.toInt()
                }
                ThemeHelper.ThemeIds.GRUVBOX_DARK -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFFFBF1C7.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFF282828.toInt()
                }
                ThemeHelper.ThemeIds.CATPPUCCIN_MOCHA -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFFCDD6F4.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFF1E1E2E.toInt()
                }
                ThemeHelper.ThemeIds.COBALT2 -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFFFFFFFF.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFF193549.toInt()
                }
                // Light Themes
                ThemeHelper.ThemeIds.SOLARIZED_LIGHT -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFF657B83.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFFFDF6E3.toInt()
                }
                ThemeHelper.ThemeIds.GITHUB_LIGHT -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFF1F2328.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFFFFFFFF.toInt()
                }
                ThemeHelper.ThemeIds.GRUVBOX_LIGHT -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFF3C3836.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFFFBF1C7.toInt()
                }
                ThemeHelper.ThemeIds.CATPPUCCIN_LATTE -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFF4C4F69.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFFEFF1F5.toInt()
                }
                ThemeHelper.ThemeIds.TOKYO_LIGHT -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFF343B58.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFFD5D6DB.toInt()
                }
                ThemeHelper.ThemeIds.NORD_LIGHT -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFF2E3440.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFFECEFF4.toInt()
                }
                ThemeHelper.ThemeIds.MATERIAL_LIGHT -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFF212121.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFFFFFFFF.toInt()
                }
                ThemeHelper.ThemeIds.ATOM_ONE_LIGHT -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFF383A42.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFFFAFAFA.toInt()
                }
                ThemeHelper.ThemeIds.AYU_LIGHT -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFF5C6773.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFFFAFAFA.toInt()
                }
                ThemeHelper.ThemeIds.PAPERCOLOR_LIGHT -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFF444444.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFFEEEEEE.toInt()
                }
                // Legacy Monokai
                ThemeHelper.ThemeIds.MONOKAI_LEGACY -> {
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND] = 0xFFF8F8F2.toInt()
                    colorScheme.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND] = 0xFF272822.toInt()
                }
                else -> {
                    // Default colors - don't change
                    return
                }
            }
            
            // Automatically set cursor color based on background brightness
            colorScheme.setCursorColorForBackground()
            
        } catch (e: Exception) {
            // Handle gracefully if terminal classes aren't available
            e.printStackTrace()
        }
    }
    
    /**
     * Get the current terminal foreground color.
     * 
     * @return The foreground color as an integer
     */
    fun getCurrentForegroundColor(): Int {
        return try {
            TerminalColors.COLOR_SCHEME.mDefaultColors[TextStyle.COLOR_INDEX_FOREGROUND]
        } catch (e: Exception) {
            0xFFFFFFFF.toInt() // Default white
        }
    }
    
    /**
     * Get the current terminal background color.
     * 
     * @return The background color as an integer
     */
    fun getCurrentBackgroundColor(): Int {
        return try {
            TerminalColors.COLOR_SCHEME.mDefaultColors[TextStyle.COLOR_INDEX_BACKGROUND]
        } catch (e: Exception) {
            0xFF000000.toInt() // Default black
        }
    }
    
    /**
     * Check if terminal colors are synchronized with the UI theme.
     * 
     * @param themeId The theme ID to check against
     * @return True if colors are synchronized, false otherwise
     */
    fun isTerminalSynchronized(themeId: Int): Boolean {
        return try {
            val terminalColors = ThemeHelper.getTerminalColors(themeId)
            val colorScheme = TerminalColors.COLOR_SCHEME
            
            // Check if the first few ANSI colors match
            for (i in 0 until minOf(4, terminalColors.size)) {
                if (colorScheme.mDefaultColors[i] != terminalColors[i]) {
                    return false
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Force refresh of terminal colors.
     * This can be called after theme changes to ensure terminal gets updated.
     */
    fun refreshTerminalColors() {
        applyThemeToTerminal(com.rk.settings.Settings.color_scheme)
    }
}
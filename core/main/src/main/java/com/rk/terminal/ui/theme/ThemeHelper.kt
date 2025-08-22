package com.rk.terminal.ui.theme

/**
 * Theme utility functions and constants
 */
object ThemeHelper {
    
    // Theme IDs for easy reference
    object ThemeIds {
        const val DEFAULT = 0
        
        // Dark Themes (1-10)
        const val DRACULA = 1
        const val ONE_DARK = 2
        const val NORD = 3
        const val TOKYO_NIGHT = 4
        const val SOLARIZED_DARK = 5
        const val MONOKAI_PRO = 6
        const val GITHUB_DARK = 7
        const val GRUVBOX_DARK = 8
        const val CATPPUCCIN_MOCHA = 9
        const val COBALT2 = 10
        
        // Light Themes (11-20)
        const val SOLARIZED_LIGHT = 11
        const val GITHUB_LIGHT = 12
        const val GRUVBOX_LIGHT = 13
        const val CATPPUCCIN_LATTE = 14
        const val TOKYO_LIGHT = 15
        const val NORD_LIGHT = 16
        const val MATERIAL_LIGHT = 17
        const val ATOM_ONE_LIGHT = 18
        const val AYU_LIGHT = 19
        const val PAPERCOLOR_LIGHT = 20
        
        // Legacy
        const val MONOKAI_LEGACY = 21
    }
    
    /**
     * Get display name for theme ID
     */
    fun getThemeName(themeId: Int): String {
        return when (themeId) {
            ThemeIds.DEFAULT -> "Default"
            
            // Dark Themes
            ThemeIds.DRACULA -> "Dracula"
            ThemeIds.ONE_DARK -> "One Dark"
            ThemeIds.NORD -> "Nord Dark"
            ThemeIds.TOKYO_NIGHT -> "Tokyo Night"
            ThemeIds.SOLARIZED_DARK -> "Solarized Dark"
            ThemeIds.MONOKAI_PRO -> "Monokai Pro"
            ThemeIds.GITHUB_DARK -> "GitHub Dark"
            ThemeIds.GRUVBOX_DARK -> "Gruvbox Dark"
            ThemeIds.CATPPUCCIN_MOCHA -> "Catppuccin Mocha"
            ThemeIds.COBALT2 -> "Cobalt2"
            
            // Light Themes
            ThemeIds.SOLARIZED_LIGHT -> "Solarized Light"
            ThemeIds.GITHUB_LIGHT -> "GitHub Light"
            ThemeIds.GRUVBOX_LIGHT -> "Gruvbox Light"
            ThemeIds.CATPPUCCIN_LATTE -> "Catppuccin Latte"
            ThemeIds.TOKYO_LIGHT -> "Tokyo Light"
            ThemeIds.NORD_LIGHT -> "Nord Light"
            ThemeIds.MATERIAL_LIGHT -> "Material Light"
            ThemeIds.ATOM_ONE_LIGHT -> "Atom One Light"
            ThemeIds.AYU_LIGHT -> "Ayu Light"
            ThemeIds.PAPERCOLOR_LIGHT -> "PaperColor Light"
            
            // Legacy
            ThemeIds.MONOKAI_LEGACY -> "Monokai (Legacy)"
            
            else -> "Unknown"
        }
    }
    
    /**
     * Check if theme is dark
     */
    fun isDarkTheme(themeId: Int): Boolean {
        return when (themeId) {
            in 1..10 -> true  // Dark themes
            ThemeIds.MONOKAI_LEGACY -> true
            else -> false     // Light themes and default
        }
    }
    
    /**
     * Get all available themes grouped by type
     */
    fun getAllThemes(): Map<String, List<Pair<Int, String>>> {
        return mapOf(
            "Dark Themes" to listOf(
                ThemeIds.DRACULA to "Dracula",
                ThemeIds.ONE_DARK to "One Dark",
                ThemeIds.NORD to "Nord Dark",
                ThemeIds.TOKYO_NIGHT to "Tokyo Night",
                ThemeIds.SOLARIZED_DARK to "Solarized Dark",
                ThemeIds.MONOKAI_PRO to "Monokai Pro",
                ThemeIds.GITHUB_DARK to "GitHub Dark",
                ThemeIds.GRUVBOX_DARK to "Gruvbox Dark",
                ThemeIds.CATPPUCCIN_MOCHA to "Catppuccin Mocha",
                ThemeIds.COBALT2 to "Cobalt2"
            ),
            "Light Themes" to listOf(
                ThemeIds.SOLARIZED_LIGHT to "Solarized Light",
                ThemeIds.GITHUB_LIGHT to "GitHub Light",
                ThemeIds.GRUVBOX_LIGHT to "Gruvbox Light",
                ThemeIds.CATPPUCCIN_LATTE to "Catppuccin Latte",
                ThemeIds.TOKYO_LIGHT to "Tokyo Light",
                ThemeIds.NORD_LIGHT to "Nord Light",
                ThemeIds.MATERIAL_LIGHT to "Material Light",
                ThemeIds.ATOM_ONE_LIGHT to "Atom One Light",
                ThemeIds.AYU_LIGHT to "Ayu Light",
                ThemeIds.PAPERCOLOR_LIGHT to "PaperColor Light"
            )
        )
    }
    
    /**
     * Get terminal ANSI colors for a theme (16 colors: 8 standard + 8 bright)
     * Returns array of color integers for terminal emulator
     */
    fun getTerminalColors(themeId: Int): IntArray {
        return when (themeId) {
            ThemeIds.DRACULA -> intArrayOf(
                // Standard colors (0-7)
                0xFF282A36.toInt(), // Black (background)
                0xFFFF5555.toInt(), // Red
                0xFF50FA7B.toInt(), // Green
                0xFFF1FA8C.toInt(), // Yellow
                0xFF6272A4.toInt(), // Blue
                0xFFBD93F9.toInt(), // Magenta
                0xFF8BE9FD.toInt(), // Cyan
                0xFFF8F8F2.toInt(), // White
                // Bright colors (8-15)
                0xFF44475A.toInt(), // Bright Black
                0xFFFF6E6E.toInt(), // Bright Red
                0xFF69FF94.toInt(), // Bright Green
                0xFFFFFFA5.toInt(), // Bright Yellow
                0xFF6272A4.toInt(), // Bright Blue
                0xFFD6ACFF.toInt(), // Bright Magenta
                0xFF9AEDFE.toInt(), // Bright Cyan
                0xFFFFFFFF.toInt()  // Bright White
            )
            
            ThemeIds.ONE_DARK -> intArrayOf(
                // Standard colors (0-7)
                0xFF282C34.toInt(), // Black
                0xFFE06C75.toInt(), // Red
                0xFF98C379.toInt(), // Green
                0xFFE5C07B.toInt(), // Yellow
                0xFF61AFEF.toInt(), // Blue
                0xFFC678DD.toInt(), // Magenta
                0xFF56B6C2.toInt(), // Cyan
                0xFFABB2BF.toInt(), // White
                // Bright colors (8-15)
                0xFF353B45.toInt(), // Bright Black
                0xFFE06C75.toInt(), // Bright Red
                0xFF98C379.toInt(), // Bright Green
                0xFFE5C07B.toInt(), // Bright Yellow
                0xFF61AFEF.toInt(), // Bright Blue
                0xFFC678DD.toInt(), // Bright Magenta
                0xFF56B6C2.toInt(), // Bright Cyan
                0xFFFFFFFF.toInt()  // Bright White
            )
            
            ThemeIds.SOLARIZED_DARK -> intArrayOf(
                // Standard colors (0-7)
                0xFF002B36.toInt(), // Black
                0xFFDC322F.toInt(), // Red
                0xFF859900.toInt(), // Green
                0xFFB58900.toInt(), // Yellow
                0xFF268BD2.toInt(), // Blue
                0xFFD33682.toInt(), // Magenta
                0xFF2AA198.toInt(), // Cyan
                0xFF839496.toInt(), // White
                // Bright colors (8-15)
                0xFF073642.toInt(), // Bright Black
                0xFFCB4B16.toInt(), // Bright Red
                0xFF859900.toInt(), // Bright Green
                0xFFB58900.toInt(), // Bright Yellow
                0xFF268BD2.toInt(), // Bright Blue
                0xFF6C71C4.toInt(), // Bright Magenta
                0xFF2AA198.toInt(), // Bright Cyan
                0xFFFDF6E3.toInt()  // Bright White
            )
            
            ThemeIds.GITHUB_LIGHT -> intArrayOf(
                // Standard colors (0-7)
                0xFFFFFFFF.toInt(), // Black (background)
                0xFFCF222E.toInt(), // Red
                0xFF1A7F37.toInt(), // Green
                0xFF9A6700.toInt(), // Yellow
                0xFF0969DA.toInt(), // Blue
                0xFF8250DF.toInt(), // Magenta
                0xFF1B7C83.toInt(), // Cyan
                0xFF1F2328.toInt(), // White
                // Bright colors (8-15)
                0xFFF6F8FA.toInt(), // Bright Black
                0xFFD1242F.toInt(), // Bright Red
                0xFF2DA44E.toInt(), // Bright Green
                0xFFBF8700.toInt(), // Bright Yellow
                0xFF218BFF.toInt(), // Bright Blue
                0xFF9558B2.toInt(), // Bright Magenta
                0xFF3192AA.toInt(), // Bright Cyan
                0xFF000000.toInt()  // Bright White
            )
            
            // Add more theme color mappings as needed...
            else -> intArrayOf(
                // Default fallback colors
                0xFF000000.toInt(), 0xFFCD0000.toInt(), 0xFF00CD00.toInt(), 0xFFCDCD00.toInt(),
                0xFF0000EE.toInt(), 0xFFCD00CD.toInt(), 0xFF00CDCD.toInt(), 0xFFE5E5E5.toInt(),
                0xFF7F7F7F.toInt(), 0xFFFF0000.toInt(), 0xFF00FF00.toInt(), 0xFFFFFF00.toInt(),
                0xFF5C5CFF.toInt(), 0xFFFF00FF.toInt(), 0xFF00FFFF.toInt(), 0xFFFFFFFF.toInt()
            )
        }
    }
}
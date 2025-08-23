package com.rk.terminal.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.toArgb
import com.rk.settings.SettingsManager

/**
 * Modern theme manager for handling the new theme system
 */
object ModernThemeManager {
    
    data class ThemeInfo(
        val id: Int,
        val name: String,
        val description: String,
        val isDark: Boolean,
        val backgroundColor: Int,
        val foregroundColor: Int,
        val accentColor: Int
    )
    
    fun getAllThemes(): List<ThemeInfo> = listOf(
        // System theme (0)
        ThemeInfo(
            id = 0,
            name = "System Default",
            description = "Follow system theme",
            isDark = false,
            backgroundColor = 0xFF121212.toInt(),
            foregroundColor = 0xFFFFFFFF.toInt(),
            accentColor = 0xFF6200EE.toInt()
        ),
        
        // Dark Themes (1-10)
        ThemeInfo(
            id = 1,
            name = "Dracula",
            description = "Dark theme for hackers",
            isDark = true,
            backgroundColor = dracula_background.toArgb(),
            foregroundColor = dracula_onBackground.toArgb(),
            accentColor = dracula_primary.toArgb()
        ),
        ThemeInfo(
            id = 2,
            name = "One Dark",
            description = "Atom's iconic One Dark theme",
            isDark = true,
            backgroundColor = onedark_background.toArgb(),
            foregroundColor = onedark_onBackground.toArgb(),
            accentColor = onedark_primary.toArgb()
        ),
        ThemeInfo(
            id = 3,
            name = "Nord",
            description = "Arctic, north-bluish color palette",
            isDark = true,
            backgroundColor = nord_background.toArgb(),
            foregroundColor = nord_onBackground.toArgb(),
            accentColor = nord_primary.toArgb()
        ),
        ThemeInfo(
            id = 4,
            name = "Tokyo Night",
            description = "A clean dark theme",
            isDark = true,
            backgroundColor = tokyo_night_background.toArgb(),
            foregroundColor = tokyo_night_onBackground.toArgb(),
            accentColor = tokyo_night_primary.toArgb()
        ),
        ThemeInfo(
            id = 5,
            name = "Solarized Dark",
            description = "Precision colors for machines and people",
            isDark = true,
            backgroundColor = solarized_dark_background.toArgb(),
            foregroundColor = solarized_dark_onBackground.toArgb(),
            accentColor = solarized_dark_primary.toArgb()
        ),
        ThemeInfo(
            id = 6,
            name = "Monokai Pro",
            description = "Professional coding theme",
            isDark = true,
            backgroundColor = monokai_background.toArgb(),
            foregroundColor = monokai_onBackground.toArgb(),
            accentColor = monokai_primary.toArgb()
        ),
        ThemeInfo(
            id = 7,
            name = "GitHub Dark",
            description = "GitHub's dark mode",
            isDark = true,
            backgroundColor = github_dark_background.toArgb(),
            foregroundColor = github_dark_onBackground.toArgb(),
            accentColor = github_dark_primary.toArgb()
        ),
        ThemeInfo(
            id = 8,
            name = "Gruvbox Dark",
            description = "Retro groove color scheme",
            isDark = true,
            backgroundColor = gruvbox_dark_background.toArgb(),
            foregroundColor = gruvbox_dark_onBackground.toArgb(),
            accentColor = gruvbox_dark_primary.toArgb()
        ),
        ThemeInfo(
            id = 9,
            name = "Catppuccin Mocha",
            description = "Soothing pastel theme",
            isDark = true,
            backgroundColor = catppuccin_mocha_background.toArgb(),
            foregroundColor = catppuccin_mocha_onBackground.toArgb(),
            accentColor = catppuccin_mocha_primary.toArgb()
        ),
        ThemeInfo(
            id = 10,
            name = "Cobalt2",
            description = "Blue-based coding theme",
            isDark = true,
            backgroundColor = cobalt2_background.toArgb(),
            foregroundColor = cobalt2_onBackground.toArgb(),
            accentColor = cobalt2_primary.toArgb()
        ),
        
        // Light Themes (11-20)
        ThemeInfo(
            id = 11,
            name = "Solarized Light",
            description = "Light version of Solarized",
            isDark = false,
            backgroundColor = solarized_light_background.toArgb(),
            foregroundColor = solarized_light_onBackground.toArgb(),
            accentColor = solarized_light_primary.toArgb()
        ),
        ThemeInfo(
            id = 12,
            name = "GitHub Light",
            description = "GitHub's light mode",
            isDark = false,
            backgroundColor = github_light_background.toArgb(),
            foregroundColor = github_light_onBackground.toArgb(),
            accentColor = github_light_primary.toArgb()
        ),
        ThemeInfo(
            id = 13,
            name = "Gruvbox Light",
            description = "Light Gruvbox variant",
            isDark = false,
            backgroundColor = gruvbox_light_background.toArgb(),
            foregroundColor = gruvbox_light_onBackground.toArgb(),
            accentColor = gruvbox_light_primary.toArgb()
        ),
        ThemeInfo(
            id = 14,
            name = "Catppuccin Latte",
            description = "Light Catppuccin variant",
            isDark = false,
            backgroundColor = catppuccin_latte_background.toArgb(),
            foregroundColor = catppuccin_latte_onBackground.toArgb(),
            accentColor = catppuccin_latte_primary.toArgb()
        ),
        ThemeInfo(
            id = 15,
            name = "Tokyo Light",
            description = "Light version of Tokyo Night",
            isDark = false,
            backgroundColor = tokyo_light_background.toArgb(),
            foregroundColor = tokyo_light_onBackground.toArgb(),
            accentColor = tokyo_light_primary.toArgb()
        ),
        ThemeInfo(
            id = 16,
            name = "Nord Light",
            description = "Light variant of Nord",
            isDark = false,
            backgroundColor = nord_light_background.toArgb(),
            foregroundColor = nord_light_onBackground.toArgb(),
            accentColor = nord_light_primary.toArgb()
        ),
        ThemeInfo(
            id = 17,
            name = "Material Light",
            description = "Google's Material Design light",
            isDark = false,
            backgroundColor = material_light_background.toArgb(),
            foregroundColor = material_light_onBackground.toArgb(),
            accentColor = material_light_primary.toArgb()
        ),
        ThemeInfo(
            id = 18,
            name = "Atom One Light",
            description = "Atom's One Light theme",
            isDark = false,
            backgroundColor = atom_one_light_background.toArgb(),
            foregroundColor = atom_one_light_onBackground.toArgb(),
            accentColor = atom_one_light_primary.toArgb()
        ),
        ThemeInfo(
            id = 19,
            name = "Ayu Light",
            description = "Clean light theme",
            isDark = false,
            backgroundColor = ayu_light_background.toArgb(),
            foregroundColor = ayu_light_onBackground.toArgb(),
            accentColor = ayu_light_primary.toArgb()
        ),
        ThemeInfo(
            id = 20,
            name = "PaperColor Light",
            description = "Paper-inspired light theme",
            isDark = false,
            backgroundColor = papercolor_light_background.toArgb(),
            foregroundColor = papercolor_light_onBackground.toArgb(),
            accentColor = papercolor_light_primary.toArgb()
        )
    )
    
    /**
     * Apply theme to the application
     */
    fun applyTheme(context: Context, themeId: Int) {
        SettingsManager.Appearance.colorScheme = themeId
        ThemeState.updateTheme(themeId)
    }
    
    /**
     * Get theme info by ID
     */
    fun getThemeById(id: Int): ThemeInfo? = getAllThemes().find { it.id == id }
    
    /**
     * Get current theme info
     */
    fun getCurrentTheme(): ThemeInfo = getThemeById(SettingsManager.Appearance.colorScheme) ?: getAllThemes()[0]
    
    /**
     * Check if onboarding was completed
     */
    fun isOnboardingCompleted(context: Context): Boolean {
        val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("onboarding_completed", false)
    }
    
    /**
     * Mark onboarding as completed
     */
    fun setOnboardingCompleted(context: Context, completed: Boolean) {
        val prefs = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
    }
}
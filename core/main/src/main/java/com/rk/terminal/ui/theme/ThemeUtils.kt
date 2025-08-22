package com.rk.terminal.ui.theme

import android.content.Context
import androidx.annotation.StringRes
import com.rk.terminal.R

/**
 * Theme utilities and examples for the ReTerminal app.
 * This demonstrates how to use the new theme system.
 */
object ThemeUtils {
    
    /**
     * Get all available dark themes for display in settings.
     */
    fun getDarkThemes(): List<ThemeInfo> {
        return listOf(
            ThemeInfo(ThemeManager.ThemeId.DRACULA, "Dracula", R.string.theme_dracula_desc),
            ThemeInfo(ThemeManager.ThemeId.ONE_DARK, "One Dark", R.string.theme_one_dark_desc),
            ThemeInfo(ThemeManager.ThemeId.NORD, "Nord", R.string.theme_nord_desc),
            ThemeInfo(ThemeManager.ThemeId.TOKYO_NIGHT, "Tokyo Night", R.string.theme_tokyo_night_desc),
            ThemeInfo(ThemeManager.ThemeId.SOLARIZED_DARK, "Solarized Dark", R.string.theme_solarized_dark_desc),
            ThemeInfo(ThemeManager.ThemeId.MONOKAI_PRO, "Monokai Pro", R.string.theme_monokai_pro_desc),
            ThemeInfo(ThemeManager.ThemeId.GITHUB_DARK, "GitHub Dark", R.string.theme_github_dark_desc),
            ThemeInfo(ThemeManager.ThemeId.GRUVBOX_DARK, "Gruvbox Dark", R.string.theme_gruvbox_dark_desc),
            ThemeInfo(ThemeManager.ThemeId.CATPPUCCIN_MOCHA, "Catppuccin Mocha", R.string.theme_catppuccin_mocha_desc),
            ThemeInfo(ThemeManager.ThemeId.COBALT2, "Cobalt2", R.string.theme_cobalt2_desc)
        )
    }
    
    /**
     * Get all available light themes for display in settings.
     */
    fun getLightThemes(): List<ThemeInfo> {
        return listOf(
            ThemeInfo(ThemeManager.ThemeId.SOLARIZED_LIGHT, "Solarized Light", R.string.theme_solarized_light_desc),
            ThemeInfo(ThemeManager.ThemeId.GITHUB_LIGHT, "GitHub Light", R.string.theme_github_light_desc),
            ThemeInfo(ThemeManager.ThemeId.GRUVBOX_LIGHT, "Gruvbox Light", R.string.theme_gruvbox_light_desc),
            ThemeInfo(ThemeManager.ThemeId.CATPPUCCIN_LATTE, "Catppuccin Latte", R.string.theme_catppuccin_latte_desc),
            ThemeInfo(ThemeManager.ThemeId.TOKYO_LIGHT, "Tokyo Light", R.string.theme_tokyo_light_desc),
            ThemeInfo(ThemeManager.ThemeId.NORD_LIGHT, "Nord Light", R.string.theme_nord_light_desc),
            ThemeInfo(ThemeManager.ThemeId.MATERIAL_LIGHT, "Material Light", R.string.theme_material_light_desc),
            ThemeInfo(ThemeManager.ThemeId.ATOM_ONE_LIGHT, "Atom One Light", R.string.theme_atom_one_light_desc),
            ThemeInfo(ThemeManager.ThemeId.AYU_LIGHT, "Ayu Light", R.string.theme_ayu_light_desc),
            ThemeInfo(ThemeManager.ThemeId.PAPERCOLOR_LIGHT, "PaperColor Light", R.string.theme_papercolor_light_desc)
        )
    }
    
    /**
     * Get all available themes.
     */
    fun getAllThemes(): List<ThemeInfo> {
        return getDarkThemes() + getLightThemes()
    }
    
    /**
     * Switch to a specific theme.
     * This is the primary method for changing themes.
     * 
     * @param context Application or Activity context
     * @param themeId The theme ID to switch to
     */
    fun switchTheme(context: Context, themeId: Int) {
        val themeManager = ThemeManager.getInstance(context)
        themeManager.setTheme(context, themeId)
    }
    
    /**
     * Get the current theme information.
     */
    fun getCurrentTheme(context: Context): ThemeInfo? {
        val themeManager = ThemeManager.getInstance(context)
        val currentId = themeManager.currentTheme
        return getAllThemes().find { it.themeId == currentId }
    }
    
    /**
     * Demo function: Switch between a few popular themes.
     * This can be used for testing or as inspiration for theme switching UI.
     */
    fun cyclePopularThemes(context: Context) {
        val popularThemes = listOf(
            ThemeManager.ThemeId.DRACULA,
            ThemeManager.ThemeId.ONE_DARK,
            ThemeManager.ThemeId.GITHUB_DARK,
            ThemeManager.ThemeId.GITHUB_LIGHT,
            ThemeManager.ThemeId.NORD,
            ThemeManager.ThemeId.TOKYO_NIGHT
        )
        
        val themeManager = ThemeManager.getInstance(context)
        val currentTheme = themeManager.currentTheme
        val currentIndex = popularThemes.indexOf(currentTheme)
        val nextIndex = (currentIndex + 1) % popularThemes.size
        val nextTheme = popularThemes[nextIndex]
        
        switchTheme(context, nextTheme)
    }
    
    /**
     * Information about a theme for display purposes.
     */
    data class ThemeInfo(
        val themeId: Int,
        val name: String,
        @StringRes val descriptionResId: Int
    ) {
        fun getDescription(context: Context): String {
            return try {
                context.getString(descriptionResId)
            } catch (e: Exception) {
                // Fallback if resource doesn't exist
                when (themeId) {
                    ThemeManager.ThemeId.DRACULA -> "Dark theme with purple accents, inspired by the Dracula color scheme"
                    ThemeManager.ThemeId.ONE_DARK -> "Popular dark theme from VS Code"
                    ThemeManager.ThemeId.NORD -> "Arctic-inspired dark theme with cool blue tones"
                    ThemeManager.ThemeId.TOKYO_NIGHT -> "Modern dark theme with vibrant highlights"
                    ThemeManager.ThemeId.GITHUB_LIGHT -> "Clean light theme matching GitHub's interface"
                    else -> "Professional color scheme for terminal and coding"
                }
            }
        }
    }
}
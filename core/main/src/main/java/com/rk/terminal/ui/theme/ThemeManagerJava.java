package com.rk.terminal.ui.theme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;
import com.rk.terminal.R;
import com.rk.settings.Settings;

/**
 * A centralized theme manager for the Android app.
 * Handles theme persistence, application, and management.
 *
 * @author Copilot
 */
public class ThemeManagerJava {
    
    private static final int DEFAULT_THEME = THEME_DRACULA_DARK;
    
    // Dark themes (10)
    public static final int THEME_DRACULA_DARK = 1;
    public static final int THEME_ONEDARK = 2;
    public static final int THEME_NORD_DARK = 3;
    public static final int THEME_TOKYO_NIGHT = 4;
    public static final int THEME_SOLARIZED_DARK = 5;
    public static final int THEME_MONOKAI_PRO = 6;
    public static final int THEME_GITHUB_DARK = 7;
    public static final int THEME_GRUVBOX_DARK = 8;
    public static final int THEME_CATPPUCCIN_MOCHA = 9;
    public static final int THEME_COBALT2 = 10;
    
    // Light themes (10)
    public static final int THEME_SOLARIZED_LIGHT = 11;
    public static final int THEME_GITHUB_LIGHT = 12;
    public static final int THEME_GRUVBOX_LIGHT = 13;
    public static final int THEME_CATPPUCCIN_LATTE = 14;
    public static final int THEME_TOKYO_LIGHT = 15;
    public static final int THEME_NORD_LIGHT = 16;
    public static final int THEME_MATERIAL_LIGHT = 17;
    public static final int THEME_ATOM_ONE_LIGHT = 18;
    public static final int THEME_AYU_LIGHT = 19;
    public static final int THEME_PAPERCOLOR_LIGHT = 20;
    
    private static ThemeManagerJava instance;
    
    private ThemeManagerJava() {
        // Private constructor for singleton
    }
    
    public static ThemeManagerJava getInstance() {
        if (instance == null) {
            instance = new ThemeManagerJava();
        }
        return instance;
    }
    
    /**
     * Set the theme for the application
     * @param context Application context
     * @param themeId Theme identifier
     */
    public void setTheme(Context context, int themeId) {
        Settings.INSTANCE.setSelected_theme(themeId);
    }
    
    /**
     * Get the currently selected theme
     * @param context Application context
     * @return Theme identifier
     */
    public int getSelectedTheme(Context context) {
        return Settings.INSTANCE.getSelected_theme();
    }
    
    /**
     * Apply the selected theme to an activity
     * @param activity The activity to apply theme to
     */
    public void applyTheme(Activity activity) {
        int themeId = getSelectedTheme(activity);
        int resourceTheme = getThemeResource(themeId);
        activity.setTheme(resourceTheme);
        
        // Set night mode based on theme
        if (isDarkTheme(themeId)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    
    /**
     * Check if a theme is dark
     * @param themeId Theme identifier
     * @return true if dark theme, false if light theme
     */
    public boolean isDarkTheme(int themeId) {
        return themeId >= THEME_DRACULA_DARK && themeId <= THEME_COBALT2;
    }
    
    /**
     * Get the resource ID for a theme
     * @param themeId Theme identifier
     * @return Theme resource ID
     */
    private int getThemeResource(int themeId) {
        switch (themeId) {
            // Dark themes
            case THEME_DRACULA_DARK:
                return R.style.Theme_Dracula_Dark;
            case THEME_ONEDARK:
                return R.style.Theme_OneDark;
            case THEME_NORD_DARK:
                return R.style.Theme_Nord_Dark;
            case THEME_TOKYO_NIGHT:
                return R.style.Theme_Tokyo_Night;
            case THEME_SOLARIZED_DARK:
                return R.style.Theme_Solarized_Dark;
            case THEME_MONOKAI_PRO:
                return R.style.Theme_Monokai_Pro;
            case THEME_GITHUB_DARK:
                return R.style.Theme_GitHub_Dark;
            case THEME_GRUVBOX_DARK:
                return R.style.Theme_Gruvbox_Dark;
            case THEME_CATPPUCCIN_MOCHA:
                return R.style.Theme_Catppuccin_Mocha;
            case THEME_COBALT2:
                return R.style.Theme_Cobalt2;
                
            // Light themes
            case THEME_SOLARIZED_LIGHT:
                return R.style.Theme_Solarized_Light;
            case THEME_GITHUB_LIGHT:
                return R.style.Theme_GitHub_Light;
            case THEME_GRUVBOX_LIGHT:
                return R.style.Theme_Gruvbox_Light;
            case THEME_CATPPUCCIN_LATTE:
                return R.style.Theme_Catppuccin_Latte;
            case THEME_TOKYO_LIGHT:
                return R.style.Theme_Tokyo_Light;
            case THEME_NORD_LIGHT:
                return R.style.Theme_Nord_Light;
            case THEME_MATERIAL_LIGHT:
                return R.style.Theme_Material_Light;
            case THEME_ATOM_ONE_LIGHT:
                return R.style.Theme_Atom_One_Light;
            case THEME_AYU_LIGHT:
                return R.style.Theme_Ayu_Light;
            case THEME_PAPERCOLOR_LIGHT:
                return R.style.Theme_PaperColor_Light;
                
            default:
                return R.style.Theme_Dracula_Dark; // Default to Dracula
        }
    }
    
    /**
     * Get theme name for display
     * @param themeId Theme identifier
     * @return Human-readable theme name
     */
    public String getThemeName(int themeId) {
        switch (themeId) {
            // Dark themes
            case THEME_DRACULA_DARK: return "Dracula";
            case THEME_ONEDARK: return "OneDark";
            case THEME_NORD_DARK: return "Nord Dark";
            case THEME_TOKYO_NIGHT: return "Tokyo Night";
            case THEME_SOLARIZED_DARK: return "Solarized Dark";
            case THEME_MONOKAI_PRO: return "Monokai Pro";
            case THEME_GITHUB_DARK: return "GitHub Dark";
            case THEME_GRUVBOX_DARK: return "Gruvbox Dark";
            case THEME_CATPPUCCIN_MOCHA: return "Catppuccin Mocha";
            case THEME_COBALT2: return "Cobalt2";
            
            // Light themes
            case THEME_SOLARIZED_LIGHT: return "Solarized Light";
            case THEME_GITHUB_LIGHT: return "GitHub Light";
            case THEME_GRUVBOX_LIGHT: return "Gruvbox Light";
            case THEME_CATPPUCCIN_LATTE: return "Catppuccin Latte";
            case THEME_TOKYO_LIGHT: return "Tokyo Light";
            case THEME_NORD_LIGHT: return "Nord Light";
            case THEME_MATERIAL_LIGHT: return "Material Light";
            case THEME_ATOM_ONE_LIGHT: return "Atom One Light";
            case THEME_AYU_LIGHT: return "Ayu Light";
            case THEME_PAPERCOLOR_LIGHT: return "PaperColor Light";
            
            default: return "Dracula";
        }
    }
}
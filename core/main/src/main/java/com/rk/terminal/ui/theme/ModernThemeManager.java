package com.rk.terminal.ui.theme;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.rk.terminal.R;
import com.rk.settings.Settings;

/**
 * Centralized Theme Manager for ReTerminal
 * Manages 20 curated modern themes with proper categorization
 * 
 * @author ReTerminal Team
 */
public class ModernThemeManager {
    
    // Theme Constants
    public static final int THEME_SYSTEM = 0;
    
    // Dark Themes (1-10)
    public static final int THEME_DRACULA = 1;
    public static final int THEME_ONE_DARK = 2;
    public static final int THEME_NORD_DARK = 3;
    public static final int THEME_TOKYO_NIGHT = 4;
    public static final int THEME_SOLARIZED_DARK = 5;
    public static final int THEME_MONOKAI_PRO = 6;
    public static final int THEME_GITHUB_DARK = 7;
    public static final int THEME_GRUVBOX_DARK = 8;
    public static final int THEME_CATPPUCCIN_MOCHA = 9;
    public static final int THEME_COBALT2 = 10;
    
    // Light Themes (11-20)
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
    
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_SELECTED_THEME = "selected_theme";
    private static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    
    /**
     * Theme Information Class
     */
    public static class ThemeInfo {
        public final int id;
        public final String name;
        public final String description;
        public final boolean isDark;
        public final int backgroundColor;
        public final int foregroundColor;
        public final int accentColor;
        
        public ThemeInfo(int id, String name, String description, boolean isDark, 
                        int backgroundColor, int foregroundColor, int accentColor) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.isDark = isDark;
            this.backgroundColor = backgroundColor;
            this.foregroundColor = foregroundColor;
            this.accentColor = accentColor;
        }
    }
    
    /**
     * Get all available themes
     */
    public static ThemeInfo[] getAllThemes() {
        return new ThemeInfo[] {
            // System Default
            new ThemeInfo(THEME_SYSTEM, "System", "Follow system theme", true, 
                         0xFF111318, 0xFFE2E2E9, 0xFFADC6FF),
            
            // Dark Themes
            new ThemeInfo(THEME_DRACULA, "Dracula", "Dark theme with purple accent", true,
                         0xFF282A36, 0xFFF8F8F2, 0xFFBD93F9),
            new ThemeInfo(THEME_ONE_DARK, "One Dark", "VS Code's popular dark theme", true,
                         0xFF282C34, 0xFFABB2BF, 0xFF61AFEF),
            new ThemeInfo(THEME_NORD_DARK, "Nord", "Arctic, north-bluish color palette", true,
                         0xFF2E3440, 0xFFECEFF4, 0xFF81A1C1),
            new ThemeInfo(THEME_TOKYO_NIGHT, "Tokyo Night", "Clean dark theme", true,
                         0xFF1A1B26, 0xFFC0CAF5, 0xFF7AA2F7),
            new ThemeInfo(THEME_SOLARIZED_DARK, "Solarized Dark", "Precision colors for machines and people", true,
                         0xFF002B36, 0xFF839496, 0xFF268BD2),
            new ThemeInfo(THEME_MONOKAI_PRO, "Monokai Pro", "Professional developer theme", true,
                         0xFF272822, 0xFFF8F8F2, 0xFFF92672),
            new ThemeInfo(THEME_GITHUB_DARK, "GitHub Dark", "GitHub's dark interface", true,
                         0xFF0D1117, 0xFFF0F6FC, 0xFF58A6FF),
            new ThemeInfo(THEME_GRUVBOX_DARK, "Gruvbox Dark", "Retro groove color scheme", true,
                         0xFF282828, 0xFFEBDBB2, 0xFF458588),
            new ThemeInfo(THEME_CATPPUCCIN_MOCHA, "Catppuccin Mocha", "Soothing pastel theme", true,
                         0xFF1E1E2E, 0xFFCDD6F4, 0xFF89B4FA),
            new ThemeInfo(THEME_COBALT2, "Cobalt2", "Refined dark blue theme", true,
                         0xFF193549, 0xFFFFFFFF, 0xFF0088FF),
            
            // Light Themes
            new ThemeInfo(THEME_SOLARIZED_LIGHT, "Solarized Light", "Precision colors for machines and people", false,
                         0xFFFDF6E3, 0xFF657B83, 0xFF268BD2),
            new ThemeInfo(THEME_GITHUB_LIGHT, "GitHub Light", "GitHub's light interface", false,
                         0xFFFFFFFF, 0xFF24292E, 0xFF0366D6),
            new ThemeInfo(THEME_GRUVBOX_LIGHT, "Gruvbox Light", "Retro groove light color scheme", false,
                         0xFFFBF1C7, 0xFF3C3836, 0xFF076678),
            new ThemeInfo(THEME_CATPPUCCIN_LATTE, "Catppuccin Latte", "Soothing pastel light theme", false,
                         0xFFEFF1F5, 0xFF4C4F69, 0xFF1E66F5),
            new ThemeInfo(THEME_TOKYO_LIGHT, "Tokyo Light", "Clean light theme", false,
                         0xFFD5D6DB, 0xFF343B58, 0xFF34548A),
            new ThemeInfo(THEME_NORD_LIGHT, "Nord Light", "Arctic light color palette", false,
                         0xFFECEFF4, 0xFF2E3440, 0xFF5E81AC),
            new ThemeInfo(THEME_MATERIAL_LIGHT, "Material Light", "Google's Material Design", false,
                         0xFFFAFAFA, 0xFF212121, 0xFF2196F3),
            new ThemeInfo(THEME_ATOM_ONE_LIGHT, "Atom One Light", "Atom's default light theme", false,
                         0xFFFAFAFA, 0xFF383A42, 0xFF4078F2),
            new ThemeInfo(THEME_AYU_LIGHT, "Ayu Light", "Modern, elegant light theme", false,
                         0xFFFAFAFA, 0xFF5C6166, 0xFF399EE6),
            new ThemeInfo(THEME_PAPERCOLOR_LIGHT, "PaperColor Light", "Inspired by Google's Material Design", false,
                         0xFFEEEEEE, 0xFF444444, 0xFF005F87)
        };
    }
    
    /**
     * Get theme info by ID
     */
    public static ThemeInfo getThemeInfo(int themeId) {
        ThemeInfo[] themes = getAllThemes();
        for (ThemeInfo theme : themes) {
            if (theme.id == themeId) {
                return theme;
            }
        }
        return themes[0]; // Return system default if not found
    }
    
    /**
     * Get dark themes only
     */
    public static ThemeInfo[] getDarkThemes() {
        ThemeInfo[] allThemes = getAllThemes();
        int darkCount = 0;
        for (ThemeInfo theme : allThemes) {
            if (theme.isDark) darkCount++;
        }
        
        ThemeInfo[] darkThemes = new ThemeInfo[darkCount];
        int index = 0;
        for (ThemeInfo theme : allThemes) {
            if (theme.isDark) {
                darkThemes[index++] = theme;
            }
        }
        return darkThemes;
    }
    
    /**
     * Get light themes only
     */
    public static ThemeInfo[] getLightThemes() {
        ThemeInfo[] allThemes = getAllThemes();
        int lightCount = 0;
        for (ThemeInfo theme : allThemes) {
            if (!theme.isDark) lightCount++;
        }
        
        ThemeInfo[] lightThemes = new ThemeInfo[lightCount];
        int index = 0;
        for (ThemeInfo theme : allThemes) {
            if (!theme.isDark) {
                lightThemes[index++] = theme;
            }
        }
        return lightThemes;
    }
    
    /**
     * Apply theme to the current context
     */
    public static void applyTheme(@NonNull Context context, int themeId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_SELECTED_THEME, themeId).apply();
        
        // Update the Settings color scheme for compatibility
        Settings.INSTANCE.setColor_scheme(themeId);
        
        // Apply theme colors to terminal
        try {
            Class<?> terminalColorsClass = Class.forName("com.termux.terminal.TerminalColors");
            java.lang.reflect.Method applyThemeMethod = terminalColorsClass.getMethod("applyTheme", int.class);
            applyThemeMethod.invoke(null, themeId);
        } catch (Exception e) {
            // If reflection fails, continue without terminal colors update
            // Terminal will use default colors
        }
    }
    
    /**
     * Get current selected theme
     */
    public static int getCurrentTheme(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_SELECTED_THEME, THEME_SYSTEM);
    }
    
    /**
     * Check if onboarding was completed
     */
    public static boolean isOnboardingCompleted(@NonNull Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);
    }
    
    /**
     * Mark onboarding as completed
     */
    public static void setOnboardingCompleted(@NonNull Context context, boolean completed) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply();
    }
    
    /**
     * Get terminal colors for a theme (for ANSI color mapping)
     */
    public static int[] getTerminalColors(int themeId) {
        ThemeInfo theme = getThemeInfo(themeId);
        
        // Return ANSI color palette based on theme
        // Format: [black, red, green, yellow, blue, magenta, cyan, white, 
        //          bright_black, bright_red, bright_green, bright_yellow, 
        //          bright_blue, bright_magenta, bright_cyan, bright_white]
        switch (themeId) {
            case THEME_DRACULA:
                return new int[] {
                    0xFF282A36, 0xFFFF5555, 0xFF50FA7B, 0xFFF1FA8C,
                    0xFFBD93F9, 0xFFFF79C6, 0xFF8BE9FD, 0xFFF8F8F2,
                    0xFF6272A4, 0xFFFF6E6E, 0xFF69FF94, 0xFFFFFFA5,
                    0xFFD6ACFF, 0xFFFF92DF, 0xFFA4FFFF, 0xFFFFFFFF
                };
            case THEME_ONE_DARK:
                return new int[] {
                    0xFF282C34, 0xFFE06C75, 0xFF98C379, 0xFFE5C07B,
                    0xFF61AFEF, 0xFFC678DD, 0xFF56B6C2, 0xFFABB2BF,
                    0xFF5C6370, 0xFFE06C75, 0xFF98C379, 0xFFE5C07B,
                    0xFF61AFEF, 0xFFC678DD, 0xFF56B6C2, 0xFFFFFFFF
                };
            case THEME_NORD_DARK:
                return new int[] {
                    0xFF3B4252, 0xFFBF616A, 0xFFA3BE8C, 0xFFEBCB8B,
                    0xFF81A1C1, 0xFFB48EAD, 0xFF88C0D0, 0xFFE5E9F0,
                    0xFF4C566A, 0xFFBF616A, 0xFFA3BE8C, 0xFFEBCB8B,
                    0xFF81A1C1, 0xFFB48EAD, 0xFF8FBCBB, 0xFFECEFF4
                };
            default:
                // Return default terminal colors for other themes
                return new int[] {
                    0xFF000000, 0xFFCD0000, 0xFF00CD00, 0xFFCDCD00,
                    0xFF6495ED, 0xFFCD00CD, 0xFF00CDCD, 0xFFE5E5E5,
                    0xFF7F7F7F, 0xFFFF0000, 0xFF00FF00, 0xFFFFFF00,
                    0xFF0000FF, 0xFFFF00FF, 0xFF00FFFF, 0xFFFFFFFF
                };
        }
    }
}
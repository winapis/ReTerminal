package com.rk.terminal.ui.theme;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatDelegate;
import com.rk.settings.Settings;
import com.rk.terminal.R;

/**
 * Centralized theme manager for the Android terminal app.
 * Handles theme switching, persistence, and application across the entire UI.
 * 
 * @author GitHub Copilot (Auto-generated for theme refactoring)
 */
public class ThemeManager {
    
    // Theme constants
    public static final class ThemeId {
        // Dark themes (10)
        public static final int DRACULA = 0;           // Default dark theme
        public static final int ONE_DARK = 1;
        public static final int NORD = 2;
        public static final int TOKYO_NIGHT = 3;
        public static final int SOLARIZED_DARK = 4;
        public static final int MONOKAI_PRO = 5;
        public static final int GITHUB_DARK = 6;
        public static final int GRUVBOX_DARK = 7;
        public static final int CATPPUCCIN_MOCHA = 8;
        public static final int COBALT2 = 9;
        
        // Light themes (10)
        public static final int SOLARIZED_LIGHT = 10;
        public static final int GITHUB_LIGHT = 11;
        public static final int GRUVBOX_LIGHT = 12;
        public static final int CATPPUCCIN_LATTE = 13;
        public static final int TOKYO_LIGHT = 14;
        public static final int NORD_LIGHT = 15;
        public static final int MATERIAL_LIGHT = 16;
        public static final int ATOM_ONE_LIGHT = 17;
        public static final int AYU_LIGHT = 18;
        public static final int PAPERCOLOR_LIGHT = 19;
        
        // Special values
        public static final int AUTO = -1;             // Follow system theme
        public static final int DEFAULT_THEME = DRACULA;
    }
    
    private static final String PREFS_THEME_KEY = "selected_theme_id";
    private static final String PREFS_NAME = "theme_prefs";
    
    private static ThemeManager instance;
    private SharedPreferences prefs;
    private int currentThemeId = ThemeId.DEFAULT_THEME;
    
    private ThemeManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        currentThemeId = prefs.getInt(PREFS_THEME_KEY, ThemeId.DEFAULT_THEME);
    }
    
    /**
     * Get the singleton instance of ThemeManager.
     */
    public static synchronized ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context.getApplicationContext());
        }
        return instance;
    }
    
    /**
     * Apply the current theme to an Activity.
     * This must be called before setContentView().
     * 
     * @param activity The activity to apply the theme to
     */
    public void applyTheme(@NonNull Activity activity) {
        // Handle night mode first
        int nightMode = Settings.INSTANCE.getDefault_night_mode();
        if (nightMode != AppCompatDelegate.getDefaultNightMode()) {
            AppCompatDelegate.setDefaultNightMode(nightMode);
        }
        
        // Apply the selected theme
        @StyleRes int themeRes = getThemeResourceId(currentThemeId);
        activity.setTheme(themeRes);
        
        // Handle OLED theme override if enabled
        if (isCurrentThemeDark() && Settings.INSTANCE.getAmoled()) {
            if (Settings.INSTANCE.getMonet()) {
                activity.setTheme(R.style.Theme_Karbon_Oled_Monet);
            } else {
                activity.setTheme(R.style.Theme_Karbon_Oled);
            }
        }
    }
    
    /**
     * Set the current theme and persist the selection.
     * 
     * @param context The context for SharedPreferences access
     * @param themeId The theme ID to set
     */
    public void setTheme(@NonNull Context context, int themeId) {
        currentThemeId = themeId;
        prefs.edit().putInt(PREFS_THEME_KEY, themeId).apply();
        
        // Update legacy Settings for backward compatibility
        Settings.INSTANCE.setColor_scheme(convertToLegacyColorScheme(themeId));
    }
    
    /**
     * Get the current theme ID.
     */
    public int getCurrentTheme() {
        return currentThemeId;
    }
    
    /**
     * Check if the current theme is a dark theme.
     */
    public boolean isCurrentThemeDark() {
        return currentThemeId >= ThemeId.DRACULA && currentThemeId <= ThemeId.COBALT2;
    }
    
    /**
     * Get the theme name for display purposes.
     */
    public String getThemeName(int themeId) {
        switch (themeId) {
            case ThemeId.DRACULA: return "Dracula";
            case ThemeId.ONE_DARK: return "One Dark";
            case ThemeId.NORD: return "Nord";
            case ThemeId.TOKYO_NIGHT: return "Tokyo Night";
            case ThemeId.SOLARIZED_DARK: return "Solarized Dark";
            case ThemeId.MONOKAI_PRO: return "Monokai Pro";
            case ThemeId.GITHUB_DARK: return "GitHub Dark";
            case ThemeId.GRUVBOX_DARK: return "Gruvbox Dark";
            case ThemeId.CATPPUCCIN_MOCHA: return "Catppuccin Mocha";
            case ThemeId.COBALT2: return "Cobalt2";
            case ThemeId.SOLARIZED_LIGHT: return "Solarized Light";
            case ThemeId.GITHUB_LIGHT: return "GitHub Light";
            case ThemeId.GRUVBOX_LIGHT: return "Gruvbox Light";
            case ThemeId.CATPPUCCIN_LATTE: return "Catppuccin Latte";
            case ThemeId.TOKYO_LIGHT: return "Tokyo Light";
            case ThemeId.NORD_LIGHT: return "Nord Light";
            case ThemeId.MATERIAL_LIGHT: return "Material Light";
            case ThemeId.ATOM_ONE_LIGHT: return "Atom One Light";
            case ThemeId.AYU_LIGHT: return "Ayu Light";
            case ThemeId.PAPERCOLOR_LIGHT: return "PaperColor Light";
            default: return "Unknown Theme";
        }
    }
    
    /**
     * Get all available dark theme IDs.
     */
    public int[] getDarkThemes() {
        return new int[] {
            ThemeId.DRACULA, ThemeId.ONE_DARK, ThemeId.NORD, ThemeId.TOKYO_NIGHT,
            ThemeId.SOLARIZED_DARK, ThemeId.MONOKAI_PRO, ThemeId.GITHUB_DARK,
            ThemeId.GRUVBOX_DARK, ThemeId.CATPPUCCIN_MOCHA, ThemeId.COBALT2
        };
    }
    
    /**
     * Get all available light theme IDs.
     */
    public int[] getLightThemes() {
        return new int[] {
            ThemeId.SOLARIZED_LIGHT, ThemeId.GITHUB_LIGHT, ThemeId.GRUVBOX_LIGHT,
            ThemeId.CATPPUCCIN_LATTE, ThemeId.TOKYO_LIGHT, ThemeId.NORD_LIGHT,
            ThemeId.MATERIAL_LIGHT, ThemeId.ATOM_ONE_LIGHT, ThemeId.AYU_LIGHT,
            ThemeId.PAPERCOLOR_LIGHT
        };
    }
    
    /**
     * Get the terminal color scheme for the current theme.
     * This integrates UI themes with terminal emulator colors.
     */
    public TerminalThemeColors getTerminalColors() {
        return TerminalThemeColors.getColorsForTheme(currentThemeId);
    }
    
    @StyleRes
    private int getThemeResourceId(int themeId) {
        switch (themeId) {
            // Dark themes
            case ThemeId.DRACULA: return R.style.Theme_Terminal_Dracula;
            case ThemeId.ONE_DARK: return R.style.Theme_Terminal_OneDark;
            case ThemeId.NORD: return R.style.Theme_Terminal_Nord;
            case ThemeId.TOKYO_NIGHT: return R.style.Theme_Terminal_TokyoNight;
            case ThemeId.SOLARIZED_DARK: return R.style.Theme_Terminal_SolarizedDark;
            case ThemeId.MONOKAI_PRO: return R.style.Theme_Terminal_MonokaiPro;
            case ThemeId.GITHUB_DARK: return R.style.Theme_Terminal_GitHubDark;
            case ThemeId.GRUVBOX_DARK: return R.style.Theme_Terminal_GruvboxDark;
            case ThemeId.CATPPUCCIN_MOCHA: return R.style.Theme_Terminal_CatppuccinMocha;
            case ThemeId.COBALT2: return R.style.Theme_Terminal_Cobalt2;
            
            // Light themes
            case ThemeId.SOLARIZED_LIGHT: return R.style.Theme_Terminal_SolarizedLight;
            case ThemeId.GITHUB_LIGHT: return R.style.Theme_Terminal_GitHubLight;
            case ThemeId.GRUVBOX_LIGHT: return R.style.Theme_Terminal_GruvboxLight;
            case ThemeId.CATPPUCCIN_LATTE: return R.style.Theme_Terminal_CatppuccinLatte;
            case ThemeId.TOKYO_LIGHT: return R.style.Theme_Terminal_TokyoLight;
            case ThemeId.NORD_LIGHT: return R.style.Theme_Terminal_NordLight;
            case ThemeId.MATERIAL_LIGHT: return R.style.Theme_Terminal_MaterialLight;
            case ThemeId.ATOM_ONE_LIGHT: return R.style.Theme_Terminal_AtomOneLight;
            case ThemeId.AYU_LIGHT: return R.style.Theme_Terminal_AyuLight;
            case ThemeId.PAPERCOLOR_LIGHT: return R.style.Theme_Terminal_PaperColorLight;
            
            // Fallback to base theme
            default: return R.style.Theme_Karbon;
        }
    }
    
    /**
     * Convert new theme ID to legacy color scheme for backward compatibility.
     */
    private int convertToLegacyColorScheme(int themeId) {
        switch (themeId) {
            case ThemeId.MONOKAI_PRO: return 1;
            case ThemeId.ONE_DARK: return 2;
            case ThemeId.DRACULA: return 3;
            case ThemeId.GITHUB_LIGHT: return 4;
            default: return 0; // Default
        }
    }
    
    /**
     * Terminal theme colors helper class to map themes to terminal ANSI colors.
     */
    public static class TerminalThemeColors {
        public final int foreground;
        public final int background;
        public final int cursor;
        public final int[] ansiColors; // 16 ANSI colors
        
        public TerminalThemeColors(int foreground, int background, int cursor, int[] ansiColors) {
            this.foreground = foreground;
            this.background = background;
            this.cursor = cursor;
            this.ansiColors = ansiColors;
        }
        
        /**
         * Get terminal colors for a specific theme.
         */
        public static TerminalThemeColors getColorsForTheme(int themeId) {
            switch (themeId) {
                case ThemeId.DRACULA:
                    return getDraculaColors();
                case ThemeId.ONE_DARK:
                    return getOneDarkColors();
                case ThemeId.NORD:
                    return getNordColors();
                case ThemeId.GITHUB_LIGHT:
                    return getGitHubLightColors();
                // Add more cases as themes are implemented
                default:
                    return getDraculaColors(); // Default to Dracula
            }
        }
        
        private static TerminalThemeColors getDraculaColors() {
            int[] ansiColors = {
                0xFF282A36, // black (background)
                0xFFFF5555, // red
                0xFF50FA7B, // green  
                0xFFF1FA8C, // yellow
                0xFFBD93F9, // blue
                0xFFFF79C6, // magenta
                0xFF8BE9FD, // cyan
                0xFFF8F8F2, // white (foreground)
                // Bright variants
                0xFF44475A, // bright black
                0xFFFF6E6E, // bright red
                0xFF69FF94, // bright green
                0xFFFFFFA5, // bright yellow
                0xFFD6ACFF, // bright blue
                0xFFFF92DF, // bright magenta
                0xFFA4FFFF, // bright cyan
                0xFFFFFFFF  // bright white
            };
            return new TerminalThemeColors(0xFFF8F8F2, 0xFF282A36, 0xFFF8F8F2, ansiColors);
        }
        
        private static TerminalThemeColors getOneDarkColors() {
            int[] ansiColors = {
                0xFF282C34, // black
                0xFFE06C75, // red
                0xFF98C379, // green
                0xFFE5C07B, // yellow
                0xFF61AFEF, // blue
                0xFFC678DD, // magenta
                0xFF56B6C2, // cyan
                0xFFABB2BF, // white
                // Bright variants
                0xFF5C6370, // bright black
                0xFFE06C75, // bright red
                0xFF98C379, // bright green
                0xFFE5C07B, // bright yellow
                0xFF61AFEF, // bright blue
                0xFFC678DD, // bright magenta
                0xFF56B6C2, // bright cyan
                0xFFFFFFFF  // bright white
            };
            return new TerminalThemeColors(0xFFABB2BF, 0xFF282C34, 0xFFABB2BF, ansiColors);
        }
        
        private static TerminalThemeColors getNordColors() {
            int[] ansiColors = {
                0xFF2E3440, // black
                0xFFBF616A, // red
                0xFFA3BE8C, // green
                0xFFEBCB8B, // yellow
                0xFF81A1C1, // blue
                0xFFB48EAD, // magenta
                0xFF88C0D0, // cyan
                0xFFE5E9F0, // white
                // Bright variants
                0xFF4C566A, // bright black
                0xFFBF616A, // bright red
                0xFFA3BE8C, // bright green
                0xFFEBCB8B, // bright yellow
                0xFF81A1C1, // bright blue
                0xFFB48EAD, // bright magenta
                0xFF8FBCBB, // bright cyan
                0xFFECEFF4  // bright white
            };
            return new TerminalThemeColors(0xFFE5E9F0, 0xFF2E3440, 0xFFE5E9F0, ansiColors);
        }
        
        private static TerminalThemeColors getGitHubLightColors() {
            int[] ansiColors = {
                0xFF24292E, // black
                0xFFD73A49, // red
                0xFF28A745, // green
                0xFFFBC02D, // yellow
                0xFF0366D6, // blue
                0xFF6F42C1, // magenta
                0xFF17A2B8, // cyan
                0xFF6A737D, // white
                // Bright variants
                0xFF959DA5, // bright black
                0xFFCB2431, // bright red
                0xFF22863A, // bright green
                0xFFE36209, // bright yellow
                0xFF005CC5, // bright blue
                0xFF5A32A3, // bright magenta
                0xFF1B7C83, // bright cyan
                0xFF24292E  // bright white
            };
            return new TerminalThemeColors(0xFF24292E, 0xFFFFFFFF, 0xFF24292E, ansiColors);
        }
    }
}
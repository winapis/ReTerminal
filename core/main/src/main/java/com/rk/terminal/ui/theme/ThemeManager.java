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
        
        // Apply terminal colors immediately
        applyTerminalColors();
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
    
    /**
     * Apply terminal colors based on the current theme.
     * This method should be called after setting a new theme to update terminal colors.
     */
    public void applyTerminalColors() {
        TerminalThemeColors colors = getTerminalColors();
        // Import TerminalColors class and update colors
        try {
            Class<?> terminalColorsClass = Class.forName("com.termux.terminal.TerminalColors");
            java.lang.reflect.Method updateMethod = terminalColorsClass.getMethod(
                "updateThemeColors", int.class, int.class, int.class, int[].class);
            updateMethod.invoke(null, colors.foreground, colors.background, colors.cursor, colors.ansiColors);
        } catch (Exception e) {
            // Fallback: log error but don't crash
            android.util.Log.w("ThemeManager", "Could not update terminal colors", e);
        }
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
                case ThemeId.TOKYO_NIGHT:
                    return getTokyoNightColors();
                case ThemeId.SOLARIZED_DARK:
                    return getSolarizedDarkColors();
                case ThemeId.MONOKAI_PRO:
                    return getMonokaiProColors();
                case ThemeId.GITHUB_DARK:
                    return getGitHubDarkColors();
                case ThemeId.GRUVBOX_DARK:
                    return getGruvboxDarkColors();
                case ThemeId.CATPPUCCIN_MOCHA:
                    return getCatppuccinMochaColors();
                case ThemeId.COBALT2:
                    return getCobalt2Colors();
                case ThemeId.SOLARIZED_LIGHT:
                    return getSolarizedLightColors();
                case ThemeId.GITHUB_LIGHT:
                    return getGitHubLightColors();
                case ThemeId.GRUVBOX_LIGHT:
                    return getGruvboxLightColors();
                case ThemeId.CATPPUCCIN_LATTE:
                    return getCatppuccinLatteColors();
                case ThemeId.TOKYO_LIGHT:
                    return getTokyoLightColors();
                case ThemeId.NORD_LIGHT:
                    return getNordLightColors();
                case ThemeId.MATERIAL_LIGHT:
                    return getMaterialLightColors();
                case ThemeId.ATOM_ONE_LIGHT:
                    return getAtomOneLightColors();
                case ThemeId.AYU_LIGHT:
                    return getAyuLightColors();
                case ThemeId.PAPERCOLOR_LIGHT:
                    return getPaperColorLightColors();
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
        
        private static TerminalThemeColors getTokyoNightColors() {
            int[] ansiColors = {
                0xFF1A1B26, // black
                0xFFF7768E, // red
                0xFF9ECE6A, // green
                0xFFE0AF68, // yellow
                0xFF7AA2F7, // blue
                0xFFBB9AF7, // magenta
                0xFF7DCFFF, // cyan
                0xFFC0CAF5, // white
                // Bright variants
                0xFF414868, // bright black
                0xFFF7768E, // bright red
                0xFF9ECE6A, // bright green
                0xFFE0AF68, // bright yellow
                0xFF7AA2F7, // bright blue
                0xFFBB9AF7, // bright magenta
                0xFF7DCFFF, // bright cyan
                0xFFFFFFFF  // bright white
            };
            return new TerminalThemeColors(0xFFC0CAF5, 0xFF1A1B26, 0xFFC0CAF5, ansiColors);
        }
        
        private static TerminalThemeColors getSolarizedDarkColors() {
            int[] ansiColors = {
                0xFF002B36, // black
                0xFFDC322F, // red
                0xFF859900, // green
                0xFFB58900, // yellow
                0xFF268BD2, // blue
                0xFFD33682, // magenta
                0xFF2AA198, // cyan
                0xFF839496, // white
                // Bright variants
                0xFF073642, // bright black
                0xFFCB4B16, // bright red
                0xFF586E75, // bright green
                0xFF657B83, // bright yellow
                0xFF839496, // bright blue
                0xFF6C71C4, // bright magenta
                0xFF93A1A1, // bright cyan
                0xFFFDF6E3  // bright white
            };
            return new TerminalThemeColors(0xFF839496, 0xFF002B36, 0xFF839496, ansiColors);
        }
        
        private static TerminalThemeColors getMonokaiProColors() {
            int[] ansiColors = {
                0xFF2D2A2E, // black
                0xFFFF6188, // red
                0xFFA9DC76, // green
                0xFFFFD866, // yellow
                0xFF78DCE8, // blue
                0xFFAB9DF2, // magenta
                0xFF78DCE8, // cyan
                0xFFFCFCFA, // white
                // Bright variants
                0xFF403E41, // bright black
                0xFFFF6188, // bright red
                0xFFA9DC76, // bright green
                0xFFFFD866, // bright yellow
                0xFF78DCE8, // bright blue
                0xFFAB9DF2, // bright magenta
                0xFF78DCE8, // bright cyan
                0xFFFFFFFF  // bright white
            };
            return new TerminalThemeColors(0xFFFCFCFA, 0xFF2D2A2E, 0xFFFCFCFA, ansiColors);
        }
        
        private static TerminalThemeColors getGitHubDarkColors() {
            int[] ansiColors = {
                0xFF0D1117, // black
                0xFFFF7B72, // red
                0xFF3FB950, // green
                0xFFD29922, // yellow
                0xFF58A6FF, // blue
                0xFFBC8CFF, // magenta
                0xFF39C5CF, // cyan
                0xFFF0F6FC, // white
                // Bright variants
                0xFF161B22, // bright black
                0xFFFF7B72, // bright red
                0xFF3FB950, // bright green
                0xFFD29922, // bright yellow
                0xFF58A6FF, // bright blue
                0xFFBC8CFF, // bright magenta
                0xFF39C5CF, // bright cyan
                0xFFFFFFFF  // bright white
            };
            return new TerminalThemeColors(0xFFF0F6FC, 0xFF0D1117, 0xFFF0F6FC, ansiColors);
        }
        
        private static TerminalThemeColors getGruvboxDarkColors() {
            int[] ansiColors = {
                0xFF282828, // black
                0xFFCC241D, // red
                0xFF98971A, // green
                0xFFD79921, // yellow
                0xFF458588, // blue
                0xFFB16286, // magenta
                0xFF689D6A, // cyan
                0xFFA89984, // white
                // Bright variants
                0xFF928374, // bright black
                0xFFFB4934, // bright red
                0xFFB8BB26, // bright green
                0xFFFABD2F, // bright yellow
                0xFF83A598, // bright blue
                0xFFD3869B, // bright magenta
                0xFF8EC07C, // bright cyan
                0xFFEBDBB2  // bright white
            };
            return new TerminalThemeColors(0xFFEBDBB2, 0xFF282828, 0xFFEBDBB2, ansiColors);
        }
        
        private static TerminalThemeColors getCatppuccinMochaColors() {
            int[] ansiColors = {
                0xFF1E1E2E, // black
                0xFFF38BA8, // red
                0xFFA6E3A1, // green
                0xFFF9E2AF, // yellow
                0xFF89B4FA, // blue
                0xFFF5C2E7, // magenta
                0xFF94E2D5, // cyan
                0xFFCDD6F4, // white
                // Bright variants
                0xFF313244, // bright black
                0xFFF38BA8, // bright red
                0xFFA6E3A1, // bright green
                0xFFF9E2AF, // bright yellow
                0xFF89B4FA, // bright blue
                0xFFF5C2E7, // bright magenta
                0xFF94E2D5, // bright cyan
                0xFFFFFFFF  // bright white
            };
            return new TerminalThemeColors(0xFFCDD6F4, 0xFF1E1E2E, 0xFFCDD6F4, ansiColors);
        }
        
        private static TerminalThemeColors getCobalt2Colors() {
            int[] ansiColors = {
                0xFF193549, // black
                0xFFFF0000, // red
                0xFF3AD900, // green
                0xFFFFFF00, // yellow
                0xFF0088FF, // blue
                0xFFFF0088, // magenta
                0xFF00FFFF, // cyan
                0xFFFFFFFF, // white
                // Bright variants
                0xFF1F4662, // bright black
                0xFFFF4444, // bright red
                0xFF66FF33, // bright green
                0xFFFFFF88, // bright yellow
                0xFF44AAFF, // bright blue
                0xFFFF44AA, // bright magenta
                0xFF88FFFF, // bright cyan
                0xFFFFFFFF  // bright white
            };
            return new TerminalThemeColors(0xFFFFFFFF, 0xFF193549, 0xFFFFFFFF, ansiColors);
        }
        
        // Light themes
        private static TerminalThemeColors getSolarizedLightColors() {
            int[] ansiColors = {
                0xFF073642, // black
                0xFFDC322F, // red
                0xFF859900, // green
                0xFFB58900, // yellow
                0xFF268BD2, // blue
                0xFFD33682, // magenta
                0xFF2AA198, // cyan
                0xFFEEE8D5, // white
                // Bright variants
                0xFF002B36, // bright black
                0xFFCB4B16, // bright red
                0xFF586E75, // bright green
                0xFF657B83, // bright yellow
                0xFF839496, // bright blue
                0xFF6C71C4, // bright magenta
                0xFF93A1A1, // bright cyan
                0xFFFDF6E3  // bright white
            };
            return new TerminalThemeColors(0xFF586E75, 0xFFFDF6E3, 0xFF586E75, ansiColors);
        }
        
        private static TerminalThemeColors getGruvboxLightColors() {
            int[] ansiColors = {
                0xFFFBF1C7, // black
                0xFFCC241D, // red
                0xFF98971A, // green
                0xFFD79921, // yellow
                0xFF458588, // blue
                0xFFB16286, // magenta
                0xFF689D6A, // cyan
                0xFF7C6F64, // white
                // Bright variants
                0xFF928374, // bright black
                0xFF9D0006, // bright red
                0xFF79740E, // bright green
                0xFFB57614, // bright yellow
                0xFF076678, // bright blue
                0xFF8F3F71, // bright magenta
                0xFF427B58, // bright cyan
                0xFF3C3836  // bright white
            };
            return new TerminalThemeColors(0xFF3C3836, 0xFFFBF1C7, 0xFF3C3836, ansiColors);
        }
        
        private static TerminalThemeColors getCatppuccinLatteColors() {
            int[] ansiColors = {
                0xFF5C5F77, // black
                0xFFD20F39, // red
                0xFF40A02B, // green
                0xFFDF8E1D, // yellow
                0xFF1E66F5, // blue
                0xFFEA76CB, // magenta
                0xFF179299, // cyan
                0xFF4C4F69, // white
                // Bright variants
                0xFF6C6F85, // bright black
                0xFFD20F39, // bright red
                0xFF40A02B, // bright green
                0xFFDF8E1D, // bright yellow
                0xFF1E66F5, // bright blue
                0xFFEA76CB, // bright magenta
                0xFF179299, // bright cyan
                0xFF4C4F69  // bright white
            };
            return new TerminalThemeColors(0xFF4C4F69, 0xFFEFF1F5, 0xFF4C4F69, ansiColors);
        }
        
        private static TerminalThemeColors getTokyoLightColors() {
            int[] ansiColors = {
                0xFFD5D6DB, // black
                0xFF8C4351, // red
                0xFF485E30, // green
                0xFF8F5E15, // yellow
                0xFF34548A, // blue
                0xFF5A4A78, // magenta
                0xFF0F4B6E, // cyan
                0xFF343B58, // white
                // Bright variants
                0xFFCBCCD1, // bright black
                0xFF8C4351, // bright red
                0xFF485E30, // bright green
                0xFF8F5E15, // bright yellow
                0xFF34548A, // bright blue
                0xFF5A4A78, // bright magenta
                0xFF0F4B6E, // bright cyan
                0xFF343B58  // bright white
            };
            return new TerminalThemeColors(0xFF343B58, 0xFFD5D6DB, 0xFF343B58, ansiColors);
        }
        
        private static TerminalThemeColors getNordLightColors() {
            int[] ansiColors = {
                0xFF3B4252, // black
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
                0xFF5E81AC, // bright blue
                0xFFB48EAD, // bright magenta
                0xFF8FBCBB, // bright cyan
                0xFF2E3440  // bright white
            };
            return new TerminalThemeColors(0xFF2E3440, 0xFFECEFF4, 0xFF2E3440, ansiColors);
        }
        
        private static TerminalThemeColors getMaterialLightColors() {
            int[] ansiColors = {
                0xFF212121, // black
                0xFFD32F2F, // red
                0xFF388E3C, // green
                0xFFFBC02D, // yellow
                0xFF1976D2, // blue
                0xFF7B1FA2, // magenta
                0xFF00ACC1, // cyan
                0xFF757575, // white
                // Bright variants
                0xFF424242, // bright black
                0xFFD32F2F, // bright red
                0xFF388E3C, // bright green
                0xFFFBC02D, // bright yellow
                0xFF1976D2, // bright blue
                0xFF7B1FA2, // bright magenta
                0xFF00ACC1, // bright cyan
                0xFF212121  // bright white
            };
            return new TerminalThemeColors(0xFF212121, 0xFFFAFAFA, 0xFF212121, ansiColors);
        }
        
        private static TerminalThemeColors getAtomOneLightColors() {
            int[] ansiColors = {
                0xFF000000, // black
                0xFFE45649, // red
                0xFF50A14F, // green
                0xFFC18401, // yellow
                0xFF4078F2, // blue
                0xFFA626A4, // magenta
                0xFF0184BC, // cyan
                0xFF383A42, // white
                // Bright variants
                0xFF000000, // bright black
                0xFFE45649, // bright red
                0xFF50A14F, // bright green
                0xFFC18401, // bright yellow
                0xFF4078F2, // bright blue
                0xFFA626A4, // bright magenta
                0xFF0184BC, // bright cyan
                0xFF383A42  // bright white
            };
            return new TerminalThemeColors(0xFF383A42, 0xFFFAFAFA, 0xFF383A42, ansiColors);
        }
        
        private static TerminalThemeColors getAyuLightColors() {
            int[] ansiColors = {
                0xFF000000, // black
                0xFFF07178, // red
                0xFF86B300, // green
                0xFFF2AE49, // yellow
                0xFF36A3D9, // blue
                0xFFA37ACC, // magenta
                0xFF4CBF99, // cyan
                0xFF5C6166, // white
                // Bright variants
                0xFF000000, // bright black
                0xFFF07178, // bright red
                0xFF86B300, // bright green
                0xFFF2AE49, // bright yellow
                0xFF36A3D9, // bright blue
                0xFFA37ACC, // bright magenta
                0xFF4CBF99, // bright cyan
                0xFF5C6166  // bright white
            };
            return new TerminalThemeColors(0xFF5C6166, 0xFFFAFAFA, 0xFF5C6166, ansiColors);
        }
        
        private static TerminalThemeColors getPaperColorLightColors() {
            int[] ansiColors = {
                0xFF000000, // black
                0xFFAF0000, // red
                0xFF008700, // green
                0xFF5F8700, // yellow
                0xFF005F87, // blue
                0xFF5F5FAF, // magenta
                0xFF0087AF, // cyan
                0xFF444444, // white
                // Bright variants
                0xFF444444, // bright black
                0xFFAF0000, // bright red
                0xFF008700, // bright green
                0xFF5F8700, // bright yellow
                0xFF005F87, // bright blue
                0xFF5F5FAF, // bright magenta
                0xFF0087AF, // bright cyan
                0xFF444444  // bright white
            };
            return new TerminalThemeColors(0xFF444444, 0xFFEEEEEE, 0xFF444444, ansiColors);
        }
    }
}
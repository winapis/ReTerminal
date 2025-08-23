package com.rk.terminal.ui.theme

import android.app.Activity
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.rk.libcommons.isDarkMode
import com.rk.settings.SettingsManager

/*
 * More Themes
 */

/*
 * Monokai Dark Theme
 */
private val MonokaiDarkColorScheme = darkColorScheme(
    primary = monokai_primary,
    onPrimary = Color.White,
    primaryContainer = monokai_surface,
    onPrimaryContainer = monokai_onSurface,
    secondary = monokai_secondary,
    onSecondary = Color.Black,
    secondaryContainer = monokai_surface,
    onSecondaryContainer = monokai_onSurface,
    tertiary = monokai_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = monokai_surface,
    onTertiaryContainer = monokai_onSurface,
    background = monokai_background,
    onBackground = monokai_onBackground,
    surface = monokai_surface,
    onSurface = monokai_onSurface,
    surfaceVariant = monokai_surface,
    onSurfaceVariant = monokai_onSurface,
    outline = monokai_accent,
    outlineVariant = monokai_surface
)

/*
 * OneDark Theme
 */
private val OneDarkColorScheme = darkColorScheme(
    primary = onedark_primary,
    onPrimary = Color.White,
    primaryContainer = onedark_surface,
    onPrimaryContainer = onedark_onSurface,
    secondary = onedark_secondary,
    onSecondary = Color.Black,
    secondaryContainer = onedark_surface,
    onSecondaryContainer = onedark_onSurface,
    tertiary = onedark_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = onedark_surface,
    onTertiaryContainer = onedark_onSurface,
    background = onedark_background,
    onBackground = onedark_onBackground,
    surface = onedark_surface,
    onSurface = onedark_onSurface,
    surfaceVariant = onedark_surface,
    onSurfaceVariant = onedark_onSurface,
    outline = onedark_accent,
    outlineVariant = onedark_surface
)

/*
 * Dracula Theme
 */
private val DraculaDarkColorScheme = darkColorScheme(
    primary = dracula_primary,
    onPrimary = Color.White,
    primaryContainer = dracula_surface,
    onPrimaryContainer = dracula_onSurface,
    secondary = dracula_secondary,
    onSecondary = Color.Black,
    secondaryContainer = dracula_surface,
    onSecondaryContainer = dracula_onSurface,
    tertiary = dracula_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = dracula_surface,
    onTertiaryContainer = dracula_onSurface,
    background = dracula_background,
    onBackground = dracula_onBackground,
    surface = dracula_surface,
    onSurface = dracula_onSurface,
    surfaceVariant = dracula_surface,
    onSurfaceVariant = dracula_onSurface,
    outline = dracula_accent,
    outlineVariant = dracula_surface
)

/*
 * GitHub Light Theme
 */
private val GitHubLightColorScheme = lightColorScheme(
    primary = github_light_primary,
    onPrimary = Color.White,
    primaryContainer = github_light_surface,
    onPrimaryContainer = github_light_onSurface,
    secondary = github_light_secondary,
    onSecondary = Color.White,
    secondaryContainer = github_light_surface,
    onSecondaryContainer = github_light_onSurface,
    tertiary = github_light_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = github_light_surface,
    onTertiaryContainer = github_light_onSurface,
    background = github_light_background,
    onBackground = github_light_onBackground,
    surface = github_light_surface,
    onSurface = github_light_onSurface,
    surfaceVariant = github_light_surface,
    onSurfaceVariant = github_light_onSurface,
    outline = github_light_accent,
    outlineVariant = github_light_surface
)

/*
 * Extended Dark Themes
 */

// Nord Dark Theme
private val NordDarkColorScheme = darkColorScheme(
    primary = nord_primary,
    onPrimary = Color.White,
    primaryContainer = nord_surface,
    onPrimaryContainer = nord_onSurface,
    secondary = nord_secondary,
    onSecondary = Color.White,
    secondaryContainer = nord_surface,
    onSecondaryContainer = nord_onSurface,
    tertiary = nord_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = nord_surface,
    onTertiaryContainer = nord_onSurface,
    background = nord_background,
    onBackground = nord_onBackground,
    surface = nord_surface,
    onSurface = nord_onSurface,
    surfaceVariant = nord_surface,
    onSurfaceVariant = nord_onSurface,
    outline = nord_accent,
    outlineVariant = nord_surface
)

// Tokyo Night Theme
private val TokyoNightColorScheme = darkColorScheme(
    primary = tokyo_night_primary,
    onPrimary = Color.White,
    primaryContainer = tokyo_night_surface,
    onPrimaryContainer = tokyo_night_onSurface,
    secondary = tokyo_night_secondary,
    onSecondary = Color.White,
    secondaryContainer = tokyo_night_surface,
    onSecondaryContainer = tokyo_night_onSurface,
    tertiary = tokyo_night_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = tokyo_night_surface,
    onTertiaryContainer = tokyo_night_onSurface,
    background = tokyo_night_background,
    onBackground = tokyo_night_onBackground,
    surface = tokyo_night_surface,
    onSurface = tokyo_night_onSurface,
    surfaceVariant = tokyo_night_surface,
    onSurfaceVariant = tokyo_night_onSurface,
    outline = tokyo_night_accent,
    outlineVariant = tokyo_night_surface
)

// Solarized Dark Theme
private val SolarizedDarkColorScheme = darkColorScheme(
    primary = solarized_dark_primary,
    onPrimary = Color.White,
    primaryContainer = solarized_dark_surface,
    onPrimaryContainer = solarized_dark_onSurface,
    secondary = solarized_dark_secondary,
    onSecondary = Color.White,
    secondaryContainer = solarized_dark_surface,
    onSecondaryContainer = solarized_dark_onSurface,
    tertiary = solarized_dark_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = solarized_dark_surface,
    onTertiaryContainer = solarized_dark_onSurface,
    background = solarized_dark_background,
    onBackground = solarized_dark_onBackground,
    surface = solarized_dark_surface,
    onSurface = solarized_dark_onSurface,
    surfaceVariant = solarized_dark_surface,
    onSurfaceVariant = solarized_dark_onSurface,
    outline = solarized_dark_accent,
    outlineVariant = solarized_dark_surface
)

// Gruvbox Dark Theme
private val GruvboxDarkColorScheme = darkColorScheme(
    primary = gruvbox_dark_primary,
    onPrimary = Color.White,
    primaryContainer = gruvbox_dark_surface,
    onPrimaryContainer = gruvbox_dark_onSurface,
    secondary = gruvbox_dark_secondary,
    onSecondary = Color.White,
    secondaryContainer = gruvbox_dark_surface,
    onSecondaryContainer = gruvbox_dark_onSurface,
    tertiary = gruvbox_dark_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = gruvbox_dark_surface,
    onTertiaryContainer = gruvbox_dark_onSurface,
    background = gruvbox_dark_background,
    onBackground = gruvbox_dark_onBackground,
    surface = gruvbox_dark_surface,
    onSurface = gruvbox_dark_onSurface,
    surfaceVariant = gruvbox_dark_surface,
    onSurfaceVariant = gruvbox_dark_onSurface,
    outline = gruvbox_dark_accent,
    outlineVariant = gruvbox_dark_surface
)

// Catppuccin Mocha Theme
private val CatppuccinMochaColorScheme = darkColorScheme(
    primary = catppuccin_mocha_primary,
    onPrimary = Color.White,
    primaryContainer = catppuccin_mocha_surface,
    onPrimaryContainer = catppuccin_mocha_onSurface,
    secondary = catppuccin_mocha_secondary,
    onSecondary = Color.White,
    secondaryContainer = catppuccin_mocha_surface,
    onSecondaryContainer = catppuccin_mocha_onSurface,
    tertiary = catppuccin_mocha_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = catppuccin_mocha_surface,
    onTertiaryContainer = catppuccin_mocha_onSurface,
    background = catppuccin_mocha_background,
    onBackground = catppuccin_mocha_onBackground,
    surface = catppuccin_mocha_surface,
    onSurface = catppuccin_mocha_onSurface,
    surfaceVariant = catppuccin_mocha_surface,
    onSurfaceVariant = catppuccin_mocha_onSurface,
    outline = catppuccin_mocha_accent,
    outlineVariant = catppuccin_mocha_surface
)

// GitHub Dark Theme
private val GitHubDarkColorScheme = darkColorScheme(
    primary = github_dark_primary,
    onPrimary = Color.White,
    primaryContainer = github_dark_surface,
    onPrimaryContainer = github_dark_onSurface,
    secondary = github_dark_secondary,
    onSecondary = Color.White,
    secondaryContainer = github_dark_surface,
    onSecondaryContainer = github_dark_onSurface,
    tertiary = github_dark_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = github_dark_surface,
    onTertiaryContainer = github_dark_onSurface,
    background = github_dark_background,
    onBackground = github_dark_onBackground,
    surface = github_dark_surface,
    onSurface = github_dark_onSurface,
    surfaceVariant = github_dark_surface,
    onSurfaceVariant = github_dark_onSurface,
    outline = github_dark_accent,
    outlineVariant = github_dark_surface
)

// Cobalt2 Theme
private val Cobalt2ColorScheme = darkColorScheme(
    primary = cobalt2_primary,
    onPrimary = Color.White,
    primaryContainer = cobalt2_surface,
    onPrimaryContainer = cobalt2_onSurface,
    secondary = cobalt2_secondary,
    onSecondary = Color.White,
    secondaryContainer = cobalt2_surface,
    onSecondaryContainer = cobalt2_onSurface,
    tertiary = cobalt2_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = cobalt2_surface,
    onTertiaryContainer = cobalt2_onSurface,
    background = cobalt2_background,
    onBackground = cobalt2_onBackground,
    surface = cobalt2_surface,
    onSurface = cobalt2_onSurface,
    surfaceVariant = cobalt2_surface,
    onSurfaceVariant = cobalt2_onSurface,
    outline = cobalt2_accent,
    outlineVariant = cobalt2_surface
)

/*
 * Extended Light Themes
 */

// Solarized Light Theme
private val SolarizedLightColorScheme = lightColorScheme(
    primary = solarized_light_primary,
    onPrimary = Color.White,
    primaryContainer = solarized_light_surface,
    onPrimaryContainer = solarized_light_onSurface,
    secondary = solarized_light_secondary,
    onSecondary = Color.White,
    secondaryContainer = solarized_light_surface,
    onSecondaryContainer = solarized_light_onSurface,
    tertiary = solarized_light_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = solarized_light_surface,
    onTertiaryContainer = solarized_light_onSurface,
    background = solarized_light_background,
    onBackground = solarized_light_onBackground,
    surface = solarized_light_surface,
    onSurface = solarized_light_onSurface,
    surfaceVariant = solarized_light_surface,
    onSurfaceVariant = solarized_light_onSurface,
    outline = solarized_light_accent,
    outlineVariant = solarized_light_surface
)

// Gruvbox Light Theme
private val GruvboxLightColorScheme = lightColorScheme(
    primary = gruvbox_light_primary,
    onPrimary = Color.White,
    primaryContainer = gruvbox_light_surface,
    onPrimaryContainer = gruvbox_light_onSurface,
    secondary = gruvbox_light_secondary,
    onSecondary = Color.White,
    secondaryContainer = gruvbox_light_surface,
    onSecondaryContainer = gruvbox_light_onSurface,
    tertiary = gruvbox_light_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = gruvbox_light_surface,
    onTertiaryContainer = gruvbox_light_onSurface,
    background = gruvbox_light_background,
    onBackground = gruvbox_light_onBackground,
    surface = gruvbox_light_surface,
    onSurface = gruvbox_light_onSurface,
    surfaceVariant = gruvbox_light_surface,
    onSurfaceVariant = gruvbox_light_onSurface,
    outline = gruvbox_light_accent,
    outlineVariant = gruvbox_light_surface
)

// Catppuccin Latte Theme
private val CatppuccinLatteColorScheme = lightColorScheme(
    primary = catppuccin_latte_primary,
    onPrimary = Color.White,
    primaryContainer = catppuccin_latte_surface,
    onPrimaryContainer = catppuccin_latte_onSurface,
    secondary = catppuccin_latte_secondary,
    onSecondary = Color.White,
    secondaryContainer = catppuccin_latte_surface,
    onSecondaryContainer = catppuccin_latte_onSurface,
    tertiary = catppuccin_latte_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = catppuccin_latte_surface,
    onTertiaryContainer = catppuccin_latte_onSurface,
    background = catppuccin_latte_background,
    onBackground = catppuccin_latte_onBackground,
    surface = catppuccin_latte_surface,
    onSurface = catppuccin_latte_onSurface,
    surfaceVariant = catppuccin_latte_surface,
    onSurfaceVariant = catppuccin_latte_onSurface,
    outline = catppuccin_latte_accent,
    outlineVariant = catppuccin_latte_surface
)

// Tokyo Light Theme
private val TokyoLightColorScheme = lightColorScheme(
    primary = tokyo_light_primary,
    onPrimary = Color.White,
    primaryContainer = tokyo_light_surface,
    onPrimaryContainer = tokyo_light_onSurface,
    secondary = tokyo_light_secondary,
    onSecondary = Color.White,
    secondaryContainer = tokyo_light_surface,
    onSecondaryContainer = tokyo_light_onSurface,
    tertiary = tokyo_light_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = tokyo_light_surface,
    onTertiaryContainer = tokyo_light_onSurface,
    background = tokyo_light_background,
    onBackground = tokyo_light_onBackground,
    surface = tokyo_light_surface,
    onSurface = tokyo_light_onSurface,
    surfaceVariant = tokyo_light_surface,
    onSurfaceVariant = tokyo_light_onSurface,
    outline = tokyo_light_accent,
    outlineVariant = tokyo_light_surface
)

// Nord Light Theme
private val NordLightColorScheme = lightColorScheme(
    primary = nord_light_primary,
    onPrimary = Color.White,
    primaryContainer = nord_light_surface,
    onPrimaryContainer = nord_light_onSurface,
    secondary = nord_light_secondary,
    onSecondary = Color.White,
    secondaryContainer = nord_light_surface,
    onSecondaryContainer = nord_light_onSurface,
    tertiary = nord_light_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = nord_light_surface,
    onTertiaryContainer = nord_light_onSurface,
    background = nord_light_background,
    onBackground = nord_light_onBackground,
    surface = nord_light_surface,
    onSurface = nord_light_onSurface,
    surfaceVariant = nord_light_surface,
    onSurfaceVariant = nord_light_onSurface,
    outline = nord_light_accent,
    outlineVariant = nord_light_surface
)

// Material Light Theme
private val MaterialLightColorScheme = lightColorScheme(
    primary = material_light_primary,
    onPrimary = Color.White,
    primaryContainer = material_light_surface,
    onPrimaryContainer = material_light_onSurface,
    secondary = material_light_secondary,
    onSecondary = Color.White,
    secondaryContainer = material_light_surface,
    onSecondaryContainer = material_light_onSurface,
    tertiary = material_light_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = material_light_surface,
    onTertiaryContainer = material_light_onSurface,
    background = material_light_background,
    onBackground = material_light_onBackground,
    surface = material_light_surface,
    onSurface = material_light_onSurface,
    surfaceVariant = material_light_surface,
    onSurfaceVariant = material_light_onSurface,
    outline = material_light_accent,
    outlineVariant = material_light_surface
)

// Atom One Light Theme
private val AtomOneLightColorScheme = lightColorScheme(
    primary = atom_one_light_primary,
    onPrimary = Color.White,
    primaryContainer = atom_one_light_surface,
    onPrimaryContainer = atom_one_light_onSurface,
    secondary = atom_one_light_secondary,
    onSecondary = Color.White,
    secondaryContainer = atom_one_light_surface,
    onSecondaryContainer = atom_one_light_onSurface,
    tertiary = atom_one_light_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = atom_one_light_surface,
    onTertiaryContainer = atom_one_light_onSurface,
    background = atom_one_light_background,
    onBackground = atom_one_light_onBackground,
    surface = atom_one_light_surface,
    onSurface = atom_one_light_onSurface,
    surfaceVariant = atom_one_light_surface,
    onSurfaceVariant = atom_one_light_onSurface,
    outline = atom_one_light_accent,
    outlineVariant = atom_one_light_surface
)

// Ayu Light Theme
private val AyuLightColorScheme = lightColorScheme(
    primary = ayu_light_primary,
    onPrimary = Color.White,
    primaryContainer = ayu_light_surface,
    onPrimaryContainer = ayu_light_onSurface,
    secondary = ayu_light_secondary,
    onSecondary = Color.White,
    secondaryContainer = ayu_light_surface,
    onSecondaryContainer = ayu_light_onSurface,
    tertiary = ayu_light_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = ayu_light_surface,
    onTertiaryContainer = ayu_light_onSurface,
    background = ayu_light_background,
    onBackground = ayu_light_onBackground,
    surface = ayu_light_surface,
    onSurface = ayu_light_onSurface,
    surfaceVariant = ayu_light_surface,
    onSurfaceVariant = ayu_light_onSurface,
    outline = ayu_light_accent,
    outlineVariant = ayu_light_surface
)

// PaperColor Light Theme
private val PaperColorLightColorScheme = lightColorScheme(
    primary = papercolor_light_primary,
    onPrimary = Color.White,
    primaryContainer = papercolor_light_surface,
    onPrimaryContainer = papercolor_light_onSurface,
    secondary = papercolor_light_secondary,
    onSecondary = Color.White,
    secondaryContainer = papercolor_light_surface,
    onSecondaryContainer = papercolor_light_onSurface,
    tertiary = papercolor_light_tertiary,
    onTertiary = Color.White,
    tertiaryContainer = papercolor_light_surface,
    onTertiaryContainer = papercolor_light_onSurface,
    background = papercolor_light_background,
    onBackground = papercolor_light_onBackground,
    surface = papercolor_light_surface,
    onSurface = papercolor_light_onSurface,
    surfaceVariant = papercolor_light_surface,
    onSurfaceVariant = papercolor_light_onSurface,
    outline = papercolor_light_accent,
    outlineVariant = papercolor_light_surface
)

/*
 * Light blue color scheme definition.
 */
private val LightColorScheme =
    lightColorScheme(
        primary = md_theme_light_primary,
        onPrimary = md_theme_light_onPrimary,
        primaryContainer = md_theme_light_primaryContainer,
        onPrimaryContainer = md_theme_light_onPrimaryContainer,
        secondary = md_theme_light_secondary,
        onSecondary = md_theme_light_onSecondary,
        secondaryContainer = md_theme_light_secondaryContainer,
        onSecondaryContainer = md_theme_light_onSecondaryContainer,
        tertiary = md_theme_light_tertiary,
        onTertiary = md_theme_light_onTertiary,
        tertiaryContainer = md_theme_light_tertiaryContainer,
        onTertiaryContainer = md_theme_light_onTertiaryContainer,
        error = md_theme_light_error,
        errorContainer = md_theme_light_errorContainer,
        onError = md_theme_light_onError,
        onErrorContainer = md_theme_light_onErrorContainer,
        background = md_theme_light_background,
        onBackground = md_theme_light_onBackground,
        surface = md_theme_light_surface,
        onSurface = md_theme_light_onSurface,
        surfaceVariant = md_theme_light_surfaceVariant,
        onSurfaceVariant = md_theme_light_onSurfaceVariant,
        outline = md_theme_light_outline,
        inverseOnSurface = md_theme_light_inverseOnSurface,
        inverseSurface = md_theme_light_inverseSurface,
        inversePrimary = md_theme_light_inversePrimary,
        surfaceTint = md_theme_light_surfaceTint,
        outlineVariant = md_theme_light_outlineVariant,
        scrim = md_theme_light_scrim,
    )

/*
 * Dark blue color scheme definition.
 */
private val DarkColorScheme =
    darkColorScheme(
        primary = md_theme_dark_primary,
        onPrimary = md_theme_dark_onPrimary,
        primaryContainer = md_theme_dark_primaryContainer,
        onPrimaryContainer = md_theme_dark_onPrimaryContainer,
        secondary = md_theme_dark_secondary,
        onSecondary = md_theme_dark_onSecondary,
        secondaryContainer = md_theme_dark_secondaryContainer,
        onSecondaryContainer = md_theme_dark_onSecondaryContainer,
        tertiary = md_theme_dark_tertiary,
        onTertiary = md_theme_dark_onTertiary,
        tertiaryContainer = md_theme_dark_tertiaryContainer,
        onTertiaryContainer = md_theme_dark_onTertiaryContainer,
        error = md_theme_dark_error,
        errorContainer = md_theme_dark_errorContainer,
        onError = md_theme_dark_onError,
        onErrorContainer = md_theme_dark_onErrorContainer,
        background = md_theme_dark_background,
        onBackground = md_theme_dark_onBackground,
        surface = md_theme_dark_surface,
        onSurface = md_theme_dark_onSurface,
        surfaceVariant = md_theme_dark_surfaceVariant,
        onSurfaceVariant = md_theme_dark_onSurfaceVariant,
        outline = md_theme_dark_outline,
        inverseOnSurface = md_theme_dark_inverseOnSurface,
        inverseSurface = md_theme_dark_inverseSurface,
        inversePrimary = md_theme_dark_inversePrimary,
        surfaceTint = md_theme_dark_surfaceTint,
        outlineVariant = md_theme_dark_outlineVariant,
        scrim = md_theme_dark_scrim,
    )


@Composable
fun KarbonTheme(
    darkTheme: Boolean = when (SettingsManager.Appearance.defaultNightMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> isDarkMode(LocalContext.current)
    },
    highContrastDarkTheme: Boolean = SettingsManager.Appearance.amoled,
    dynamicColor: Boolean = SettingsManager.Appearance.monet,
    content: @Composable () -> Unit,
) {
    // Use global theme state for reactive changes
    val currentTheme by ThemeState.currentTheme
    
    val colorScheme = when {
        // If user has selected a custom theme (non-zero), use it regardless of dynamic color settings
        currentTheme != 0 -> {
            when (currentTheme) {
                // Dark Themes (1-10)
                1 -> DraculaDarkColorScheme
                2 -> OneDarkColorScheme
                3 -> NordDarkColorScheme
                4 -> TokyoNightColorScheme
                5 -> SolarizedDarkColorScheme
                6 -> MonokaiDarkColorScheme // Monokai Pro
                7 -> GitHubDarkColorScheme
                8 -> GruvboxDarkColorScheme
                9 -> CatppuccinMochaColorScheme
                10 -> Cobalt2ColorScheme
                
                // Light Themes (11-20)
                11 -> SolarizedLightColorScheme
                12 -> GitHubLightColorScheme
                13 -> GruvboxLightColorScheme
                14 -> CatppuccinLatteColorScheme
                15 -> TokyoLightColorScheme
                16 -> NordLightColorScheme
                17 -> MaterialLightColorScheme
                18 -> AtomOneLightColorScheme
                19 -> AyuLightColorScheme
                20 -> PaperColorLightColorScheme
                
                else -> {
                    // Fallback to default theme behavior for unknown theme IDs
                    when {
                        darkTheme && highContrastDarkTheme ->
                            DarkColorScheme.copy(background = Color.Black, surface = Color.Black)
                        darkTheme -> DarkColorScheme
                        else -> LightColorScheme
                    }
                }
            }
        }
        
        // If system theme (0) is selected and dynamic color is enabled, use dynamic colors
        dynamicColor && supportsDynamicTheming() -> {
            val context = LocalContext.current
            when {
                darkTheme && highContrastDarkTheme ->
                    dynamicDarkColorScheme(context)
                        .copy(background = Color.Black, surface = Color.Black)
                darkTheme -> dynamicDarkColorScheme(context)
                else -> dynamicLightColorScheme(context)
            }
        }
        
        // Default system themes when no custom theme and no dynamic colors
        else -> {
            when {
                darkTheme && highContrastDarkTheme ->
                    DarkColorScheme.copy(background = Color.Black, surface = Color.Black)
                darkTheme -> DarkColorScheme
                else -> LightColorScheme
            }
        }
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).apply {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    // Update status bar appearance based on theme
                    isAppearanceLightStatusBars = when (currentTheme) {
                        // Dark themes (1-10) should have light content on status bar
                        1, 2, 3, 4, 5, 6, 7, 8, 9, 10 -> false
                        // Light themes (11-20) should have dark content on status bar
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20 -> true
                        else -> !darkTheme // Default behavior
                    }
                    isAppearanceLightNavigationBars = when (currentTheme) {
                        // Dark themes (1-10) should have light content on navigation bar
                        1, 2, 3, 4, 5, 6, 7, 8, 9, 10 -> false
                        // Light themes (11-20) should have dark content on navigation bar
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20 -> true
                        else -> !darkTheme // Default behavior
                    }
                }
            }
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun supportsDynamicTheming() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

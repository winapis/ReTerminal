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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.rk.libcommons.isDarkMode
import com.rk.settings.Settings

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
    darkTheme: Boolean = when (Settings.default_night_mode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> isDarkMode(LocalContext.current)
    },
    highContrastDarkTheme: Boolean = Settings.amoled,
    dynamicColor: Boolean = Settings.monet,
    content: @Composable () -> Unit,
) {
    val selectedTheme = Settings.selected_theme
    val colorScheme = when {
        // If dynamic color is enabled and supported, use it (overrides custom themes)
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
        
        // Custom theme selection based on Settings.selected_theme (new ThemeManagerJava)
        else -> {
            when (selectedTheme) {
                1 -> DraculaDarkColorScheme // Dracula (always dark)
                2 -> OneDarkColorScheme // OneDark (always dark)
                3 -> MonokaiDarkColorScheme // Nord Dark (placeholder, using Monokai)
                4 -> MonokaiDarkColorScheme // Tokyo Night (placeholder, using Monokai)
                5 -> MonokaiDarkColorScheme // Solarized Dark (placeholder, using Monokai)
                6 -> MonokaiDarkColorScheme // Monokai Pro
                7 -> MonokaiDarkColorScheme // GitHub Dark (placeholder, using Monokai)
                8 -> MonokaiDarkColorScheme // Gruvbox Dark (placeholder, using Monokai)
                9 -> MonokaiDarkColorScheme // Catppuccin Mocha (placeholder, using Monokai)
                10 -> MonokaiDarkColorScheme // Cobalt2 (placeholder, using Monokai)
                11 -> LightColorScheme // Solarized Light (placeholder, using default light)
                12 -> GitHubLightColorScheme // GitHub Light
                13 -> LightColorScheme // Gruvbox Light (placeholder, using default light)
                14 -> LightColorScheme // Catppuccin Latte (placeholder, using default light)
                15 -> LightColorScheme // Tokyo Light (placeholder, using default light)
                16 -> LightColorScheme // Nord Light (placeholder, using default light)
                17 -> LightColorScheme // Material Light (placeholder, using default light)
                18 -> LightColorScheme // Atom One Light (placeholder, using default light)
                19 -> LightColorScheme // Ayu Light (placeholder, using default light)
                20 -> LightColorScheme // PaperColor Light (placeholder, using default light)
                else -> {
                    // Default themes based on dark/light mode
                    when {
                        darkTheme && highContrastDarkTheme ->
                            DarkColorScheme.copy(background = Color.Black, surface = Color.Black)
                        darkTheme -> DarkColorScheme
                        else -> LightColorScheme
                    }
                }
            }
        }
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            (view.context as Activity).apply {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    // Update status bar appearance based on theme
                    val isDarkTheme = selectedTheme in 1..10 // Dark themes are 1-10
                    isAppearanceLightStatusBars = !isDarkTheme
                    isAppearanceLightNavigationBars = !isDarkTheme
                }
            }
        }
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.S)
fun supportsDynamicTheming() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

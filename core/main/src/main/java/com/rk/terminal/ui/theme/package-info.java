package com.rk.terminal.ui.theme;

/**
 * Modern Theme System Documentation
 * 
 * This package contains a comprehensive theme management system with the following components:
 * 
 * Theme Management Architecture:
 * ├── ModernThemeManager - Central theme coordinator
 * │   ├── getAllThemes() - Returns all available themes
 * │   ├── applyTheme() - Applies selected theme
 * │   ├── getCurrentTheme() - Gets active theme info
 * │   └── ThemeInfo data class - Theme metadata
 * │
 * ├── ThemeState - Reactive theme state management
 * │   ├── currentTheme - Observable theme ID
 * │   ├── updateTheme() - Updates theme with recomposition
 * │   ├── isDarkModeEnabled() - Check dark mode status
 * │   ├── isAmoledEnabled() - Check AMOLED mode
 * │   └── isDynamicThemingEnabled() - Check Material You status
 * │
 * ├── Theme.kt - Theme definitions and KarbonTheme composable
 * │   ├── Color schemes for all themes (20+ themes available)
 * │   ├── Material Design 3 compliant color tokens
 * │   ├── Dynamic theming support (Android 12+)
 * │   └── AMOLED mode support
 * │
 * ├── Color.kt - Color definitions and palettes
 * │   ├── Theme-specific color definitions
 * │   ├── Material Design 3 color tokens
 * │   └── Accessibility compliant color combinations
 * │
 * └── Type.kt - Typography system
 *     ├── Material Design 3 typography scale
 *     ├── Adaptive font sizing
 *     └── Accessibility support
 * 
 * Available Themes:
 * 
 * System Themes:
 * 0 - System Default (follows system theme)
 * 
 * Dark Themes (1-10):
 * 1 - Dracula (Dark theme for hackers)
 * 2 - One Dark (Atom's iconic theme)
 * 3 - Nord (Arctic color palette)
 * 4 - Tokyo Night (Clean dark theme)
 * 5 - Solarized Dark (Precision colors)
 * 6 - Monokai Pro (Professional coding)
 * 7 - GitHub Dark (GitHub's dark mode)
 * 8 - Gruvbox Dark (Retro groove)
 * 9 - Catppuccin Mocha (Soothing pastels)
 * 10 - Cobalt2 (Blue-based coding)
 * 
 * Light Themes (11-20):
 * 11 - Solarized Light
 * 12 - GitHub Light
 * 13 - Gruvbox Light
 * 14 - Catppuccin Latte
 * 15 - Tokyo Light
 * 16 - Nord Light
 * 17 - Material Light
 * 18 - Atom One Light
 * 19 - Ayu Light
 * 20 - PaperColor Light
 * 
 * Key Features:
 * 
 * 1. Reactive Theme Changes:
 *    - Instant theme switching without restart
 *    - Global state management with Compose
 *    - Automatic UI recomposition on theme change
 * 
 * 2. Material Design 3 Compliance:
 *    - Proper color token usage
 *    - Semantic color naming
 *    - Accessibility contrast requirements
 *    - Dynamic color support (Android 12+)
 * 
 * 3. Advanced Theme Options:
 *    - AMOLED mode for pure black backgrounds
 *    - Theme variant override (force light/dark)
 *    - Material You integration
 *    - High contrast mode support
 * 
 * 4. Performance Optimizations:
 *    - Efficient color scheme caching
 *    - Minimal recomposition impact
 *    - Lazy theme loading
 * 
 * Usage Examples:
 * 
 * // Apply a theme
 * ModernThemeManager.applyTheme(context, 3) // Nord theme
 * 
 * // Get current theme information
 * val currentTheme = ModernThemeManager.getCurrentTheme()
 * println("Current theme: ${currentTheme.name}")
 * 
 * // Check theme state
 * if (ThemeState.isDarkModeEnabled()) {
 *     // Handle dark mode specific logic
 * }
 * 
 * // Use in Composable
 * @Composable
 * fun MyScreen() {
 *     KarbonTheme { // Automatically applies current theme
 *         // Your UI content
 *     }
 * }
 * 
 * Integration with Settings:
 * - Themes are stored in SettingsManager.Appearance.colorScheme
 * - AMOLED mode in SettingsManager.Appearance.amoled
 * - Dynamic colors in SettingsManager.Appearance.monet
 * - Theme variant in SettingsManager.Appearance.themeVariant
 * 
 * @author ReTerminal Team
 * @since 2024
 */
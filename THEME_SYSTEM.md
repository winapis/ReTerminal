# ReTerminal Theme System

This document describes the new comprehensive theming system implemented for the ReTerminal Android app.

## Overview

The new theme system provides 20 curated themes (10 dark, 10 light) with complete integration between the UI and terminal emulator. Each theme is carefully designed to provide excellent contrast and readability while maintaining aesthetic appeal.

## Architecture

### Core Components

1. **ThemeManager.java** - Central theme management in Java
2. **Theme.kt** - Compose MaterialTheme integration 
3. **Color.kt** - Theme color definitions
4. **themes_dark.xml / themes_light.xml** - XML theme resources
5. **ThemeUtils.kt** - Utility functions and examples

### Key Features

- **Centralized Management**: Single ThemeManager handles all theme operations
- **Persistence**: Theme selection saved in SharedPreferences
- **Terminal Integration**: UI themes automatically sync with terminal colors
- **Backward Compatibility**: Works with existing Settings system
- **Material Design**: Full Material 3 theme support

## Available Themes

### Dark Themes (10)
1. **Dracula** (Default) - Purple accents, popular developer theme
2. **One Dark** - VS Code's popular dark theme
3. **Nord** - Arctic-inspired cool blue tones
4. **Tokyo Night** - Modern theme with vibrant highlights
5. **Solarized Dark** - Classic low-contrast theme
6. **Monokai Pro** - Enhanced Monokai with better contrast
7. **GitHub Dark** - GitHub's official dark theme
8. **Gruvbox Dark** - Retro groove with warm colors
9. **Catppuccin Mocha** - Warm theme with pastel colors
10. **Cobalt2** - Electric blue high-contrast theme

### Light Themes (10)
1. **Solarized Light** - Classic low-contrast light theme
2. **GitHub Light** - GitHub's clean interface theme
3. **Gruvbox Light** - Retro groove light variant
4. **Catppuccin Latte** - Soft pastel light theme
5. **Tokyo Light** - Clean minimal light theme
6. **Nord Light** - Arctic-inspired light variant
7. **Material Light** - Google Material Design
8. **Atom One Light** - Atom editor's light theme
9. **Ayu Light** - Minimal theme with excellent readability
10. **PaperColor Light** - High contrast for clear visibility

## Usage

### Basic Theme Switching

```kotlin
// Switch to a specific theme
ThemeUtils.switchTheme(context, ThemeManager.ThemeId.DRACULA)

// Get current theme info
val currentTheme = ThemeUtils.getCurrentTheme(context)

// Cycle through popular themes (for testing)
ThemeUtils.cyclePopularThemes(context)
```

### In Activities

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply theme before setContent
        val themeManager = ThemeManager.getInstance(this)
        themeManager.applyTheme(this)
        
        setContent {
            KarbonTheme {
                // Your UI content
            }
        }
    }
}
```

### Getting Theme Information

```kotlin
// Get all available themes
val allThemes = ThemeUtils.getAllThemes()
val darkThemes = ThemeUtils.getDarkThemes()
val lightThemes = ThemeUtils.getLightThemes()

// Theme information includes name and description
allThemes.forEach { theme ->
    println("${theme.name}: ${theme.getDescription(context)}")
}
```

## Integration Points

### 1. Activity Theme Application
Themes are applied in Activity.onCreate() before setContentView() using:
```kotlin
ThemeManager.getInstance(this).applyTheme(this)
```

### 2. Compose Integration
The KarbonTheme Composable automatically uses the current theme:
```kotlin
@Composable
fun KarbonTheme(content: @Composable () -> Unit) {
    val themeManager = ThemeManager.getInstance(LocalContext.current)
    val colorScheme = getColorSchemeForTheme(themeManager.currentTheme)
    MaterialTheme(colorScheme = colorScheme, content = content)
}
```

### 3. Terminal Color Synchronization
Terminal colors are automatically updated when themes change:
```kotlin
// Colors are applied automatically, but can be manually triggered
themeManager.applyTerminalColors()
```

## Theme Structure

Each theme defines:
- **Primary colors**: Main brand colors and their variants
- **Secondary colors**: Supporting colors for variety
- **Surface colors**: Backgrounds and containers
- **ANSI colors**: 16 terminal colors (black, red, green, yellow, blue, magenta, cyan, white + bright variants)
- **Special colors**: Foreground, background, cursor

## WCAG Compliance

All themes are designed with accessibility in mind:
- Minimum 4.5:1 contrast ratio for normal text
- Minimum 3:1 contrast ratio for large text
- Clear color differentiation for terminal syntax highlighting
- Appropriate cursor visibility on all backgrounds

## Settings Integration

The theme system integrates with the existing Settings:
- `Settings.color_scheme` maintained for backward compatibility
- New themes stored in separate SharedPreferences
- Automatic migration of legacy theme selections

## Technical Notes

### Performance
- Themes are cached in memory for quick access
- Color schemes are pre-computed for all themes
- Terminal color updates use efficient native methods

### Thread Safety
- ThemeManager uses singleton pattern with synchronized access
- SharedPreferences operations are thread-safe
- UI updates are performed on main thread

### Memory Usage
- Minimal memory footprint with color value caching
- No bitmap resources required for themes
- Efficient color array management for terminal integration

## Future Enhancements

Potential improvements for future versions:
1. **Custom Theme Creation**: Allow users to create and save custom themes
2. **Theme Import/Export**: Share themes between devices
3. **Automatic Theme Switching**: Based on time of day or system settings
4. **Theme Preview**: Live preview before applying themes
5. **Color Picker Integration**: Fine-tune individual colors

## Migration Guide

For existing code using the old theme system:

### Before
```kotlin
// Old approach
when (Settings.color_scheme) {
    1 -> MonokaiTheme
    2 -> OneDarkTheme
    // ...
}
```

### After
```kotlin
// New approach
val themeManager = ThemeManager.getInstance(context)
val currentTheme = themeManager.currentTheme
// Themes are automatically applied via KarbonTheme
```

The new system maintains backward compatibility while providing much more flexibility and better integration.
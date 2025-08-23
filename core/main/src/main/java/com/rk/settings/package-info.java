package com.rk.settings;

/**
 * Settings categories and organization documentation
 * 
 * This package now contains a well-organized settings management system
 * with the following structure:
 * 
 * SettingsManager (Main settings organization)
 * ├── Appearance (Theme and visual settings)
 * │   ├── amoled - OLED black mode
 * │   ├── monet - Material You dynamic colors
 * │   ├── defaultNightMode - Dark/light mode preference
 * │   ├── colorScheme - Active theme ID
 * │   └── themeVariant - Theme override (auto/light/dark)
 * │
 * ├── Terminal (Terminal-specific settings)
 * │   ├── fontSize - Text size in SP
 * │   ├── opacity - Background transparency
 * │   ├── cursorStyle - Cursor appearance (block/underline/bar)
 * │   ├── blackTextColor - Force dark text
 * │   ├── customBackgroundName - Custom background image
 * │   └── customFontName - Custom font file
 * │
 * ├── Interface (UI element visibility)
 * │   ├── statusBar - Show status bar
 * │   ├── horizontalStatusBar - Status bar in landscape
 * │   ├── toolbar - Show toolbar
 * │   ├── toolbarInHorizontal - Toolbar in landscape
 * │   ├── virtualKeys - Show virtual keyboard
 * │   └── hideSoftKeyboardIfHardware - Auto-hide soft keyboard
 * │
 * ├── System (System-level configuration)
 * │   ├── workingMode - Default Linux distribution
 * │   ├── graphicsAcceleration - Hardware acceleration
 * │   ├── ignoreStoragePermission - Skip storage permission
 * │   └── githubIntegration - GitHub features enabled
 * │
 * ├── Root (Root access configuration)
 * │   ├── enabled - Root access enabled
 * │   ├── provider - Root method (su/magisk/etc)
 * │   ├── busyboxInstalled - BusyBox availability
 * │   ├── verified - Root access verified
 * │   ├── busyboxPath - BusyBox binary location
 * │   └── useMounts - Enhanced filesystem mounts
 * │
 * └── Feedback (User feedback settings)
 *     ├── bell - Terminal bell sound
 *     └── vibrate - Haptic feedback
 * 
 * Migration and Compatibility:
 * - Automatic migration from old settings structure
 * - Backward compatibility through Settings object
 * - Preserves all user preferences during upgrade
 * - Type-safe access with organized categories
 * 
 * Usage Examples:
 * 
 * // New organized approach (recommended)
 * SettingsManager.Appearance.colorScheme = 5
 * SettingsManager.Terminal.fontSize = 14
 * SettingsManager.Interface.statusBar = true
 * 
 * // Legacy compatibility (still works)
 * Settings.color_scheme = 5
 * Settings.terminal_font_size = 14
 * Settings.statusBar = true
 * 
 * @author ReTerminal Team
 * @since 2024
 */
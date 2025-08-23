package com.rk.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.rk.components.compose.preferences.normal.Preference
import com.rk.libcommons.application
import com.rk.terminal.ui.screens.settings.WorkingMode

/**
 * Centralized settings manager with organized categories and improved structure
 */
object SettingsManager {
    
    /**
     * Appearance Settings
     */
    object Appearance {
        var amoled
            get() = Preference.getBoolean(key = "appearance_amoled", default = false)
            set(value) = Preference.setBoolean(key = "appearance_amoled", value)
        
        var monet
            get() = Preference.getBoolean(
                key = "appearance_monet",
                default = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            )
            set(value) = Preference.setBoolean(key = "appearance_monet", value)
        
        var defaultNightMode
            get() = Preference.getInt(key = "appearance_night_mode", default = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            set(value) = Preference.setInt(key = "appearance_night_mode", value)
        
        var colorScheme
            get() = Preference.getInt(key = "appearance_color_scheme", default = 0)
            set(value) = Preference.setInt(key = "appearance_color_scheme", value)
        
        var themeVariant
            get() = Preference.getInt(key = "appearance_theme_variant", default = 0)
            set(value) = Preference.setInt(key = "appearance_theme_variant", value)
    }
    
    /**
     * Terminal Settings
     */
    object Terminal {
        var fontSize
            get() = Preference.getInt(key = "terminal_font_size", default = 13)
            set(value) = Preference.setInt(key = "terminal_font_size", value)
        
        var opacity
            get() = Preference.getFloat(key = "terminal_opacity", default = 1.0f)
            set(value) = Preference.setFloat(key = "terminal_opacity", value)
        
        var cursorStyle
            get() = Preference.getInt(key = "terminal_cursor_style", default = 0) // 0=block, 1=underline, 2=bar
            set(value) = Preference.setInt(key = "terminal_cursor_style", value)
        
        var blackTextColor
            get() = Preference.getBoolean(key = "terminal_black_text", default = false)
            set(value) = Preference.setBoolean(key = "terminal_black_text", value)
        
        var customBackgroundName
            get() = Preference.getString(key = "terminal_custom_bg_name", default = "No Image Selected")
            set(value) = Preference.setString(key = "terminal_custom_bg_name", value)
        
        var customFontName
            get() = Preference.getString(key = "terminal_custom_font_name", default = "No Font Selected")
            set(value) = Preference.setString(key = "terminal_custom_font_name", value)
    }
    
    /**
     * Interface Settings
     */
    object Interface {
        var statusBar
            get() = Preference.getBoolean(key = "interface_status_bar", default = true)
            set(value) = Preference.setBoolean(key = "interface_status_bar", value)
        
        var horizontalStatusBar
            get() = Preference.getBoolean(key = "interface_horizontal_status_bar", default = true)
            set(value) = Preference.setBoolean(key = "interface_horizontal_status_bar", value)
        
        var toolbar
            get() = Preference.getBoolean(key = "interface_toolbar", default = true)
            set(value) = Preference.setBoolean(key = "interface_toolbar", value)
        
        var toolbarInHorizontal
            get() = Preference.getBoolean(key = "interface_toolbar_horizontal", default = true)
            set(value) = Preference.setBoolean(key = "interface_toolbar_horizontal", value)
        
        var virtualKeys
            get() = Preference.getBoolean(key = "interface_virtual_keys", default = true)
            set(value) = Preference.setBoolean(key = "interface_virtual_keys", value)
        
        var hideSoftKeyboardIfHardware
            get() = Preference.getBoolean(key = "interface_hide_soft_keyboard", default = true)
            set(value) = Preference.setBoolean(key = "interface_hide_soft_keyboard", value)
    }
    
    /**
     * System Settings
     */
    object System {
        var workingMode
            get() = Preference.getInt(key = "system_working_mode", default = WorkingMode.ALPINE)
            set(value) = Preference.setInt(key = "system_working_mode", value)
        
        var graphicsAcceleration
            get() = Preference.getBoolean(key = "system_graphics_acceleration", default = false)
            set(value) = Preference.setBoolean(key = "system_graphics_acceleration", value)
        
        var ignoreStoragePermission
            get() = Preference.getBoolean(key = "system_ignore_storage_permission", default = false)
            set(value) = Preference.setBoolean(key = "system_ignore_storage_permission", value)
        
        var githubIntegration
            get() = Preference.getBoolean(key = "system_github", default = true)
            set(value) = Preference.setBoolean(key = "system_github", value)
    }
    
    /**
     * Root Configuration Settings
     */
    object Root {
        var enabled
            get() = Preference.getBoolean(key = "root_enabled", default = false)
            set(value) = Preference.setBoolean(key = "root_enabled", value)
        
        var provider
            get() = Preference.getString(key = "root_provider", default = "none")
            set(value) = Preference.setString(key = "root_provider", value)
        
        var busyboxInstalled
            get() = Preference.getBoolean(key = "root_busybox_installed", default = false)
            set(value) = Preference.setBoolean(key = "root_busybox_installed", value)
        
        var verified
            get() = Preference.getBoolean(key = "root_verified", default = false)
            set(value) = Preference.setBoolean(key = "root_verified", value)
        
        var busyboxPath
            get() = Preference.getString(key = "root_busybox_path", default = "")
            set(value) = Preference.setString(key = "root_busybox_path", value)
        
        var useMounts
            get() = Preference.getBoolean(key = "root_use_mounts", default = false)
            set(value) = Preference.setBoolean(key = "root_use_mounts", value)
    }
    
    /**
     * Feedback Settings
     */
    object Feedback {
        var bell
            get() = Preference.getBoolean(key = "feedback_bell", default = false)
            set(value) = Preference.setBoolean(key = "feedback_bell", value)
        
        var vibrate
            get() = Preference.getBoolean(key = "feedback_vibrate", default = true)
            set(value) = Preference.setBoolean(key = "feedback_vibrate", value)
    }
    
    /**
     * Migration helper to move old settings to new organized structure
     */
    @SuppressLint("ApplySharedPref")
    fun migrateOldSettings() {
        val oldSettings = application!!.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val newPrefs = application!!.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        
        // Only migrate if old settings exist and new ones don't
        if (oldSettings.contains("oled") && !newPrefs.contains("appearance_amoled")) {
            val editor = newPrefs.edit()
            
            // Migrate appearance settings
            if (oldSettings.contains("oled")) {
                editor.putBoolean("appearance_amoled", oldSettings.getBoolean("oled", false))
            }
            if (oldSettings.contains("monet")) {
                editor.putBoolean("appearance_monet", oldSettings.getBoolean("monet", Build.VERSION.SDK_INT >= Build.VERSION_CODES.S))
            }
            if (oldSettings.contains("default_night_mode")) {
                editor.putInt("appearance_night_mode", oldSettings.getInt("default_night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM))
            }
            if (oldSettings.contains("color_scheme")) {
                editor.putInt("appearance_color_scheme", oldSettings.getInt("color_scheme", 0))
            }
            if (oldSettings.contains("theme_variant")) {
                editor.putInt("appearance_theme_variant", oldSettings.getInt("theme_variant", 0))
            }
            
            // Migrate terminal settings
            if (oldSettings.contains("terminal_font_size")) {
                editor.putInt("terminal_font_size", oldSettings.getInt("terminal_font_size", 13))
            }
            if (oldSettings.contains("terminal_opacity")) {
                editor.putFloat("terminal_opacity", oldSettings.getFloat("terminal_opacity", 1.0f))
            }
            if (oldSettings.contains("cursor_style")) {
                editor.putInt("terminal_cursor_style", oldSettings.getInt("cursor_style", 0))
            }
            if (oldSettings.contains("blackText")) {
                editor.putBoolean("terminal_black_text", oldSettings.getBoolean("blackText", false))
            }
            if (oldSettings.contains("custom_bg_name")) {
                editor.putString("terminal_custom_bg_name", oldSettings.getString("custom_bg_name", "No Image Selected"))
            }
            if (oldSettings.contains("custom_ttf_name")) {
                editor.putString("terminal_custom_font_name", oldSettings.getString("custom_ttf_name", "No Font Selected"))
            }
            
            // Migrate interface settings
            if (oldSettings.contains("statusBar")) {
                editor.putBoolean("interface_status_bar", oldSettings.getBoolean("statusBar", true))
            }
            if (oldSettings.contains("horizontal_statusBar")) {
                editor.putBoolean("interface_horizontal_status_bar", oldSettings.getBoolean("horizontal_statusBar", true))
            }
            if (oldSettings.contains("toolbar")) {
                editor.putBoolean("interface_toolbar", oldSettings.getBoolean("toolbar", true))
            }
            if (oldSettings.contains("toolbar_h")) {
                editor.putBoolean("interface_toolbar_horizontal", oldSettings.getBoolean("toolbar_h", true))
            }
            if (oldSettings.contains("virtualKeys")) {
                editor.putBoolean("interface_virtual_keys", oldSettings.getBoolean("virtualKeys", true))
            }
            if (oldSettings.contains("force_soft_keyboard")) {
                editor.putBoolean("interface_hide_soft_keyboard", oldSettings.getBoolean("force_soft_keyboard", true))
            }
            
            // Migrate system settings
            if (oldSettings.contains("workingMode")) {
                editor.putInt("system_working_mode", oldSettings.getInt("workingMode", WorkingMode.ALPINE))
            }
            if (oldSettings.contains("graphics_acceleration")) {
                editor.putBoolean("system_graphics_acceleration", oldSettings.getBoolean("graphics_acceleration", false))
            }
            if (oldSettings.contains("ignore_storage_permission")) {
                editor.putBoolean("system_ignore_storage_permission", oldSettings.getBoolean("ignore_storage_permission", false))
            }
            if (oldSettings.contains("github")) {
                editor.putBoolean("system_github", oldSettings.getBoolean("github", true))
            }
            
            // Migrate root settings
            if (oldSettings.contains("root_enabled")) {
                editor.putBoolean("root_enabled", oldSettings.getBoolean("root_enabled", false))
            }
            if (oldSettings.contains("root_provider")) {
                editor.putString("root_provider", oldSettings.getString("root_provider", "none"))
            }
            if (oldSettings.contains("busybox_installed")) {
                editor.putBoolean("root_busybox_installed", oldSettings.getBoolean("busybox_installed", false))
            }
            if (oldSettings.contains("root_verified")) {
                editor.putBoolean("root_verified", oldSettings.getBoolean("root_verified", false))
            }
            if (oldSettings.contains("busybox_path")) {
                editor.putString("root_busybox_path", oldSettings.getString("busybox_path", ""))
            }
            if (oldSettings.contains("use_root_mounts")) {
                editor.putBoolean("root_use_mounts", oldSettings.getBoolean("use_root_mounts", false))
            }
            
            // Migrate feedback settings
            if (oldSettings.contains("bell")) {
                editor.putBoolean("feedback_bell", oldSettings.getBoolean("bell", false))
            }
            if (oldSettings.contains("vibrate")) {
                editor.putBoolean("feedback_vibrate", oldSettings.getBoolean("vibrate", true))
            }
            
            editor.apply()
        }
    }
    
    /**
     * Initialize the settings manager
     */
    fun initialize() {
        migrateOldSettings()
    }
    
    /**
     * Clear all settings data
     */
    @SuppressLint("ApplySharedPref")
    fun clearAllSettings() {
        Preference.clearData()
    }
}
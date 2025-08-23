package com.rk.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.pm.PackageInfoCompat
import com.rk.components.compose.preferences.normal.Preference
import com.rk.libcommons.application
import com.rk.terminal.ui.screens.settings.WorkingMode
import java.nio.charset.Charset

/**
 * Legacy Settings object maintained for backward compatibility
 * @deprecated Use SettingsManager instead for new code
 */
@Deprecated("Use SettingsManager for new code", ReplaceWith("SettingsManager"))
object Settings {
    //Boolean

    var amoled
        get() = SettingsManager.Appearance.amoled
        set(value) { SettingsManager.Appearance.amoled = value }
        
    var monet
        get() = SettingsManager.Appearance.monet
        set(value) { SettingsManager.Appearance.monet = value }
        
    var ignore_storage_permission
        get() = SettingsManager.System.ignoreStoragePermission
        set(value) { SettingsManager.System.ignoreStoragePermission = value }
        
    var github
        get() = SettingsManager.System.githubIntegration
        set(value) { SettingsManager.System.githubIntegration = value }

   var default_night_mode
        get() = SettingsManager.Appearance.defaultNightMode
        set(value) { SettingsManager.Appearance.defaultNightMode = value }

    var terminal_font_size
        get() = SettingsManager.Terminal.fontSize
        set(value) { SettingsManager.Terminal.fontSize = value }
        
    var working_Mode
        get() = SettingsManager.System.workingMode
        set(value) { SettingsManager.System.workingMode = value }

    var custom_background_name
        get() = SettingsManager.Terminal.customBackgroundName
        set(value) { SettingsManager.Terminal.customBackgroundName = value }
    var custom_font_name
        get() = SettingsManager.Terminal.customFontName
        set(value) { SettingsManager.Terminal.customFontName = value }

    var blackTextColor
        get() = SettingsManager.Terminal.blackTextColor
        set(value) { SettingsManager.Terminal.blackTextColor = value }

    var bell
        get() = SettingsManager.Feedback.bell
        set(value) { SettingsManager.Feedback.bell = value }

    var vibrate
        get() = SettingsManager.Feedback.vibrate
        set(value) { SettingsManager.Feedback.vibrate = value }

    var toolbar
        get() = SettingsManager.Interface.toolbar
        set(value) { SettingsManager.Interface.toolbar = value }

    var statusBar
        get() = SettingsManager.Interface.statusBar
        set(value) { SettingsManager.Interface.statusBar = value }

    var horizontal_statusBar
        get() = SettingsManager.Interface.horizontalStatusBar
        set(value) { SettingsManager.Interface.horizontalStatusBar = value }

    var toolbar_in_horizontal
        get() = SettingsManager.Interface.toolbarInHorizontal
        set(value) { SettingsManager.Interface.toolbarInHorizontal = value }

    var virtualKeys
        get() = SettingsManager.Interface.virtualKeys
        set(value) { SettingsManager.Interface.virtualKeys = value }

    var hide_soft_keyboard_if_hwd
        get() = SettingsManager.Interface.hideSoftKeyboardIfHardware
        set(value) { SettingsManager.Interface.hideSoftKeyboardIfHardware = value }

    var graphics_acceleration
        get() = SettingsManager.System.graphicsAcceleration
        set(value) { SettingsManager.System.graphicsAcceleration = value }

    var terminal_opacity
        get() = SettingsManager.Terminal.opacity
        set(value) { SettingsManager.Terminal.opacity = value }

    var cursor_style
        get() = SettingsManager.Terminal.cursorStyle
        set(value) { SettingsManager.Terminal.cursorStyle = value }

    var color_scheme
        get() = SettingsManager.Appearance.colorScheme
        set(value) { SettingsManager.Appearance.colorScheme = value }

    var theme_variant
        get() = SettingsManager.Appearance.themeVariant
        set(value) { SettingsManager.Appearance.themeVariant = value }

    // Root-related settings
    var root_enabled
        get() = SettingsManager.Root.enabled
        set(value) { SettingsManager.Root.enabled = value }

    var root_provider
        get() = SettingsManager.Root.provider
        set(value) { SettingsManager.Root.provider = value }

    var busybox_installed
        get() = SettingsManager.Root.busyboxInstalled
        set(value) { SettingsManager.Root.busyboxInstalled = value }

    var root_verified
        get() = SettingsManager.Root.verified
        set(value) { SettingsManager.Root.verified = value }

    var busybox_path
        get() = SettingsManager.Root.busyboxPath
        set(value) { SettingsManager.Root.busyboxPath = value }

    var use_root_mounts
        get() = SettingsManager.Root.useMounts
        set(value) { SettingsManager.Root.useMounts = value }

    /**
     * Initialize legacy settings compatibility layer
     */
    init {
        SettingsManager.initialize()
    }
}

object Preference {
    private var sharedPreferences: SharedPreferences = application!!.getSharedPreferences("Settings", Context.MODE_PRIVATE)

    //store the result into memory for faster access
    private val stringCache = hashMapOf<String, String?>()
    private val boolCache = hashMapOf<String, Boolean>()
    private val intCache = hashMapOf<String, Int>()
    private val longCache = hashMapOf<String, Long>()
    private val floatCache = hashMapOf<String, Float>()

    @SuppressLint("ApplySharedPref")
    fun clearData(){
        sharedPreferences.edit().clear().commit()
    }

    fun removeKey(key: String){
        if (sharedPreferences.contains(key).not()){
            return
        }

        sharedPreferences.edit().remove(key).apply()

        if (stringCache.containsKey(key)){
            stringCache.remove(key)
            return
        }

        if (boolCache.containsKey(key)){
            boolCache.remove(key)
            return
        }

        if (intCache.containsKey(key)){
            intCache.remove(key)
            return
        }

        if (longCache.containsKey(key)){
            longCache.remove(key)
            return
        }

        if (floatCache.containsKey(key)){
            floatCache.remove(key)
            return
        }
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        runCatching {
            return boolCache[key] ?: sharedPreferences.getBoolean(key, default)
                .also { boolCache[key] = it }
        }.onFailure {
            it.printStackTrace()
            setBoolean(key, default)
        }
        return default
    }

    fun setBoolean(key: String, value: Boolean) {
        boolCache[key] = value
        runCatching {
            val editor = sharedPreferences.edit()
            editor.putBoolean(key, value)
            editor.apply()
        }.onFailure { it.printStackTrace() }
    }



    fun getString(key: String, default: String): String {
        runCatching {
            return stringCache[key] ?: sharedPreferences.getString(key, default)!!
                .also { stringCache[key] = it }
        }.onFailure {
            it.printStackTrace()
            setString(key, default)
        }
        return default
    }
    fun setString(key: String, value: String?) {
        stringCache[key] = value
        runCatching {
            val editor = sharedPreferences.edit()
            editor.putString(key, value)
            editor.apply()
        }.onFailure {
            it.printStackTrace()
        }

    }

    fun getInt(key: String, default: Int): Int {
        runCatching {
            return intCache[key] ?: sharedPreferences.getInt(key, default)
                .also { intCache[key] = it }
        }.onFailure {
            it.printStackTrace()
            setInt(key, default)
        }
        return default
    }

    fun setInt(key: String, value: Int) {
        intCache[key] = value
        runCatching {
            val editor = sharedPreferences.edit()
            editor.putInt(key, value)
            editor.apply()
        }.onFailure {
            it.printStackTrace()
        }

    }

    fun getLong(key: String, default: Long): Long {
        runCatching {
            return longCache[key] ?: sharedPreferences.getLong(key, default)
                .also { longCache[key] = it }
        }.onFailure {
            it.printStackTrace()
            setLong(key, default)
        }
        return default
    }

    fun setLong(key: String, value: Long) {
        longCache[key] = value
        runCatching {
            val editor = sharedPreferences.edit()
            editor.putLong(key,value)
            editor.apply()
        }.onFailure {
            it.printStackTrace()
        }
    }

    fun getFloat(key: String, default: Float): Float {
        runCatching {
            return floatCache[key] ?: sharedPreferences.getFloat(key, default)
                .also { floatCache[key] = it }
        }.onFailure {
            it.printStackTrace()
            setFloat(key, default)
        }
        return default
    }

    fun setFloat(key: String, value: Float) {
        floatCache[key] = value
        runCatching {
            val editor = sharedPreferences.edit()
            editor.putFloat(key,value)
            editor.apply()
        }.onFailure {
            it.printStackTrace()
        }
    }

}

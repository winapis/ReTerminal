package com.rk.terminal.ui.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import com.rk.settings.Settings

/**
 * Haptic feedback utility for enhanced user experience.
 * Provides consistent haptic feedback across the app.
 */
object HapticFeedback {
    
    /**
     * Perform light haptic feedback for button presses and selections.
     * 
     * @param context The context to get vibrator service
     */
    fun lightClick(context: Context) {
        if (!Settings.vibrate) return
        
        performHaptic(context, HapticType.LIGHT_CLICK)
    }
    
    /**
     * Perform medium haptic feedback for important actions.
     * 
     * @param context The context to get vibrator service
     */
    fun mediumClick(context: Context) {
        if (!Settings.vibrate) return
        
        performHaptic(context, HapticType.MEDIUM_CLICK)
    }
    
    /**
     * Perform heavy haptic feedback for critical actions.
     * 
     * @param context The context to get vibrator service
     */
    fun heavyClick(context: Context) {
        if (!Settings.vibrate) return
        
        performHaptic(context, HapticType.HEAVY_CLICK)
    }
    
    /**
     * Perform error haptic feedback for validation failures.
     * 
     * @param context The context to get vibrator service
     */
    fun error(context: Context) {
        if (!Settings.vibrate) return
        
        performHaptic(context, HapticType.ERROR)
    }
    
    /**
     * Perform success haptic feedback for completed actions.
     * 
     * @param context The context to get vibrator service
     */
    fun success(context: Context) {
        if (!Settings.vibrate) return
        
        performHaptic(context, HapticType.SUCCESS)
    }
    
    /**
     * Perform the actual haptic feedback based on type.
     * 
     * @param context The context to get vibrator service
     * @param type The type of haptic feedback to perform
     */
    private fun performHaptic(context: Context, type: HapticType) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = ContextCompat.getSystemService(context, VibratorManager::class.java)
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                ContextCompat.getSystemService(context, Vibrator::class.java)
            }
            
            vibrator?.let { vib ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = when (type) {
                        HapticType.LIGHT_CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                        HapticType.MEDIUM_CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                        HapticType.HEAVY_CLICK -> VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK)
                        HapticType.ERROR -> VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1)
                        HapticType.SUCCESS -> VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 50), -1)
                    }
                    vib.vibrate(effect)
                } else {
                    // Fallback for older Android versions
                    @Suppress("DEPRECATION")
                    when (type) {
                        HapticType.LIGHT_CLICK -> vib.vibrate(50)
                        HapticType.MEDIUM_CLICK -> vib.vibrate(100)
                        HapticType.HEAVY_CLICK -> vib.vibrate(longArrayOf(0, 100, 50, 100), -1)
                        HapticType.ERROR -> vib.vibrate(longArrayOf(0, 100, 50, 100), -1)
                        HapticType.SUCCESS -> vib.vibrate(longArrayOf(0, 50, 50, 50), -1)
                    }
                }
            }
        } catch (e: Exception) {
            // Silently handle vibration errors
        }
    }
    
    /**
     * Enum for different types of haptic feedback.
     */
    private enum class HapticType {
        LIGHT_CLICK,
        MEDIUM_CLICK,
        HEAVY_CLICK,
        ERROR,
        SUCCESS
    }
}
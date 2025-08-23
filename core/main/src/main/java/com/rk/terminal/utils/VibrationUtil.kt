package com.rk.terminal.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.rk.settings.SettingsManager

/**
 * Utility class for handling vibration and haptic feedback throughout the app
 */
object VibrationUtil {
    
    enum class VibrationPattern {
        LIGHT,      // Light tap (30ms)
        MEDIUM,     // Medium tap (50ms)
        HEAVY,      // Heavy tap (100ms)
        DOUBLE_TAP, // Double tap pattern
        ERROR,      // Error feedback pattern
        SUCCESS,    // Success feedback pattern
        NOTIFICATION // Notification pattern
    }
    
    /**
     * Perform vibration with specified pattern
     */
    @RequiresPermission(android.Manifest.permission.VIBRATE)
    fun vibrate(context: Context, pattern: VibrationPattern = VibrationPattern.LIGHT) {
        if (!SettingsManager.Feedback.vibrate) return
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrateModern(context, pattern)
            } else {
                vibrateLegacy(context)
            }
        } catch (e: Exception) {
            // Silently fail if vibration is not available
        }
    }
    
    /**
     * Perform haptic feedback on a view
     */
    fun performHapticFeedback(view: View?, pattern: VibrationPattern = VibrationPattern.LIGHT): Boolean {
        if (!SettingsManager.Feedback.vibrate || view == null) return false
        
        return try {
            // First try system vibration for more precise control
            vibrate(view.context, pattern)
            
            // Also perform view haptic feedback as fallback
            val hapticConstant = when (pattern) {
                VibrationPattern.LIGHT -> HapticFeedbackConstants.VIRTUAL_KEY
                VibrationPattern.MEDIUM -> HapticFeedbackConstants.KEYBOARD_TAP
                VibrationPattern.HEAVY -> HapticFeedbackConstants.LONG_PRESS
                VibrationPattern.ERROR -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    HapticFeedbackConstants.REJECT
                } else {
                    HapticFeedbackConstants.VIRTUAL_KEY
                }
                VibrationPattern.SUCCESS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    HapticFeedbackConstants.CONFIRM
                } else {
                    HapticFeedbackConstants.VIRTUAL_KEY
                }
                else -> HapticFeedbackConstants.VIRTUAL_KEY
            }
            
            view.performHapticFeedback(hapticConstant)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    @RequiresPermission(android.Manifest.permission.VIBRATE)
    private fun vibrateModern(context: Context, pattern: VibrationPattern) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (!vibrator.hasVibrator()) return
        
        val effect = when (pattern) {
            VibrationPattern.LIGHT -> VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
            VibrationPattern.MEDIUM -> VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            VibrationPattern.HEAVY -> VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            VibrationPattern.DOUBLE_TAP -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30), -1)
                } else {
                    VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                }
            }
            VibrationPattern.ERROR -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
                } else {
                    VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE)
                }
            }
            VibrationPattern.SUCCESS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
                } else {
                    VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE)
                }
            }
            VibrationPattern.NOTIFICATION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1)
                } else {
                    VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                }
            }
        }
        
        vibrator.vibrate(effect)
    }
    
    @Suppress("DEPRECATION")
    @RequiresPermission(android.Manifest.permission.VIBRATE)
    private fun vibrateLegacy(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(30) // Short vibration for legacy devices
        }
    }
    
    /**
     * Quick vibration for button presses
     */
    fun vibrateButton(context: Context) = vibrate(context, VibrationPattern.LIGHT)
    
    /**
     * Medium vibration for important actions
     */
    fun vibrateAction(context: Context) = vibrate(context, VibrationPattern.MEDIUM)
    
    /**
     * Heavy vibration for critical actions
     */
    fun vibrateCritical(context: Context) = vibrate(context, VibrationPattern.HEAVY)
    
    /**
     * Success vibration pattern
     */
    fun vibrateSuccess(context: Context) = vibrate(context, VibrationPattern.SUCCESS)
    
    /**
     * Error vibration pattern
     */
    fun vibrateError(context: Context) = vibrate(context, VibrationPattern.ERROR)
    
    /**
     * Notification vibration pattern
     */
    fun vibrateNotification(context: Context) = vibrate(context, VibrationPattern.NOTIFICATION)
}
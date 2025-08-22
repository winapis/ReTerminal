package com.rk.terminal.ui.screens.terminal.virtualkeys

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import android.widget.Button
import com.rk.settings.Settings
import com.rk.terminal.ui.screens.terminal.virtualkeys.VirtualKeysView.IVirtualKeysView
import com.termux.terminal.TerminalSession

class VirtualKeyClient(val session: TerminalSession) : IVirtualKeysView {
    override fun onVirtualKeyButtonClick(
        view: View?,
        buttonInfo: VirtualKeyButton?,
        button: Button?,
    ) {
        val key = buttonInfo?.key
        if (key.isNullOrEmpty()) {
            return
        }
        when (key) {
            "ESC" -> session.write("\u001B") // ESC
            "TAB" -> session.write("\u0009") // TAB
            "HOME" -> session.write("\u001B[H") // HOME
            "UP" -> session.write("\u001B[A") // UP Arrow (ANSI escape code)
            "DOWN" -> session.write("\u001B[B") // DOWN Arrow (ANSI escape code)
            "LEFT" -> session.write("\u001B[D") // LEFT Arrow (ANSI escape code)
            "RIGHT" -> session.write("\u001B[C") // RIGHT Arrow (ANSI escape code)
            "PGUP" -> session.write("\u001B[5~") // Page Up (ANSI escape code)
            "PGDN" -> session.write("\u001B[6~") // Page Down (ANSI escape code)
            "END" -> session.write("\u001B[4~") // End (ANSI escape code, may vary)
            else -> session.write(buttonInfo.key)
        }
    }

    override fun performVirtualKeyButtonHapticFeedback(
        view: View?,
        buttonInfo: VirtualKeyButton?,
        button: Button?,
    ): Boolean {
        if (!Settings.vibrate || view == null) {
            return false
        }

        return try {
            // Check if haptic feedback is enabled in system settings
            val contentResolver = view.context.contentResolver
            val hapticEnabled = android.provider.Settings.System.getInt(
                contentResolver, 
                android.provider.Settings.System.HAPTIC_FEEDBACK_ENABLED, 
                1
            ) == 1

            if (!hapticEnabled) {
                return false
            }

            // Use modern haptic feedback API if available (API 26+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = view.context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    view.context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                }

                if (vibrator?.hasVibrator() == true) {
                    // Use lighter haptic feedback for better user experience
                    val effect = VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(effect)
                    return true
                }
            } else {
                // For older Android versions, try vibrator service first
                @Suppress("DEPRECATION")
                val vibrator = view.context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator?.hasVibrator() == true) {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(30) // Reduced from 50ms to 30ms for lighter feedback
                    return true
                }
            }

            // Always fallback to view's haptic feedback
            return view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        } catch (e: Exception) {
            // If all else fails, use basic haptic feedback
            try {
                return view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            } catch (e2: Exception) {
                // Last resort: return false to let VirtualKeysView handle it
                return false
            }
        }
    }
}

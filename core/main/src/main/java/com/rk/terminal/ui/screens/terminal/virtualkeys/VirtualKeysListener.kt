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
import com.termux.terminal.TerminalSession

class VirtualKeysListener(val session: TerminalSession) : VirtualKeysView.IVirtualKeysView {

    override fun onVirtualKeyButtonClick(
        view: View?,
        buttonInfo: VirtualKeyButton?,
        button: Button?,
    ) {

        val key = buttonInfo?.key ?: return
        val writeable: String =
            when (key) {
                "UP" -> "\u001B[A" // Escape sequence for Up Arrow
                "DOWN" -> "\u001B[B" // Escape sequence for Down Arrow
                "LEFT" -> "\u001B[D" // Escape sequence for Left Arrow
                "RIGHT" -> "\u001B[C" // Escape sequence for Right Arrow
                "ENTER" -> "\u000D" // Carriage Return for Enter
                "PGUP" -> "\u001B[5~" // Escape sequence for Page Up
                "PGDN" -> "\u001B[6~" // Escape sequence for Page Down
                "TAB" -> "\u0009" // Horizontal Tab
                "HOME" -> "\u001B[H" // Escape sequence for Home
                "END" -> "\u001B[F" // Escape sequence for End
                "ESC" -> "\u001B" // Escape
                else -> key
            }

        session.write(writeable)
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
            // Use modern haptic feedback API if available (API 26+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = view.context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    view.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

                if (vibrator.hasVibrator()) {
                    val effect = VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(effect)
                    true
                } else {
                    false
                }
            } else {
                // Fallback to view's haptic feedback for older Android versions
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                true
            }
        } catch (e: Exception) {
            // Fallback to basic haptic feedback if vibrator service fails
            view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            true
        }
    }
}

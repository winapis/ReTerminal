package com.rk.libcommons

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rk.terminal.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@OptIn(kotlinx.coroutines.DelicateCoroutinesApi::class)
class LoadingPopup(private val ctx: Activity, hideAfterMillis: Long?,scope: CoroutineScope = GlobalScope) {
    private var dialog: AlertDialog? = null
    private lateinit var dialogView: View

    init {
        ctx.runOnUiThread {
            val inflater1: LayoutInflater = ctx.layoutInflater
            // Create a temporary parent for proper layout parameter resolution
            val tempParent = FrameLayout(ctx)
            dialogView = inflater1.inflate(R.layout.progress_dialog, tempParent, false)
            dialogView.findViewById<TextView>(R.id.progress_message).text = ctx.getString(com.rk.resources.R.string.please_wait)
            dialog =
                MaterialAlertDialogBuilder(ctx).setView(dialogView).setCancelable(false).create()

            if (hideAfterMillis != null) {
                show()
                scope.launch {
                    delay(hideAfterMillis)
                    withContext(Dispatchers.Main){
                        hide()
                    }
                }
            }
        }
    }

    fun setMessage(message: String): LoadingPopup {
        dialogView.findViewById<TextView>(R.id.progress_message).text = message
        return this
    }

    fun show(): LoadingPopup {
        ctx.runOnUiThread {
            if (dialog?.isShowing?.not() == true) {
                dialog?.show()
            }
        }
        return this
    }

    fun hide() {
        ctx.runOnUiThread {
            if (dialog != null && dialog?.isShowing == true) {
                dialog?.dismiss()
            }
        }
    }

    fun getDialog(): AlertDialog? {
        return dialog
    }
}

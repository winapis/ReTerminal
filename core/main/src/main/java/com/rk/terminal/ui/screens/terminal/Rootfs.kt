package com.rk.terminal.ui.screens.terminal

import android.os.Environment
import androidx.compose.runtime.mutableStateOf
import com.rk.libcommons.application
import com.rk.libcommons.child
import com.rk.terminal.App
import java.io.File

object Rootfs {
    val reTerminal = application!!.filesDir

    init {
        if (reTerminal.exists().not()){
            reTerminal.mkdirs()
        }
    }

    var isDownloaded = mutableStateOf(isFilesDownloaded())
    fun isFilesDownloaded(): Boolean{
        if (!reTerminal.exists()) return false
        if (!reTerminal.child("proot").exists()) return false
        if (!reTerminal.child("libtalloc.so.2").exists()) return false
        
        // Check if any distribution rootfs exists (both .tar.gz and .tar.xz formats)
        val distributionNames = listOf("alpine", "ubuntu", "debian", "arch", "kali")
        return distributionNames.any { name ->
            reTerminal.child("$name.tar.gz").exists() || reTerminal.child("$name.tar.xz").exists()
        }
    }
}
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
        
        // Check if any distribution rootfs exists
        val distributions = listOf("alpine.tar.gz", "ubuntu.tar.gz", "debian.tar.gz", "arch.tar.gz", "kali.tar.gz")
        return distributions.any { reTerminal.child(it).exists() }
    }
}
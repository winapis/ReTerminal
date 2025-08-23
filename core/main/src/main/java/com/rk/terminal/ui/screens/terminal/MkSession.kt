package com.rk.terminal.ui.screens.terminal

import android.os.Environment
import com.rk.libcommons.alpineDir
import com.rk.libcommons.alpineHomeDir
import com.rk.libcommons.application
import com.rk.libcommons.child
import com.rk.libcommons.createFileIfNot
import com.rk.libcommons.localBinDir
import com.rk.libcommons.localDir
import com.rk.libcommons.localLibDir
import com.rk.libcommons.pendingCommand
import com.rk.settings.Settings
import com.rk.terminal.App
import com.rk.terminal.App.Companion.getTempDir
import com.rk.terminal.BuildConfig
import com.rk.terminal.ui.activities.terminal.MainActivity
import com.rk.terminal.ui.screens.settings.WorkingMode
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import java.io.File
import java.io.FileOutputStream

object MkSession {
    fun createSession(
        activity: MainActivity, sessionClient: TerminalSessionClient, session_id: String,workingMode:Int
    ): TerminalSession {
        with(activity) {
            val envVariables = mapOf(
                "ANDROID_ART_ROOT" to System.getenv("ANDROID_ART_ROOT"),
                "ANDROID_DATA" to System.getenv("ANDROID_DATA"),
                "ANDROID_I18N_ROOT" to System.getenv("ANDROID_I18N_ROOT"),
                "ANDROID_ROOT" to System.getenv("ANDROID_ROOT"),
                "ANDROID_RUNTIME_ROOT" to System.getenv("ANDROID_RUNTIME_ROOT"),
                "ANDROID_TZDATA_ROOT" to System.getenv("ANDROID_TZDATA_ROOT"),
                "BOOTCLASSPATH" to System.getenv("BOOTCLASSPATH"),
                "DEX2OATBOOTCLASSPATH" to System.getenv("DEX2OATBOOTCLASSPATH"),
                "EXTERNAL_STORAGE" to System.getenv("EXTERNAL_STORAGE")
            )

            val workingDir = pendingCommand?.workingDir ?: alpineHomeDir().path

            val initFile: File = localBinDir().child("init-host")

            if (initFile.exists().not()){
                initFile.createFileIfNot()
                initFile.writeText(assets.open("init-host.sh").bufferedReader().use { it.readText() })
            }

            // Create enhanced root version if root is enabled
            val initRootFile: File = localBinDir().child("init-host-root")
            if (initRootFile.exists().not()){
                initRootFile.createFileIfNot()
                initRootFile.writeText(assets.open("init-host-root.sh").bufferedReader().use { it.readText() })
            }


            localBinDir().child("init").apply {
                if (exists().not()){
                    createFileIfNot()
                    writeText(assets.open("init.sh").bufferedReader().use { it.readText() })
                }
            }

            // Copy distribution-specific init scripts
            val distributionScripts = listOf("alpine", "ubuntu", "debian", "arch", "kali")
            distributionScripts.forEach { dist ->
                localBinDir().child("init-$dist").apply {
                    if (exists().not()) {
                        createFileIfNot()
                        writeText(assets.open("init-$dist.sh").bufferedReader().use { it.readText() })
                    }
                }
            }
            
            // Copy graphics setup script
            localBinDir().child("setup-graphics.sh").apply {
                if (exists().not()) {
                    createFileIfNot()
                    writeText(assets.open("setup-graphics.sh").bufferedReader().use { it.readText() })
                }
            }

            // Convert working mode to distribution name
            val selectedDistribution = when(workingMode) {
                WorkingMode.ALPINE -> "alpine"
                WorkingMode.UBUNTU -> "ubuntu"
                WorkingMode.DEBIAN -> "debian"
                WorkingMode.ARCH -> "arch"
                WorkingMode.KALI -> "kali"
                else -> "alpine" // Default fallback
            }
            
            // Debug: Log the selected distribution
            if (BuildConfig.DEBUG) {
                android.util.Log.d("MkSession", "Selected distribution: $selectedDistribution (working mode: $workingMode)")
            }

            // Handle graphics acceleration setting
            val distributionDir = localDir().child("distribution")
            val graphicsEnabledFile = distributionDir.child(".reterminal_graphics_enabled")
            
            if (Settings.graphics_acceleration) {
                // Create flag file to enable graphics acceleration
                if (distributionDir.exists()) {
                    graphicsEnabledFile.createFileIfNot()
                    graphicsEnabledFile.writeText("enabled")
                }
            } else {
                // Remove flag file to disable graphics acceleration
                if (graphicsEnabledFile.exists()) {
                    graphicsEnabledFile.delete()
                }
            }

            val env = mutableListOf(
                "PATH=${System.getenv("PATH")}:/sbin:${localBinDir().absolutePath}",
                "HOME=/sdcard",
                "PUBLIC_HOME=${getExternalFilesDir(null)?.absolutePath}",
                "COLORTERM=truecolor",
                "TERM=xterm-256color",
                "LANG=C.UTF-8",
                "BIN=${localBinDir()}",
                "DEBUG=${BuildConfig.DEBUG}",
                "PREFIX=${filesDir.parentFile!!.path}",
                "LD_LIBRARY_PATH=${localLibDir().absolutePath}",
                "LINKER=${if(File("/system/bin/linker64").exists()){"/system/bin/linker64"}else{"/system/bin/linker"}}",
                "NATIVE_LIB_DIR=${applicationInfo.nativeLibraryDir}",
                "PKG=${packageName}",
                "RISH_APPLICATION_ID=${packageName}",
                "PKG_PATH=${applicationInfo.sourceDir}",
                "PROOT_TMP_DIR=${getTempDir().child(session_id).also { if (it.exists().not()){it.mkdirs()} }}",
                "TMPDIR=${getTempDir().absolutePath}",
                "SELECTED_DISTRIBUTION=$selectedDistribution",
                // Root-related environment variables
                "ROOT_ENABLED=${Settings.root_enabled}",
                "ROOT_VERIFIED=${Settings.root_verified}",
                "ROOT_PROVIDER=${Settings.root_provider}",
                "BUSYBOX_INSTALLED=${Settings.busybox_installed}",
                "BUSYBOX_PATH=${Settings.busybox_path}",
                "USE_ROOT_MOUNTS=${Settings.use_root_mounts}"
            )

            if (File(applicationInfo.nativeLibraryDir).child("libproot-loader32.so").exists()){
                env.add("PROOT_LOADER32=${applicationInfo.nativeLibraryDir}/libproot-loader32.so")
            }

            if (File(applicationInfo.nativeLibraryDir).child("libproot-loader.so").exists()){
                env.add("PROOT_LOADER=${applicationInfo.nativeLibraryDir}/libproot-loader.so")
            }




            env.addAll(envVariables.map { "${it.key}=${it.value}" })

            localDir().child("stat").apply {
                if (exists().not()){
                    writeText(stat)
                }
            }

            localDir().child("vmstat").apply {
                if (exists().not()){
                    writeText(vmstat)
                }
            }

            pendingCommand?.env?.let {
                env.addAll(it)
            }

            val args: Array<String>

            val shell = if (pendingCommand == null) {
                args = if (workingMode != WorkingMode.ANDROID){
                    // Choose the appropriate init script based on root configuration
                    val initScriptFile = if (Settings.root_enabled && Settings.root_verified) {
                        localBinDir().child("init-host-root")
                    } else {
                        initFile
                    }
                    arrayOf("-c", initScriptFile.absolutePath)
                }else{
                    arrayOf()
                }
                "/system/bin/sh"
            } else{
                args = pendingCommand!!.args
                pendingCommand!!.shell
            }

            pendingCommand = null
            return TerminalSession(
                shell,
                workingDir,
                args,
                env.toTypedArray(),
                TerminalEmulator.DEFAULT_TERMINAL_TRANSCRIPT_ROWS,
                sessionClient,
            )
        }

    }
}
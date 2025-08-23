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
import com.rk.settings.SettingsManager
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

            // Create unified init script that handles everything
            val unifiedInitFile: File = localBinDir().child("init-unified")
            if (unifiedInitFile.exists().not()){
                unifiedInitFile.createFileIfNot()
                unifiedInitFile.writeText(assets.open("init-unified.sh").bufferedReader().use { it.readText() })
            }

            // Copy distribution-specific configuration scripts
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
            
            if (SettingsManager.System.graphicsAcceleration) {
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
                "ROOT_ENABLED=${SettingsManager.Root.enabled}",
                "ROOT_VERIFIED=${SettingsManager.Root.verified}",
                "ROOT_PROVIDER=${SettingsManager.Root.provider}",
                "BUSYBOX_INSTALLED=${SettingsManager.Root.busyboxInstalled}",
                "BUSYBOX_PATH=${SettingsManager.Root.busyboxPath}",
                "USE_ROOT_MOUNTS=${SettingsManager.Root.useMounts}"
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
                    // Use the unified init script that handles both root and non-root scenarios
                    arrayOf("-c", unifiedInitFile.absolutePath)
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
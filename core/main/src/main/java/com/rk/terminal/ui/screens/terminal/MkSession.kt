package com.rk.terminal.ui.screens.terminal

import com.rk.libcommons.child
import com.rk.libcommons.createFileIfNot
import com.rk.libcommons.localBinDir
import com.rk.libcommons.localLibDir
import com.rk.libcommons.pendingCommand
import com.rk.settings.Settings
import com.rk.terminal.App.Companion.getTempDir
import com.rk.terminal.BuildConfig
import com.rk.terminal.ui.activities.terminal.MainActivity
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import com.termux.terminal.TerminalSessionClient
import java.io.File
import java.io.FileOutputStream

object MkSession {
    fun createSession(
        activity: MainActivity, sessionClient: TerminalSessionClient, session_id: String
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

            val workingDir = pendingCommand?.workingDir ?: activity.filesDir.absolutePath

            val tmpDir = File(getTempDir(), "terminal/$session_id")

            if (tmpDir.exists()) {
                tmpDir.deleteRecursively()
            }

            tmpDir.mkdirs()

            val initFile: File
            val rish: File
            localBinDir().apply {
                initFile = child("init").createFileIfNot()
                rish = child("rish").createFileIfNot()
                child("exec").createFileIfNot().writeText(assets.open("exec.sh").bufferedReader().use { it.readText() })
                initFile.writeText(assets.open("init.sh").bufferedReader().use { it.readText() })
                rish.writeText(assets.open("rish.sh").bufferedReader().use { it.readText() })
            }



            assets.list("arm/bin")?.forEach { fileName ->
                val outputFile = File(localBinDir(), fileName)
                if (outputFile.exists().not()){
                    val inputStream = assets.open("arm/bin/$fileName")
                    inputStream.use { input ->
                        FileOutputStream(outputFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }

            assets.list("arm/lib")?.forEach { fileName ->
                val outputFile = File(localLibDir(), fileName)
                if (outputFile.exists().not()){
                    val inputStream = assets.open("arm/lib/$fileName")
                    inputStream.use { input ->
                        FileOutputStream(outputFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }

            val env = mutableListOf(
                "PROOT_TMP_DIR=${tmpDir.absolutePath}",
                "PATH=${System.getenv("PATH")}:/sbin:${localBinDir().absolutePath}",
                "HOME=${filesDir.path}",
                "PUBLIC_HOME=${getExternalFilesDir(null)?.absolutePath}",
                "COLORTERM=truecolor",
                "TERM=xterm-256color",
                "LANG=C.UTF-8",
                "BIN=${localBinDir()}",
                "EXEC=sh ${localBinDir().child("exec")}",
                "DEBUG=${BuildConfig.DEBUG}",
                "PREFIX=${filesDir.parentFile!!.path}",
                "LD_LIBRARY_PATH=${localLibDir().absolutePath}",
                "LINKER=${if(File("/system/bin/linker64").exists()){"/system/bin/linker64"}else{"/system/bin/linker"}}",
                "PKG=${packageName}",
                "RISH_APPLICATION_ID=${packageName}",
                "PKG_PATH=${applicationInfo.sourceDir}"
            )


            env.addAll(envVariables.map { "${it.key}=${it.value}" })

            pendingCommand?.env?.let {
                env.addAll(it)
            }

            val args: Array<String>

            val shell = if (pendingCommand == null) {
                if (Settings.use_shizuku){
                    args = arrayOf("-c",initFile.absolutePath,rish.absolutePath)
                    "/system/bin/sh"
                }else{
                    args = arrayOf("-c",initFile.absolutePath)
                    "/system/bin/sh"
                }

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
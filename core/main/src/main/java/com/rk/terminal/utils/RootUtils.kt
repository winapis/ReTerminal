package com.rk.terminal.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

data class RootInfo(
    val isRootAvailable: Boolean = false,
    val rootProvider: RootProvider = RootProvider.NONE,
    val isBusyBoxInstalled: Boolean = false,
    val hasSuAccess: Boolean = false
)

enum class RootProvider {
    NONE,
    MAGISK,
    KERNELSU,
    SUPERSU,
    UNKNOWN
}

object RootUtils {
    private var cachedRootInfo: RootInfo? = null
    
    /**
     * Check if root access is available and which root provider is being used
     */
    suspend fun checkRootAccess(): RootInfo = withContext(Dispatchers.IO) {
        cachedRootInfo?.let { return@withContext it }
        
        val hasSuAccess = testSuCommand()
        val rootProvider = detectRootProvider()
        val isBusyBoxInstalled = checkBusyBoxInstallation()
        
        val rootInfo = RootInfo(
            isRootAvailable = hasSuAccess,
            rootProvider = rootProvider,
            isBusyBoxInstalled = isBusyBoxInstalled,
            hasSuAccess = hasSuAccess
        )
        
        cachedRootInfo = rootInfo
        return@withContext rootInfo
    }
    
    /**
     * Test if su command works and app has root access
     */
    private suspend fun testSuCommand(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder("su", "-c", "id")
                .redirectErrorStream(true)
                .start()
            
            val outputReader = BufferedReader(InputStreamReader(process.inputStream))
            val output = outputReader.readText()
            
            val exitCode = process.waitFor()
            
            // Check if the command succeeded and output contains uid=0 (root)
            return@withContext exitCode == 0 && output.contains("uid=0")
        } catch (e: Exception) {
            return@withContext false
        }
    }
    
    /**
     * Detect which root management solution is being used
     */
    private suspend fun detectRootProvider(): RootProvider = withContext(Dispatchers.IO) {
        try {
            // Check for Magisk
            if (executeRootCommand("magisk --version").isSuccess) {
                return@withContext RootProvider.MAGISK
            }
            
            // Check for KernelSU
            if (executeRootCommand("kernelsu --version").isSuccess ||
                executeRootCommand("ksud --version").isSuccess) {
                return@withContext RootProvider.KERNELSU
            }
            
            // Check for SuperSU
            if (executeRootCommand("su --version").run { 
                isSuccess && output.contains("SUPERSU", ignoreCase = true) 
            }) {
                return@withContext RootProvider.SUPERSU
            }
            
            // If su works but we can't identify the provider
            if (testSuCommand()) {
                return@withContext RootProvider.UNKNOWN
            }
            
            return@withContext RootProvider.NONE
        } catch (e: Exception) {
            return@withContext RootProvider.NONE
        }
    }
    
    /**
     * Check if BusyBox is installed and accessible
     */
    private suspend fun checkBusyBoxInstallation(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Try to find busybox in common locations
            val busyboxLocations = listOf(
                "/system/bin/busybox",
                "/system/xbin/busybox",
                "/sbin/busybox"
            )
            
            for (location in busyboxLocations) {
                val result = executeRootCommand("$location --help")
                if (result.isSuccess && result.output.contains("BusyBox", ignoreCase = true)) {
                    return@withContext true
                }
            }
            
            return@withContext false
        } catch (e: Exception) {
            return@withContext false
        }
    }
    
    /**
     * Execute a command with root privileges
     */
    suspend fun executeRootCommand(command: String): CommandResult = withContext(Dispatchers.IO) {
        try {
            val process = ProcessBuilder("su", "-c", command)
                .redirectErrorStream(true)
                .start()
            
            val outputReader = BufferedReader(InputStreamReader(process.inputStream))
            val output = outputReader.readText()
            
            val exitCode = process.waitFor()
            
            return@withContext CommandResult(
                isSuccess = exitCode == 0,
                exitCode = exitCode,
                output = output
            )
        } catch (e: Exception) {
            return@withContext CommandResult(
                isSuccess = false,
                exitCode = -1,
                output = "Error: ${e.message}"
            )
        }
    }
    
    /**
     * Install BusyBox using Magisk module
     */
    suspend fun installBusyBoxViaMagisk(): CommandResult = withContext(Dispatchers.IO) {
        // This would require downloading and flashing the BusyBox module
        // For now, we'll return instructions for manual installation
        return@withContext CommandResult(
            isSuccess = false,
            exitCode = -1,
            output = "Please install BusyBox manually through Magisk Manager or KernelSU"
        )
    }
    
    /**
     * Get the appropriate su command prefix for root operations
     */
    fun getSuPrefix(): String {
        return "su -c"
    }
    
    /**
     * Get BusyBox binary path if available
     */
    suspend fun getBusyBoxPath(): String? = withContext(Dispatchers.IO) {
        val busyboxLocations = listOf(
            "/system/bin/busybox",
            "/system/xbin/busybox",
            "/sbin/busybox"
        )
        
        for (location in busyboxLocations) {
            val result = executeRootCommand("test -x $location && echo 'found'")
            if (result.isSuccess && result.output.trim() == "found") {
                return@withContext location
            }
        }
        
        // Try busybox in PATH
        val result = executeRootCommand("which busybox")
        if (result.isSuccess && result.output.trim().isNotEmpty()) {
            return@withContext result.output.trim()
        }
        
        return@withContext null
    }
    
    /**
     * Clear cached root information to force re-detection
     */
    fun clearCache() {
        cachedRootInfo = null
    }
    
    /**
     * Format root provider name for display
     */
    fun formatRootProviderName(provider: String): String {
        return when (provider.lowercase()) {
            "magisk" -> "Magisk"
            "kernelsu" -> "KernelSU"
            "supersu" -> "SuperSU"
            "unknown" -> "Unknown"
            else -> "None"
        }
    }
    
    /**
     * Check if the device needs a reboot after BusyBox installation
     */
    suspend fun checkRebootRequired(): Boolean = withContext(Dispatchers.IO) {
        // This is a simple heuristic - in practice, you might want to check
        // if there are pending Magisk/KernelSU module installations
        val result = executeRootCommand("ls /data/adb/modules/ | grep -i busybox")
        return@withContext result.isSuccess && result.output.isNotEmpty()
    }
}

data class CommandResult(
    val isSuccess: Boolean,
    val exitCode: Int,
    val output: String
)
package com.rk.terminal.ui.screens.downloader

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.rk.libcommons.*
import com.rk.terminal.ui.activities.terminal.MainActivity
import com.rk.terminal.ui.screens.terminal.Rootfs
import com.rk.terminal.ui.screens.terminal.TerminalScreen
import com.rk.terminal.ui.screens.settings.WorkingMode
import com.rk.settings.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.net.UnknownHostException

@Composable
fun Downloader(
    modifier: Modifier = Modifier,
    mainActivity: MainActivity,
    navController: NavHostController
) {
    val context = LocalContext.current
    var progress by remember { mutableFloatStateOf(0f) }
    var progressText by remember { mutableStateOf("Installing") }
    var isSetupComplete by remember { mutableStateOf(false) }
    var needsDownload by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {

        try {
            val abi = Build.SUPPORTED_ABIS.firstOrNull {
                it in abiMap
            } ?: throw RuntimeException("Unsupported CPU")

            val selectedDistribution = when (Settings.working_Mode) {
                WorkingMode.UBUNTU -> "ubuntu"
                WorkingMode.DEBIAN -> "debian"
                WorkingMode.ARCH -> "arch"
                WorkingMode.KALI -> "kali"
                else -> "alpine" // Default to Alpine for ALPINE and ANDROID modes
            }

            val filesToDownload = listOf(
                "libtalloc.so.2" to abiMap[abi]!!.talloc,
                "proot" to abiMap[abi]!!.proot,
                "${selectedDistribution}.tar.gz" to abiMap[abi]!!.distributions[selectedDistribution]!!
            ).map { (name, url) -> 
                // All files go directly in the files directory where init scripts expect them
                DownloadFile(url, Rootfs.reTerminal.child(name))
            }

            needsDownload = filesToDownload.any { !it.outputFile.exists() }

            setupEnvironment(
                filesToDownload,
                onProgress = { completed, total, currentProgress ->
                    if (needsDownload) {
                        progress = ((completed + currentProgress) / total).coerceIn(0f, 1f)
                        progressText = "Downloading.. ${(progress * 100).toInt()}%"
                    }
                },
                onComplete = {
                    isSetupComplete = true
                },
                onError = { error ->
                    toast(if (error is UnknownHostException) "Network Error" else "Setup Failed: ${error.message}")
                }
            )
        } catch (e: Exception) {
            toast(if (e is UnknownHostException) "Network Error" else "Setup Failed: ${e.message}")
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (!isSetupComplete) {
            if (needsDownload) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(progressText, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth(0.8f))
                }
            }
        } else {
            TerminalScreen(mainActivityActivity = mainActivity, navController = navController)
        }
    }
}

private data class DownloadFile(val url: String, val outputFile: File)

private suspend fun setupEnvironment(
    filesToDownload: List<DownloadFile>,
    onProgress: (Int, Int, Float) -> Unit,
    onComplete: () -> Unit,
    onError: (Exception) -> Unit
) {
    withContext(Dispatchers.IO) {
        try {
            var completedFiles = 0
            val totalFiles = filesToDownload.size

            filesToDownload.forEach { file ->
                val outputFile = file.outputFile.apply { parentFile?.mkdirs() }
                if (!outputFile.exists()) {
                    downloadFile(file.url, outputFile) { downloaded, total ->
                        runOnUiThread { onProgress(completedFiles, totalFiles, downloaded.toFloat() / total) }
                    }
                }
                completedFiles++
                runOnUiThread { onProgress(completedFiles, totalFiles, 1f) }
                outputFile.setExecutable(true, false)
            }
            runOnUiThread { onComplete() }
        } catch (e: Exception) {
            localDir().deleteRecursively()
            withContext(Dispatchers.Main) { onError(e) }
        }
    }
}

private suspend fun downloadFile(url: String, outputFile: File, onProgress: (Long, Long) -> Unit) {
    withContext(Dispatchers.IO) {
        OkHttpClient().newCall(Request.Builder().url(url).build()).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to download file: ${response.code}")

            val body = response.body ?: throw Exception("Empty response body")
            val totalBytes = body.contentLength()
            var downloadedBytes = 0L

            outputFile.outputStream().use { output ->
                body.byteStream().use { input ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        downloadedBytes += bytesRead
                        withContext(Dispatchers.Main) { onProgress(downloadedBytes, totalBytes) }
                    }
                }
            }
        }
    }
}

private val abiMap = mapOf(
    "x86_64" to AbiUrls(
        talloc = "https://raw.githubusercontent.com/Xed-Editor/Karbon-PackagesX/main/x86_64/libtalloc.so.2",
        proot = "https://raw.githubusercontent.com/Xed-Editor/Karbon-PackagesX/main/x86_64/proot",
        distributions = mapOf(
            "alpine" to "https://dl-cdn.alpinelinux.org/alpine/v3.21/releases/x86_64/alpine-minirootfs-3.21.0-x86_64.tar.gz",
            "ubuntu" to "https://cdimage.ubuntu.com/ubuntu-base/releases/22.04/release/ubuntu-base-22.04.1-base-amd64.tar.gz",
            "debian" to "https://github.com/debuerreotype/docker-debian-artifacts/raw/dist-amd64/bookworm/rootfs.tar.xz",
            "arch" to "https://mirror.archlinux.org/iso/latest/archlinux-bootstrap-x86_64.tar.gz",
            "kali" to "https://kali.download/base-images/kali-2024.1/kali-linux-docker-amd64.tar.xz"
        )
    ),
    "arm64-v8a" to AbiUrls(
        talloc = "https://raw.githubusercontent.com/Xed-Editor/Karbon-PackagesX/main/aarch64/libtalloc.so.2",
        proot = "https://raw.githubusercontent.com/Xed-Editor/Karbon-PackagesX/main/aarch64/proot",
        distributions = mapOf(
            "alpine" to "https://dl-cdn.alpinelinux.org/alpine/v3.21/releases/aarch64/alpine-minirootfs-3.21.0-aarch64.tar.gz",
            "ubuntu" to "https://cdimage.ubuntu.com/ubuntu-base/releases/22.04/release/ubuntu-base-22.04.1-base-arm64.tar.gz",
            "debian" to "https://github.com/debuerreotype/docker-debian-artifacts/raw/dist-arm64v8/bookworm/rootfs.tar.xz",
            "arch" to "https://mirror.archlinux.org/iso/latest/archlinux-bootstrap-aarch64.tar.gz",
            "kali" to "https://kali.download/base-images/kali-2024.1/kali-linux-docker-arm64.tar.xz"
        )
    ),
    "armeabi-v7a" to AbiUrls(
        talloc = "https://raw.githubusercontent.com/Xed-Editor/Karbon-PackagesX/main/arm/libtalloc.so.2",
        proot = "https://raw.githubusercontent.com/Xed-Editor/Karbon-PackagesX/main/arm/proot",
        distributions = mapOf(
            "alpine" to "https://dl-cdn.alpinelinux.org/alpine/v3.21/releases/armhf/alpine-minirootfs-3.21.0-armhf.tar.gz",
            "ubuntu" to "https://cdimage.ubuntu.com/ubuntu-base/releases/22.04/release/ubuntu-base-22.04.1-base-armhf.tar.gz",
            "debian" to "https://github.com/debuerreotype/docker-debian-artifacts/raw/dist-arm32v7/bookworm/rootfs.tar.xz",
            "arch" to "https://mirror.archlinux.org/iso/latest/archlinux-bootstrap-armv7h.tar.gz",
            "kali" to "https://kali.download/base-images/kali-2024.1/kali-linux-docker-armhf.tar.xz"
        )
    )
)

private data class AbiUrls(val talloc: String, val proot: String, val distributions: Map<String, String>)

private data class DistributionInfo(
    val name: String,
    val packageManager: String,
    val initScript: String
)

private val distributionMap = mapOf(
    WorkingMode.ALPINE to DistributionInfo("alpine", "apk", "init-alpine.sh"),
    WorkingMode.UBUNTU to DistributionInfo("ubuntu", "apt", "init-ubuntu.sh"),
    WorkingMode.DEBIAN to DistributionInfo("debian", "apt", "init-debian.sh"),
    WorkingMode.ARCH to DistributionInfo("arch", "pacman", "init-arch.sh"),
    WorkingMode.KALI to DistributionInfo("kali", "apt", "init-kali.sh")
)

package com.rk.terminal.ui.screens.welcome

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.rk.terminal.ui.theme.ModernThemeManager
import com.rk.terminal.ui.routes.MainActivityRoutes
import com.rk.settings.Settings
import com.rk.terminal.utils.RootUtils
import com.rk.terminal.utils.RootProvider
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentStep by remember { mutableIntStateOf(0) }
    var permissionsGranted by remember { mutableStateOf(false) }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
        if (permissionsGranted) {
            currentStep = 2 // Move to theme selection
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress indicator
        LinearProgressIndicator(
            progress = { (currentStep + 1) / 4f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        when (currentStep) {
            0 -> AppIntroductionStep(
                onNext = { currentStep = 1 }
            )
            1 -> PermissionsStep(
                onNext = { 
                    // Request permissions
                    val permissions = mutableListOf<String>()
                    
                    // Add notification permission for Android 13+
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    
                    // Add storage permissions for all versions
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                    
                    if (permissions.isNotEmpty()) {
                        permissionLauncher.launch(permissions.toTypedArray())
                    } else {
                        // If no permissions needed, just move to next step
                        currentStep = 2
                    }
                },
                onSkip = { currentStep = 2 }
            )
            2 -> RootConfigurationStep(
                onNext = { currentStep = 3 },
                onSkip = { currentStep = 3 }
            )
            3 -> InitialSetupStep(
                onComplete = {
                    // Mark onboarding as completed and navigate to main screen
                    ModernThemeManager.setOnboardingCompleted(context, true)
                    navController.navigate(MainActivityRoutes.MainScreen.route) {
                        popUpTo(MainActivityRoutes.Welcome.route) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun AppIntroductionStep(
    onNext: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Step 1: About ReTerminal",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ReTerminal is a powerful Linux terminal emulator that brings desktop-class terminal experience to your Android device.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "What you can do:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val capabilities = listOf(
                    "Run Linux commands and scripts",
                    "Install packages with APT, YUM, or Pacman",
                    "Code with vim, nano, or emacs",
                    "Use development tools like git, python, node.js",
                    "Access files and manage your system"
                )
                
                capabilities.forEach { capability ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = capability,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun PermissionsStep(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Step 2: Permissions",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ReTerminal needs certain permissions to provide the best experience.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Required permissions:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val permissions = buildList {
                    add("Storage access - To read and write files")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        add("Notifications - For terminal session alerts")
                    }
                    add("Internet access - For package downloads")
                }
                
                permissions.forEach { permission ->
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(20.dp)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = permission,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Grant Permissions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Skip for now",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun InitialSetupStep(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    var selectedDistro by remember { mutableIntStateOf(Settings.working_Mode) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Step 4: Initial Setup",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Configure your initial preferences to get started.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Default Linux Distribution",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val distros = listOf(
                    0 to "Alpine Linux (Lightweight)",
                    2 to "Ubuntu (Popular)",
                    3 to "Debian (Stable)",
                    4 to "Arch Linux (Advanced)"
                )
                
                distros.forEach { (id, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedDistro == id,
                            onClick = { 
                                selectedDistro = id
                                Settings.working_Mode = id
                            }
                        )
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Text(
                    text = "You can change these settings later in the app preferences.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onComplete,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Complete Setup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RootConfigurationStep(
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var rootCheckState by remember { mutableStateOf("idle") } // idle, checking, found, not_found, error
    var rootInfo by remember { mutableStateOf<com.rk.terminal.utils.RootInfo?>(null) }
    var showBusyBoxDialog by remember { mutableStateOf(false) }
    var userWantsRoot by remember { mutableStateOf(false) }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Step 3: Root Configuration (Optional)",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ReTerminal can optionally use root access for enhanced functionality. Root is NOT required.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = if (userWantsRoot) "Root Configuration" else "Do you want to use root access?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (!userWantsRoot) {
                    val rootFeatures = listOf(
                        "Enhanced filesystem access",
                        "Better network and device permissions", 
                        "Support for advanced Linux features",
                        "BusyBox utilities integration"
                    )
                    
                    rootFeatures.forEach { feature ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(end = 8.dp)
                            )
                            Text(
                                text = feature,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    // Root verification status
                    when (rootCheckState) {
                        "idle" -> {
                            Text(
                                text = "Click 'Verify Root' to check root access",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        "checking" -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Checking root access...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        "found" -> {
                            rootInfo?.let { info ->
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Root access verified!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Root Provider: ${info.rootProvider.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    
                                    Text(
                                        text = "BusyBox: ${if (info.isBusyBoxInstalled) "Installed" else "Not installed"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    
                                    if (!info.isBusyBoxInstalled) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Note: BusyBox is recommended for full functionality",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                        "not_found" -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Root access not available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        "error" -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Error checking root access",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!userWantsRoot) {
            // Initial choice buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        userWantsRoot = true
                        rootCheckState = "idle"
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Yes, use root")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                OutlinedButton(
                    onClick = {
                        // Continue without root
                        Settings.root_enabled = false
                        onNext()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("No, continue without root")
                }
            }
        } else {
            // Root verification buttons
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (rootCheckState == "idle" || rootCheckState == "error") {
                    Button(
                        onClick = {
                            rootCheckState = "checking"
                            scope.launch {
                                try {
                                    val info = RootUtils.checkRootAccess()
                                    rootInfo = info
                                    rootCheckState = if (info.isRootAvailable) "found" else "not_found"
                                    
                                    if (info.isRootAvailable) {
                                        // Save root configuration
                                        Settings.root_enabled = true
                                        Settings.root_verified = true
                                        Settings.root_provider = info.rootProvider.name.lowercase()
                                        Settings.busybox_installed = info.isBusyBoxInstalled
                                        Settings.use_root_mounts = true
                                        
                                        // Save BusyBox path if available
                                        val busyboxPath = RootUtils.getBusyBoxPath()
                                        if (busyboxPath != null) {
                                            Settings.busybox_path = busyboxPath
                                        }
                                    }
                                } catch (e: Exception) {
                                    rootCheckState = "error"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = rootCheckState != "checking"
                    ) {
                        Text("Verify Root Access")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (rootCheckState == "found" && rootInfo?.isBusyBoxInstalled == false) {
                    OutlinedButton(
                        onClick = { showBusyBoxDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Install BusyBox")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Button(
                    onClick = {
                        if (rootCheckState == "found") {
                            onNext()
                        } else if (rootCheckState == "not_found") {
                            // Ask if user wants to continue without root
                            Settings.root_enabled = false
                            onNext()
                        } else {
                            onNext()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = rootCheckState != "checking"
                ) {
                    Text(
                        when (rootCheckState) {
                            "found" -> "Continue with Root"
                            "not_found" -> "Continue without Root"
                            else -> "Continue"
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TextButton(
                    onClick = {
                        userWantsRoot = false
                        rootCheckState = "idle"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back")
                }
            }
        }
    }
    
    // BusyBox installation dialog
    if (showBusyBoxDialog) {
        AlertDialog(
            onDismissRequest = { showBusyBoxDialog = false },
            title = { Text("BusyBox Installation") },
            text = { 
                Text(
                    "BusyBox provides additional Unix utilities for enhanced functionality. " +
                    "Please install BusyBox through your root manager (Magisk or KernelSU) and restart your device.\n\n" +
                    "Recommended BusyBox module: BusyBox for Android NDK"
                )
            },
            confirmButton = {
                TextButton(onClick = { showBusyBoxDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}
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
import com.rk.settings.SettingsManager
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
                    
                    // Add storage permissions (minSdk is 24, so this check is always true)
                    permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                    permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    
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
    var selectedDistro by remember { mutableIntStateOf(SettingsManager.System.workingMode) }

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
                                SettingsManager.System.workingMode = id
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
    var showRootWarningDialog by remember { mutableStateOf(false) }
    var userWantsRoot by remember { mutableStateOf(false) }
    
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
            text = "Step 3: Root Configuration (Optional)",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¿Deseas usar acceso root para funciones avanzadas? (Opcional)",
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
                    text = if (userWantsRoot) "Configuración de Root" else "¿Deseas usar acceso root?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (!userWantsRoot) {
                    val rootFeatures = listOf(
                        "Acceso mejorado al sistema de archivos",
                        "Mejores permisos de red y dispositivos", 
                        "Soporte para funciones avanzadas de Linux",
                        "Integración con utilidades BusyBox"
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
                                text = "Haz clic en 'Verificar Root' para comprobar el acceso",
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
                                    text = "Verificando acceso root...",
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
                                            text = "¡Acceso root verificado!",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Proveedor Root: ${RootUtils.formatRootProviderName(info.rootProvider)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    
                                    Text(
                                        text = "BusyBox: ${if (info.isBusyBoxInstalled) "Instalado" else "No instalado"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    
                                    if (!info.isBusyBoxInstalled) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Nota: BusyBox es recomendado para funcionalidad completa",
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
                                    text = "Acceso root no disponible",
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
                                    text = "Error verificando acceso root",
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
                    Text("✅ Sí")
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                OutlinedButton(
                    onClick = {
                        // Continue without root
                        SettingsManager.Root.enabled = false
                        onNext()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("❌ No")
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
                                    if (info.isRootAvailable) {
                                        rootCheckState = "found"
                                        // Save root configuration
                                        SettingsManager.Root.enabled = true
                                        SettingsManager.Root.verified = true
                                        SettingsManager.Root.provider = info.rootProvider.name.lowercase()
                                        SettingsManager.Root.busyboxInstalled = info.isBusyBoxInstalled
                                        SettingsManager.Root.useMounts = true
                                        
                                        // Save BusyBox path if available
                                        val busyboxPath = RootUtils.getBusyBoxPath()
                                        if (busyboxPath != null) {
                                            SettingsManager.Root.busyboxPath = busyboxPath
                                        }
                                    } else {
                                        rootCheckState = "not_found"
                                        showRootWarningDialog = true
                                    }
                                } catch (e: Exception) {
                                    rootCheckState = "error"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = rootCheckState != "checking"
                    ) {
                        Text("Verificar Acceso Root")
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (rootCheckState == "found" && rootInfo?.isBusyBoxInstalled == false) {
                    OutlinedButton(
                        onClick = { showBusyBoxDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Instalar BusyBox")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Button(
                    onClick = {
                        if (rootCheckState == "found") {
                            onNext()
                        } else if (rootCheckState == "not_found") {
                            // Ask if user wants to continue without root
                            SettingsManager.Root.enabled = false
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
                            "found" -> "Continuar con Root"
                            "not_found" -> "Continuar sin Root"
                            else -> "Continuar"
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
                    Text("Atrás")
                }
            }
        }
    }
    
    // BusyBox installation dialog
    if (showBusyBoxDialog) {
        AlertDialog(
            onDismissRequest = { showBusyBoxDialog = false },
            title = { Text("Instalación de BusyBox Requerida") },
            text = { 
                Text(
                    "BusyBox proporciona utilidades Unix esenciales para funcionalidad root mejorada. " +
                    "Para instalar BusyBox:\n\n" +
                    "1. Descarga el ZIP BusyBox Installer desde:\n" +
                    "https://xdaforums.com/attachments/update-busybox-installer-v1-36-1-all-signed-zip.6000117\n\n" +
                    "2. Flashea el ZIP a través de tu administrador root (Magisk o KernelSU)\n\n" +
                    "3. Reinicia tu dispositivo después de la instalación\n\n" +
                    "Después del reinicio, BusyBox estará disponible para funcionalidad mejorada."
                )
            },
            confirmButton = {
                TextButton(onClick = { showBusyBoxDialog = false }) {
                    Text("Vale, lo instalaré")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBusyBoxDialog = false }) {
                    Text("Continuar sin BusyBox")
                }
            }
        )
    }
    
    // Root warning dialog
    if (showRootWarningDialog) {
        AlertDialog(
            onDismissRequest = { showRootWarningDialog = false },
            title = { Text("Acceso Root No Encontrado") },
            text = { 
                Text(
                    "No se encontraron permisos root para esta app. ¿Deseas continuar sin root?\n\n" +
                    "Sin root, ReTerminal funcionará en modo básico pero seguirá siendo completamente funcional."
                )
            },
            confirmButton = {
                TextButton(onClick = { 
                    showRootWarningDialog = false
                    SettingsManager.Root.enabled = false
                    onNext()
                }) {
                    Text("Sí, continuar sin root")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showRootWarningDialog = false
                    userWantsRoot = false
                    rootCheckState = "idle"
                }) {
                    Text("Volver atrás")
                }
            }
        )
    }
}
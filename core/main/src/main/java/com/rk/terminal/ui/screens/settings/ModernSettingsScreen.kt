package com.rk.terminal.ui.screens.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rk.components.compose.preferences.base.PreferenceGroup
import com.rk.components.compose.preferences.base.PreferenceLayout
import com.rk.components.compose.preferences.base.PreferenceTemplate
import com.rk.components.compose.preferences.switch.PreferenceSwitch
import com.rk.resources.strings
import com.rk.settings.SettingsManager
import com.rk.terminal.ui.activities.terminal.MainActivity
import com.rk.terminal.ui.components.SettingsToggle
import kotlinx.coroutines.CoroutineScope
import com.rk.terminal.ui.routes.MainActivityRoutes
import com.rk.terminal.utils.RootUtils
import com.rk.terminal.utils.VibrationUtil
import kotlinx.coroutines.launch

/**
 * Data class for settings category
 */
data class SettingsCategory(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String? = null,
    val onClick: (() -> Unit)? = null
)

/**
 * Modern settings screen with improved organization and better UX
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ModernSettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    mainActivity: MainActivity
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedOption by remember { mutableIntStateOf(SettingsManager.System.workingMode) }
    var showRootWarningDialog by remember { mutableStateOf(false) }

    // Real-time root status checking every 10 seconds
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000) // Check every 10 seconds
            try {
                if (SettingsManager.Root.enabled) {
                    RootUtils.clearCache()
                    val rootInfo = RootUtils.checkRootAccess()
                    SettingsManager.Root.verified = rootInfo.isRootAvailable
                    SettingsManager.Root.provider = rootInfo.rootProvider.name.lowercase()
                    SettingsManager.Root.busyboxInstalled = rootInfo.isBusyBoxInstalled
                    
                    val busyboxPath = RootUtils.getBusyBoxPath()
                    if (busyboxPath != null) {
                        SettingsManager.Root.busyboxPath = busyboxPath
                    }
                    
                    // If root was lost while enabled, disable it
                    if (!rootInfo.isRootAvailable) {
                        SettingsManager.Root.enabled = false
                        SettingsManager.Root.useMounts = false
                    }
                }
            } catch (e: Exception) {
                // Root checking failed, keep current state
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(strings.settings),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Main Settings Categories
        item {
            SettingsCategorySection(
                title = "Appearance & Themes",
                categories = listOf(
                    SettingsCategory(
                        title = "Theme Selection",
                        description = "Choose your preferred color scheme",
                        icon = Icons.Default.Star,
                        route = MainActivityRoutes.ThemeSelection.route
                    ),
                    SettingsCategory(
                        title = "Visual Customization",
                        description = "Fonts, transparency, and visual settings",
                        icon = Icons.Default.Build,
                        route = MainActivityRoutes.Customization.route
                    )
                ),
                navController = navController
            )
        }

        // Working Mode Selection
        item {
            WorkingModeSection(
                selectedOption = selectedOption,
                onSelectionChanged = { newMode ->
                    selectedOption = newMode
                    SettingsManager.System.workingMode = newMode
                    VibrationUtil.vibrateButton(context)
                }
            )
        }

        // Interface Settings
        item {
            InterfaceSettingsSection()
        }

        // Root Configuration
        item {
            RootConfigurationSection(
                showRootWarningDialog = showRootWarningDialog,
                onShowRootWarningDialog = { showRootWarningDialog = it },
                scope = scope
            )
        }

        // Advanced Settings
        item {
            AdvancedSettingsSection()
        }
    }

    // Root warning dialog
    if (showRootWarningDialog) {
        RootWarningDialog(
            onDismiss = { showRootWarningDialog = false },
            onConfirm = {
                VibrationUtil.vibrateError(context)
                SettingsManager.Root.enabled = false
                SettingsManager.Root.useMounts = false
                showRootWarningDialog = false
            },
            onCancel = {
                VibrationUtil.vibrateButton(context)
                showRootWarningDialog = false
            }
        )
    }
}

@Composable
private fun SettingsCategorySection(
    title: String,
    categories: List<SettingsCategory>,
    navController: NavController
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            categories.forEachIndexed { index, category ->
                SettingsCategoryItem(
                    category = category,
                    onClick = {
                        if (category.route != null) {
                            navController.navigate(category.route)
                        } else {
                            category.onClick?.invoke()
                        }
                    }
                )
                
                if (index < categories.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SettingsCategoryItem(
    category: SettingsCategory,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                indication = ripple(),
                interactionSource = interactionSource,
                onClick = onClick
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = category.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = category.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun WorkingModeSection(
    selectedOption: Int,
    onSelectionChanged: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Default Working Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            val workingModes = listOf(
                Triple(WorkingMode.ALPINE, "Alpine Linux", "Lightweight Linux distribution"),
                Triple(WorkingMode.UBUNTU, "Ubuntu", "Ubuntu Linux ARM64"),
                Triple(WorkingMode.DEBIAN, "Debian", "Debian Linux ARM64"),
                Triple(WorkingMode.ARCH, "Arch Linux", "Arch Linux ARM64"),
                Triple(WorkingMode.KALI, "Kali Linux", "Kali Linux ARM64"),
                Triple(WorkingMode.ANDROID, "Android", "ReTerminal Android shell")
            )
            
            workingModes.forEachIndexed { index, (mode, name, description) ->
                WorkingModeItem(
                    mode = mode,
                    name = name,
                    description = description,
                    isSelected = selectedOption == mode,
                    onSelect = { onSelectionChanged(mode) }
                )
                
                if (index < workingModes.size - 1) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkingModeItem(
    mode: Int,
    name: String,
    description: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                indication = ripple(),
                interactionSource = interactionSource,
                onClick = onSelect
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary
            )
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InterfaceSettingsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Interface",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            PreferenceSwitch(
                label = "Status Bar",
                description = "Show status bar in terminal",
                checked = SettingsManager.Interface.statusBar,
                onCheckedChange = { SettingsManager.Interface.statusBar = it }
            )
            
            PreferenceSwitch(
                label = "Horizontal Status Bar",
                description = "Show status bar in landscape mode",
                checked = SettingsManager.Interface.horizontalStatusBar,
                onCheckedChange = { SettingsManager.Interface.horizontalStatusBar = it }
            )
            
            PreferenceSwitch(
                label = "Virtual Keyboard",
                description = "Show virtual keys for terminal",
                checked = SettingsManager.Interface.virtualKeys,
                onCheckedChange = { SettingsManager.Interface.virtualKeys = it }
            )
            
            PreferenceSwitch(
                label = "Toolbar",
                description = "Show terminal toolbar",
                checked = SettingsManager.Interface.toolbar,
                onCheckedChange = { SettingsManager.Interface.toolbar = it }
            )
        }
    }
}

@Composable
private fun RootConfigurationSection(
    showRootWarningDialog: Boolean,
    onShowRootWarningDialog: (Boolean) -> Unit,
    scope: CoroutineScope
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Root Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            PreferenceSwitch(
                label = "Enable Root Access",
                description = if (SettingsManager.Root.enabled) {
                    "Root access is enabled (${RootUtils.formatRootProviderName(SettingsManager.Root.provider)})"
                } else {
                    "Root access is disabled - using rootless mode"
                },
                enabled = SettingsManager.Root.verified || !SettingsManager.Root.enabled,
                checked = SettingsManager.Root.enabled,
                onCheckedChange = { enabled ->
                    if (!enabled) {
                        onShowRootWarningDialog(true)
                    } else if (SettingsManager.Root.verified) {
                        SettingsManager.Root.enabled = true
                        SettingsManager.Root.useMounts = true
                    } else {
                        scope.launch {
                            try {
                                val rootInfo = RootUtils.checkRootAccess()
                                if (rootInfo.isRootAvailable) {
                                    SettingsManager.Root.enabled = true
                                    SettingsManager.Root.verified = true
                                    SettingsManager.Root.provider = rootInfo.rootProvider.name.lowercase()
                                    SettingsManager.Root.busyboxInstalled = rootInfo.isBusyBoxInstalled
                                    SettingsManager.Root.useMounts = true
                                    
                                    val busyboxPath = RootUtils.getBusyBoxPath()
                                    if (busyboxPath != null) {
                                        SettingsManager.Root.busyboxPath = busyboxPath
                                    }
                                } else {
                                    SettingsManager.Root.enabled = false
                                }
                            } catch (e: Exception) {
                                SettingsManager.Root.enabled = false
                            }
                        }
                    }
                }
            )
            
            if (SettingsManager.Root.enabled) {
                PreferenceSwitch(
                    label = "Use Root Mounts",
                    description = "Enable enhanced filesystem mounts and bindings",
                    checked = SettingsManager.Root.useMounts,
                    enabled = SettingsManager.Root.enabled,
                    onCheckedChange = { SettingsManager.Root.useMounts = it }
                )
            }
        }
    }
}

@Composable
private fun AdvancedSettingsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Advanced",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            PreferenceSwitch(
                label = "Graphics Acceleration",
                description = "Enable OpenGL/Vulkan acceleration in Linux distributions",
                checked = SettingsManager.System.graphicsAcceleration,
                onCheckedChange = { SettingsManager.System.graphicsAcceleration = it }
            )
            
            PreferenceSwitch(
                label = "Bell Sound",
                description = "Play bell sound for terminal notifications",
                checked = SettingsManager.Feedback.bell,
                onCheckedChange = { SettingsManager.Feedback.bell = it }
            )
            
            PreferenceSwitch(
                label = "Haptic Feedback",
                description = "Vibrate on virtual key presses",
                checked = SettingsManager.Feedback.vibrate,
                onCheckedChange = { SettingsManager.Feedback.vibrate = it }
            )
        }
    }
}

@Composable
private fun RootWarningDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Disable Root Access") },
        text = { 
            Text(
                "Disabling root access will:\n" +
                "• Remove enhanced filesystem mounts\n" +
                "• Disable BusyBox integration\n" +
                "• Fall back to standard proot mode\n\n" +
                "You can re-enable root access later. Continue?"
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Disable Root")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}
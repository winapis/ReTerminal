package com.rk.terminal.ui.screens.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.rk.terminal.ui.routes.MainActivityRoutes
import com.rk.terminal.utils.RootUtils
import com.rk.terminal.utils.VibrationUtil
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsCard(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    title: @Composable () -> Unit,
    description: @Composable () -> Unit = {},
    startWidget: (@Composable () -> Unit)? = null,
    endWidget: (@Composable () -> Unit)? = null,
    isEnabled: Boolean = true,
    onClick: () -> Unit
) {
    PreferenceTemplate(
        modifier = modifier
            .combinedClickable(
                enabled = isEnabled,
                indication = ripple(),
                interactionSource = interactionSource,
                onClick = onClick
            ),
        contentModifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 16.dp)
            .padding(start = 16.dp),
        title = title,
        description = description,
        startWidget = startWidget,
        endWidget = endWidget,
        applyPaddings = false
    )

}


object WorkingMode{
    const val ALPINE = 0
    const val ANDROID = 1
    const val UBUNTU = 2
    const val DEBIAN = 3
    const val ARCH = 4
    const val KALI = 5
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(modifier: Modifier = Modifier,navController: NavController,mainActivity: MainActivity) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedOption by remember { mutableIntStateOf(SettingsManager.System.workingMode) }
    var showRootWarningDialog by remember { mutableStateOf(false) }
    
    // Reactive state management for root settings
    var rootEnabled by remember { mutableStateOf(SettingsManager.Root.enabled) }
    var rootUseMounts by remember { mutableStateOf(SettingsManager.Root.useMounts) }
    var rootVerified by remember { mutableStateOf(SettingsManager.Root.verified) }
    var rootProvider by remember { mutableStateOf(SettingsManager.Root.provider) }
    var rootBusyboxInstalled by remember { mutableStateOf(SettingsManager.Root.busyboxInstalled) }
    var rootBusyboxPath by remember { mutableStateOf(SettingsManager.Root.busyboxPath) }

    // Real-time root status checking every 10 seconds
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000) // Check every 10 seconds
            try {
                if (rootEnabled) {
                    RootUtils.clearCache()
                    val rootInfo = RootUtils.checkRootAccess()
                    rootVerified = rootInfo.isRootAvailable
                    rootProvider = rootInfo.rootProvider.name.lowercase()
                    rootBusyboxInstalled = rootInfo.isBusyBoxInstalled
                    
                    SettingsManager.Root.verified = rootVerified
                    SettingsManager.Root.provider = rootProvider
                    SettingsManager.Root.busyboxInstalled = rootBusyboxInstalled
                    
                    val busyboxPath = RootUtils.getBusyBoxPath()
                    if (busyboxPath != null) {
                        rootBusyboxPath = busyboxPath
                        SettingsManager.Root.busyboxPath = busyboxPath
                    }
                    
                    // If root was lost while enabled, disable it
                    if (!rootInfo.isRootAvailable) {
                        rootEnabled = false
                        rootUseMounts = false
                        SettingsManager.Root.enabled = false
                        SettingsManager.Root.useMounts = false
                    }
                }
            } catch (e: Exception) {
                // Root checking failed, keep current state
            }
        }
    }

    PreferenceLayout(label = stringResource(strings.settings)) {
        // Distribution Selection Group
        PreferenceGroup(heading = "Distribution") {
            Text(
                text = "Select your preferred Linux distribution",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            SettingsCard(
                title = { Text("Alpine Linux") },
                description = { Text("Lightweight, security-oriented distribution") },
                startWidget = {
                    RadioButton(
                        modifier = Modifier.padding(start = 8.dp),
                        selected = selectedOption == WorkingMode.ALPINE,
                        onClick = {
                            VibrationUtil.vibrateButton(context)
                            selectedOption = WorkingMode.ALPINE
                            SettingsManager.System.workingMode = selectedOption
                        })
                },
                onClick = {
                    VibrationUtil.vibrateButton(context)
                    selectedOption = WorkingMode.ALPINE
                    SettingsManager.System.workingMode = selectedOption
                })

            SettingsCard(
                title = { Text("Ubuntu Linux") },
                description = { Text("Popular, user-friendly distribution with extensive package support") },
                startWidget = {
                    RadioButton(
                        modifier = Modifier.padding(start = 8.dp),
                        selected = selectedOption == WorkingMode.UBUNTU,
                        onClick = {
                            selectedOption = WorkingMode.UBUNTU
                            SettingsManager.System.workingMode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.UBUNTU
                    SettingsManager.System.workingMode = selectedOption
                })

            SettingsCard(
                title = { Text("Debian Linux") },
                description = { Text("Stable and reliable distribution, basis for Ubuntu") },
                startWidget = {
                    RadioButton(
                        modifier = Modifier.padding(start = 8.dp),
                        selected = selectedOption == WorkingMode.DEBIAN,
                        onClick = {
                            selectedOption = WorkingMode.DEBIAN
                            SettingsManager.System.workingMode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.DEBIAN
                    SettingsManager.System.workingMode = selectedOption
                })

            SettingsCard(
                title = { Text("Arch Linux") },
                description = { Text("Rolling release distribution for advanced users") },
                startWidget = {
                    RadioButton(
                        modifier = Modifier.padding(start = 8.dp),
                        selected = selectedOption == WorkingMode.ARCH,
                        onClick = {
                            selectedOption = WorkingMode.ARCH
                            SettingsManager.System.workingMode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.ARCH
                    SettingsManager.System.workingMode = selectedOption
                })

            SettingsCard(
                title = { Text("Kali Linux") },
                description = { Text("Penetration testing and security auditing distribution") },
                startWidget = {
                    RadioButton(
                        modifier = Modifier.padding(start = 8.dp),
                        selected = selectedOption == WorkingMode.KALI,
                        onClick = {
                            selectedOption = WorkingMode.KALI
                            SettingsManager.System.workingMode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.KALI
                    SettingsManager.System.workingMode = selectedOption
                })

            SettingsCard(
                title = { Text("Android Shell") },
                description = { Text("Native Android terminal environment") },
                startWidget = {
                    RadioButton(
                        modifier = Modifier
                            .padding(start = 8.dp),
                        selected = selectedOption == WorkingMode.ANDROID,
                        onClick = {
                            selectedOption = WorkingMode.ANDROID
                            SettingsManager.System.workingMode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.ANDROID
                    SettingsManager.System.workingMode = selectedOption
                })
        }


        // Root Configuration Group
        PreferenceGroup(heading = "Root Configuration") {
            Text(
                text = "Configure root access and enhanced features",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            PreferenceSwitch(
                label = "Enable Root Access",
                description = if (rootEnabled) {
                    "Root access is enabled (${RootUtils.formatRootProviderName(rootProvider)})"
                } else {
                    "Root access is disabled - using rootless mode"
                },
                enabled = rootVerified || !rootEnabled,
                checked = rootEnabled,
                onCheckedChange = { enabled ->
                    if (!enabled) {
                        // Disabling root - show warning
                        showRootWarningDialog = true
                    } else if (rootVerified) {
                        // Re-enabling root if previously verified
                        rootEnabled = true
                        rootUseMounts = true
                        SettingsManager.Root.enabled = true
                        SettingsManager.Root.useMounts = true
                    } else {
                        // Root not verified, need to verify first
                        scope.launch {
                            try {
                                val rootInfo = RootUtils.checkRootAccess()
                                if (rootInfo.isRootAvailable) {
                                    rootEnabled = true
                                    rootVerified = true
                                    rootProvider = rootInfo.rootProvider.name.lowercase()
                                    rootBusyboxInstalled = rootInfo.isBusyBoxInstalled
                                    rootUseMounts = true
                                    
                                    SettingsManager.Root.enabled = true
                                    SettingsManager.Root.verified = true
                                    SettingsManager.Root.provider = rootProvider
                                    SettingsManager.Root.busyboxInstalled = rootBusyboxInstalled
                                    SettingsManager.Root.useMounts = true
                                    
                                    val busyboxPath = RootUtils.getBusyBoxPath()
                                    if (busyboxPath != null) {
                                        rootBusyboxPath = busyboxPath
                                        SettingsManager.Root.busyboxPath = busyboxPath
                                    }
                                } else {
                                    // Root not available
                                    rootEnabled = false
                                    SettingsManager.Root.enabled = false
                                }
                            } catch (e: Exception) {
                                rootEnabled = false
                                SettingsManager.Root.enabled = false
                            }
                        }
                    }
                }
            )
            
            if (rootEnabled) {
                SettingsCard(
                    title = { Text("Root Provider") },
                    description = { Text(RootUtils.formatRootProviderName(rootProvider)) },
                    onClick = { VibrationUtil.vibrateButton(context) }
                )
                
                SettingsCard(
                    title = { Text("BusyBox Status") },
                    description = { 
                        Text(
                            if (rootBusyboxInstalled) {
                                "Installed at ${rootBusyboxPath.takeIf { it.isNotEmpty() } ?: "system path"}"
                            } else {
                                "Not installed - some features may be limited"
                            }
                        )
                    },
                    onClick = { VibrationUtil.vibrateButton(context) }
                )
                
                PreferenceSwitch(
                    label = "Use Root Mounts",
                    description = "Enable enhanced filesystem mounts and bindings. Changes apply to new terminal sessions.",
                    checked = rootUseMounts,
                    enabled = rootEnabled,
                    onCheckedChange = { enabled ->
                        VibrationUtil.vibrateAction(context)
                        rootUseMounts = enabled
                        SettingsManager.Root.useMounts = enabled
                        if (enabled) {
                            VibrationUtil.vibrateSuccess(context)
                        }
                    }
                )
            }
        }


        // Appearance & Customization Group
        PreferenceGroup(heading = "Appearance & Customization") {
            Text(
                text = "Customize the look and feel of your terminal",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            SettingsToggle(
                label = "Theme Selection",
                showSwitch = false,
                default = false,
                sideEffect = {
                   navController.navigate(MainActivityRoutes.ThemeSelection.route)
            }, endWidget = {
                Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null,modifier = Modifier.padding(16.dp))
            })
            
            SettingsToggle(
                label = "Customizations",
                showSwitch = false,
                default = false,
                sideEffect = {
                   navController.navigate(MainActivityRoutes.Customization.route)
            }, endWidget = {
                Icon(imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null,modifier = Modifier.padding(16.dp))
            })
        }
    }
    
    // Root warning dialog
    if (showRootWarningDialog) {
        AlertDialog(
            onDismissRequest = { showRootWarningDialog = false },
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
                TextButton(
                    onClick = { 
                        VibrationUtil.vibrateError(context)
                        rootEnabled = false
                        rootUseMounts = false
                        SettingsManager.Root.enabled = false
                        SettingsManager.Root.useMounts = false
                        showRootWarningDialog = false
                    }
                ) {
                    Text("Disable Root")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    VibrationUtil.vibrateButton(context)
                    showRootWarningDialog = false 
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
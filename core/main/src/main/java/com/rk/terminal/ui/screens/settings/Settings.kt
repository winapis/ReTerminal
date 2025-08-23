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

    PreferenceLayout(label = stringResource(strings.settings)) {
        PreferenceGroup(heading = "Default Working mode") {

            SettingsCard(
                title = { Text("Alpine") },
                description = {Text("Alpine Linux")},
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
                title = { Text("Ubuntu") },
                description = {Text("Ubuntu Linux ARM64")},
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
                title = { Text("Debian") },
                description = {Text("Debian Linux ARM64")},
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
                description = {Text("Arch Linux ARM64")},
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
                description = {Text("Kali Linux ARM64")},
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
                title = { Text("Android") },
                description = {Text("ReTerminal Android shell")},
                startWidget = {
                    RadioButton(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            ,
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


        PreferenceGroup(heading = "Root Configuration") {
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
                        // Disabling root - show warning
                        showRootWarningDialog = true
                    } else if (SettingsManager.Root.verified) {
                        // Re-enabling root if previously verified
                        SettingsManager.Root.enabled = true
                        SettingsManager.Root.useMounts = true
                    } else {
                        // Root not verified, need to verify first
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
                                    // Root not available
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
                SettingsCard(
                    title = { Text("Root Provider") },
                    description = { Text(RootUtils.formatRootProviderName(SettingsManager.Root.provider)) },
                    onClick = { VibrationUtil.vibrateButton(context) }
                )
                
                SettingsCard(
                    title = { Text("BusyBox Status") },
                    description = { 
                        Text(
                            if (SettingsManager.Root.busyboxInstalled) {
                                "Installed at ${SettingsManager.Root.busyboxPath.takeIf { it.isNotEmpty() } ?: "system path"}"
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
                    checked = SettingsManager.Root.useMounts,
                    enabled = SettingsManager.Root.enabled,
                    onCheckedChange = { enabled ->
                        VibrationUtil.vibrateAction(context)
                        SettingsManager.Root.useMounts = enabled
                        if (enabled) {
                            VibrationUtil.vibrateSuccess(context)
                        }
                    }
                )
            }
        }


        PreferenceGroup(heading = "Appearance") {
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
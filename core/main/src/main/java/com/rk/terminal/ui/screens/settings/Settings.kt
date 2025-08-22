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
import com.rk.settings.Settings
import com.rk.terminal.ui.activities.terminal.MainActivity
import com.rk.terminal.ui.components.SettingsToggle
import com.rk.terminal.ui.routes.MainActivityRoutes
import com.rk.terminal.utils.RootUtils
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
    var selectedOption by remember { mutableIntStateOf(Settings.working_Mode) }
    var showRootWarningDialog by remember { mutableStateOf(false) }

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
                            selectedOption = WorkingMode.ALPINE
                            Settings.working_Mode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.ALPINE
                    Settings.working_Mode = selectedOption
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
                            Settings.working_Mode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.UBUNTU
                    Settings.working_Mode = selectedOption
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
                            Settings.working_Mode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.DEBIAN
                    Settings.working_Mode = selectedOption
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
                            Settings.working_Mode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.ARCH
                    Settings.working_Mode = selectedOption
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
                            Settings.working_Mode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.KALI
                    Settings.working_Mode = selectedOption
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
                            Settings.working_Mode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.ANDROID
                    Settings.working_Mode = selectedOption
                })
        }


        PreferenceGroup(heading = "Root Configuration") {
            PreferenceSwitch(
                label = "Enable Root Access",
                description = if (Settings.root_enabled) {
                    "Root access is enabled (${Settings.root_provider.uppercase()})"
                } else {
                    "Root access is disabled - using rootless mode"
                },
                enabled = Settings.root_verified || !Settings.root_enabled,
                checked = Settings.root_enabled,
                onCheckedChange = { enabled ->
                    if (!enabled) {
                        // Disabling root - show warning
                        showRootWarningDialog = true
                    } else if (Settings.root_verified) {
                        // Re-enabling root if previously verified
                        Settings.root_enabled = true
                        Settings.use_root_mounts = true
                    } else {
                        // Root not verified, need to verify first
                        scope.launch {
                            try {
                                val rootInfo = RootUtils.checkRootAccess()
                                if (rootInfo.isRootAvailable) {
                                    Settings.root_enabled = true
                                    Settings.root_verified = true
                                    Settings.root_provider = rootInfo.rootProvider.name.lowercase()
                                    Settings.busybox_installed = rootInfo.isBusyBoxInstalled
                                    Settings.use_root_mounts = true
                                    
                                    val busyboxPath = RootUtils.getBusyBoxPath()
                                    if (busyboxPath != null) {
                                        Settings.busybox_path = busyboxPath
                                    }
                                } else {
                                    // Root not available
                                    Settings.root_enabled = false
                                }
                            } catch (e: Exception) {
                                Settings.root_enabled = false
                            }
                        }
                    }
                }
            )
            
            if (Settings.root_enabled) {
                SettingsCard(
                    title = { Text("Root Provider") },
                    description = { Text(Settings.root_provider.uppercase()) },
                    onClick = { }
                )
                
                SettingsCard(
                    title = { Text("BusyBox Status") },
                    description = { 
                        Text(
                            if (Settings.busybox_installed) {
                                "Installed at ${Settings.busybox_path.takeIf { it.isNotEmpty() } ?: "system path"}"
                            } else {
                                "Not installed - some features may be limited"
                            }
                        )
                    },
                    onClick = { }
                )
                
                PreferenceSwitch(
                    label = "Use Root Mounts",
                    description = "Enable enhanced filesystem mounts and bindings",
                    checked = Settings.use_root_mounts,
                    enabled = Settings.root_enabled,
                    onCheckedChange = { enabled ->
                        Settings.use_root_mounts = enabled
                    }
                )
            }
            
            SettingsCard(
                title = { Text("Re-verify Root Access") },
                description = { Text("Check root status and update configuration") },
                onClick = {
                    scope.launch {
                        try {
                            RootUtils.clearCache()
                            val rootInfo = RootUtils.checkRootAccess()
                            Settings.root_verified = rootInfo.isRootAvailable
                            Settings.root_provider = rootInfo.rootProvider.name.lowercase()
                            Settings.busybox_installed = rootInfo.isBusyBoxInstalled
                            
                            val busyboxPath = RootUtils.getBusyBoxPath()
                            if (busyboxPath != null) {
                                Settings.busybox_path = busyboxPath
                            }
                            
                            if (!rootInfo.isRootAvailable && Settings.root_enabled) {
                                Settings.root_enabled = false
                                Settings.use_root_mounts = false
                            }
                        } catch (e: Exception) {
                            Settings.root_verified = false
                        }
                    }
                }
            )
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
                        Settings.root_enabled = false
                        Settings.use_root_mounts = false
                        showRootWarningDialog = false
                    }
                ) {
                    Text("Disable Root")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRootWarningDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
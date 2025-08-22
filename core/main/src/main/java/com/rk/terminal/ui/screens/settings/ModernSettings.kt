package com.rk.terminal.ui.screens.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rk.components.compose.preferences.base.PreferenceGroup
import com.rk.components.compose.preferences.base.PreferenceLayout
import com.rk.components.compose.preferences.base.PreferenceTemplate
import com.rk.resources.strings
import com.rk.settings.Settings
import com.rk.terminal.ui.activities.terminal.MainActivity
import com.rk.terminal.ui.components.SettingsToggle
import com.rk.terminal.ui.routes.MainActivityRoutes
import com.rk.terminal.ui.theme.ThemeHelper
import com.rk.terminal.ui.theme.ThemeManager

/**
 * Modern Settings screen with theme selection, search, and enhanced UI.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ModernSettings(
    navController: NavController, 
    mainActivity: MainActivity
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTheme by remember { mutableIntStateOf(Settings.color_scheme) }
    var selectedWorkingMode by remember { mutableIntStateOf(Settings.working_Mode) }
    var showThemeDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with search
        item {
            Column {
                Text(
                    text = stringResource(strings.settings),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search settings") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
        
        // Theme Selection Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Theme",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                ThemeHelper.getThemeName(selectedTheme),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showThemeDialog = true }) {
                            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Theme preview
                    ThemePreview(selectedTheme)
                }
            }
        }
        
        // Working Mode Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Default Working Mode",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val workingModes = listOf(
                        0 to "Alpine Linux", // WorkingMode.ALPINE
                        2 to "Ubuntu Linux ARM64", // WorkingMode.UBUNTU
                        3 to "Debian Linux ARM64", // WorkingMode.DEBIAN
                        4 to "Arch Linux ARM64", // WorkingMode.ARCH
                        5 to "Kali Linux ARM64", // WorkingMode.KALI
                        1 to "ReTerminal Android shell" // WorkingMode.ANDROID
                    )
                    
                    workingModes.forEach { (mode, description) ->
                        WorkingModeItem(
                            mode = mode,
                            description = description,
                            selected = selectedWorkingMode == mode,
                            onClick = {
                                selectedWorkingMode = mode
                                Settings.working_Mode = mode
                            }
                        )
                    }
                }
            }
        }
        
        // Customizations Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                ListItem(
                    headlineContent = { Text("Customizations") },
                    supportingContent = { Text("Font, colors, and appearance") },
                    trailingContent = { 
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null) 
                    },
                    modifier = Modifier.combinedClickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { navController.navigate(MainActivityRoutes.Customization.route) }
                    )
                )
            }
        }
    }
    
    // Theme Selection Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = selectedTheme,
            onThemeSelected = { themeId ->
                selectedTheme = themeId
                ThemeManager.setTheme(context, themeId)
                Settings.color_scheme = themeId
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkingModeItem(
    mode: Int,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = when (mode) {
                    0 -> "Alpine" // ALPINE
                    2 -> "Ubuntu" // UBUNTU
                    3 -> "Debian" // DEBIAN
                    4 -> "Arch Linux" // ARCH
                    5 -> "Kali Linux" // KALI
                    1 -> "Android" // ANDROID
                    else -> "Unknown"
                },
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ThemePreview(themeId: Int) {
    val isDark = ThemeHelper.isDarkTheme(themeId)
    val terminalColors = ThemeHelper.getTerminalColors(themeId)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(terminalColors[0])) // Background color
            .border(
                1.dp, 
                MaterialTheme.colorScheme.outline, 
                RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            // Terminal text preview
            Text(
                text = "$ echo 'Hello, World!'",
                color = Color(terminalColors[7]), // Foreground color
                fontSize = 12.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            Text(
                text = "Hello, World!",
                color = Color(terminalColors[2]), // Green color
                fontSize = 12.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            Text(
                text = "$ _",
                color = Color(terminalColors[7]), // Foreground color
                fontSize = 12.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ThemeSelectionDialog(
    currentTheme: Int,
    onThemeSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val themes = ThemeHelper.getAllThemes()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Theme") },
        text = {
            LazyColumn(
                modifier = Modifier.height(400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                themes.forEach { (category, themeList) ->
                    item {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(themeList) { (themeId, themeName) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = { 
                                        onThemeSelected(themeId)
                                        onDismiss()
                                    }
                                ),
                            colors = if (currentTheme == themeId) {
                                CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            } else {
                                CardDefaults.cardColors()
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Mini theme preview
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(ThemeHelper.getTerminalColors(themeId)[0]))
                                        .border(
                                            1.dp,
                                            Color(ThemeHelper.getTerminalColors(themeId)[7]),
                                            RoundedCornerShape(4.dp)
                                        )
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = themeName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                if (currentTheme == themeId) {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}


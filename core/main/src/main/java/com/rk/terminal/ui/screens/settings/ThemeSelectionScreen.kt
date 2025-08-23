package com.rk.terminal.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rk.terminal.ui.theme.ModernThemeManager
import com.rk.terminal.ui.theme.ThemeState
import com.rk.terminal.utils.VibrationUtil
import com.rk.settings.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTheme by remember { mutableIntStateOf(ThemeState.getCurrentTheme()) }
    val allThemes = ModernThemeManager.getAllThemes()
    val darkThemes = allThemes.filter { it.isDark }
    val lightThemes = allThemes.filter { !it.isDark }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text("Theme Selection") },
            navigationIcon = {
                IconButton(onClick = { 
                    VibrationUtil.vibrateButton(context)
                    navController.navigateUp() 
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // System Default
            item {
                ThemeGroupHeader(
                    title = "System",
                    icon = Icons.Default.Menu
                )
            }
            
            item {
                ThemeCard(
                    theme = allThemes[0], // System theme
                    isSelected = selectedTheme == 0,
                    onClick = {
                        VibrationUtil.vibrateAction(context)
                        selectedTheme = 0
                        ThemeState.updateTheme(0)
                        ModernThemeManager.applyTheme(context, 0)
                    }
                )
            }

            // Dark Themes Section
            item {
                ThemeGroupHeader(
                    title = "Dark Themes",
                    icon = Icons.Default.Menu
                )
            }

            items(darkThemes.filter { it.id != 0 }) { theme ->
                ThemeCard(
                    theme = theme,
                    isSelected = selectedTheme == theme.id,
                    onClick = {
                        VibrationUtil.vibrateAction(context)
                        selectedTheme = theme.id
                        ThemeState.updateTheme(theme.id)
                        ModernThemeManager.applyTheme(context, theme.id)
                    }
                )
            }

            // Light Themes Section
            item {
                ThemeGroupHeader(
                    title = "Light Themes",
                    icon = Icons.Default.Info
                )
            }

            items(lightThemes) { theme ->
                ThemeCard(
                    theme = theme,
                    isSelected = selectedTheme == theme.id,
                    onClick = {
                        VibrationUtil.vibrateAction(context)
                        selectedTheme = theme.id
                        ThemeState.updateTheme(theme.id)
                        ModernThemeManager.applyTheme(context, theme.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeGroupHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ThemeCard(
    theme: ModernThemeManager.ThemeInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme preview
            ThemePreview(
                backgroundColor = Color(theme.backgroundColor),
                foregroundColor = Color(theme.foregroundColor),
                accentColor = Color(theme.accentColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Theme info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = theme.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = theme.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
            }

            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ThemePreview(
    backgroundColor: Color,
    foregroundColor: Color,
    accentColor: Color
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        // Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp))
            ) {
                // Background color
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) { 
                        drawRect(backgroundColor)
                    }
                }
                
                // Terminal preview simulation
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top bar simulation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(2.dp))
                    ) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) { 
                            drawRect(accentColor)
                        }
                    }
                    
                    // Text simulation
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (it == 2) 0.6f else 0.8f)
                                    .height(2.dp)
                                    .clip(RoundedCornerShape(1.dp))
                            ) {
                                androidx.compose.foundation.Canvas(
                                    modifier = Modifier.fillMaxSize()
                                ) { 
                                    drawRect(foregroundColor.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
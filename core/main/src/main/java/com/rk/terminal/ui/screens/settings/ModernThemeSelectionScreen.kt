package com.rk.terminal.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rk.terminal.ui.theme.ModernThemeManager
import com.rk.terminal.ui.theme.ThemeState
import com.rk.terminal.utils.VibrationUtil
import com.rk.settings.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernThemeSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val availableThemes = ModernThemeManager.getAllThemes()
    var currentTheme by remember { mutableIntStateOf(SettingsManager.Appearance.colorScheme) }
    var showingCategory by remember { mutableStateOf("all") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Theme Selection") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Theme variant toggle
                    var showVariantMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showVariantMenu = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Theme options")
                    }
                    
                    DropdownMenu(
                        expanded = showVariantMenu,
                        onDismissRequest = { showVariantMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Auto (System)") },
                            onClick = {
                                SettingsManager.Appearance.themeVariant = 0
                                showVariantMenu = false
                            },
                            leadingIcon = { 
                                if (SettingsManager.Appearance.themeVariant == 0) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Light Mode") },
                            onClick = {
                                SettingsManager.Appearance.themeVariant = 1
                                showVariantMenu = false
                            },
                            leadingIcon = { 
                                if (SettingsManager.Appearance.themeVariant == 1) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Dark Mode") },
                            onClick = {
                                SettingsManager.Appearance.themeVariant = 2
                                showVariantMenu = false
                            },
                            leadingIcon = { 
                                if (SettingsManager.Appearance.themeVariant == 2) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Category Filter
            item {
                ThemeCategoryFilter(
                    currentCategory = showingCategory,
                    onCategoryChanged = { showingCategory = it }
                )
            }

            // Dynamic theming options (Android 12+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                item {
                    DynamicThemingSection()
                }
            }

            // Theme preview grid
            item {
                val filteredThemes = when (showingCategory) {
                    "dark" -> availableThemes.filter { it.isDark && it.id != 0 }
                    "light" -> availableThemes.filter { !it.isDark && it.id != 0 }
                    "system" -> availableThemes.filter { it.id == 0 }
                    else -> availableThemes
                }

                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(280.dp),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(600.dp) // Fixed height for nested scrolling
                ) {
                    items(filteredThemes) { theme ->
                        ThemePreviewCard(
                            theme = theme,
                            isSelected = currentTheme == theme.id,
                            onSelect = {
                                VibrationUtil.vibrateButton(context)
                                currentTheme = theme.id
                                ModernThemeManager.applyTheme(context, theme.id)
                            }
                        )
                    }
                }
            }

            // Theme info section
            item {
                val selectedTheme = availableThemes.find { it.id == currentTheme }
                selectedTheme?.let { theme ->
                    ThemeInfoCard(theme = theme)
                }
            }
        }
    }
}

@Composable
private fun ThemeCategoryFilter(
    currentCategory: String,
    onCategoryChanged: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Categories",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip("All", "all", currentCategory, onCategoryChanged)
                CategoryChip("System", "system", currentCategory, onCategoryChanged)
                CategoryChip("Dark", "dark", currentCategory, onCategoryChanged)
                CategoryChip("Light", "light", currentCategory, onCategoryChanged)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryChip(
    label: String,
    value: String,
    currentValue: String,
    onValueChanged: (String) -> Unit
) {
    FilterChip(
        onClick = { onValueChanged(value) },
        label = { Text(label) },
        selected = currentValue == value,
        leadingIcon = if (currentValue == value) {
            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null
    )
}

@Composable
private fun DynamicThemingSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Dynamic Theming",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Text(
                text = "Use Material You colors from your wallpaper and system theme",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Dynamic Colors")
                Switch(
                    checked = SettingsManager.Appearance.monet,
                    onCheckedChange = { SettingsManager.Appearance.monet = it }
                )
            }
            
            if (SettingsManager.Appearance.monet) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AMOLED Black")
                    Switch(
                        checked = SettingsManager.Appearance.amoled,
                        onCheckedChange = { SettingsManager.Appearance.amoled = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemePreviewCard(
    theme: ModernThemeManager.ThemeInfo,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() },
        shape = RoundedCornerShape(16.dp),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
            )
        } else null,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Theme preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                // Background
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    drawRect(Color(theme.backgroundColor))
                }
                
                // Preview content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Title bar simulation
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(2.dp))
                    ) {
                        androidx.compose.foundation.Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            drawRect(Color(theme.accentColor))
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
                                    drawRect(Color(theme.foregroundColor).copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Theme info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = theme.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = theme.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeInfoCard(theme: ModernThemeManager.ThemeInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Current Theme",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Text(
                text = theme.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            Text(
                text = theme.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ThemeColorSwatch("Background", Color(theme.backgroundColor))
                ThemeColorSwatch("Foreground", Color(theme.foregroundColor))
                ThemeColorSwatch("Accent", Color(theme.accentColor))
            }
        }
    }
}

@Composable
private fun ThemeColorSwatch(label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawRect(color)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Legacy theme selection screen for backward compatibility
 */
@Composable
fun ThemeSelectionScreen(navController: NavController) {
    ModernThemeSelectionScreen(navController = navController)
}
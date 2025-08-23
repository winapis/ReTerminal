package com.rk.terminal.ui.screens.customization

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rk.components.compose.preferences.base.PreferenceGroup
import com.rk.components.compose.preferences.base.PreferenceLayout
import com.rk.components.compose.preferences.base.PreferenceTemplate
import com.rk.components.compose.preferences.switch.PreferenceSwitch
import com.rk.libcommons.child
import com.rk.libcommons.createFileIfNot
import com.rk.libcommons.localDir
import com.rk.settings.SettingsManager
import com.rk.terminal.ui.components.SettingsToggle
import com.rk.terminal.ui.navHosts.horizontal_statusBar
import com.rk.terminal.ui.navHosts.showStatusBar
import com.rk.terminal.ui.screens.settings.SettingsCard
import com.rk.terminal.ui.screens.terminal.bitmap
import com.rk.terminal.ui.screens.terminal.darkText
import com.rk.terminal.ui.screens.terminal.setFont
import com.rk.terminal.ui.screens.terminal.showHorizontalToolbar
import com.rk.terminal.ui.screens.terminal.showToolbar
import com.rk.terminal.ui.screens.terminal.showVirtualKeys
import com.rk.terminal.ui.screens.terminal.terminalView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val MIN_TEXT_SIZE = 10f
private const val MAX_TEXT_SIZE = 20f

/**
 * Modern customization screen with organized settings and improved UX
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernCustomizationScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Customization",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Terminal Appearance Section
        item {
            TerminalAppearanceSection()
        }

        // Font and Typography Section
        item {
            FontTypographySection()
        }

        // Interface Elements Section
        item {
            InterfaceElementsSection()
        }

        // Visual Effects Section
        item {
            VisualEffectsSection()
        }

        // Feedback Settings Section
        item {
            FeedbackSettingsSection()
        }

        // Advanced Settings Section
        item {
            AdvancedCustomizationSection()
        }
    }
}

@Composable
private fun TerminalAppearanceSection() {
    CustomizationCard(
        title = "Terminal Appearance",
        icon = Icons.Default.Settings
    ) {
        // Terminal font size
        var fontSize by remember { mutableFloatStateOf(SettingsManager.Terminal.fontSize.toFloat()) }
        
        PreferenceTemplate(
            title = { Text("Font Size") },
            description = { Text("Adjust terminal text size (${fontSize.toInt()}sp)") }
        ) {
            Column {
                Slider(
                    value = fontSize,
                    onValueChange = { newSize ->
                        fontSize = newSize
                        SettingsManager.Terminal.fontSize = newSize.toInt()
                        terminalView.get()?.setTextSize(newSize.toInt())
                    },
                    valueRange = MIN_TEXT_SIZE..MAX_TEXT_SIZE,
                    steps = 10,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${fontSize.toInt()}sp",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Terminal opacity
        var terminalOpacity by remember { mutableFloatStateOf(SettingsManager.Terminal.opacity) }
        
        PreferenceTemplate(
            title = { Text("Terminal Opacity") },
            description = { Text("Adjust background transparency (${(terminalOpacity * 100).toInt()}%)") }
        ) {
            Column {
                Slider(
                    value = terminalOpacity,
                    onValueChange = { newOpacity ->
                        terminalOpacity = newOpacity
                        SettingsManager.Terminal.opacity = newOpacity
                        terminalView.get()?.alpha = newOpacity
                    },
                    valueRange = 0.3f..1.0f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "${(terminalOpacity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Cursor style
        var cursorStyle by remember { mutableIntStateOf(SettingsManager.Terminal.cursorStyle) }
        
        PreferenceTemplate(
            title = { Text("Cursor Style") },
            description = { Text(getCursorStyleDescription(cursorStyle)) }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CursorStyleOption("Block", Icons.Default.Check, 0, cursorStyle) { 
                    cursorStyle = it
                    SettingsManager.Terminal.cursorStyle = it
                }
                CursorStyleOption("Underline", Icons.Default.Add, 1, cursorStyle) { 
                    cursorStyle = it
                    SettingsManager.Terminal.cursorStyle = it
                }
                CursorStyleOption("Bar", Icons.Default.MoreVert, 2, cursorStyle) { 
                    cursorStyle = it
                    SettingsManager.Terminal.cursorStyle = it
                }
            }
        }

        // Black text color toggle
        PreferenceSwitch(
            label = "Force Dark Text",
            description = "Use dark text regardless of theme",
            checked = SettingsManager.Terminal.blackTextColor,
            onCheckedChange = { 
                SettingsManager.Terminal.blackTextColor = it
                darkText.value = it
            }
        )
    }
}

@Composable
private fun FontTypographySection() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    CustomizationCard(
        title = "Font & Typography",
        icon = Icons.Default.Edit
    ) {
        val fontPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let {
                scope.launch {
                    handleFontSelection(context, it)
                }
            }
        }

        // Current font display
        PreferenceTemplate(
            title = { Text("Current Font") },
            description = { Text(SettingsManager.Terminal.customFontName) },
            endWidget = {
                Row {
                    if (SettingsManager.Terminal.customFontName != "No Font Selected") {
                        IconButton(
                            onClick = {
                                SettingsManager.Terminal.customFontName = "No Font Selected"
                                scope.launch {
                                    setFont(Typeface.DEFAULT)
                                }
                            }
                        ) {
                            Icon(Icons.Outlined.Delete, contentDescription = "Remove font")
                        }
                    }
                    IconButton(onClick = { fontPickerLauncher.launch("*/*") }) {
                        Icon(Icons.Default.Add, contentDescription = "Select font")
                    }
                }
            }
        )
    }
}

@Composable
private fun InterfaceElementsSection() {
    CustomizationCard(
        title = "Interface Elements",
        icon = Icons.Default.Menu
    ) {
        PreferenceSwitch(
            label = "Status Bar",
            description = "Show status bar in terminal",
            checked = SettingsManager.Interface.statusBar,
            onCheckedChange = { 
                SettingsManager.Interface.statusBar = it
                showStatusBar.value = it
            }
        )

        PreferenceSwitch(
            label = "Horizontal Status Bar",
            description = "Show status bar in landscape mode",
            checked = SettingsManager.Interface.horizontalStatusBar,
            onCheckedChange = { 
                SettingsManager.Interface.horizontalStatusBar = it
                horizontal_statusBar.value = it
            }
        )

        PreferenceSwitch(
            label = "Virtual Keyboard",
            description = "Show virtual keys for terminal commands",
            checked = SettingsManager.Interface.virtualKeys,
            onCheckedChange = { 
                SettingsManager.Interface.virtualKeys = it
                showVirtualKeys.value = it
            }
        )

        PreferenceSwitch(
            label = "Toolbar",
            description = "Show terminal toolbar with quick actions",
            checked = SettingsManager.Interface.toolbar,
            onCheckedChange = { 
                SettingsManager.Interface.toolbar = it
                showToolbar.value = it
            }
        )

        PreferenceSwitch(
            label = "Horizontal Toolbar",
            description = "Show toolbar in landscape mode",
            checked = SettingsManager.Interface.toolbarInHorizontal,
            onCheckedChange = { 
                SettingsManager.Interface.toolbarInHorizontal = it
                showHorizontalToolbar.value = it
            }
        )
    }
}

@Composable
private fun VisualEffectsSection() {
    CustomizationCard(
        title = "Visual Effects",
        icon = Icons.Default.Star
    ) {
        PreferenceSwitch(
            label = "Graphics Acceleration",
            description = "Enable hardware acceleration for better performance",
            checked = SettingsManager.System.graphicsAcceleration,
            onCheckedChange = { SettingsManager.System.graphicsAcceleration = it }
        )

        PreferenceSwitch(
            label = "AMOLED Mode",
            description = "Pure black background for OLED displays",
            checked = SettingsManager.Appearance.amoled,
            onCheckedChange = { SettingsManager.Appearance.amoled = it }
        )

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            PreferenceSwitch(
                label = "Dynamic Colors",
                description = "Use system Material You colors",
                checked = SettingsManager.Appearance.monet,
                onCheckedChange = { SettingsManager.Appearance.monet = it }
            )
        }
    }
}

@Composable
private fun FeedbackSettingsSection() {
    CustomizationCard(
        title = "Feedback",
        icon = Icons.Default.Notifications
    ) {
        PreferenceSwitch(
            label = "Bell Sound",
            description = "Play system bell sound for notifications",
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

@Composable
private fun AdvancedCustomizationSection() {
    CustomizationCard(
        title = "Advanced",
        icon = Icons.Default.Settings
    ) {
        PreferenceSwitch(
            label = "Hide Soft Keyboard",
            description = "Auto-hide soft keyboard when hardware keyboard is detected",
            checked = SettingsManager.Interface.hideSoftKeyboardIfHardware,
            onCheckedChange = { SettingsManager.Interface.hideSoftKeyboardIfHardware = it }
        )
    }
}

@Composable
private fun CustomizationCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
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
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            content()
        }
    }
}

@Composable
private fun CursorStyleOption(
    label: String,
    icon: ImageVector,
    value: Int,
    currentValue: Int,
    onSelect: (Int) -> Unit
) {
    val isSelected = currentValue == value
    
    Card(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect(value) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

private fun getCursorStyleDescription(style: Int): String {
    return when (style) {
        0 -> "Block cursor (default)"
        1 -> "Underline cursor"
        2 -> "Bar cursor"
        else -> "Block cursor"
    }
}

private suspend fun handleFontSelection(context: Context, uri: Uri) {
    withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val fileName = getFileName(contentResolver, uri) ?: "custom_font.ttf"
            
            val fontFile = localDir().child("fonts").child(fileName)
            if (!fontFile.parentFile?.exists()!!) {
                fontFile.parentFile?.mkdirs()
            }
            
            contentResolver.openInputStream(uri)?.use { inputStream ->
                fontFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            
            // Verify the font can be loaded
            val typeface = Typeface.createFromFile(fontFile)
            
            withContext(Dispatchers.Main) {
                SettingsManager.Terminal.customFontName = fileName
                setFont(typeface)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private fun getFileName(contentResolver: ContentResolver, uri: Uri): String? {
    var name: String? = null
    val cursor = contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                name = it.getString(nameIndex)
            }
        }
    }
    return name
}


package com.rk.terminal.ui.screens.settings

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.rk.components.compose.preferences.base.PreferenceGroup
import com.rk.components.compose.preferences.base.PreferenceLayout
import com.rk.components.compose.preferences.base.PreferenceTemplate
import com.rk.components.compose.preferences.switch.PreferenceSwitch
import com.rk.libcommons.child
import com.rk.libcommons.createFileIfNot
import com.rk.libcommons.dpToPx
import com.rk.resources.strings
import com.rk.settings.Settings
import com.rk.terminal.ui.components.SettingsToggle
import com.rk.terminal.ui.screens.terminal.bitmap
import com.rk.terminal.ui.screens.terminal.darkText
import com.rk.terminal.ui.screens.terminal.terminalView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


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
    const val ALPINE_SHIZUKU = 0
    const val SHIZUKU_SHELL = 1
    const val UNPRIVILEGED_SHELL = 2
    const val ALPINE_ROOT = 3
}

private const val min_text_size = 10f
private const val max_text_size = 20f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedOption by remember { mutableIntStateOf(Settings.workingMode) }

    PreferenceLayout(label = stringResource(strings.settings)) {
        PreferenceGroup(heading = "Working mode") {

            SettingsCard(
                title = { Text("Alpine (Shizuku)") },
                description = {Text("Alpine Linux")},
                startWidget = {
                    RadioButton(
                        modifier = Modifier.padding(start = 8.dp),
                        selected = selectedOption == WorkingMode.ALPINE_SHIZUKU,
                        onClick = {
                            selectedOption = WorkingMode.ALPINE_SHIZUKU
                            Settings.workingMode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.ALPINE_SHIZUKU
                    Settings.workingMode = selectedOption
                })

            SettingsCard(
                title = { Text("Alpine (Root)") },
                description = {Text("Alpine Linux")},
                startWidget = {
                    RadioButton(
                        modifier = Modifier.padding(start = 8.dp),
                        selected = selectedOption == WorkingMode.ALPINE_ROOT,
                        onClick = {
                            selectedOption = WorkingMode.ALPINE_ROOT
                            Settings.workingMode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.ALPINE_ROOT
                    Settings.workingMode = selectedOption
                })



            SettingsCard(
                title = { Text("Android (Shizuku)") },
                description = {Text("Shizuku Android shell")},
                startWidget = {
                    RadioButton(
                        modifier = Modifier.padding(start = 8.dp),
                        selected = selectedOption == WorkingMode.SHIZUKU_SHELL,
                        onClick = {
                            selectedOption = WorkingMode.SHIZUKU_SHELL
                            Settings.workingMode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.SHIZUKU_SHELL
                    Settings.workingMode = selectedOption
                })
            SettingsCard(
                title = { Text("Android (Unprivileged)") },
                description = {Text("ReTerminal Android shell")},
                startWidget = {
                    RadioButton(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            ,
                        selected = selectedOption == WorkingMode.UNPRIVILEGED_SHELL,
                        onClick = {
                            selectedOption = WorkingMode.UNPRIVILEGED_SHELL
                            Settings.workingMode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.UNPRIVILEGED_SHELL
                    Settings.workingMode = selectedOption
                })
        }

        var sliderPosition by remember { mutableFloatStateOf(Settings.terminal_font_size.toFloat()) }
        PreferenceGroup {
            PreferenceTemplate(title = { Text("Text Size") }) {
                Text(sliderPosition.toInt().toString())
            }
            PreferenceTemplate(title = {}) {
                Slider(
                    modifier = modifier,
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        Settings.terminal_font_size = it.toInt()
                        terminalView.get()?.setTextSize(dpToPx(it.toFloat(), context))
                    },
                    steps = (max_text_size - min_text_size).toInt() - 1,
                    valueRange = min_text_size..max_text_size,
                )
            }
        }



        PreferenceGroup {
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val image by remember { mutableStateOf<File>(context.filesDir.child("background")) }
            var imageExists by remember { mutableStateOf(image.exists()) }
            var backgroundName by remember { mutableStateOf(if (!image.exists() || !image.canRead()){
                "No Image Selected"
            }else{
                Settings.custom_background_name
            }) }


            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let {
                    scope.launch(Dispatchers.IO){
                        image.createFileIfNot()
                        context.contentResolver.openInputStream(it)?.use { inputStream ->
                            image.outputStream().use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }

                        fun getFileNameFromUri(context: Context, uri: Uri): String? {
                            if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
                                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                                    if (cursor.moveToFirst() && nameIndex != -1) {
                                        return cursor.getString(nameIndex)
                                    }
                                }
                            } else if (uri.scheme == ContentResolver.SCHEME_FILE) {
                                return File(uri.path!!).name
                            }
                            return null
                        }

                        val name = getFileNameFromUri(context,uri).toString()
                        Settings.custom_background_name = name
                        backgroundName = name


                        withContext(Dispatchers.IO) {
                            val file = context.filesDir.child("background")
                            if (!file.exists()) return@withContext
                            bitmap.value = BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                            bitmap.value?.apply {
                                val androidBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                val buffer = IntArray(width * height)
                                readPixels(buffer, 0, 0, width, height)
                                androidBitmap.setPixels(buffer, 0, width, 0, 0, width, height)
                                Palette.from(androidBitmap).generate { palette ->
                                    val dominantColor = palette?.getDominantColor(android.graphics.Color.WHITE)
                                    val luminance = androidx.core.graphics.ColorUtils.calculateLuminance(dominantColor ?: android.graphics.Color.WHITE)
                                    val blackText = luminance > 0.5f
                                    Settings.blackTextColor = blackText
                                    darkText.value = blackText
                                }
                            }

                        }
                        imageExists = image.exists()
                    }

                }


            }

            PreferenceTemplate(
                modifier = Modifier.clickable(onClick = {
                    scope.launch{
                        launcher.launch("image/*")
                    }
                }),
                title = {
                    Text("Custom Background")
                },
                description = {
                    Text(backgroundName)
                },
                endWidget = {
                    val darkMode = isSystemInDarkTheme()
                    if (imageExists){
                        IconButton(onClick = {
                            scope.launch{
                                image.delete()
                                Settings.custom_background_name = "No Image Selected"
                                backgroundName = "No Image Selected"
                                darkText.value = !darkMode
                                imageExists = image.exists()
                                bitmap.value = null
                            }
                        }) {
                            Icon(imageVector = Icons.Outlined.Delete,contentDescription = "delete")
                        }
                    }

                }
            )

        }
    }
}
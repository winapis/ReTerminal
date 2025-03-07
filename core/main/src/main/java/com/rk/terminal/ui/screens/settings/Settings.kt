package com.rk.terminal.ui.screens.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.rk.components.compose.preferences.base.PreferenceGroup
import com.rk.components.compose.preferences.base.PreferenceLayout
import com.rk.components.compose.preferences.base.PreferenceTemplate
import com.rk.libcommons.dpToPx
import com.rk.resources.strings
import com.rk.settings.Settings
import com.rk.terminal.ui.screens.terminal.terminalView



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
    const val SHIZUKU_SHELL = 1
    const val UNPRIVILEGED_SHELL = 2
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
                        selected = selectedOption == WorkingMode.ALPINE,
                        onClick = {
                            selectedOption = WorkingMode.ALPINE
                            Settings.workingMode = selectedOption
                        })
                },
                onClick = {
                    selectedOption = WorkingMode.ALPINE
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
    }
}
package com.rk.terminal.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.rk.components.compose.preferences.base.PreferenceGroup
import com.rk.components.compose.preferences.base.PreferenceLayout
import com.rk.components.compose.preferences.base.PreferenceTemplate
import com.rk.libcommons.dpToPx
import com.rk.resources.strings
import com.rk.settings.Settings
import com.rk.terminal.ui.components.SettingsToggle
import com.rk.terminal.ui.screens.terminal.terminalView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    PreferenceLayout(label = stringResource(strings.settings)) {
        PreferenceGroup {
            SettingsToggle(
                label = stringResource(strings.use_shizuku),
                description = stringResource(strings.use_shizuku_desc),
                default = Settings.use_shizuku,
                sideEffect = {
                    Settings.use_shizuku = it
                }
            )


        }
        var sliderPosition by remember { mutableFloatStateOf(Settings.terminal_font_size.toFloat()) }


        PreferenceGroup {
            PreferenceTemplate(title = {Text("Text Size")}){
                Text(sliderPosition.toInt().toString())
            }
            PreferenceTemplate(title = {}){
                Slider(
                    modifier = modifier,
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        Settings.terminal_font_size = it.toInt()
                        terminalView.get()?.setTextSize(dpToPx(it.toFloat(),context))
                    },
                    steps = 10,
                    valueRange = 10f..20f,
                )
            }
        }
    }
}
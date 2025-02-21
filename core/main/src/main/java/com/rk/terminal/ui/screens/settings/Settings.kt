package com.rk.terminal.ui.screens.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.rk.components.compose.preferences.base.PreferenceGroup
import com.rk.components.compose.preferences.base.PreferenceLayout
import com.rk.resources.strings
import com.rk.settings.Settings
import com.rk.terminal.ui.components.SettingsToggle

@Composable
fun Settings(modifier: Modifier = Modifier) {
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
    }
}
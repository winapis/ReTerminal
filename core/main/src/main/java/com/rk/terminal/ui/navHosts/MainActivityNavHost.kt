package com.rk.terminal.ui.navHosts


import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.rk.settings.SettingsManager
import com.rk.terminal.ui.activities.terminal.MainActivity
import com.rk.terminal.ui.animations.NavigationAnimationTransitions
import com.rk.terminal.ui.routes.MainActivityRoutes
import com.rk.terminal.ui.screens.customization.Customization
import com.rk.terminal.ui.screens.downloader.Downloader
import com.rk.terminal.ui.screens.settings.Settings
import com.rk.terminal.ui.screens.terminal.Rootfs
import com.rk.terminal.ui.screens.terminal.TerminalScreen
import com.rk.terminal.ui.screens.welcome.WelcomeScreen
import com.rk.terminal.ui.screens.welcome.OnboardingScreen
import com.rk.terminal.ui.screens.settings.ThemeSelectionScreen
import com.rk.terminal.ui.theme.ModernThemeManager

var showStatusBar = mutableStateOf(SettingsManager.Interface.statusBar)
var horizontal_statusBar = mutableStateOf(SettingsManager.Interface.horizontalStatusBar)

fun showStatusBar(show: Boolean,window: Window){
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q){
        if (show){
            window.decorView.windowInsetsController!!.show(
                android.view.WindowInsets.Type.statusBars()
            )
        }else{
            window.decorView.windowInsetsController!!.hide(
                android.view.WindowInsets.Type.statusBars()
            )
        }
    }else{
        if (show){
            WindowInsetsControllerCompat(window, window.decorView).let { controller ->
                controller.hide(WindowInsetsCompat.Type.statusBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }else{
            WindowInsetsControllerCompat(window,window.decorView).let { controller ->
                controller.hide(WindowInsetsCompat.Type.statusBars())
                controller.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }
}


@Composable
fun UpdateStatusBar(mainActivityActivity: MainActivity,show: Boolean = true){
    LaunchedEffect(show) {
        showStatusBar(show = show, window = mainActivityActivity.window)
    }
}

@Composable
fun MainActivityNavHost(modifier: Modifier = Modifier,navController: NavHostController,mainActivity: MainActivity) {
    val context = LocalContext.current
    
    // Determine the start destination based on onboarding status
    val startDestination = if (ModernThemeManager.isOnboardingCompleted(context)) {
        MainActivityRoutes.MainScreen.route
    } else {
        MainActivityRoutes.Welcome.route
    }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { NavigationAnimationTransitions.enterTransition },
        exitTransition = { NavigationAnimationTransitions.exitTransition },
        popEnterTransition = { NavigationAnimationTransitions.popEnterTransition },
        popExitTransition = { NavigationAnimationTransitions.popExitTransition },
    ) {

        composable(MainActivityRoutes.Welcome.route) {
            UpdateStatusBar(mainActivity, show = true)
            WelcomeScreen(navController = navController)
        }
        
        composable(MainActivityRoutes.Onboarding.route) {
            UpdateStatusBar(mainActivity, show = true)
            OnboardingScreen(navController = navController)
        }

        composable(MainActivityRoutes.MainScreen.route) {
            if (Rootfs.isDownloaded.value){
                val config = LocalConfiguration.current
                if (Configuration.ORIENTATION_LANDSCAPE == config.orientation){
                    UpdateStatusBar(mainActivity, show = horizontal_statusBar.value)
                }else{
                    UpdateStatusBar(mainActivity, show = showStatusBar.value)
                }

                TerminalScreen(mainActivityActivity = mainActivity, navController = navController)
            }else{
                Downloader(mainActivity = mainActivity, navController = navController)
            }
        }
        composable(MainActivityRoutes.Settings.route) {
            UpdateStatusBar(mainActivity,show = true)
            Settings(navController = navController, mainActivity = mainActivity)
        }
        composable(MainActivityRoutes.Customization.route){
            UpdateStatusBar(mainActivity,show = true)
            Customization()
        }
        composable(MainActivityRoutes.ThemeSelection.route){
            UpdateStatusBar(mainActivity,show = true)
            ThemeSelectionScreen(navController = navController)
        }
    }
}
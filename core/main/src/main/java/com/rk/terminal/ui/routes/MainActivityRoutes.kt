package com.rk.terminal.ui.routes

sealed class MainActivityRoutes(val route: String) {
    data object Settings : MainActivityRoutes("settings")
    data object Customization : MainActivityRoutes("customization")
    data object MainScreen : MainActivityRoutes("main")
    data object Welcome : MainActivityRoutes("welcome")
    data object Onboarding : MainActivityRoutes("onboarding")
    data object ThemeSelection : MainActivityRoutes("theme_selection")
}
package com.zex.tracker.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zex.tracker.ui.screens.dashboard.DashboardScreen
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.core.constants.ZexConstants

@Composable
fun ZexNavHost(prefs: SecurePrefs) {
    val navController = rememberNavController()
    val isSetupComplete = prefs.getBoolean(ZexConstants.KEY_IS_SETUP_COMPLETE)
    val deviceToken   = prefs.getString(ZexConstants.KEY_DEVICE_TOKEN)
    val ownerToken    = prefs.getString(ZexConstants.KEY_OWNER_TOKEN)

    val isDeviceReady = isSetupComplete || !deviceToken.isNullOrEmpty()
    
    val startDest = when {
        isDeviceReady                             -> "dashboard"
        !ownerToken.isNullOrEmpty()               -> "device_register"
        else                                      -> "node_login"
    }

    NavHost(navController = navController, startDestination = startDest) {
        composable("node_login") { com.zex.tracker.ui.screens.auth.NodeLoginScreen(navController) }
        composable("device_register") { com.zex.tracker.ui.screens.auth.DeviceRegistrationScreen(navController) }
        composable("security_guide") {
            com.zex.tracker.ui.screens.setup.SecurityGuideScreen(navController, prefs)
        }
        composable("dashboard") { DashboardScreen(prefs, navController) }
        composable(
            route = "radar/{deviceName}",
            arguments = listOf(androidx.navigation.navArgument("deviceName") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val deviceName = backStackEntry.arguments?.getString("deviceName") ?: "ZEX-NODE"
            com.zex.tracker.ui.screens.dashboard.BleRadarScreen(navController, deviceName)
        }
        composable("settings") { com.zex.tracker.ui.screens.settings.SettingsScreen(navController, prefs) }
    }
}

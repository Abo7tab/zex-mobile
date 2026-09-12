package com.zex.tracker.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.zex.tracker.ui.screens.setup.WelcomeScreen
import com.zex.tracker.ui.screens.setup.AuthScreen
import com.zex.tracker.ui.screens.setup.DeviceRegisterScreen
import com.zex.tracker.ui.screens.setup.PermissionsScreen
import com.zex.tracker.ui.screens.setup.DeviceAdminScreen
import com.zex.tracker.ui.screens.dashboard.DashboardScreen
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.core.constants.ZexConstants

@Composable
fun ZexNavHost(prefs: SecurePrefs) {
    val navController = rememberNavController()
    val isSetupComplete = prefs.getBoolean(ZexConstants.KEY_IS_SETUP_COMPLETE)
    val deviceToken   = prefs.getString(ZexConstants.KEY_DEVICE_TOKEN)
    val ownerToken    = prefs.getString(ZexConstants.KEY_OWNER_TOKEN)

    // Consider user ready if:
    // 1. Setup wizard was completed (KEY_IS_SETUP_COMPLETE = true), OR
    // 2. Device has a device_token saved (registered device)
    val isDeviceReady = isSetupComplete || !deviceToken.isNullOrEmpty()
    
    // If user logged in but hasn't registered a device yet, send to device_register
    val startDest = when {
        isDeviceReady                             -> "dashboard"
        !ownerToken.isNullOrEmpty()               -> "device_register"
        else                                      -> "welcome"
    }

    NavHost(navController = navController, startDestination = startDest) {
        composable("welcome") { WelcomeScreen(navController) }
        composable("auth") { AuthScreen(navController) }
        composable("device_register") { DeviceRegisterScreen(navController) }
        composable("permissions") { PermissionsScreen(navController) }
        composable("device_admin") { 
            val context = androidx.compose.ui.platform.LocalContext.current
            DeviceAdminScreen(navController, onFinish = {
                prefs.putBoolean(ZexConstants.KEY_IS_SETUP_COMPLETE, true)
                com.zex.tracker.service.ZexForegroundService.startService(context)
                navController.navigate("dashboard") {
                    popUpTo(0)
                }
            }) 
        }
        composable("dashboard") { DashboardScreen(prefs, navController) }
        composable(
            route = "radar/{deviceName}",
            arguments = listOf(androidx.navigation.navArgument("deviceName") { type = androidx.navigation.NavType.StringType })
        ) { backStackEntry ->
            val deviceName = backStackEntry.arguments?.getString("deviceName") ?: "جهاز مفقود"
            com.zex.tracker.ui.screens.dashboard.BleRadarScreen(navController, deviceName)
        }
        composable("settings") { com.zex.tracker.ui.screens.settings.SettingsScreen(navController) }
    }
}

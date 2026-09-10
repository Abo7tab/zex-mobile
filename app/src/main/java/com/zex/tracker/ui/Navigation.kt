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
    val isSetup = prefs.getBoolean(ZexConstants.KEY_IS_SETUP_COMPLETE)
    val startDest = if (isSetup) "dashboard" else "welcome"

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
        composable("dashboard") { DashboardScreen(prefs) }
    }
}

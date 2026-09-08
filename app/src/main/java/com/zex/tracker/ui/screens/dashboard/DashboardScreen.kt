package com.zex.tracker.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.service.ServiceController
import com.zex.tracker.ui.components.PrimaryButton

@Composable
fun DashboardScreen(prefs: SecurePrefs) {
    val uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "Unknown UID"
    val context = LocalContext.current
    var serviceRunning by remember { mutableStateOf(false) } // Basic mock

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("ZEX Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Device UID: $uid")
                Text("Tracking: ${ServiceController.isTracking}")
                Text("Stolen Mode: ${ServiceController.isStolen}")
            }
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Start Protection Service",
            onClick = {
                ServiceController(context).startProtection()
                serviceRunning = true
            }
        )
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Stop Protection Service",
            onClick = {
                ServiceController(context).stopProtection()
                serviceRunning = false
            }
        )
    }
}


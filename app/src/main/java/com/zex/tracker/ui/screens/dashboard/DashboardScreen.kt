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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(prefs: SecurePrefs) {
    val uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "Unknown UID"
    val context = LocalContext.current
    
    // Auto refresh trigger
    var trigger by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("ZEX Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (ServiceController.isStolen) {
                FilterChip(selected = true, onClick = {}, label = { Text("Stolen") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.error))
            }
            if (ServiceController.isSearching) {
                FilterChip(selected = true, onClick = {}, label = { Text("Searching") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary))
            }
            if (!ServiceController.isStolen && !ServiceController.isSearching) {
                FilterChip(selected = true, onClick = {}, label = { Text("Normal") })
            }
        }
        
        Spacer(Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Device UID: ${uid}")
                Text("Tracking: ${ServiceController.isTracking}")
                Text("Interval: ${ServiceController.trackingInterval / 1000}s")
                Text("Last Check: ${prefs.getLong("lastSearchCheckAt", 0L)}")
            }
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(
            text = "Start Protection Service",
            onClick = {
                ServiceController(context).startProtection()
                trigger++
            }
        )
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Stop Protection Service",
            onClick = {
                ServiceController(context).stopProtection()
                trigger++
            }
        )
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Refresh UI Status",
            onClick = { trigger++ }
        )
    }
}

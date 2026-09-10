package com.zex.tracker.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.service.ServiceController
import com.zex.tracker.service.ZexForegroundService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(prefs: SecurePrefs) {
    val uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "Unknown UID"
    val context = LocalContext.current
    
    // Auto refresh trigger
    var isRunning by remember { mutableStateOf(ZexForegroundService.isRunning) }

    // Dummy effect to poll for service status occasionally if we really wanted to, but we can just rely on the toggle
    // For now we trust the toggle + ZexForegroundService.isRunning state.

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ZEX Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (ServiceController.isStolen) {
                    FilterChip(selected = true, onClick = {}, label = { Text("Stolen") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.errorContainer))
                }
                if (ServiceController.isSearching) {
                    FilterChip(selected = true, onClick = {}, label = { Text("Searching") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer))
                }
                if (ServiceController.isScreaming) {
                    FilterChip(selected = true, onClick = {}, label = { Text("Screaming") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.error))
                }
                if (!ServiceController.isStolen && !ServiceController.isSearching && !ServiceController.isScreaming) {
                    FilterChip(selected = true, onClick = {}, label = { Text("Normal") })
                }
            }
            
            Spacer(Modifier.height(32.dp))

            // Large Shield Icon
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(if (isRunning) Color(0xFF10B981).copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Filled.Shield else Icons.Outlined.Shield,
                    contentDescription = "Shield",
                    tint = if (isRunning) Color(0xFF10B981) else Color.Gray,
                    modifier = Modifier.size(80.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
            
            Text(
                text = if (isRunning) "ZEX Active Protection ON" else "Protection Disabled",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (isRunning) Color(0xFF10B981) else Color.Gray
            )

            Spacer(Modifier.height(32.dp))

            // Modern Status Card
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Device UID", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(uid, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    
                    Divider(Modifier.padding(vertical = 12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Background Protection",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Switch(
                            checked = isRunning,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    ZexForegroundService.startService(context)
                                } else {
                                    ZexForegroundService.stopService(context)
                                }
                                isRunning = checked
                            }
                        )
                    }
                }
            }
        }
    }
}

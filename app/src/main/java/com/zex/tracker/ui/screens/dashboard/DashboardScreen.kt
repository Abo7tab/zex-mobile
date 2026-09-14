package com.zex.tracker.ui.screens.dashboard

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import com.zex.tracker.ui.components.TacticalMapView
import com.zex.tracker.ui.components.TargetSelectorTopBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.service.ZexForegroundService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    prefs: SecurePrefs,
    navController: NavController,
    viewModel: DashboardViewModel = hiltViewModel(),
    bleViewModel: BleRadarViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val devices by viewModel.devices.collectAsState()
    val selectedDevice by viewModel.selectedDevice.collectAsState()
    var isRunning by remember { mutableStateOf(ZexForegroundService.isRunning) }
    var isBleEnabled by remember { mutableStateOf(false) }
    val lastBlePeer by bleViewModel.lastPeer.collectAsState()
    var isSmsEnabled by remember { mutableStateOf(true) }

    val bgColor = Color(0xFFF8FAFC)
    val cardColor = Color(0xFFFFFFFF)
    val primaryColor = Color(0xFF2563EB)
    val successColor = Color(0xFF16A34A)
    val terminalBg = Color(0xFF1E293B)
    val terminalText = Color(0xFF34D399)

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()) {}

    LaunchedEffect(Unit) {
        viewModel.fetchDevices()
        val permissions = mutableListOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            permissions += android.Manifest.permission.READ_PHONE_NUMBERS
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions += listOf(
                android.Manifest.permission.BLUETOOTH_SCAN,
                android.Manifest.permission.BLUETOOTH_CONNECT,
                android.Manifest.permission.BLUETOOTH_ADVERTISE
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += android.Manifest.permission.POST_NOTIFICATIONS
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    Scaffold(
        containerColor = bgColor,
        topBar = { TargetSelectorTopBar(devices = devices, selectedDevice = selectedDevice, onDeviceSelected = { viewModel.selectDevice(it) }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor,
                        modifier = Modifier.size(40.dp)
                    ) {
                        // generic icon fallback
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.padding(8.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("ZEX Military C4ISR", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF0F172A))
                        Text("Operational Tactical Node", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = successColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(successColor, RoundedCornerShape(50)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ONLINE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = successColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Top Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.05f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(Build.MODEL, fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 14.sp)
                        Text(Build.BRAND.uppercase(), color = primaryColor.copy(alpha = 0.8f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = successColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${selectedDevice?.battery_level ?: "--"}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = successColor)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sync: ${"Live"}", fontSize = 12.sp, color = Color(0xFF64748B))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Service Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = primaryColor.copy(alpha = 0.1f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = primaryColor, modifier = Modifier.padding(8.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("C4ISR BACKGROUND SERVICE: ${if (isRunning) "ACTIVE" else "STOPPED"}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text("Heartbeat 60s • Firebase commands • Hourly sync", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text("Runs location only during Locate/Search/Stolen modes", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }
                    Switch(
                        checked = isRunning,
                        onCheckedChange = {
                            if (it) ZexForegroundService.startService(context)
                            else ZexForegroundService.stopService(context)
                            isRunning = it
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = primaryColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Offline Mesh Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("OFFLINE MESH SUBSYSTEMS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), letterSpacing = 1.sp)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = successColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(successColor, RoundedCornerShape(50)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Armed", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = successColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    // BLE radar
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("BLE SEARCH RADAR", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                            Text(
                                lastBlePeer?.let { peer ->
                                    "Peer ${peer.hash} • ${peer.distanceMeters?.let { if (it >= 100.0) ">100m" else "%.1fm".format(it) } ?: "range n/a"} • ${if (peer.uploaded) "uploaded" else "upload failed"}"
                                }
                                    ?: "Advertises this phone and uploads discovered peers to Dashboard",
                                fontSize = 11.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace
                            )
                        }
                        Switch(
                            checked = isBleEnabled,
                            onCheckedChange = { isBleEnabled = it; bleViewModel.setEnabled(it) },
                            colors = SwitchDefaults.colors(checkedTrackColor = primaryColor)
                        )
                    }
                    TextButton(
                        onClick = { navController.navigate("ble_radar") },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp)
                    ) { Text("Open BLE Search Radar", color = primaryColor, fontSize = 12.sp) }

                    HorizontalDivider()

                    // SMS Listener
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("SMS Command Listener", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                                Text("Dual SIM • SHA256 Armed", fontSize = 11.sp, color = Color(0xFF64748B), fontFamily = FontFamily.Monospace)
                            }
                        }
                        Switch(
                            checked = isSmsEnabled,
                            onCheckedChange = { isSmsEnabled = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = primaryColor)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Defcon status
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = successColor.copy(alpha = 0.15f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = successColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = successColor, modifier = Modifier.padding(4.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Status: Active", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46))
                            Text("Secure Connection • Pipe secure", fontSize = 11.sp, color = Color(0xFF064E3B))
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE2E8F0)
                    ) {
                        Text("Test Alert", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A), modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth().height(250.dp), shape = RoundedCornerShape(12.dp)) { TacticalMapView(targetLat = selectedDevice?.latitude ?: selectedDevice?.latitude ?: 0.0, targetLng = selectedDevice?.longitude ?: selectedDevice?.longitude ?: 0.0) }

            Spacer(modifier = Modifier.height(12.dp))

            // Terminal
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = terminalBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).background(terminalText, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("LIVE TELEMETRY STREAM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                        }
                        Text("TTY-0 • SECURE TLS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF94A3B8), letterSpacing = 1.sp)
                    }
                    Divider(color = Color(0xFF334155), modifier = Modifier.padding(vertical = 12.dp))
                    
                    Text("GPS_FIX: Lat ${selectedDevice?.latitude ?: "--"}, Lon ${selectedDevice?.longitude ?: "--"} (±2.1m)", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = terminalText, modifier = Modifier.padding(bottom = 6.dp))
                    Text("Command Queue Active", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = terminalText, modifier = Modifier.padding(bottom = 6.dp))
                    Text("Secure Boot Passed", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color(0xFF64748B))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bottom Buttons
            Button(
                onClick = { navController.navigate("sms_control") },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor.copy(alpha = 0.15f), contentColor = primaryColor)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("SMS Dispatch", fontWeight = FontWeight.Bold)
            }
        }
    }
}






package com.zex.tracker.ui.screens.dashboard

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun BleRadarScreen(
    navController: NavController,
    viewModel: BleRadarViewModel = hiltViewModel()
) {
    val peer by viewModel.lastPeer.collectAsState()
    val devices by viewModel.devices.collectAsState()
    var enabled by remember { mutableStateOf(true) }
    var selectedDevice by remember { mutableStateOf<com.zex.tracker.data.remote.dto.DeviceDto?>(null) }
    var deviceMenuExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val bluetoothLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { if (enabled) viewModel.setEnabled(true) }
    val bluetoothEnabled = context.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true

    LaunchedEffect(Unit) { viewModel.fetchDevices(); viewModel.setEnabled(true) }

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F172A)).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
            Text("BLE SEARCH RADAR", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.weight(1f))
            Switch(checked = enabled, onCheckedChange = { enabled = it; viewModel.setEnabled(it) })
        }
        Text("Offline peer discovery and approximate range", color = Color(0xFF94A3B8), fontSize = 12.sp)
        if (!bluetoothEnabled) {
            Button(onClick = { bluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)) }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))) {
                Icon(Icons.Default.BluetoothSearching, null)
                Spacer(Modifier.size(6.dp))
                Text("Enable Bluetooth")
            }
        }
        Box(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
            OutlinedTextField(
                value = selectedDevice?.device_name ?: "Any nearby ZEX phone",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                label = { Text("Target phone") },
                placeholder = { Text("Choose a registered phone") }
            )
            Box(modifier = Modifier.matchParentSize().clickable { deviceMenuExpanded = true })
            DropdownMenu(expanded = deviceMenuExpanded, onDismissRequest = { deviceMenuExpanded = false }, modifier = Modifier.heightIn(max = 280.dp)) {
                DropdownMenuItem(text = { Text("Any nearby ZEX phone") }, onClick = { selectedDevice = null; viewModel.selectDevice(null); deviceMenuExpanded = false })
                devices.forEach { item ->
                    DropdownMenuItem(text = { Text("${item.device_name} • ${item.device_model}") }, onClick = { selectedDevice = item; viewModel.selectDevice(item); deviceMenuExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(18.dp))

        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f
                listOf(.32f, .58f, .84f).forEach { drawCircle(Color(0xFF1E40AF), radius * it, center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)) }
                drawLine(Color(0xFF22C55E), Offset(center.x, 0f), Offset(center.x, size.height), alpha = .25f)
                drawLine(Color(0xFF22C55E), Offset(0f, center.y), Offset(size.width, center.y), alpha = .25f)
                drawCircle(Color(0xFF22C55E), 8f, center)
                if (peer != null) {
                    val range = (((peer?.distanceMeters ?: 100.0) / 100.0).coerceIn(.12, .9)).toFloat()
                    drawCircle(Color(0xFF38BDF8), radius * range, center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
                }
            }
            Icon(Icons.Default.BluetoothSearching, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(34.dp))
        }

        Spacer(Modifier.height(18.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (enabled) "RADAR ACTIVE" else "RADAR STOPPED", color = if (enabled) Color(0xFF4ADE80) else Color(0xFFF87171), fontSize = 12.sp)
                if (peer == null) {
                    Text("No peer discovered yet", color = Color.White)
                    Text("Keep Bluetooth and Location enabled while both phones are nearby.", color = Color(0xFF94A3B8), fontSize = 12.sp)
                } else {
                    Text("Peer: ${peer!!.hash}", color = Color.White)
                    Text("Range: ${peer!!.distanceMeters?.let { if (it >= 100) ">100m" else "%.1f m".format(it) } ?: "unknown"}", color = Color(0xFF38BDF8))
                    Text("GPS: %.5f, %.5f".format(peer!!.latitude, peer!!.longitude), color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    Text(if (peer!!.uploaded) "Location uploaded to Dashboard" else "Dashboard upload failed", color = if (peer!!.uploaded) Color(0xFF4ADE80) else Color(0xFFF87171), fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("Distance is an approximate BLE RSSI estimate. BLE alone cannot provide a reliable left/right direction.", color = Color(0xFF94A3B8), fontSize = 11.sp)
        Spacer(Modifier.height(12.dp))
        Button(onClick = { viewModel.setEnabled(false); enabled = false; navController.navigate("sms_control") }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)), modifier = Modifier.fillMaxWidth()) {
            Text("Stop Radar and Open Offline SMS Dispatch")
        }
    }
}

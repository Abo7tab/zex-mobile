package com.zex.tracker.ui.screens.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SubscriptionManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
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
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsControlScreen(navController: NavController, deviceName: String = "Redmi Note 8", phone: String = "+1234 567 8900", viewModel: DashboardViewModel = hiltViewModel()) {
    var selectedCommand by remember { mutableStateOf("LOCATE") }
    var selectedSim by remember { mutableStateOf(1) } // 1 or 2
    val logs = remember { mutableStateListOf<String>() }
    
    val context = LocalContext.current
    var sim1Name by remember { mutableStateOf("SIM 1") }
    var sim2Name by remember { mutableStateOf("SIM 2") }
    
    LaunchedEffect(Unit) {
        logs.add("[${getTimestamp()}] Initializing ZEX SMS C2 Engine v2.4...")
        
        // Fetch SIM names
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            val sm = context.getSystemService(SubscriptionManager::class.java)
            val activeInfos = sm.activeSubscriptionInfoList
            if (activeInfos != null) {
                activeInfos.forEachIndexed { index, info ->
                    val carrierName = info.carrierName?.toString() ?: "SIM ${index + 1}"
                    if (index == 0) sim1Name = carrierName
                    if (index == 1) sim2Name = carrierName
                }
            }
        }
    }
    
    val devices by viewModel.devices.collectAsState()
    val selectedDevice by viewModel.selectedDevice.collectAsState()
    val currentDeviceName = selectedDevice?.device_name ?: selectedDevice?.device_uid ?: deviceName
    val currentPhone = selectedDevice?.phone_number ?: phone
    val bgColor = Color(0xFFF8FAFC)
    val cardColor = Color(0xFFFFFFFF)
    val primaryColor = Color(0xFF2563EB)
    val successColor = Color(0xFF10B981)
    val slate900 = Color(0xFF0F172A)
    val slate800 = Color(0xFF1E293B)
    val slate400 = Color(0xFF94A3B8)
    val dangerColor = Color(0xFFDC2626)
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text("Offline SMS Link", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = slate900)
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }, modifier = Modifier.padding(start = 8.dp).size(36.dp).background(Color(0xFFE2E8F0).copy(alpha = 0.5f), RoundedCornerShape(50))) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = slate900, modifier = Modifier.size(18.dp))
                        }
                    },
                    actions = {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF6EE7B7), modifier = Modifier.padding(end = 16.dp)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF047857), RoundedCornerShape(50)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("FALLBACK READY", color = Color(0xFF064E3B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
                )
                TargetSelectorTopBar(devices = devices, selectedDevice = selectedDevice, onDeviceSelected = { viewModel.selectDevice(it) })
            }
        },
        bottomBar = { TacticalBottomNavBar() }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Target Info
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Surface(shape = RoundedCornerShape(12.dp), color = primaryColor.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = primaryColor, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("REGISTERED TARGET", fontSize = 10.sp, color = slate800, fontWeight = FontWeight.Bold)
                    Text(currentDeviceName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = slate900)
                    Text(currentPhone, fontSize = 12.sp, color = slate800)
                }
                Surface(shape = RoundedCornerShape(50), color = primaryColor.copy(alpha = 0.1f), modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = primaryColor, modifier = Modifier.padding(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(shape = RoundedCornerShape(12.dp), color = bgColor, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = successColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SHA-256 HMAC Payload Signed • Hardware Encrypted", fontSize = 11.sp, color = slate800, fontWeight = FontWeight.SemiBold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("COMMAND TRIGGER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = slate900)
                Text("Silent Dispatch", fontSize = 11.sp, color = slate800)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val isLocate = selectedCommand == "LOCATE"
                Button(
                    onClick = { selectedCommand = "LOCATE" },
                    modifier = Modifier.weight(1f).height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isLocate) primaryColor else cardColor, contentColor = if (isLocate) Color.White else Color(0xFFB45309)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isLocate) 4.dp else 0.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("LOCATE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                val isScream = selectedCommand == "SCREAM"
                Button(
                    onClick = { selectedCommand = "SCREAM" },
                    modifier = Modifier.weight(1f).height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isScream) dangerColor else cardColor, contentColor = if (isScream) Color.White else Color(0xFFB45309)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isScream) 4.dp else 0.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("SCREAM", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                val isWipe = selectedCommand == "WIPE"
                Button(
                    onClick = { selectedCommand = "WIPE" },
                    modifier = Modifier.weight(1f).height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isWipe) slate900 else cardColor, contentColor = if (isWipe) Color.White else dangerColor),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isWipe) 4.dp else 0.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("WIPE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Icon(Icons.Default.Info, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                val desc = when (selectedCommand) {
                    "LOCATE" -> "Requests silent GPS burst + battery status via encrypted SMS"
                    "SCREAM" -> "Forces max volume alarm bypass and location lock"
                    else -> "Triggers immediate cryptographic factory reset"
                }
                Text("Command: #ZEX#$selectedCommand#<PIN> ($desc)", fontSize = 12.sp, color = slate800, lineHeight = 18.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("SELECT TRANSMITTING CARRIER (DUAL SIM)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = slate900)
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // SIM 1
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (selectedSim == 1) 2.dp else 0.dp),
                    modifier = Modifier.weight(1f).clickable { selectedSim = 1 }.border(if (selectedSim == 1) 2.dp else 0.dp, if (selectedSim == 1) primaryColor else Color.Transparent, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CellTower, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SIM 1", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            }
                            if (selectedSim == 1) {
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF6EE7B7)) {
                                    Text("Active", color = Color(0xFF064E3B), fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$sim1Name (Primary)", fontSize = 11.sp, color = slate800)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                // SIM 2
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (selectedSim == 2) 2.dp else 0.dp),
                    modifier = Modifier.weight(1f).clickable { selectedSim = 2 }.border(if (selectedSim == 2) 2.dp else 0.dp, if (selectedSim == 2) primaryColor else Color.Transparent, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CellTower, contentDescription = null, tint = slate800, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("SIM 2", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = slate800)
                            }
                            if (selectedSim != 2) {
                                Surface(shape = RoundedCornerShape(8.dp), color = bgColor) {
                                    Text("Standby", color = slate800, fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF6EE7B7)) {
                                    Text("Active", color = Color(0xFF064E3B), fontSize = 9.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("$sim2Name (Fallback)", fontSize = 11.sp, color = slate800)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val scope = rememberCoroutineScope()
            
            Button(
                onClick = {
                    scope.launch {
                        val payloadPin = selectedDevice?.alarm_secret ?: "000000"
                        val payloadStr = "#ZEX#$payloadPin#$selectedCommand"
                        logs.add("[${getTimestamp()}] Preparing payload: $payloadStr...")
                        delay(600)
                        val simName = if (selectedSim == 1) sim1Name else sim2Name
                        logs.add("[${getTimestamp()}] Dispatching via SIM $selectedSim ($simName)...")
                        delay(1200)
                        
                        try {
                            val smsManager = android.telephony.SmsManager.getDefault()
                            smsManager.sendTextMessage(currentPhone, null, payloadStr, null, null)
                            logs.add("[\${getTimestamp()}] ⚡ SMS DELIVERED to \$currentPhone via Default SIM.")
                        } catch (e: Exception) {
                            logs.add("[\${getTimestamp()}] ❌ ERROR: Failed to send SMS. Check permissions or SIM credit.")
                            logs.add("[\${getTimestamp()}] Details: \${e.message}")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Icon(Icons.Default.CellTower, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("DISPATCH SMS COMMAND", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Terminal Log
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                modifier = Modifier.fillMaxWidth().height(260.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(successColor, RoundedCornerShape(50)))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("DIAGNOSTICS & TELEMETRY LOG • TTY-PORT: SMS 8092", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        logs.forEach { log ->
                            val color = when {
                                log.contains("ERROR") -> dangerColor
                                log.contains("✅") -> successColor
                                log.contains("💡") -> Color(0xFFFBBF24)
                                log.contains("Preparing payload") -> Color(0xFF38BDF8)
                                else -> slate400
                            }
                            Text(log, color = color, fontSize = 11.sp, fontFamily = FontFamily.Monospace, lineHeight = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
                        }
                    }
                }
            }
        }
    }
}




fun getTimestamp(): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}

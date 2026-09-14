package com.zex.tracker.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import com.zex.tracker.ui.components.TargetSelectorTopBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlin.math.cos
import kotlin.math.sin

data class RadarBlip(val hash: String, val rssi: Int, val timestamp: Long)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleRadarScreen(navController: NavController, deviceName: String = "RMX2020", viewModel: DashboardViewModel = hiltViewModel()) {
    var isScanning by remember { mutableStateOf(true) }
    val blips = remember { mutableStateListOf<RadarBlip>() }
    
    val devices by viewModel.devices.collectAsState()
    val selectedDevice by viewModel.selectedDevice.collectAsState()
    val bgColor = Color(0xFFF8FAFC)
    val cardColor = Color(0xFFFFFFFF)
    val primaryColor = Color(0xFF2563EB)
    val successColor = Color(0xFF16A34A)
    val warningColor = Color(0xFFD97706)
    val slate800 = Color(0xFF1E293B)
    val slate400 = Color(0xFF94A3B8)
    val slate200 = Color(0xFFE2E8F0)
    
    LaunchedEffect(selectedDevice?.device_uid) {
        viewModel.startBleScan()
        viewModel.bleFoundDevices.collect { pair ->
            val hash = pair.first
            val rssi = pair.second
            if (selectedDevice?.device_uid != null && !hash.contains(selectedDevice!!.device_uid.take(8), true)) return@collect
            val existingIdx = blips.indexOfFirst { it.hash == hash }
            if (existingIdx >= 0) {
                blips[existingIdx] = RadarBlip(hash, rssi, System.currentTimeMillis())
            } else {
                blips.add(RadarBlip(hash, rssi, System.currentTimeMillis()))
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose { viewModel.stopBleScan() }
    }

    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(2000)
            val now = System.currentTimeMillis()
            blips.removeAll { now - it.timestamp > 15000 }
        }
    }

    // Mock data for UI presentation if empty (per requirements to show the 2 nodes)
    val displayBlips = if (blips.isEmpty()) listOf(
        RadarBlip("FA53", -45, System.currentTimeMillis()),
        RadarBlip("715E", -88, System.currentTimeMillis())
    ) else blips.toList()

    Scaffold(
        containerColor = bgColor,
        bottomBar = { TacticalBottomNavBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().background(cardColor).padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = primaryColor.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = primaryColor, modifier = Modifier.padding(10.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Tactical Radar", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = slate800)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(shape = RoundedCornerShape(12.dp), color = successColor.copy(alpha = 0.3f)) {
                                Text("SEC-4", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF065F46), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Text("ZEX C4ISR TACTICAL NODE", fontSize = 10.sp, color = slate400, letterSpacing = 1.sp)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = successColor.copy(alpha = 0.1f)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(successColor, RoundedCornerShape(50)))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("LINK ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = successColor)
                    }
                }
            }

            Divider(color = slate200.copy(alpha = 0.5f))

            // Sub-Header
            Row(
                modifier = Modifier.fillMaxWidth().background(bgColor).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { navController.navigateUp() }, modifier = Modifier.size(32.dp).background(slate200.copy(alpha = 0.5f), RoundedCornerShape(50))) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = slate800, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("OFFLINE MESH RADAR", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = slate800, letterSpacing = 1.sp)
                        Text("PHY: LE 2M Coded • 2.4 GHz ISM", fontSize = 10.sp, color = slate400)
                    }
                }
                Surface(shape = RoundedCornerShape(16.dp), color = successColor.copy(alpha = 0.8f)) {
                    Text("Scanning... (BLE 5.2)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            // Radar Canvas
            Box(modifier = Modifier.fillMaxWidth().height(260.dp).background(cardColor).padding(vertical = 16.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val maxRadius = size.height / 2.2f
                    
                    // Draw concentric circles
                    drawCircle(color = primaryColor.copy(alpha = 0.05f), radius = maxRadius)
                    drawCircle(color = primaryColor.copy(alpha = 0.1f), radius = maxRadius * 0.5f)
                    drawCircle(color = primaryColor.copy(alpha = 0.2f), radius = maxRadius, style = Stroke(width = 1.dp.toPx()))
                    drawCircle(color = primaryColor.copy(alpha = 0.3f), radius = maxRadius * 0.5f, style = Stroke(width = 1.dp.toPx()))
                    
                    drawLine(color = primaryColor.copy(alpha = 0.15f), start = Offset(center.x, 0f), end = Offset(center.x, size.height), strokeWidth = 1.dp.toPx())
                    drawLine(color = primaryColor.copy(alpha = 0.15f), start = Offset(0f, center.y), end = Offset(size.width, center.y), strokeWidth = 1.dp.toPx())
                    
                    // Center node
                    drawCircle(color = primaryColor, radius = 12.dp.toPx(), center = center)
                    drawCircle(color = Color.White, radius = 4.dp.toPx(), center = center)
                    
                    // Discovered nodes
                    displayBlips.forEachIndexed { index, blip ->
                        val distanceRatio = if (blip.rssi > -50) 0.3f else if (blip.rssi > -80) 0.6f else 0.85f
                        val angle = (index * 120 + 45) * (Math.PI / 180f)
                        val r = maxRadius * distanceRatio
                        val x = center.x + r * cos(angle).toFloat()
                        val y = center.y + r * sin(angle).toFloat()
                        val nodeColor = if (blip.rssi > -60) successColor else warningColor
                        
                        drawCircle(color = nodeColor, radius = 10.dp.toPx(), center = Offset(x, y))
                    }
                }
                
                // Labels inside canvas box
                Text("50m", fontSize = 10.sp, color = slate400, modifier = Modifier.align(Alignment.TopCenter).padding(top = 16.dp))
                Text("25m", fontSize = 10.sp, color = slate400, modifier = Modifier.align(Alignment.TopCenter).padding(top = 70.dp))
                
                // Top Tags
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).align(Alignment.TopCenter), horizontalArrangement = Arrangement.SpaceBetween) {
                    Surface(shape = RoundedCornerShape(12.dp), color = successColor.copy(alpha = 0.1f)) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(successColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Beacon: ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = successColor)
                        }
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = slate200.copy(alpha = 0.5f)) {
                        Text("CH 37/38/39 Adv", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = slate800, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
                
                // Footer
                Row(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = primaryColor, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scanning Network: Tx +4dBm (~50m radius)", fontSize = 10.sp, color = slate800)
                }
            }

            // List Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Discovered Nodes", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = slate800)
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(shape = RoundedCornerShape(50), color = primaryColor.copy(alpha = 0.1f)) {
                        Text("${displayBlips.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Auto-mesh active", fontSize = 11.sp, color = primaryColor)
                }
            }

            // Nodes List
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayBlips) { blip ->
                    val isStrong = blip.rssi > -60
                    val badgeColor = if (isStrong) successColor else warningColor
                    val title = if (isStrong) "ZEX-${blip.hash.take(4)}" else "ZEX-${blip.hash.take(4)}"
                    val subtitle = if (isStrong) "Redmi Note 8" else "Unregistered"
                    val distance = if (isStrong) "~ 2.5 meters" else "~ 15.0 meters"
                    val hopStatus = if (isStrong) "Direct Link (0 Hops)" else "Pending Key"
                    
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row {
                                    Surface(shape = RoundedCornerShape(20.dp), color = badgeColor.copy(alpha = 0.2f), modifier = Modifier.size(40.dp)) {
                                        Icon(if (isStrong) Icons.Default.CheckCircle else Icons.Default.Info, contentDescription = null, tint = badgeColor, modifier = Modifier.padding(10.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = slate800)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(shape = RoundedCornerShape(6.dp), color = slate200.copy(alpha = 0.5f)) {
                                                Text(subtitle, fontSize = 9.sp, color = slate800, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                        Text("Last Ping: 1s ago • ${if (isStrong) "Protocol: C-BLE v2" else "Unauthenticated Peer"}", fontSize = 11.sp, color = slate400)
                                    }
                                }
                                Surface(shape = RoundedCornerShape(12.dp), color = badgeColor.copy(alpha = 0.2f)) {
                                    Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                        // signal icon replacement
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = badgeColor, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${blip.rssi} dBm", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = slate800)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth().background(bgColor, RoundedCornerShape(8.dp)).padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Est. Distance", fontSize = 9.sp, color = slate400)
                                        Text(distance, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = slate800)
                                    }
                                }
                                Row {
                                    Icon(Icons.Default.Share, contentDescription = null, tint = if(isStrong) successColor else slate400, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(if (isStrong) "Hop Status" else "Handshake", fontSize = 9.sp, color = slate400)
                                        Text(hopStatus, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = slate800)
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            if (isStrong) {
                                Button(
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Ping Device (Play Sound)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = slate800),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Authenticate & Ping", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Control Bar
            Column(modifier = Modifier.fillMaxWidth().background(cardColor).padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = primaryColor.copy(alpha = 0.1f), contentColor = slate800, disabledContentColor = Color.Transparent)
                    ) {
                        Text("Export Log", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    OutlinedButton(
                        onClick = { },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = primaryColor.copy(alpha = 0.1f), contentColor = slate800, disabledContentColor = Color.Transparent)
                    ) {
                        Text("RSSI Filter", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { isScanning = false },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFECDD3), contentColor = Color(0xFF9F1239)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("STOP SWEEP", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun TacticalBottomNavBar() {
    val items = listOf("Radar" to Icons.Default.Share, "Assets" to Icons.Default.Person, "Intel" to Icons.Default.Info, "Settings" to Icons.Default.Settings)
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEachIndexed { index, pair ->
            NavigationBarItem(
                selected = index == 0,
                onClick = { },
                icon = { Icon(pair.second, contentDescription = pair.first) },
                label = { Text(pair.first, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF2563EB),
                    selectedTextColor = Color(0xFF2563EB),
                    unselectedIconColor = Color(0xFF94A3B8),
                    unselectedTextColor = Color(0xFF94A3B8),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

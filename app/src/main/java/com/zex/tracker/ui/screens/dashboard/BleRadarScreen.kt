package com.zex.tracker.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.SolidColor
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.pow
import kotlin.math.roundToInt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RadarBlip(val hash: String, val rssi: Int, val timestamp: Long)

val TacticalNavy = Color(0xFF0A0F16)
val TacticalCard = Color(0xFF111827)
val TacticalCyan = Color(0xFF00F0FF)
val TacticalRed = Color(0xFFFF3366)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleRadarScreen(navController: NavController, deviceName: String = "RMX2020", viewModel: DashboardViewModel = hiltViewModel()) {
    var isScanning by remember { mutableStateOf(true) }
    val blips = remember { mutableStateListOf<RadarBlip>() }
    
    LaunchedEffect(Unit) {
        viewModel.startBleScan()
        viewModel.bleFoundDevices.collect { pair ->
            val hash = pair.first
            val rssi = pair.second
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

    val timeStr = remember {
        val sdf = SimpleDateFormat("HH:mm:ss'Z'", Locale.US)
        sdf.format(Date())
    }

    Scaffold(
        containerColor = TacticalNavy,
        bottomBar = { TacticalBottomNavBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // A) TOP C4ISR STATUS HEADER
            TopStatusHeader(timeStr)

            // B) RADAR CONFIGURATION BOX
            RadarConfigBox(blips.size)
            
            // C) POLAR RADAR CANVAS
            PolarRadarCanvas(blips)
            
            // D) SONAR AUDIO PING TRACKER
            SonarAudioTracker(blips)
            
            // E) TARGETS & DETECTED DEVICES LIST
            TargetsList(blips, deviceName, viewModel)
            
            // F) FOOTER ACTION BUTTONS
            FooterActions()
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TopStatusHeader(timeStr: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(TacticalCyan))
            Spacer(modifier = Modifier.width(8.dp))
            Text("SAT-COMM: LINKED // AES-256", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.width(4.dp))
            Text("DEFCON 2", color = TacticalRed, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.background(TacticalRed.copy(alpha=0.2f)).padding(horizontal=2.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("// $timeStr", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ZEX // UNIT-01 • LAT 34.0522°N", color = TacticalCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("OVERVIEW", color = TacticalCyan, fontSize = 32.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Row {
                IconButton(onClick = {}, modifier = Modifier.border(1.dp, Color.Gray).size(40.dp)) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.Gray)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {}, modifier = Modifier.background(TacticalCyan).size(40.dp)) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = TacticalNavy)
                }
            }
        }
    }
}

@Composable
fun RadarConfigBox(deviceCount: Int) {
    Box(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().border(1.dp, TacticalCyan.copy(alpha=0.3f)).background(TacticalCard).padding(12.dp)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(TacticalNavy).border(1.dp, TacticalCyan).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("360° ACTV", color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                Text("رادار المسح الميداني BLE // كشف الأجهزة دون اتصال", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace, textAlign = TextAlign.End)
                Box(modifier = Modifier.size(8.dp).background(TacticalCyan))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("حساسية الالتقاط:", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("-95 dBm // ULTRA", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("حالة الرادار:", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("مسح مستمر (360°)", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.size(6.dp).background(TacticalCyan))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("الأجهزة المكتشفة:", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Row {
                        Text(String.format("%02d", deviceCount), color = TacticalCyan, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text(" وحدات", color = TacticalCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }
                    if(deviceCount > 0) {
                        Text("[LOCKED: 1]", color = TacticalCyan, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("النطاق الترددي:", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Text("2.4 GHz BLE Mesh", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
fun PolarRadarCanvas(blips: List<RadarBlip>) {
    val transition = rememberInfiniteTransition()
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(modifier = Modifier.padding(16.dp).fillMaxWidth().background(TacticalCard).border(1.dp, TacticalCyan.copy(alpha=0.1f)).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("REFRESH: 48 FPS // LOW_LAT", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("RETICLE: POLAR_01", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val maxRadius = size.width / 2f
                
                // Rings
                val ringRadii = listOf(0.2f, 0.5f, 0.8f, 1.0f)
                val ringLabels = listOf("5M", "15M", "30M")
                
                ringRadii.forEachIndexed { index, fraction ->
                    drawCircle(TacticalCyan.copy(alpha = 0.2f), maxRadius * fraction, style = Stroke(1f))
                    if (index < ringLabels.size) {
                        // We would draw text here, but Canvas text requires TextMeasurer (Compose 1.3+).
                        // I will skip canvas text and just draw UI elements if needed, or stick to rings.
                    }
                }
                
                // Crosshairs
                drawLine(TacticalCyan.copy(alpha = 0.3f), Offset(center.x, 0f), Offset(center.x, size.height), strokeWidth = 1f)
                drawLine(TacticalCyan.copy(alpha = 0.3f), Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = 1f)
                
                // Degree marks
                // Simplification for the sweep
                rotate(rotation, center) {
                    val sweepPath = Path().apply {
                        moveTo(center.x, center.y)
                        lineTo(center.x, 0f)
                        arcTo(
                            rect = androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height),
                            startAngleDegrees = 270f,
                            sweepAngleDegrees = 45f,
                            forceMoveTo = false
                        )
                        close()
                    }
                    drawPath(sweepPath, Brush.sweepGradient(
                        0.0f to Color.Transparent,
                        0.7f to TacticalCyan.copy(alpha = 0.1f),
                        0.99f to TacticalCyan.copy(alpha = 0.6f),
                        1.0f to Color.Transparent,
                        center = center
                    ))
                    drawLine(TacticalCyan, center, Offset(center.x, 0f), strokeWidth = 2f)
                }
                
                // Blips
                blips.forEachIndexed { index, blip ->
                    val distanceMeters = 10.0.pow((-59 - blip.rssi) / 20.0)
                    // clamp distance visually to 50 meters for max radius
                    val distanceFraction = (distanceMeters / 50.0).toFloat().coerceIn(0.1f, 1f)
                    val r = maxRadius * distanceFraction
                    val angle = (Math.abs(blip.hash.hashCode()) % 360).toDouble()
                    val bx = center.x + r * cos(Math.toRadians(angle)).toFloat()
                    val by = center.y + r * sin(Math.toRadians(angle)).toFloat()
                    
                    val age = System.currentTimeMillis() - blip.timestamp
                    val alpha = (1f - (age / 15000f)).coerceIn(0.2f, 1f)
                    
                    if (index == 0) { // Locked target mock
                        drawRect(TacticalCyan, Offset(bx - 8f, by - 8f), androidx.compose.ui.geometry.Size(16f, 16f))
                    } else if (index % 2 == 0) {
                        drawRect(TacticalRed.copy(alpha=alpha), Offset(bx - 6f, by - 6f), androidx.compose.ui.geometry.Size(12f, 12f), style = Stroke(2f))
                    } else {
                        // Diamond
                        val path = Path().apply {
                            moveTo(bx, by - 8f)
                            lineTo(bx + 8f, by)
                            lineTo(bx, by + 8f)
                            lineTo(bx - 8f, by)
                            close()
                        }
                        drawPath(path, TacticalCyan.copy(alpha=alpha))
                    }
                }
                
                drawRect(TacticalCyan, Offset(center.x - 4f, center.y - 4f), androidx.compose.ui.geometry.Size(8f, 8f))
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(TacticalCyan))
            Text("أقرب هدف: 8.4 م | زاوية السمت: 048° شمال شرق", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
fun SonarAudioTracker(blips: List<RadarBlip>) {
    val bestRssi = blips.maxOfOrNull { it.rssi } ?: -100
    val percentage = ((bestRssi + 100) / 60f).coerceIn(0f, 1f) * 100f
    
    Box(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().border(1.dp, TacticalCyan.copy(alpha=0.3f)).background(TacticalCard).padding(16.dp)) {
        Column {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.background(TacticalCyan).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text("مفعل", color = TacticalNavy, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("نبض صوتي تكتيكي عند الاقتراب", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Audio Ping Tracker (Sonar Modulation)", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
                Icon(Icons.Filled.Notifications, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${bestRssi} dBm [${percentage.roundToInt()}%]", color = TacticalCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text("مستوى استقرار الإشارة (RSSI // RMX2020):", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (i in 0..9) {
                    val isActive = i < (percentage / 10).toInt()
                    Box(modifier = Modifier.weight(1f).height(12.dp).padding(horizontal = 2.dp).background(if (isActive) TacticalCyan else Color.DarkGray))
                }
            }
        }
    }
}

@Composable
fun TargetsList(blips: List<RadarBlip>, targetDevice: String, viewModel: DashboardViewModel) {
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("COUNT: ${String.format("%02d", blips.size)} /\nSCAN", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
            Text("قائمة الأجهزة والكواشف\nالمكتشفة", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        blips.forEachIndexed { index, blip ->
            val dist = 10.0.pow((-59 - blip.rssi) / 20.0)
            val isTarget = index == 0 // Mocking first as target
            val cardColor = if (isTarget) TacticalNavy else TacticalCard
            val borderColor = if (isTarget) TacticalCyan else Color.Gray.copy(alpha=0.3f)
            
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp).border(1.dp, borderColor).background(cardColor).padding(12.dp)) {
                Column {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("${String.format("%.1f", dist)} متر", color = TacticalCyan, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                            Text("${blip.rssi} dBm", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isTarget) {
                                    Text("جهازك المستهدف", color = Color.White, fontSize = 10.sp, modifier = Modifier.background(TacticalRed).padding(horizontal = 4.dp, vertical = 2.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(targetDevice, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                } else {
                                    Text("وحدة مقترنة", color = Color.Gray, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Device ${blip.hash.take(4)}", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text("MAC: ${blip.hash.take(16)} // LE_CONN", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    if (isTarget) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("تشفير الإشارة:", color = Color.Gray, fontSize = 10.sp)
                                Text("UNENCRYPTED", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                            Column {
                                Text("نوع الحزمة:", color = Color.Gray, fontSize = 10.sp)
                                Text("ADV_IND", color = Color.White, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("مستوى البطارية:", color = Color.Gray, fontSize = 10.sp)
                                Text("41%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(
                                onClick = { },
                                shape = CutCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                border = androidx.compose.foundation.BorderStroke(1.dp, TacticalCyan),
                                modifier = Modifier.weight(1f).height(40.dp)
                            ) {
                                Text("تتبع بالسمت الحركي", color = TacticalCyan, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.pingDevice() },
                                shape = CutCornerShape(4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TacticalCyan),
                                modifier = Modifier.weight(1.5f).height(40.dp)
                            ) {
                                Text("إرسال نبضة صوتية / صفارة", color = TacticalNavy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("88%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("الحالة التشغيلية: خامل في النطاق", color = Color.Gray, fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(modifier = Modifier.size(6.dp).background(TacticalCyan))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FooterActions() {
    Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
        Button(
            onClick = { },
            shape = CutCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TacticalCyan),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("إعادة معايرة المسح النبضي // RE-CALIBRATE SCAN", color = TacticalNavy, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { },
            shape = CutCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Filled.Share, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("تصدير خريطة الإشارات والمصفوفة الميدانية (CSV / KML)", color = Color.Gray, fontSize = 12.sp)
        }
    }
}

@Composable
fun TacticalBottomNavBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF06090E))
            .border(1.dp, TacticalCyan.copy(alpha=0.1f))
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavBarItem("OVERVIEW", Icons.Outlined.Refresh, true)
        NavBarItem("RADAR", Icons.Filled.LocationOn, false)
        
        // SOS Button
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(TacticalRed.copy(alpha=0.2f), shape = RoundedCornerShape(8.dp))
                .border(1.dp, TacticalRed, shape = RoundedCornerShape(8.dp))
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Warning, contentDescription = "SOS", tint = TacticalRed)
        }
        
        NavBarItem("TARGETS", Icons.Outlined.Info, false)
        NavBarItem("COMMS", Icons.Filled.Settings, false)
    }
}

@Composable
fun NavBarItem(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, isActive: Boolean) {
    val color = if (isActive) TacticalCyan else Color.Gray
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { }) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = color, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = if(isActive) FontWeight.Bold else FontWeight.Normal)
    }
}

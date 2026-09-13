package com.zex.tracker.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import kotlin.math.cos
import kotlin.math.sin

data class RadarBlip(val hash: String, val rssi: Int, val timestamp: Long)

@Composable
fun BleRadarScreen(navController: NavController, deviceName: String = "الجهاز المستهدف", viewModel: DashboardViewModel = hiltViewModel()) {
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

    // Cleanup old blips
    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(2000)
            val now = System.currentTimeMillis()
            blips.removeAll { now - it.timestamp > 10000 }
        }
    }

    val transition = rememberInfiniteTransition()
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("ZEX Radar", color = Color(0xFF06B6D4), style = MaterialTheme.typography.headlineMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Text("البحث النشط بالبلوتوث الميداني", color = Color.White)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Box(modifier = Modifier.size(300.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = size.width / 2
                
                // Background grid
                drawCircle(Color(0xFF06B6D4).copy(alpha = 0.1f), radius)
                drawCircle(Color(0xFF06B6D4).copy(alpha = 0.2f), radius * 0.75f, style = Stroke(1f))
                drawCircle(Color(0xFF06B6D4).copy(alpha = 0.4f), radius * 0.5f, style = Stroke(1f))
                drawCircle(Color(0xFF06B6D4).copy(alpha = 0.6f), radius * 0.25f, style = Stroke(1f))
                
                drawLine(Color(0xFF06B6D4).copy(alpha = 0.3f), Offset(center.x, 0f), Offset(center.x, size.height))
                drawLine(Color(0xFF06B6D4).copy(alpha = 0.3f), Offset(0f, center.y), Offset(size.width, center.y))
                
                // Radar sweep line
                val sweepX = center.x + radius * cos(Math.toRadians(rotation.toDouble())).toFloat()
                val sweepY = center.y + radius * sin(Math.toRadians(rotation.toDouble())).toFloat()
                drawLine(Color(0xFF06B6D4), center, Offset(sweepX, sweepY), strokeWidth = 3f)
                
                // Draw Blips based on RSSI
                blips.forEach { blip ->
                    // RSSI roughly -40 (close) to -100 (far)
                    val distanceFraction = ((Math.abs(blip.rssi) - 40f) / 60f).coerceIn(0f, 1f)
                    val r = radius * distanceFraction
                    // Stable angle per device hash
                    val angle = (Math.abs(blip.hash.hashCode()) % 360).toDouble()
                    val bx = center.x + r * cos(Math.toRadians(angle)).toFloat()
                    val by = center.y + r * sin(Math.toRadians(angle)).toFloat()
                    
                    // Alpha based on age
                    val age = System.currentTimeMillis() - blip.timestamp
                    val alpha = (1f - (age / 10000f)).coerceIn(0.1f, 1f)
                    
                    drawCircle(Color(0xFF06B6D4).copy(alpha = alpha), 12f, Offset(bx, by))
                    drawCircle(Color.White.copy(alpha = alpha), 4f, Offset(bx, by))
                }
            }
            
            // Central dot
            Canvas(modifier = Modifier.size(16.dp)) {
                drawCircle(Color(0xFF06B6D4))
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (blips.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF06B6D4).copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("تم رصد الهدف!", color = Color(0xFF06B6D4), style = MaterialTheme.typography.titleLarge, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("تم التقاط إشارة $deviceName. اقترب أكثر باتجاه النقطة للمركز.", color = Color.White, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        }
        
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
        ) {
            Text("إغلاق الرادار", color = Color.White)
        }
    }
}

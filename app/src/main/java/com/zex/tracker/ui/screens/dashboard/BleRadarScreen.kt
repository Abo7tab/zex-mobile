package com.zex.tracker.ui.screens.dashboard

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleRadarScreen(navController: NavController, deviceName: String = "الجهاز المفقود") {
    var isScanning by remember { mutableStateOf(true) }
    
    val transition = rememberInfiniteTransition()
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Dummy devices for visualization
    val blips = remember {
        listOf(
            Offset(0.3f, 0.4f),
            Offset(-0.5f, 0.2f),
            Offset(0.1f, -0.6f)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("بحث عن: $deviceName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("جاري البحث عن $deviceName ميدانياً...", color = MaterialTheme.colorScheme.onBackground, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(300.dp)
                    .background(Color(0xFF0F172A), CircleShape), // Dark radar circle
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 2
                    val center = Offset(size.width / 2, size.height / 2)
                    
                    // Draw Grid
                    drawCircle(Color(0xFF1565C0).copy(alpha = 0.3f), radius, center, style = Stroke(2f))
                    drawCircle(Color(0xFF1565C0).copy(alpha = 0.3f), radius * 0.66f, center, style = Stroke(1.5f))
                    drawCircle(Color(0xFF1565C0).copy(alpha = 0.3f), radius * 0.33f, center, style = Stroke(1f))
                    
                    drawLine(Color(0xFF1565C0).copy(alpha = 0.3f), Offset(center.x, 0f), Offset(center.x, size.height), strokeWidth = 1f)
                    drawLine(Color(0xFF1565C0).copy(alpha = 0.3f), Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = 1f)

                    // Draw Sweep
                    rotate(rotation, center) {
                        val sweepGradient = Brush.sweepGradient(
                            0f to Color.Transparent,
                            0.9f to Color(0xFF1565C0).copy(alpha = 0.1f),
                            1f to Color(0xFF1565C0).copy(alpha = 0.8f)
                        )
                        drawArc(
                            brush = sweepGradient,
                            startAngle = -90f,
                            sweepAngle = 90f,
                            useCenter = true,
                            topLeft = Offset(0f, 0f),
                            size = size
                        )
                    }

                    // Draw Blips (in a real app, calculate relative to real distance and angle)
                    blips.forEach { blip ->
                        val blipCenter = Offset(
                            center.x + blip.x * radius,
                            center.y + blip.y * radius
                        )
                        drawCircle(Color(0xFF64B5F6), 8f, blipCenter)
                        drawCircle(Color.White, 3f, blipCenter)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("إيقاف الرادار والعودة")
            }
        }
    }
}


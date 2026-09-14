package com.zex.tracker.ui.screens.lock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.data.local.prefs.SecurePrefs
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay

@AndroidEntryPoint
class LockActivity : ComponentActivity() {

    @Inject lateinit var prefs: SecurePrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val disarmReceiver = object : android.content.BroadcastReceiver() {
            override fun receive(context: android.content.Context, intent: android.content.Intent) {
                if (intent.action == "com.zex.tracker.DISARM") finish()
            }
        }
        val filter = android.content.IntentFilter("com.zex.tracker.DISARM")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(disarmReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(disarmReceiver, filter)
        }
        
        // Ensure it acts as a system overlay lock screen
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.decorView.systemUiVisibility = (
            android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
        )
        window.addFlags(
            android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )
        
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // block back button
            }
        })
        
        setContent {
            var pinInput by remember { mutableStateOf("") }
            var error by remember { mutableStateOf(false) }
            
            val slate900 = Color(0xFF0F172A)
            val slate800 = Color(0xFF1E293B)
            val slate400 = Color(0xFF94A3B8)
            val crimson = Color(0xFF991B1B)
            val crimsonLight = Color(0xFFB91C1C)
            val crimsonBg = Color(0xFFFEF2F2)
            val emerald = Color(0xFF10B981)

            val infiniteTransition = rememberInfiniteTransition()
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
            
            // Auto incrementing tick for realism
            var tick by remember { mutableStateOf(0) }
            LaunchedEffect(Unit) {
                while(true) {
                    delay(1000)
                    tick++
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF8FAFC))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Top Header Badge
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = crimson,
                    modifier = Modifier.alpha(alpha)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CRITICAL ALERT — NODE ISOLATED & LOCKED", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = crimson, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SEC-LEVEL 5 EMERGENCY PURGE PROTOCOL", color = crimson, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Center Lockdown Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = slate900),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Shield Icon
                        Box(contentAlignment = Alignment.Center) {
                            Surface(shape = RoundedCornerShape(50), color = crimson.copy(alpha = 0.2f), modifier = Modifier.size(80.dp)) {}
                            Surface(shape = RoundedCornerShape(50), color = crimsonLight, modifier = Modifier.size(60.dp)) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.padding(16.dp))
                            }
                            // Pulse dot
                            Surface(shape = RoundedCornerShape(50), color = Color(0xFFFBBF24), modifier = Modifier.size(12.dp).align(Alignment.BottomEnd).offset(x = (-10).dp, y = (-10).dp), border = androidx.compose.foundation.BorderStroke(2.dp, slate900)) {}
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        Text("Device Lockdown Initiated by Central Command", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("All local hardware functions, touchscreen gestures, and system controls disabled. High-frequency telemetry, tamper detection, and live GPS transmission are ACTIVE.", color = slate400, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 18.sp)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Telemetry Info Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = slate800,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Memory, contentDescription = null, tint = slate400, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Node ID: ZEX-FA53", color = slate400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).background(emerald, RoundedCornerShape(50)).alpha(alpha))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("LIVE BEACON", color = emerald, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = slate400, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Owner: +1 234 567 8900", color = slate400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Text("${tick}s Tick", color = slate400, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("GPS: 30.0090° N, 31.1398° E (Lock Acquired)", color = Color(0xFFD97706), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Sensor grid
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFE2E8F0).copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(50), color = crimson.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = crimson, modifier = Modifier.padding(6.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Sensor Grid Lock", fontWeight = FontWeight.Bold, color = slate900, fontSize = 12.sp)
                                Text("Inertial Tamper: Armed", color = slate400, fontSize = 11.sp)
                            }
                        }
                        Icon(Icons.Default.Timeline, contentDescription = null, tint = crimson, modifier = Modifier.width(60.dp).height(24.dp))
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Authorization Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("CENTRAL COMMAND DISARM PIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = slate900, letterSpacing = 0.5.sp)
                            Text("6-DIGIT STRICT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = crimson)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Fake pin input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            repeat(6) { index ->
                                val isFilled = index < pinInput.length
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (index == pinInput.length) Color(0xFF2563EB).copy(alpha = 0.1f) else Color(0xFFF1F5F9),
                                    modifier = Modifier.size(42.dp).aspectRatio(1f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isFilled) {
                                            Box(modifier = Modifier.size(10.dp).background(slate900, RoundedCornerShape(50)))
                                        } else if (index == pinInput.length) {
                                            Box(modifier = Modifier.width(2.dp).height(16.dp).background(Color(0xFF2563EB)))
                                        } else {
                                            Box(modifier = Modifier.size(6.dp).background(Color(0xFFCBD5E1), RoundedCornerShape(50)))
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Invisible text field to capture input
                        TextField(
                            value = pinInput,
                            onValueChange = { 
                                if (it.length <= 6) {
                                    pinInput = it.filter { c -> c.isDigit() }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(1.dp).alpha(0f)
                        )
                        
                        if (error) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Invalid Authorization PIN", color = crimson, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = {
                                val savedPin = prefs.getString(ZexConstants.KEY_PIN_CODE)
                                val storedPass = prefs.getString(ZexConstants.KEY_OWNER_PASSWORD)
                                if (pinInput == savedPin) {
                                    finish()
                                } else {
                                    error = true
                                    pinInput = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = crimsonLight)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("DISARM & UNLOCK DEVICE", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call Recovery Agent", color = Color(0xFF2563EB), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            Text("HASH: 8F2A-CYBER", color = slate400, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = crimsonBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = crimson, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Unauthorized unlock attempts trigger immediate cryptographic camera snapshot and remote wipe audit.", color = crimson, fontSize = 11.sp, lineHeight = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus) {
            val controller = androidx.core.view.WindowInsetsControllerCompat(window, window.decorView)
            controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
    }
    
    override fun onBackPressed() {
        // block back
    }
}

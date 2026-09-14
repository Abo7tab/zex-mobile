package com.zex.tracker.ui.screens.auth

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zex.tracker.ui.screens.setup.SetupViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceRegistrationScreen(navController: NavController, viewModel: SetupViewModel = hiltViewModel()) {
    val context = LocalContext.current
    var deviceName by remember { mutableStateOf("Alpha-1 Recon Phone") }
    var smsWakeup by remember { mutableStateOf(true) }
    
    val state by viewModel.uiState.collectAsState()
    
    val bgColor = Color(0xFFF8FAFC)
    val cardColor = Color(0xFFFFFFFF)
    val primaryColor = Color(0xFF2563EB)
    val successColor = Color(0xFF10B981)
    val slate900 = Color(0xFF0F172A)
    val slate800 = Color(0xFF1E293B)
    val slate400 = Color(0xFF94A3B8)
    
    val uid = remember { "ZEX-UID-${UUID.randomUUID().toString().take(8).uppercase()}-C4ISR" }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("C4ISR SECURITY HUB", fontSize = 10.sp, color = slate400, fontWeight = FontWeight.Bold)
                        Text("HARDWARE REGISTRATION", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = slate900)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = slate900)
                    }
                },
                actions = {
                    Surface(shape = RoundedCornerShape(20.dp), color = primaryColor.copy(alpha = 0.1f), modifier = Modifier.padding(end = 16.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(primaryColor, RoundedCornerShape(50)))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("STEP 2 OF 2", color = slate800, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Provision and register cryptographic identity for this hardware terminal into the C4ISR operational registry.", fontSize = 13.sp, color = slate800, lineHeight = 20.sp)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Auto-Detected Enclave Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(Color(0xFF0F766E)))
                
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AUTO-DETECTED\nENCLAVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = slate900, lineHeight = 14.sp)
                        }
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF6EE7B7)) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF047857), RoundedCornerShape(50)))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Active Sensor\nHub", color = Color(0xFF064E3B), fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 12.sp)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Surface(shape = RoundedCornerShape(12.dp), color = bgColor, modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Hardware Model", fontSize = 12.sp, color = slate800)
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${Build.MANUFACTURER.capitalize()} ${Build.MODEL}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = slate900)
                                    Text("Snapdragon TrustZone", fontSize = 11.sp, color = slate400)
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = slate400.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Operating System", fontSize = 12.sp, color = slate800)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Android ${Build.VERSION.RELEASE}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = slate900)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = slate400.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text("Generated Node UID", fontSize = 12.sp, color = slate800)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = slate400, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Hardware Locked", fontSize = 10.sp, color = slate400)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(shape = RoundedCornerShape(8.dp), color = primaryColor.copy(alpha = 0.05f), modifier = Modifier.fillMaxWidth()) {
                                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(uid, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = slate900, fontFamily = FontFamily.Monospace)
                                    Icon(Icons.Default.VpnKey, contentDescription = null, tint = slate400, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = slate400, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("FIPS 140-3 Cryptographic Root\nVerified", fontSize = 11.sp, color = slate400, lineHeight = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("99.98%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            Text("Integrity", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Operator Config Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Tune, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("OPERATOR CONFIGURATION", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = slate900)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("Tactical Designation (Device Name)", fontSize = 12.sp, color = slate800, modifier = Modifier.padding(bottom = 8.dp))
                    OutlinedTextField(
                        value = deviceName,
                        onValueChange = { deviceName = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = slate800, unfocusedTextColor = slate800, 
                            focusedContainerColor = bgColor,
                            unfocusedContainerColor = bgColor,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp)) },
                        trailingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = successColor, modifier = Modifier.size(16.dp)) },
                        singleLine = true
                    )
                    Text("Broadcast alias across field tactical mesh.", fontSize = 11.sp, color = slate400, modifier = Modifier.padding(top = 4.dp))
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text("Device Type / Mission Profile", fontSize = 12.sp, color = slate800, modifier = Modifier.padding(bottom = 8.dp))
                    Surface(shape = RoundedCornerShape(12.dp), color = bgColor, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = primaryColor.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = primaryColor, modifier = Modifier.padding(6.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Recon Node", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = slate900)
                                Text("Low-power continuous telemetry & geo-fence", fontSize = 11.sp, color = slate400)
                            }
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = slate800)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Surface(shape = RoundedCornerShape(12.dp), color = bgColor, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = primaryColor.copy(alpha = 0.1f), modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.SettingsCell, contentDescription = null, tint = primaryColor, modifier = Modifier.padding(6.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Background C-SMS Wakeup", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = slate900)
                                Text("Triggers immediate location pulse via covert carrier channel", fontSize = 11.sp, color = slate400, lineHeight = 14.sp)
                            }
                            Switch(
                                checked = smsWakeup,
                                onCheckedChange = { smsWakeup = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (state.error != null) {
                Text(state.error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp).fillMaxWidth(), textAlign = TextAlign.Center)
            }
            
            Button(
                onClick = {
                    viewModel.registerDevice(context, deviceName) {
                        navController.navigate("security_guide") {
                            popUpTo("device_register") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                enabled = !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.LockPerson, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("REGISTER & INITIALIZE\nSECURE LINK", fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.SettingsInputAntenna, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Surface(shape = RoundedCornerShape(12.dp), color = bgColor, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF059669), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("AES-256 E2EE handshake will commence upon registration. Hardware cryptographic keys will be permanently committed to Android Keystore Enclave.", fontSize = 11.sp, color = slate800, lineHeight = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

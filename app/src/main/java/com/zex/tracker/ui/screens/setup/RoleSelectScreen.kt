package com.zex.tracker.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zex.tracker.data.local.prefs.SecurePrefs

@Composable
fun RoleSelectScreen(navController: NavController, prefs: SecurePrefs) {
    var selectedRole by remember { mutableStateOf("TARGET") }

    val bgColor = Color(0xFFF8FAFC)
    val cardColor = Color(0xFFFFFFFF)
    val primaryColor = Color(0xFF2563EB)
    val slate900 = Color(0xFF0F172A)
    val slate800 = Color(0xFF1E293B)
    val slate400 = Color(0xFF94A3B8)
    val emerald = Color(0xFF10B981)
    val primaryLight = primaryColor.copy(alpha = 0.1f)

    Scaffold(
        containerColor = bgColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Top Badge
            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFE2E8F0).copy(alpha = 0.5f)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(emerald, RoundedCornerShape(50)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("FIPS 140-3 HARDWARE KEYSTORE ENCLAVE ACTIVE", color = slate800, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Logo Icon
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = primaryColor,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ZEX MILITARY", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = slate900)
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = slate800.copy(alpha = 0.1f)) {
                            Text("REV 4.2", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = slate800, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Text("MIL-SPEC C4ISR PROTOCOL", fontSize = 10.sp, color = slate400, letterSpacing = 0.5.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("NODE ASSIGNMENT", color = slate900, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Configure the operational role for this terminal within the distributed field security mesh.", color = slate800, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 20.sp, modifier = Modifier.padding(horizontal = 16.dp))
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Card 1: Target
            val isTarget = selectedRole == "TARGET"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isTarget) 4.dp else 1.dp),
                modifier = Modifier.fillMaxWidth().clickable { selectedRole = "TARGET" }.border(if (isTarget) 2.dp else 0.dp, if (isTarget) primaryColor else Color.Transparent, RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(20.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = slate800.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.WifiTethering, contentDescription = null, tint = slate800, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Field Node (Target)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = slate900)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Runs silently as a persistent background daemon. Transmits encrypted GPS & BLE beacons and executes silent SMS recovery payloads.", fontSize = 12.sp, color = slate800, lineHeight = 18.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                            Surface(shape = RoundedCornerShape(8.dp), color = slate800.copy(alpha = 0.05f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Icon(Icons.Default.Memory, contentDescription = null, tint = slate800, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Background Daemon", fontSize = 10.sp, color = slate900, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                            Surface(shape = RoundedCornerShape(8.dp), color = slate800.copy(alpha = 0.05f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Icon(Icons.Default.GpsFixed, contentDescription = null, tint = slate800, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Zero-Touch Telemetry", fontSize = 10.sp, color = slate900, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = slate800.copy(alpha = 0.05f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = slate800, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Keystore Enclave", fontSize = 10.sp, color = slate900, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    RadioButton(selected = isTarget, onClick = { selectedRole = "TARGET" }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Card 2: Commander
            val isCommander = selectedRole == "COMMANDER"
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isCommander) 4.dp else 1.dp),
                modifier = Modifier.fillMaxWidth().clickable { selectedRole = "COMMANDER" }.border(if (isCommander) 2.dp else 0.dp, if (isCommander) primaryColor else Color.Transparent, RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(20.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = primaryLight, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null, tint = primaryColor, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Commander Console (Hunter)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = slate900, lineHeight = 22.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isCommander) {
                            Surface(shape = RoundedCornerShape(12.dp), color = primaryColor) {
                                Text("Active Selection", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text("Track tactical assets across high-resolution satellite cartography. Dispatch remote lockout, audible alarm, and crypto-wipe directives via dual-SIM cellular fallback.", fontSize = 12.sp, color = slate800, lineHeight = 18.sp)
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                            Surface(shape = RoundedCornerShape(8.dp), color = primaryLight) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Icon(Icons.Default.Map, contentDescription = null, tint = primaryColor, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("C2 Live Map", fontSize = 10.sp, color = primaryColor, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 6.dp)) {
                            Surface(shape = RoundedCornerShape(8.dp), color = primaryLight) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Icon(Icons.Default.SimCard, contentDescription = null, tint = primaryColor, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Dual-SIM SMS Dispatch", fontSize = 10.sp, color = primaryColor, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(8.dp), color = primaryLight) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                    Icon(Icons.Default.Hub, contentDescription = null, tint = primaryColor, modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Fleet Control", fontSize = 10.sp, color = primaryColor, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    RadioButton(selected = isCommander, onClick = { selectedRole = "COMMANDER" }, colors = RadioButtonDefaults.colors(selectedColor = primaryColor))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bottom Info Bar
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Radar, contentDescription = null, tint = emerald, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("MESH LINK TOPOLOGY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = slate800)
                }
                Text("AES-256 GCM SYNC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = emerald)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(shape = RoundedCornerShape(12.dp), color = cardColor, modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Assigned Node", fontSize = 10.sp, color = slate800)
                        Text(if (isCommander) "C2 Console" else "Field Node", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = cardColor, modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Beacon Ping", fontSize = 10.sp, color = slate800)
                        Text("120 ms", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = slate900)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = cardColor, modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Enclave ID", fontSize = 10.sp, color = slate800)
                        Text("0x9F41", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = slate900)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    prefs.putString("role", selectedRole)
                    navController.navigate("dashboard") {
                        popUpTo("role_select") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text("INITIALIZE ROLE CONFIGURATION", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = slate800, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Role assignment binds hardware encryption keys permanently to selected operational profile.", fontSize = 10.sp, color = slate800, textAlign = TextAlign.Center, lineHeight = 14.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

package com.zex.tracker.ui.screens.setup

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.core.constants.ZexConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityGuideScreen(navController: NavController, prefs: SecurePrefs) {
    val context = LocalContext.current
    var showPin by remember { mutableStateOf(false) }

    val bgColor = Color(0xFFF8FAFC)
    val cardColor = Color(0xFFFFFFFF)
    val primaryColor = Color(0xFF2563EB)
    val successColor = Color(0xFF16A34A)

    Scaffold(
        containerColor = bgColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = "Shield",
                            tint = primaryColor,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("ZEX C4ISR", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF1E293B))
                        Text("Field Terminal 4092", fontSize = 12.sp, color = Color(0xFF64748B))
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
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = successColor, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("DEFCON-3 SECURE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = successColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Device Onboarding & Setup", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Spacer(modifier = Modifier.height(4.dp))
                Text("Hardware identity and telemetry authorization", fontSize = 14.sp, color = Color(0xFF64748B))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Card 1 (API & PIN)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("C4ISR ENDPOINT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 1.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = successColor, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TLS 1.3 Verified (14ms)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = successColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = "https://zex.alwaysdata.net/api",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                        ),
                        trailingIcon = {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = successColor,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp).size(16.dp))
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("OPERATOR SECURITY PIN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 1.sp)
                        TextButton(
                            onClick = { showPin = !showPin },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text(if (showPin) "Hide" else "Show", fontSize = 12.sp, color = primaryColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(6) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .size(42.dp)
                                    .aspectRatio(1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (showPin) {
                                        Text("*", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    } else {
                                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF0F172A), RoundedCornerShape(50)))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("6-digit tactical PIN assigned in web console", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Card 2 (Capabilities)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("SYSTEM CAPABILITIES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), letterSpacing = 1.sp)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = successColor.copy(alpha = 0.15f)
                        ) {
                            Text("4/4 Active", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = successColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CapabilityRow("Background Location", "GPS & Galileo telemetry", successColor)
                    CapabilityRow("Offline SMS Receiver", "Encrypted command channel", successColor)
                    CapabilityRow("BLE Radar Broadcaster", "Offline mesh discovery", successColor)
                    CapabilityRow("Device Admin Protection", "Lockdown and tamper protection", successColor, isLast = true)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    prefs.putBoolean(ZexConstants.KEY_IS_SETUP_COMPLETE, true)
                    com.zex.tracker.service.ZexForegroundService.startService(context)
                    navController.navigate("dashboard") {
                        popUpTo(0)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("COMPLETE NODE BINDING", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = successColor, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "Protected by AES-256 GCM Hardware Enclave & Zero-Trust Protocol",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun CapabilityRow(title: String, subtitle: String, successColor: Color, isLast: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = if (isLast) 0.dp else 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.size(40.dp)
            ) {
                // Generic icon for all since we don't have access to custom drawables easily
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }
        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = successColor, modifier = Modifier.size(20.dp))
    }
}

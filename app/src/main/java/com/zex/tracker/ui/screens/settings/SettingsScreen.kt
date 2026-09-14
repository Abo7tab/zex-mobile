package com.zex.tracker.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.data.local.prefs.SecurePrefs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    prefs: SecurePrefs,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showToken by remember { mutableStateOf(false) }
    var tokenValue by remember { mutableStateOf(prefs.getString(ZexConstants.KEY_DEVICE_TOKEN) ?: "zex_live_7x89fa9sdf87sd98f7s9") }
    var phoneValue by remember { mutableStateOf("+1 234 567 8900") }
    
    val bgColor = Color(0xFFF8FAFC)
    val cardColor = Color(0xFFFFFFFF)
    val primaryColor = Color(0xFF2563EB)
    val dangerColor = Color(0xFFDC2626)

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("NODE CONFIGURATION", fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp, color = Color(0xFF0F172A)) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0F172A))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Authentication & Heartbeat Section
            Text("AUTHENTICATION & HEARTBEAT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("API Authorization Token", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tokenValue,
                        onValueChange = { tokenValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        visualTransformation = if (showToken) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color(0xFF1E293B), unfocusedTextColor = Color(0xFF1E293B), 
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = Color(0xFFE2E8F0),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showToken = !showToken }) {
                                Icon(Icons.Default.Lock, contentDescription = "Toggle Visibility", tint = primaryColor)
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Heartbeat Interval", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    Text("Frequency of background location syncs", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("60 Seconds", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color(0xFF64748B))
                        }
                    }
                }
            }

            // Offline SMS Fallback Section
            Text("OFFLINE SMS FALLBACK", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Trusted Command Center Number", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                    Text("Accept offline commands only from this phone", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneValue,
                        onValueChange = { phoneValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color(0xFF1E293B), unfocusedTextColor = Color(0xFF1E293B), 
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC),
                            focusedBorderColor = Color(0xFFE2E8F0),
                            unfocusedBorderColor = Color(0xFFE2E8F0),
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = "Phone", tint = Color(0xFF64748B))
                        }
                    )
                }
            }

            // Danger Zone
            Text("DANGER ZONE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = dangerColor, modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedButton(
                        onClick = { Toast.makeText(context, "Telemetry cleared", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B))
                    ) {
                        Text("Clear Local Telemetry Data", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Button(
                        onClick = { Toast.makeText(context, "Node unbind initiated", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = dangerColor)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unbind Node & Factory Reset", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

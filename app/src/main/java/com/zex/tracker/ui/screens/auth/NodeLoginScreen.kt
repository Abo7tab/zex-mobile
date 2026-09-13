package com.zex.tracker.ui.screens.auth

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zex.tracker.data.remote.dto.LoginRequest
import com.zex.tracker.ui.screens.setup.SetupViewModel

@Composable
fun NodeLoginScreen(navController: NavController, viewModel: SetupViewModel = hiltViewModel()) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    val state by viewModel.uiState.collectAsState()
    
    val bgColor = Color(0xFFF8FAFC)
    val cardColor = Color(0xFFFFFFFF)
    val primaryColor = Color(0xFF2563EB)
    val successColor = Color(0xFF10B981)
    val slate800 = Color(0xFF1E293B)
    val slate400 = Color(0xFF94A3B8)
    
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
            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFF1F5F9)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Box(modifier = Modifier.size(6.dp).background(successColor, RoundedCornerShape(50)))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SEC-OPS LEVEL 1 • ACTIVE", color = slate800, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Logo Icon
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = cardColor,
                    shadowElevation = 8.dp,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = primaryColor, modifier = Modifier.padding(20.dp))
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = primaryColor,
                    modifier = Modifier.size(24.dp).offset(x = 8.dp, y = 8.dp),
                    shadowElevation = 4.dp
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.padding(4.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("TACTICAL NODE INITIALIZATION", color = slate800, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Authenticate operator credentials to bind this mobile hardware to the C4ISR command center.", color = slate400, fontSize = 13.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Surface(shape = RoundedCornerShape(20.dp), color = successColor.copy(alpha = 0.1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = successColor, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ENCRYPTED TLS 1.3 SECURE CHANNEL", color = successColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Form Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Green line indicator
                Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(successColor))
                
                Column(modifier = Modifier.padding(24.dp)) {
                    if (state.error != null) {
                        Text(state.error!!, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
                    }
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Operator Identifier", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = slate800)
                        Text("MIL-NET SSO", fontSize = 10.sp, color = slate400, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = bgColor,
                            unfocusedContainerColor = bgColor,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        placeholder = { Text("operator@c4isr.zex.mil", fontSize = 13.sp, color = slate400) },
                        leadingIcon = { Icon(Icons.Default.AccountBox, contentDescription = null, tint = slate400, modifier = Modifier.size(20.dp)) },
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Operator Security Token", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = slate800)
                        Text("Emergency Bypass Code", fontSize = 10.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = bgColor,
                            unfocusedContainerColor = bgColor,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = slate400, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = null, tint = slate400, modifier = Modifier.size(20.dp))
                            }
                        },
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Surface(shape = RoundedCornerShape(8.dp), color = bgColor, modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Memory, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Hardware Enclave: FIPS 140-3\nValidated", fontSize = 11.sp, color = slate800, lineHeight = 14.sp)
                            }
                            Text("SYNCHRONIZED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = successColor)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            viewModel.loginOwner(LoginRequest(email, password)) {
                                navController.navigate("device_register") {
                                    popUpTo("node_login") { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("VERIFY CREDENTIALS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f), color = slate400.copy(alpha = 0.3f))
                Text(" OR ALTERNATIVE METHOD ", fontSize = 10.sp, color = slate400, fontWeight = FontWeight.Bold)
                Divider(modifier = Modifier.weight(1f), color = slate400.copy(alpha = 0.3f))
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Fast Pair Card
            Surface(shape = RoundedCornerShape(16.dp), color = cardColor, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = primaryColor.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = primaryColor, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Web Dashboard Fast-Pair", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = slate800)
                        Text("Scan authorization token from C4ISR portal", fontSize = 12.sp, color = slate400, lineHeight = 16.sp)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = slate800, modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Biometric Card
            Surface(shape = RoundedCornerShape(16.dp), color = cardColor, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(12.dp), color = successColor.copy(alpha = 0.1f), modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = successColor, modifier = Modifier.padding(12.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Biometric Hardware Passkey", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = slate800)
                        Text("Operator touch identity profile enrolled", fontSize = 12.sp, color = slate400, lineHeight = 16.sp)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = primaryColor.copy(alpha = 0.1f)) {
                        Text("SCAN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = slate800, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Memory, contentDescription = null, tint = slate400, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("NODE HARDWARE #ZEX-904-TX", fontSize = 10.sp, color = slate400, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("ZEX C4ISR Protocol • Zero-Trust Mobile Node Spec v2.4\n• MIL-STD-810H Enclave Verified", fontSize = 9.sp, color = slate400, textAlign = TextAlign.Center, lineHeight = 14.sp)
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

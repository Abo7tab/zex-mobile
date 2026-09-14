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
import com.zex.tracker.data.remote.dto.RegisterRequest
import com.zex.tracker.ui.screens.setup.SetupViewModel

@Composable
fun NodeLoginScreen(navController: NavController, viewModel: SetupViewModel = hiltViewModel()) {
    var isRegister by remember { mutableStateOf(false) }
    
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    
    var showPassword by remember { mutableStateOf(false) }
    val state by viewModel.uiState.collectAsState()

    val primaryColor = Color(0xFF2563EB)
    val successColor = Color(0xFF16A34A)
    val bgColor = Color(0xFFF8FAFC)
    val slate800 = Color(0xFF1E293B)
    val slate400 = Color(0xFF94A3B8)
    val cardColor = Color.White

    Box(modifier = Modifier.fillMaxSize().background(bgColor)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Header Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = successColor.copy(alpha = 0.1f),
                modifier = Modifier.border(1.dp, successColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(successColor))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SECURE TLS 1.3 CONNECTION", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = successColor)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Text("TACTICAL NODE\nINITIALIZATION", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = slate800, textAlign = TextAlign.Center, lineHeight = 34.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Enter operator credentials to authenticate this device as a command or target node in the mesh network.", fontSize = 14.sp, color = slate400, textAlign = TextAlign.Center, lineHeight = 20.sp)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Tabs
            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFE2E8F0)).padding(4.dp)) {
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (!isRegister) cardColor else Color.Transparent)
                        .clickable { isRegister = false }.padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("LOGIN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (!isRegister) primaryColor else slate400)
                }
                Box(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isRegister) cardColor else Color.Transparent)
                        .clickable { isRegister = true }.padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("REGISTER", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isRegister) primaryColor else slate400)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            // Form Card
            Surface(shape = RoundedCornerShape(20.dp), color = cardColor, shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    
                    if (!state.error.isNullOrEmpty()) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFEE2E2), modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                            Text(state.error!!, color = Color(0xFFB91C1C), fontSize = 12.sp, modifier = Modifier.padding(12.dp))
                        }
                    }
                    
                    if (isRegister) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Operator Name", fontSize = 10.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = slate800, unfocusedTextColor = slate800, focusedContainerColor = bgColor, unfocusedContainerColor = bgColor, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Phone Number", fontSize = 10.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone, onValueChange = { phone = it }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = slate800, unfocusedTextColor = slate800, focusedContainerColor = bgColor, unfocusedContainerColor = bgColor, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Badge, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Operator Identifier (Email)", fontSize = 10.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = slate800, unfocusedTextColor = slate800, 
                            focusedContainerColor = bgColor, unfocusedContainerColor = bgColor,
                            focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
                        ),
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = slate400, modifier = Modifier.size(20.dp)) },
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isRegister) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Security PIN (6 Digits)", fontSize = 10.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pinCode, onValueChange = { pinCode = it }, modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = slate800, unfocusedTextColor = slate800, focusedContainerColor = bgColor, unfocusedContainerColor = bgColor, focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Operator Security Token", fontSize = 10.sp, color = primaryColor, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = slate800, unfocusedTextColor = slate800, 
                            focusedContainerColor = bgColor, unfocusedContainerColor = bgColor,
                            focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent
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
                            if (isRegister) {
                                viewModel.registerOwner(RegisterRequest(name, email, phone, password, password, pinCode)) {
                                    navController.navigate("device_register") {
                                        popUpTo("node_login") { inclusive = true }
                                    }
                                }
                            } else {
                                viewModel.loginOwner(LoginRequest(email, password)) {
                                    navController.navigate("device_register") {
                                        popUpTo("node_login") { inclusive = true }
                                    }
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
                            Icon(if (isRegister) Icons.Default.PersonAdd else Icons.Default.Key, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isRegister) "REGISTER CREDENTIALS" else "VERIFY CREDENTIALS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

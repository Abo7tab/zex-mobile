package com.zex.tracker.ui.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zex.tracker.ui.components.PrimaryButton
import com.zex.tracker.ui.components.LoadingOverlay
import com.zex.tracker.ui.components.ErrorBanner
import com.zex.tracker.data.remote.dto.RegisterRequest
import com.zex.tracker.data.remote.dto.LoginRequest

@Composable
fun WelcomeScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Welcome to ZEX Tracker", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))
        PrimaryButton("Continue", onClick = { navController.navigate("auth") })
    }
}

@Composable
fun AuthScreen(navController: NavController, viewModel: SetupViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var isLogin by remember { mutableStateOf(false) }
    
    // Simple state
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }

    LoadingOverlay(state.isLoading)

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text(if (isLogin) "Login" else "Register", style = MaterialTheme.typography.headlineMedium)
        ErrorBanner(state.error)
        
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth())
        
        if (!isLogin) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = pin, onValueChange = { pin = it }, label = { Text("6-digit PIN") }, modifier = Modifier.fillMaxWidth())
        }
        
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Submit", onClick = {
            if (isLogin) {
                viewModel.loginOwner(LoginRequest(email, pass)) { navController.navigate("device_register") }
            } else {
                viewModel.registerOwner(RegisterRequest(name, email, phone, pass, pass, pin)) { navController.navigate("device_register") }
            }
        })
        TextButton(onClick = { isLogin = !isLogin }) { Text("Toggle Login/Register") }
    }
}

@Composable
fun DeviceRegisterScreen(navController: NavController, viewModel: SetupViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var deviceName by remember { mutableStateOf("My Phone") }
    val context = LocalContext.current

    LoadingOverlay(state.isLoading)

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Register Device", style = MaterialTheme.typography.headlineMedium)
        ErrorBanner(state.error)
        OutlinedTextField(value = deviceName, onValueChange = { deviceName = it }, label = { Text("Device Name") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        PrimaryButton("Register", onClick = {
            viewModel.registerDevice(context, deviceName) { navController.navigate("permissions") }
        })
    }
}

@Composable
fun PermissionsScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Permissions", style = MaterialTheme.typography.headlineMedium)
        Text("Please grant necessary permissions (Location, SMS, etc).")
        Spacer(Modifier.height(32.dp))
        PrimaryButton("Grant & Continue", onClick = { navController.navigate("device_admin") })
    }
}

@Composable
fun DeviceAdminScreen(navController: NavController, onFinish: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Device Admin", style = MaterialTheme.typography.headlineMedium)
        Text("Enable Device Admin for wipe/lock features.")
        Spacer(Modifier.height(32.dp))
        PrimaryButton("Enable", onClick = onFinish)
        TextButton(onClick = onFinish) { Text("Skip for now") }
    }
}

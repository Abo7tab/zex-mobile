package com.zex.tracker.ui.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Search

import androidx.compose.ui.Modifier
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zex.tracker.ui.components.PrimaryButton
import com.zex.tracker.ui.components.LoadingOverlay
import com.zex.tracker.ui.components.ErrorBanner
import com.zex.tracker.data.remote.dto.RegisterRequest
import com.zex.tracker.data.remote.dto.LoginRequest
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.provider.Settings
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.zex.tracker.receiver.ZexDeviceAdminReceiver
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.Alignment

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun WelcomeScreen(navController: NavController) {
    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp).background(MaterialTheme.colorScheme.background), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Card(shape = CutCornerShape(4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("منظومة ZEX العسكرية", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("[ SYS_AUTH_REQUIRED ]", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    Spacer(Modifier.height(32.dp))
                    PrimaryButton("بدء التشفير // INITIATE", onClick = { navController.navigate("auth") })
                }
            }
        }
    }
}

@Composable
fun AuthScreen(navController: NavController, viewModel: SetupViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var isLogin by remember { mutableStateOf(false) }
    
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    LoadingOverlay(state.isLoading)

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center) {
            Card(shape = CutCornerShape(4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(if (isLogin) "AUTHENTICATE TERMINAL" else "TACTICAL OPS ENLIST", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(if (isLogin) "بروتوكول الدخول التكتيكي" else "بروتوكول تسجيل العناصر", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    ErrorBanner(state.error)
                    
                    OutlinedTextField(
                        value = email, 
                        onValueChange = { email = it }, 
                        label = { Text("OPERATOR CALL SIGN") }, 
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            focusedTextColor = androidx.compose.ui.graphics.Color.White,
                            unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        shape = CutCornerShape(4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = pass, 
                        onValueChange = { pass = it }, 
                        label = { Text("TACTICAL ACCESS KEY") }, 
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            focusedLabelColor = MaterialTheme.colorScheme.primary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            focusedTextColor = androidx.compose.ui.graphics.Color.White,
                            unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                        ),
                        shape = CutCornerShape(4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    )
                    
                    if (!isLogin) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = name, 
                            onValueChange = { name = it }, 
                            label = { Text("OPERATOR NAME") }, 
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                focusedTextColor = androidx.compose.ui.graphics.Color.White,
                                unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            shape = CutCornerShape(4.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone, 
                            onValueChange = { phone = it }, 
                            label = { Text("SECURE PHONE NUMBER") }, 
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                focusedTextColor = androidx.compose.ui.graphics.Color.White,
                                unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            shape = CutCornerShape(4.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = pin, 
                            onValueChange = { pin = it }, 
                            label = { Text("6-DIGIT PIN") }, 
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                focusedTextColor = androidx.compose.ui.graphics.Color.White,
                                unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
                                cursorColor = MaterialTheme.colorScheme.primary,
                                focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                            ),
                            shape = CutCornerShape(4.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                        )
                    }
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            if (isLogin) {
                                viewModel.loginOwner(LoginRequest(email, pass)) { navController.navigate("device_register") }
                            } else {
                                viewModel.registerOwner(RegisterRequest(name, email, phone, pass, pass, pin)) { navController.navigate("device_register") }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = CutCornerShape(4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.background
                        )
                    ) {
                        Text(if (isLogin) "دخول // AUTHENTICATE" else "تسجيل // ENLIST NOW", fontWeight = FontWeight.Bold, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { isLogin = !isLogin }, modifier = Modifier.fillMaxWidth()) { 
                        Text(if (isLogin) "إنشاء حساب جديد // REGISTER OPERATOR" else "العودة للدخول // BACK TO LOGIN", color = MaterialTheme.colorScheme.primary, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace) 
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceRegisterScreen(navController: NavController, viewModel: SetupViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var deviceName by remember { mutableStateOf(Build.MODEL ?: "My Phone") }
    val context = LocalContext.current

    LoadingOverlay(state.isLoading)

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("تسجيل الجهاز", style = MaterialTheme.typography.headlineMedium)
                    ErrorBanner(state.error)
                    OutlinedTextField(value = deviceName, onValueChange = { deviceName = it }, label = { Text("اسم الجهاز") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton("تأكيد", onClick = {
                        viewModel.registerDevice(context, deviceName) { navController.navigate("permissions") }
                    })
                }
            }
        }
    }
}

@Composable
fun PermissionsScreen(navController: NavController) {
    val context = LocalContext.current
    var isLocGranted by remember { mutableStateOf(false) }
    var isSmsGranted by remember { mutableStateOf(false) }
    var isBleGranted by remember { mutableStateOf(false) }
    var isOverlayGranted by remember { mutableStateOf(false) }

    fun updateStatuses() {
        isLocGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        isSmsGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        
        isBleGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        
        isOverlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    LaunchedEffect(Unit) { updateStatuses() }

    val totalModules = 4
    val activeModules = listOf(isLocGranted, isSmsGranted, isBleGranted, isOverlayGranted).count { it }
    val progress = activeModules.toFloat() / totalModules.toFloat()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        updateStatuses()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text("ZEX DIRECT-BRIDGE", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("SYSTEM READINESS", color = MaterialTheme.colorScheme.onBackground)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 8.dp,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${(progress * 100).toInt()}%", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text("$activeModules OF $totalModules ONLINE", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onBackground)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))

        fun grantAll() {
            val perms = mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_SMS
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                perms.add(Manifest.permission.BLUETOOTH_SCAN)
                perms.add(Manifest.permission.BLUETOOTH_CONNECT)
                perms.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                perms.add(Manifest.permission.POST_NOTIFICATIONS)
            }
            launcher.launch(perms.toTypedArray())
            
            if (!isOverlayGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:" + context.packageName))
                context.startActivity(intent)
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = if (isLocGranted) Color(0xFF00FA9A) else Color.Gray)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("GPS & PRECISION COORDS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("ACCESS_FINE_LOCATION", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
        
        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = if (isOverlayGranted) Color(0xFF00FA9A) else Color.Gray)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("OVERLAY & EXEC LOCKDOWN", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("SYSTEM_ALERT_WINDOW", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Email, contentDescription = null, tint = if (isSmsGranted) Color(0xFF00FA9A) else Color.Gray)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("ENCRYPTED SMS DISPATCH", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("SEND_SMS / RECEIVE_SMS", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, contentDescription = null, tint = if (isBleGranted) Color(0xFF00FA9A) else Color.Gray)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("BLE RADAR & BEACONS", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("BLUETOOTH_SCAN/ADVERTISE", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = { grantAll() },
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.CutCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
        ) {
            Text("GRANT ALL & INITIALIZE SYSTEM", fontWeight = FontWeight.Bold)
        }
        
        val canProceed = isLocGranted && isSmsGranted && isBleGranted && isOverlayGranted
        Button(
            onClick = { navController.navigate("deviceAdmin") },
            enabled = canProceed,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (canProceed) "CONTINUE TO DEVICE PROTECTION" else "GRANT REQUIRED PERMISSIONS TO CONTINUE")
        }
    }
}

@Composable
fun DeviceAdminScreen(navController: NavController, onFinish: () -> Unit) {
    val context = LocalContext.current
    var isAdminEnabled by remember { mutableStateOf(false) }
    var isAccessibilityEnabled by remember { mutableStateOf(false) }
    var isBatteryExempt by remember { mutableStateOf(false) }
    var hasLocationAndSms by remember { mutableStateOf(false) }

    fun updateStatuses() {
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        isAccessibilityEnabled = enabledServices?.contains(context.packageName) == true
        
        val dpm = context.getSystemService(android.content.Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val component = ComponentName(context, ZexDeviceAdminReceiver::class.java)
        isAdminEnabled = dpm.isAdminActive(component)
        
        val pm = context.getSystemService(android.os.PowerManager::class.java)
        isBatteryExempt = pm.isIgnoringBatteryOptimizations(context.packageName)
        
        hasLocationAndSms = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        updateStatuses()
    }

    LaunchedEffect(Unit) {
        updateStatuses()
        val pm = context.getSystemService(android.os.PowerManager::class.java)
        if (!pm.isIgnoringBatteryOptimizations(context.packageName)) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            launcher.launch(intent)
        }
    }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth().weight(1f)) {
                Column(modifier = Modifier.padding(16.dp).verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    Text("إعداد حماية الجهاز", style = MaterialTheme.typography.headlineMedium)
                    Text("قم بتفعيل مدير الجهاز لخصائص المسح/القفل، تعطيل تحسين البطارية، والسماح بالعرض فوق التطبيقات الأخرى.", modifier = Modifier.padding(vertical = 8.dp))
                    Spacer(Modifier.height(16.dp))
                    
                    PrimaryButton("تفعيل مدير الجهاز (القفل عن بُعد)", onClick = {
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, ComponentName(context, ZexDeviceAdminReceiver::class.java))
                            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "مطلوب للقفل والمسح عن بُعد.")
                        }
                        launcher.launch(intent)
                    })
                    Spacer(Modifier.height(12.dp))
                    
                    PrimaryButton("العرض فوق التطبيقات الأخرى", onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                        launcher.launch(intent)
                    })
                    Spacer(Modifier.height(12.dp))
                    
                    PrimaryButton("إعفاء التطبيق من موفر البطارية", onClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        launcher.launch(intent)
                    })
                    Spacer(Modifier.height(12.dp))

                    PrimaryButton("إعدادات إمكانية الوصول", onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        launcher.launch(intent)
                    })
                    Spacer(Modifier.height(12.dp))

                    PrimaryButton("تأمين زر إيقاف التشغيل بكلمة سر", onClick = {
                        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                        launcher.launch(intent)
                    })
                    Text("لأقصى درجات الأمان، يرجى تفعيل 'تأمين إيقاف التشغيل بكلمة مرور' من إعدادات النظام.", modifier = Modifier.padding(vertical = 4.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(12.dp))

                    PrimaryButton("السماح بالعمل في الخلفية والتشغيل التلقائي", onClick = {
                        try {
                            val intent = Intent().apply { component = ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity") }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val intent = Intent().apply { component = ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity") }
                                context.startActivity(intent)
                            } catch (e2: Exception) {
                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
                                context.startActivity(intent)
                            }
                        }
                    })
                    Text("يرجى يدوياً تفعيل العمل بالخلفية والتشغيل التلقائي لأجهزة شاومي/أوبو/ريلمي.", modifier = Modifier.padding(vertical = 4.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    
                    Spacer(Modifier.height(24.dp))
                    Text("قائمة التحقق من حالة الحماية", style = MaterialTheme.typography.titleMedium, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    
                    val items = listOf(
                        "صلاحيات الموقع والرسائل القصيرة" to hasLocationAndSms,
                        "مدير الجهاز" to isAdminEnabled,
                        "منع إيقاف التشغيل (إمكانية الوصول)" to isAccessibilityEnabled,
                        "إعفاء موفر البطارية" to isBatteryExempt
                    )
                    
                    items.forEach { (text, isOk) ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            if (isOk) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "OK", tint = androidx.compose.ui.graphics.Color.Green, modifier = Modifier.size(20.dp))
                            } else {
                                Icon(Icons.Default.Warning, contentDescription = "Missing", tint = androidx.compose.ui.graphics.Color.Red, modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(text, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { 
                    navController.navigate("security_guide") 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("التالي: إعدادات الأمان والحماية 🛡️", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }
}

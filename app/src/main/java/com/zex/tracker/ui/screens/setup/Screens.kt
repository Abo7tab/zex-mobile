package com.zex.tracker.ui.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@Composable
fun WelcomeScreen(navController: NavController) {
    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("منظومة ZEX لتتبع وتأمين الهواتف", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(32.dp))
                    PrimaryButton("متابعة", onClick = { navController.navigate("auth") })
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

    LoadingOverlay(state.isLoading)

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(if (isLogin) "تسجيل الدخول" else "إنشاء حساب", style = MaterialTheme.typography.headlineMedium)
                    ErrorBanner(state.error)
                    
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("البريد الإلكتروني") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("كلمة المرور") }, modifier = Modifier.fillMaxWidth())
                    
                    if (!isLogin) {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("الاسم") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم الهاتف") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = pin, onValueChange = { pin = it }, label = { Text("رمز PIN من 6 أرقام") }, modifier = Modifier.fillMaxWidth())
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    PrimaryButton("تأكيد", onClick = {
                        if (isLogin) {
                            viewModel.loginOwner(LoginRequest(email, pass)) { navController.navigate("device_register") }
                        } else {
                            viewModel.registerOwner(RegisterRequest(name, email, phone, pass, pass, pin)) { navController.navigate("device_register") }
                        }
                    })
                    TextButton(onClick = { isLogin = !isLogin }) { Text("التبديل بين تسجيل الدخول/إنشاء حساب") }
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
    var isStandardGranted by remember { mutableStateOf(false) }
    var isOverlayGranted by remember { mutableStateOf(false) }

    val checkPermissions = {
        val fineLoc = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val sms = ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        
        var ble = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ble = ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                  ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        }
        
        isStandardGranted = fineLoc && sms && ble
        isOverlayGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.provider.Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    LaunchedEffect(Unit) {
        checkPermissions()
    }

    // A lifecycle observer could be added to re-check when returning from settings, but simple buttons work too

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        checkPermissions()
    }

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp), verticalArrangement = Arrangement.Center) {
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("الصلاحيات المطلوبة للحماية", style = MaterialTheme.typography.headlineMedium)
                        if (isStandardGranted && isOverlayGranted) Icon(Icons.Default.CheckCircle, contentDescription = "Granted", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 8.dp))
                    }
                    Text("نظام ZEX يحتاج لصلاحيات الموقع، الرسائل، البلوتوث (للرادار)، والظهور فوق التطبيقات (لشاشة القفل المستعصية) للعمل بكفاءة.", modifier = Modifier.padding(vertical = 8.dp))
                    Spacer(Modifier.height(16.dp))
                    
                    PrimaryButton(if (isStandardGranted) "✅ الصلاحيات الأساسية مكتملة" else "1. منح الصلاحيات الأساسية", onClick = { 
                        val perms = mutableListOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.READ_PHONE_STATE
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            perms.add(Manifest.permission.BLUETOOTH_SCAN)
                            perms.add(Manifest.permission.BLUETOOTH_ADVERTISE)
                            perms.add(Manifest.permission.BLUETOOTH_CONNECT)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            perms.add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            perms.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        }
                        launcher.launch(perms.toTypedArray())
                    })
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Button(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            val intent = android.content.Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = if (isOverlayGranted) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary)) {
                        Text(if (isOverlayGranted) "✅ صلاحية القفل مكتملة" else "2. منح صلاحية القفل (العرض فوق التطبيقات)")
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Button(onClick = { checkPermissions() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                        Text("تحديث حالة الصلاحيات")
                    }

                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { navController.navigate("device_admin") },
                        enabled = isStandardGranted && isOverlayGranted,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("متابعة")
                    }
                }
            }
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
                    com.zex.tracker.service.ZexForegroundService.startService(context)
                    navController.navigate("dashboard") 
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("إنهاء التهيئة وبدء الحماية", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        }
    }
}

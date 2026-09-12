package com.zex.tracker.ui.screens.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.data.remote.dto.DeviceDto
import com.zex.tracker.service.ZexForegroundService
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(prefs: SecurePrefs, navController: NavController, viewModel: DashboardViewModel = hiltViewModel()) {
    val uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "Unknown UID"
    val context = LocalContext.current
    
    var isRunning by remember { mutableStateOf(ZexForegroundService.isRunning) }
    val devices by viewModel.devices.collectAsState()
    var selectedTargetDevice by remember { mutableStateOf<DeviceDto?>(null) }
    var expanded by remember { mutableStateOf(false) }

    var targetPhone by remember { mutableStateOf("") }
    
    val commandOptions = listOf(
        "📍 تحديد الموقع (LOCATE)" to "LOCATE",
        "🚨 تشغيل الإنذار (SCREAM)" to "SCREAM",
        "🛑 إيقاف الإنذار (STOP_SCREAM)" to "STOP_SCREAM",
        "⚠️ وضع السرقة الشامل (STOLEN)" to "STOLEN_MODE",
        "✅ إلغاء وضع السرقة (FOUND)" to "FOUND_MODE",
        "🔒 قفل الشاشة قسرياً (LOCK)" to "LOCK",
        "🌐 تفعيل بيانات الهاتف (ENABLE_NET)" to "ENABLE_NET",
        "⚡ تتبع مستمر لحظي (TRACK)" to "CONTINUOUS_TRACK"
    )
    
    var selectedCommand by remember { mutableStateOf(commandOptions[0]) }
    var commandExpanded by remember { mutableStateOf(false) }

    var isBleScanning by remember { mutableStateOf(false) }
    
    var sims by remember { mutableStateOf<List<SubscriptionInfo>>(emptyList()) }
    var selectedSimId by remember { mutableStateOf(-1) }
    var simExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchDevices()
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                val subManager = context.getSystemService(SubscriptionManager::class.java)
                val activeSims = subManager.activeSubscriptionInfoList ?: emptyList()
                sims = activeSims
                if (activeSims.isNotEmpty()) {
                    selectedSimId = activeSims[0].subscriptionId
                }
            }
        } catch (e: Exception) {}
    }

    LaunchedEffect(devices) {
        if (devices.isNotEmpty() && selectedTargetDevice == null) {
            val nonSelf = devices.firstOrNull { it.device_uid != uid }
            selectedTargetDevice = nonSelf ?: devices.first()
            if (nonSelf?.phone_number != null) {
                targetPhone = nonSelf.phone_number
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("لوحة تحكم المالك", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary),
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            BatteryOptimizationWarning()
            AutoStartWarning(prefs)
            
            val currentDevice = devices.find { it.device_uid == uid }
            
            // 1. Current Device Card
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("📱 هذا الجهاز (الجهاز الحالي)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Spacer(Modifier.height(8.dp))
                    Text("الموديل: ${currentDevice?.device_model ?: android.os.Build.MODEL}")
                    Text("البطارية: ${currentDevice?.battery_level ?: "--"}%")
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("حالة الحماية: " + (if (isRunning) "نشطة" else "معطلة"), fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = isRunning,
                            onCheckedChange = { checked ->
                                if (checked) ZexForegroundService.startService(context)
                                else ZexForegroundService.stopService(context)
                                isRunning = checked
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            
            // BLE Radar Card
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📡 رادار ZEX الميداني", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text("البحث عن الأجهزة القريبة غير المتصلة بالإنترنت عبر البلوتوث.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 12.dp))
                    Button(
                        onClick = {
                            val targetName = selectedTargetDevice?.device_name ?: "الجهاز المفقود"
                            navController.navigate("radar/${Uri.encode(targetName)}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("📡 فتح رادار البلوتوث الميداني (BLE Radar)")
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 2. Remote Target Card
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🎯 التحكم بالجهاز المستهدف", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    
                    if (devices.size > 1) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedTargetDevice?.device_name ?: "اختر جهازك",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                label = { Text("أجهزتك المسجلة") }
                            )
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                devices.filter { it.device_uid != uid }.forEach { device ->
                                    DropdownMenuItem(
                                        text = { Text("${device.device_name} - ${device.device_model}") },
                                        onClick = {
                                            selectedTargetDevice = device
                                            targetPhone = device.phone_number ?: ""
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = targetPhone,
                        onValueChange = { targetPhone = it },
                        label = { Text("الجهاز المستهدف (رقم الهاتف)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = commandExpanded,
                        onExpandedChange = { commandExpanded = !commandExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCommand.first,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = commandExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            label = { Text("اختر الأمر") }
                        )
                        ExposedDropdownMenu(
                            expanded = commandExpanded,
                            onDismissRequest = { commandExpanded = false }
                        ) {
                            commandOptions.forEach { cmd ->
                                DropdownMenuItem(
                                    text = { Text(cmd.first) },
                                    onClick = {
                                        selectedCommand = cmd
                                        commandExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (sims.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        ExposedDropdownMenuBox(
                            expanded = simExpanded,
                            onExpandedChange = { simExpanded = !simExpanded }
                        ) {
                            val selectedSim = sims.find { it.subscriptionId == selectedSimId }
                            val subManager = context.getSystemService(android.telephony.SubscriptionManager::class.java)
                            val selectedNumber = if (android.os.Build.VERSION.SDK_INT >= 33 && selectedSim != null) {
                                subManager.getPhoneNumber(selectedSimId)
                            } else {
                                @Suppress("DEPRECATION")
                                selectedSim?.number
                            }
                            val selectedSimName = selectedSim?.displayName?.toString() ?: "اختر الشريحة"
                            val selectedText = if (!selectedNumber.isNullOrBlank()) "$selectedSimName ($selectedNumber)" else selectedSimName

                            OutlinedTextField(
                                value = selectedText,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = simExpanded) },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                label = { Text("الشريحة المرسلة") }
                            )
                            ExposedDropdownMenu(
                                expanded = simExpanded,
                                onDismissRequest = { simExpanded = false }
                            ) {
                                sims.forEach { sim ->
                                    val number = if (android.os.Build.VERSION.SDK_INT >= 33) {
                                        subManager.getPhoneNumber(sim.subscriptionId)
                                    } else {
                                        @Suppress("DEPRECATION")
                                        sim.number
                                    }
                                    val name = sim.displayName?.toString() ?: "شريحة ${sim.subscriptionId}"
                                    val text = if (!number.isNullOrBlank()) "$name ($number)" else name
                                    DropdownMenuItem(
                                        text = { Text(text) },
                                        onClick = {
                                            selectedSimId = sim.subscriptionId
                                            simExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            var cleanPhone = targetPhone.replace(" ", "")
                            if (cleanPhone.startsWith("01")) {
                                cleanPhone = "+20${cleanPhone.substring(1)}"
                            }
                            if (cleanPhone.isNotBlank()) {
                                val message = "#ZEX#357005#${selectedCommand.second}"
                                try {
                                    val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        if (selectedSimId != -1) context.getSystemService(SmsManager::class.java).createForSubscriptionId(selectedSimId)
                                        else context.getSystemService(SmsManager::class.java)
                                    } else {
                                        if (selectedSimId != -1) SmsManager.getSmsManagerForSubscriptionId(selectedSimId)
                                        else SmsManager.getDefault()
                                    }
                                    smsManager.sendTextMessage(cleanPhone, null, message, null, null)
                                    Toast.makeText(context, "محاولة الإرسال في الخلفية تمت...", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    // Fallback to official messaging app
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$cleanPhone")).apply {
                                        putExtra("sms_body", message)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إرسال أمر SMS طوارئ")
                    }
                }
            }
        }
    }
}


@Composable
fun BatteryOptimizationWarning() {
    val context = LocalContext.current
    val powerManager = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
    val isIgnoring = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        powerManager.isIgnoringBatteryOptimizations(context.packageName)
    } else true

    if (!isIgnoring) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("تنبيه خطير: تحسين البطارية", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(Modifier.height(8.dp))
                Text("نظام أندرويد قد يقتل التطبيق في الخلفية ويوقف تتبع اللوكيشن. يرجى إعفاء التطبيق من قيود البطارية.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    try {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = android.net.Uri.parse("package:${context.packageName}")
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(context, "لا يمكن فتح الإعدادات تلقائياً", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("إعفاء من القيود")
                }
            }
        }
    }
}

@Composable
fun AutoStartWarning(prefs: SecurePrefs) {
    val context = LocalContext.current
    var isDismissed by remember { mutableStateOf(prefs.getBoolean("auto_start_dismissed", false)) }

    if (!isDismissed && com.zex.tracker.utils.AutoStartUtils.isAutoStartRestrictiveDevice()) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("التشغيل التلقائي مطلوب", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    IconButton(onClick = {
                        prefs.putBoolean("auto_start_dismissed", true)
                        isDismissed = true
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text("لضمان عمل التتبع بعد إعادة تشغيل الهاتف، يرجى تفعيل (التشغيل التلقائي) للتطبيق في الإعدادات. إذا كنت قد قمت بتفعيله بالفعل، يمكنك تجاهل هذه الرسالة.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    com.zex.tracker.utils.AutoStartUtils.openAutoStartSettings(context)
                }) {
                    Text("إعدادات التشغيل التلقائي")
                }
            }
        }
    }
}




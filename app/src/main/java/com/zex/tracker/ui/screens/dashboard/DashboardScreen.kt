package com.zex.tracker.ui.screens.dashboard

import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.data.remote.dto.DeviceDto
import com.zex.tracker.service.ServiceController
import com.zex.tracker.service.ZexForegroundService
import kotlinx.coroutines.launch

import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(prefs: SecurePrefs, viewModel: DashboardViewModel = hiltViewModel()) {
    val uid = prefs.getString(ZexConstants.KEY_DEVICE_UID) ?: "Unknown UID"
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    var isRunning by remember { mutableStateOf(ZexForegroundService.isRunning) }
    val devices by viewModel.devices.collectAsState()
    var selectedDevice by remember { mutableStateOf<DeviceDto?>(null) }
    var expanded by remember { mutableStateOf(false) }

    // SMS Tools State
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

    LaunchedEffect(Unit) {
        viewModel.fetchDevices()
    }

    LaunchedEffect(devices) {
        if (devices.isNotEmpty() && selectedDevice == null) {
            selectedDevice = devices.find { it.device_uid == uid } ?: devices[0]
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("لوحة تحكم المالك", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary, titleContentColor = MaterialTheme.colorScheme.onPrimary)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Device Selector
            if (devices.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedDevice?.device_name ?: "اختر الجهاز",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        label = { Text("الجهاز المحدد") }
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        devices.forEach { device ->
                            DropdownMenuItem(
                                text = { Text(device.device_name) },
                                onClick = {
                                    selectedDevice = device
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Map Button
            Button(
                onClick = {
                    val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q="))
                    context.startActivity(mapIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("فتح خرائط جوجل")
            }

            Spacer(Modifier.height(24.dp))

            // Protection Status (Local Device Only)
            if (selectedDevice?.device_uid == uid) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("حماية الجهاز الحالي", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isRunning) "نشط" else "معطل")
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
            }

            Spacer(Modifier.height(24.dp))

            // Offline SMS Toolkit
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("أدوات طوارئ SMS بدون إنترنت", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("إرسال أوامر عبر رسائل SMS للتحكم بالجهاز بدون إنترنت.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 16.dp))

                    OutlinedTextField(
                        value = targetPhone,
                        onValueChange = { targetPhone = it },
                        label = { Text("الجهاز المستهدف (رقم الهاتف)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = commandExpanded,
                        onExpandedChange = { commandExpanded = !commandExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCommand.first,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = commandExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
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

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (targetPhone.isNotBlank()) {
                                try {
                                    val smsManager = context.getSystemService(SmsManager::class.java)
                                    val fallbackPin = "357005"
                                    val message = "#ZEX#$fallbackPin#${selectedCommand.second}"
                                    smsManager.sendTextMessage(targetPhone, null, message, null, null)
                                } catch (e: Exception) {
                                    e.printStackTrace()
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

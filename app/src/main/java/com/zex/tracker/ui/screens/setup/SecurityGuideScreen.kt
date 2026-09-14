package com.zex.tracker.ui.screens.setup

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.receiver.ZexDeviceAdminReceiver
import kotlinx.coroutines.launch

@Composable
fun SecurityGuideScreen(navController: NavController, prefs: SecurePrefs) {
    val context = LocalContext.current
    val bgColor = Color(0xFFF1F5F9)
    val cardColor = Color.White
    val primaryColor = Color(0xFF2563EB)
    val successColor = Color(0xFF10B981)
    
    var showPin by remember { mutableStateOf(false) }

    // State for permissions
    var hasLocation by remember { mutableStateOf(false) }
    var hasSms by remember { mutableStateOf(false) }
    var hasBle by remember { mutableStateOf(false) }
    var hasAdmin by remember { mutableStateOf(false) }
    var hasBattery by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        hasLocation = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        hasSms = results[Manifest.permission.RECEIVE_SMS] == true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            hasBle = results[Manifest.permission.BLUETOOTH_SCAN] == true
        } else {
            hasBle = results[Manifest.permission.ACCESS_FINE_LOCATION] == true
        }
    }

    val adminLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, ZexDeviceAdminReceiver::class.java)
        hasAdmin = dpm.isAdminActive(adminComponent)
    }

    val batteryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        hasBattery = pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    // Check initial states
    LaunchedEffect(Unit) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context, ZexDeviceAdminReceiver::class.java)
        hasAdmin = dpm.isAdminActive(adminComponent)

        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        hasBattery = pm.isIgnoringBatteryOptimizations(context.packageName)

        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    val allDone = hasLocation && hasSms && hasBle && hasAdmin && hasBattery

    Scaffold(
        containerColor = bgColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            // Header
            Text(
                "SYSTEM HARDENING",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Verify Tactical Access",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A),
                lineHeight = 34.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "The node requires deep OS integration to guarantee 100% telemetry uptime and tamper resistance.",
                fontSize = 14.sp,
                color = Color(0xFF64748B),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CapabilityRow("Background Location", "GPS & Galileo telemetry", successColor, hasLocation) {
                        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        permissionLauncher.launch(permissions)
                    }
                    CapabilityRow("Offline SMS", "Encrypted command channel", successColor, hasSms) {
                        val permissions = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS)
                        permissionLauncher.launch(permissions)
                    }
                    CapabilityRow("BLE Radar", "Offline mesh discovery", successColor, hasBle) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            permissionLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_ADVERTISE, Manifest.permission.BLUETOOTH_CONNECT))
                        }
                    }
                    CapabilityRow("Device Admin", "Lockdown protection", successColor, hasAdmin) {
                        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                        val adminComponent = ComponentName(context, ZexDeviceAdminReceiver::class.java)
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Required for Remote Wipe and Screen Lock.")
                        adminLauncher.launch(intent)
                    }
                    CapabilityRow("Ignore Battery Limits", "Ensures 100% uptime", successColor, hasBattery, isLast = true) {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = Uri.parse("package:${context.packageName}")
                        batteryLauncher.launch(intent)
                    }
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
                enabled = allDone,
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor, disabledContainerColor = Color(0xFFCBD5E1)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if(allDone) "COMPLETE NODE BINDING" else "WAITING FOR PERMISSIONS", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun CapabilityRow(title: String, subtitle: String, successColor: Color, isGranted: Boolean, isLast: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isLast) 0.dp else 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0F172A))
                Text(subtitle, fontSize = 12.sp, color = Color(0xFF64748B))
            }
        }
        if (isGranted) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = successColor, modifier = Modifier.size(24.dp))
        } else {
            Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)), shape = RoundedCornerShape(8.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp), modifier = Modifier.height(32.dp)) {
                Text("FIX", fontSize = 12.sp)
            }
        }
    }
}

package com.zex.tracker.ui.screens.setup

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.core.constants.ZexConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityGuideScreen(navController: NavController, prefs: SecurePrefs) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("خطوات حماية الهاتف من السرقة 🛡️") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "لحماية الهاتف بشكل كامل ومنع السارق من إيقافه أو مسح بياناته، يرجى تفعيل إعدادات الأمان الرسمية التالية:",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 1. PIN for Power Off
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. قفل زر الطاقة (Power Button)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("يمنع أي شخص من إغلاق الموبايل إلا بعد إدخال الرمز السري (PIN). ابحث عن 'طلب كلمة السر للإيقاف'.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                        context.startActivity(intent)
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("فتح إعدادات الشاشة والقفل")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Google Theft Protection
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2. حماية Google من السرقة (Theft Protection)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("ميزة ذكية تقفل الشاشة فوراً إذا تم سحب الهاتف بقوة أو فُصلت عنه الشبكة.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        try {
                            val intent = Intent().apply {
                                setClassName("com.google.android.gms", "com.google.android.gms.setupservices.theftprotection.TheftProtectionSettingsActivity")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                            context.startActivity(intent)
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("فتح إعدادات Google للحماية")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Autostart & Battery
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("3. التشغيل التلقائي وإدارة البطارية", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("لمنع نظام الهاتف من إغلاق تطبيق التتبع في الخلفية (مهم جداً لأجهزة Xiaomi, Oppo).", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = {
                        com.zex.tracker.utils.AutoStartUtils.openAutoStartSettings(context)
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text("إعدادات التشغيل التلقائي")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    prefs.putBoolean(ZexConstants.KEY_IS_SETUP_COMPLETE, true)
                    com.zex.tracker.service.ZexForegroundService.startService(context)
                    navController.navigate("dashboard") {
                        popUpTo(0)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("إكمال الإعداد وفتح لوحة التحكم", fontWeight = FontWeight.Bold)
            }
        }
    }
}


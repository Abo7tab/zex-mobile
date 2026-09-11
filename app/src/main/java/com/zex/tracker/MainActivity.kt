package com.zex.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.ui.ZexNavHost
import com.zex.tracker.ui.theme.ZexTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var securePrefs: SecurePrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        
        ZexLogger.i("MainActivity", "onCreate started")

        setContent {
            ZexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ZexNavHost(prefs = securePrefs)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = com.zex.tracker.data.local.prefs.SecurePrefs(this)
        val deviceUid = prefs.getString(com.zex.tracker.core.constants.ZexConstants.KEY_DEVICE_UID)
        if (!deviceUid.isNullOrEmpty()) {
            com.zex.tracker.service.ZexForegroundService.startService(this)
            
            val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.zex.tracker.service.ZexWatchdogWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
                .build()
            androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "ZexWatchdogWorker",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
}

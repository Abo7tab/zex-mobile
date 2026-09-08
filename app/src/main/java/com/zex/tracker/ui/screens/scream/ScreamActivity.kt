package com.zex.tracker.ui.screens.scream

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import at.favre.lib.crypto.bcrypt.BCrypt
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.security.ScreamManager
import com.zex.tracker.service.ServiceController
import com.zex.tracker.core.logging.ZexLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ScreamActivity : ComponentActivity() {

    @Inject lateinit var screamManager: ScreamManager
    @Inject lateinit var prefs: SecurePrefs

    private val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "ACTION_STOP_SCREAM_AND_FINISH") {
                screamManager.stopScream()
                ServiceController.isScreaming = false
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screamManager.startScream()
        
        val filter = IntentFilter("ACTION_STOP_SCREAM_AND_FINISH")
        androidx.core.content.ContextCompat.registerReceiver(this, stopReceiver, filter, androidx.core.content.ContextCompat.RECEIVER_NOT_EXPORTED)

        setContent {
            var pinInput by remember { mutableStateOf("") }
            var error by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier.fillMaxSize().background(Color.Red).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("DEVICE MARKED AS STOLEN", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(32.dp))
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it },
                    label = { Text("Enter Password/PIN to stop") },
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
                if (error) Text("Incorrect Password", color = Color.Yellow)
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val storedHash = prefs.getString("owner_password_hash")
                        if (!storedHash.isNullOrEmpty()) {
                            val verified = BCrypt.verifyer().verify(pinInput.toCharArray(), storedHash.toByteArray()).verified
                            if (verified) {
                                screamManager.stopScream()
                                ServiceController.isScreaming = false
                                finish()
                            } else {
                                error = true
                            }
                        } else {
                            // API fallback not fully needed if we have hash, but we gracefully log
                            ZexLogger.w("ScreamActivity", "No stored hash available to verify.")
                            error = true
                        }
                    }
                }) {
                    Text("STOP")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(stopReceiver) } catch (e: Exception) {}
    }

    override fun onBackPressed() {
        // block back
    }
}

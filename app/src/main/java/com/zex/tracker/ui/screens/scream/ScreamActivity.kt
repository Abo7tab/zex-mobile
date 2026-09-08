package com.zex.tracker.ui.screens.scream

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
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.security.ScreamManager
import com.zex.tracker.service.ServiceController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScreamActivity : ComponentActivity() {

    @Inject lateinit var screamManager: ScreamManager
    @Inject lateinit var prefs: SecurePrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        screamManager.startScream()
        
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
                    label = { Text("Enter PIN to stop") },
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
                if (error) Text("Incorrect PIN", color = Color.Yellow)
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    val savedPin = prefs.getString(ZexConstants.KEY_PIN_CODE)
                    if (pinInput == savedPin) {
                        screamManager.stopScream()
                        ServiceController.isScreaming = false
                        finish()
                    } else {
                        error = true
                    }
                }) {
                    Text("STOP")
                }
            }
        }
    }

    override fun onBackPressed() {
        // block back
    }
}

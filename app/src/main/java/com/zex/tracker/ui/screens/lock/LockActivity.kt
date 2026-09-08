package com.zex.tracker.ui.screens.lock

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
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LockActivity : ComponentActivity() {

    @Inject lateinit var prefs: SecurePrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var pinInput by remember { mutableStateOf("") }
            
            Column(
                modifier = Modifier.fillMaxSize().background(Color.Black).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("LOCKED BY OWNER", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(32.dp))
                OutlinedTextField(
                    value = pinInput,
                    onValueChange = { pinInput = it },
                    label = { Text("Enter PIN to unlock") },
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    val savedPin = prefs.getString(ZexConstants.KEY_PIN_CODE)
                    if (pinInput == savedPin) {
                        finish()
                    }
                }) {
                    Text("UNLOCK")
                }
            }
        }
    }
    
    override fun onBackPressed() {}
}

package com.zex.tracker

import android.os.Build
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.ui.theme.ZexTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * MainActivity
 * Entry point for ZEX App.
 * Displays initial health checks for API and Firebase.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
                    HomeScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    var apiHealthOk by remember { mutableStateOf<Boolean?>(null) }
    var firebaseHealthOk by remember { mutableStateOf<Boolean?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            apiHealthOk = checkApiHealth()
            firebaseHealthOk = checkFirebaseHealth()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(text = "Version: ${BuildConfig.VERSION_NAME}")
        Text(text = "Android SDK: ${Build.VERSION.SDK_INT}")
        
        Spacer(modifier = Modifier.height(32.dp))
        
        HealthIndicator("API Backend", apiHealthOk)
        Spacer(modifier = Modifier.height(8.dp))
        HealthIndicator("Firebase RTDB", firebaseHealthOk)
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = { 
                ZexLogger.i("HomeScreen", "Start Setup clicked")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.start_setup))
        }
    }
}

@Composable
fun HealthIndicator(name: String, isHealthy: Boolean?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(0.7f)
    ) {
        Text(text = name, fontWeight = FontWeight.Medium)
        
        val color = when (isHealthy) {
            true -> Color.Green
            false -> Color.Red
            null -> Color.Gray
        }
        
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color = color, shape = androidx.compose.foundation.shape.CircleShape)
        )
    }
}

/**
 * Perform a simple GET request to check API health.
 */
suspend fun checkApiHealth(): Boolean = withContext(Dispatchers.IO) {
    try {
        ZexLogger.i("HealthCheck", "Checking API Health...")
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
            
        // Using the base URL to check if the server is reachable
        val request = Request.Builder()
            .url(BuildConfig.API_BASE_URL)
            .build()
            
        val response = client.newCall(request).execute()
        val isSuccess = response.isSuccessful || response.code == 404 // 404 means server is up but no root route
        ZexLogger.i("HealthCheck", "API Health result: $isSuccess, code: ${response.code}")
        isSuccess
    } catch (e: Exception) {
        ZexLogger.e("HealthCheck", "API Health check failed", e)
        false
    }
}

/**
 * Perform a simple unauthenticated GET to Firebase DB to check reachability.
 */
suspend fun checkFirebaseHealth(): Boolean = withContext(Dispatchers.IO) {
    try {
        ZexLogger.i("HealthCheck", "Checking Firebase Health...")
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
            
        val request = Request.Builder()
            .url("${BuildConfig.FIREBASE_DB_URL}/.json")
            .build()
            
        val response = client.newCall(request).execute()
        // Unauthenticated access returns 401, which is fine (means reachable)
        val isSuccess = response.isSuccessful || response.code == 401
        ZexLogger.i("HealthCheck", "Firebase Health result: $isSuccess, code: ${response.code}")
        isSuccess
    } catch (e: Exception) {
        ZexLogger.e("HealthCheck", "Firebase Health check failed", e)
        false
    }
}

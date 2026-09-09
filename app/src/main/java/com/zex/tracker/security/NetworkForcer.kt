package com.zex.tracker.security

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import com.zex.tracker.core.logging.ZexLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkForcer @Inject constructor(@ApplicationContext private val context: Context) {
    fun forceNetwork() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                if (wifiManager?.isWifiEnabled == false) {
                    wifiManager.isWifiEnabled = true
                    ZexLogger.i("NetworkForcer", "Enabled WiFi via legacy API")
                }
            } else {
                ZexLogger.w("NetworkForcer", "Cannot force WiFi on Android 10+. Requesting via ConnectivityManager.")
                val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val request = NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                cm.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {})
            }
        } catch (e: Exception) {
            ZexLogger.e("NetworkForcer", "Failed to force network", e)
        }
    }

    fun releaseNetwork() {
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                if (wifiManager?.isWifiEnabled == true) {
                    wifiManager.isWifiEnabled = false
                    ZexLogger.i("NetworkForcer", "Disabled WiFi via legacy API")
                }
            } else {
                ZexLogger.i("NetworkForcer", "Best-effort network release on Android 10+")
            }
        } catch (e: Exception) {
            ZexLogger.e("NetworkForcer", "Failed to release network", e)
        }
    }
}

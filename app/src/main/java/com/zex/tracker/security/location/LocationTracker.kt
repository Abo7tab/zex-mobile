package com.zex.tracker.security.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.zex.tracker.core.logging.ZexLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationTracker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(context)
    
    private var locationCallback: LocationCallback? = null

    suspend fun getCurrentLocation(): Location? {
        if (!hasLocationPermission()) {
            ZexLogger.w("LocationTracker", "Location permission denied. Failing gracefully.")
            return null
        }
        return try {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
        } catch (e: SecurityException) {
            ZexLogger.e("LocationTracker", "SecurityException getting location", e)
            null
        } catch (e: Exception) {
            ZexLogger.e("LocationTracker", "Failed to get location", e)
            null
        }
    }

    fun startContinuous(intervalMs: Long, onLocation: (Location) -> Unit) {
        if (!hasLocationPermission()) {
            ZexLogger.w("LocationTracker", "Location permission denied. Cannot start continuous.")
            return
        }
        try {
            // Remove existing to prevent leak/race
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
            
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
                .setMinUpdateIntervalMillis(intervalMs / 2)
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let(onLocation)
                }
            }

            fusedLocationClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
            ZexLogger.i("LocationTracker", "Started continuous tracking at ${intervalMs}ms")
        } catch (e: SecurityException) {
            ZexLogger.e("LocationTracker", "SecurityException in startContinuous", e)
        }
    }

    fun stopContinuous() {
        try {
            locationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
            locationCallback = null
            ZexLogger.i("LocationTracker", "Stopped continuous tracking")
        } catch (e: Exception) {
            ZexLogger.e("LocationTracker", "Error stopping continuous", e)
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}

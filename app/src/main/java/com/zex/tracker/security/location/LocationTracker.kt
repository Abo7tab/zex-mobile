package com.zex.tracker.security.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Looper
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
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var locationCallback: LocationCallback? = null

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
            if (location != null) {
                ZexLogger.d("LocationTracker", "Fused location: ${location.provider} - acc: ${location.accuracy}")
                location
            } else {
                fallbackLocation()
            }
        } catch (e: Exception) {
            ZexLogger.e("LocationTracker", "Fused location failed", e)
            fallbackLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fallbackLocation(): Location? {
        return try {
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }
            if (bestLocation != null) {
                ZexLogger.d("LocationTracker", "Fallback location: ${bestLocation.provider} - acc: ${bestLocation.accuracy}")
            }
            bestLocation
        } catch (e: Exception) {
            ZexLogger.e("LocationTracker", "Fallback location failed", e)
            null
        }
    }

    @SuppressLint("MissingPermission")
    fun startContinuous(intervalMs: Long, onLocation: (Location) -> Unit) {
        stopContinuous()
        try {
            val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs).build()
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { onLocation(it) }
                }
            }
            fusedLocationClient.requestLocationUpdates(request, locationCallback!!, Looper.getMainLooper())
            ZexLogger.i("LocationTracker", "Continuous tracking started (${intervalMs}ms)")
        } catch (e: Exception) {
            ZexLogger.e("LocationTracker", "Failed to start continuous tracking", e)
        }
    }

    fun stopContinuous() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            locationCallback = null
            ZexLogger.i("LocationTracker", "Continuous tracking stopped")
        }
    }
}


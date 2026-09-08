package com.zex.tracker.security

import android.content.Context
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.data.remote.ApiResult
import com.zex.tracker.data.repository.DeviceRepository
import com.zex.tracker.service.ServiceController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchModeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: SecurePrefs,
    private val deviceRepo: DeviceRepository,
    private val networkForcer: NetworkForcer,
    private val serviceController: ServiceController
) {
    suspend fun checkOwnerSearching(): Boolean {
        prefs.putLong("lastSearchCheckAt", System.currentTimeMillis())
        val result = deviceRepo.sendHeartbeat()
        if (result is ApiResult.Success) {
            val payload = result.data
            syncSearchState(payload.owner_is_searching, payload.search_interval_seconds)
            return payload.owner_is_searching
        } else {
            // Fallback status check
            val statusResult = deviceRepo.getDeviceStatus()
            if (statusResult is ApiResult.Success) {
                val status = statusResult.data
                syncSearchState(status.is_searching, status.search_interval_seconds)
                return status.is_searching
            }
        }
        return prefs.getBoolean("isSearching", false)
    }

    private fun syncSearchState(isSearching: Boolean, interval: Int) {
        prefs.putBoolean("isSearching", isSearching)
        prefs.putInt("searchIntervalSeconds", interval)
        if (isSearching) {
            enterSearchMode("heartbeat_sync", interval)
        } else {
            // only exit if not stolen
            if (!prefs.getBoolean("isStolen", false)) {
                exitSearchMode("heartbeat_sync")
            }
        }
    }

    fun enterSearchMode(reason: String, intervalSeconds: Int = 30) {
        ZexLogger.i("SearchMode", "Entering search mode. Reason: ${reason}")
        prefs.putBoolean("isSearching", true)
        prefs.putInt("searchIntervalSeconds", intervalSeconds)
        networkForcer.forceNetwork()

        ServiceController.isTracking = true
        ServiceController.trackingInterval = intervalSeconds * 1000L
        ServiceController.isSearching = true
        serviceController.startProtection() // Refresh service
    }

    fun exitSearchMode(reason: String) {
        ZexLogger.i("SearchMode", "Exiting search mode. Reason: ${reason}")
        prefs.putBoolean("isSearching", false)
        ServiceController.isSearching = false
        
        if (!prefs.getBoolean("isStolen", false)) {
            ServiceController.isTracking = false
        }
        serviceController.startProtection() // Refresh service notification
    }
}

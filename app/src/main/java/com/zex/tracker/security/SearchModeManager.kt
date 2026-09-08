package com.zex.tracker.security

import android.content.Context
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.data.repository.DeviceRepository
import com.zex.tracker.service.ServiceController
import com.zex.tracker.data.remote.ApiResult
import com.zex.tracker.data.remote.dto.HeartbeatResponsePayload
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchModeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val deviceRepo: DeviceRepository,
    private val prefs: SecurePrefs,
    private val networkForcer: NetworkForcer
) {
    fun enterSearchMode(reason: String, intervalSeconds: Int) {
        ZexLogger.i("SearchModeManager", "Entering search mode: $reason")
        ServiceController.isSearching = true
        ServiceController.trackingInterval = (intervalSeconds * 1000).toLong()
        ServiceController(context).startProtection()
    }

    fun exitSearchMode(reason: String) {
        ZexLogger.i("SearchModeManager", "Exiting search mode: $reason")
        ServiceController.isSearching = false
        ServiceController.trackingInterval = 15 * 60 * 1000L
        ServiceController(context).startProtection()
    }

    suspend fun checkOwnerSearching(): HeartbeatResponsePayload? {
        networkForcer.forceNetwork()
        val result = deviceRepo.sendHeartbeat()
        if (result is ApiResult.Success) {
            val payload = result.data
            prefs.putLong("lastSearchCheckAt", System.currentTimeMillis())
            

            
            if (payload.owner_is_searching) {
                if (!ServiceController.isSearching) {
                    enterSearchMode("owner_is_searching", payload.search_interval_seconds)
                } else if (ServiceController.trackingInterval != (payload.search_interval_seconds * 1000).toLong()) {
                    enterSearchMode("interval_updated", payload.search_interval_seconds)
                }
            } else {
                if (ServiceController.isSearching && !ServiceController.isStolen) {
                    exitSearchMode("owner_stopped_searching")
                }
            }
            return payload
        }
        return null
    }
}

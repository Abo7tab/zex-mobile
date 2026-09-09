package com.zex.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.security.Scheduler
import com.zex.tracker.security.SearchModeManager
import com.zex.tracker.security.NetworkForcer
import com.zex.tracker.security.location.LocationTracker
import com.zex.tracker.data.repository.DeviceRepository
import com.zex.tracker.data.remote.ApiResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HourlyAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var searchModeManager: SearchModeManager
    @Inject lateinit var scheduler: Scheduler
    @Inject lateinit var networkForcer: NetworkForcer
    @Inject lateinit var locationTracker: LocationTracker
    @Inject lateinit var deviceRepo: DeviceRepository

    override fun onReceive(context: Context, intent: Intent) {
        ZexLogger.i("HourlyAlarmReceiver", "Waking up to perform search mode check")
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // a) Force network ON
                networkForcer.forceNetwork()
                
                // b) Fetch live location and dispatch
                val location = locationTracker.getCurrentLocation()
                if (location != null) {
                    deviceRepo.sendLocation(location)
                }

                // c) Dispatch sendHeartbeat
                val hbResult = deviceRepo.sendHeartbeat()
                
                // d) If no commands and not searching/stolen, release network
                if (hbResult is ApiResult.Success) {
                    val pending = hbResult.data.pending_commands ?: emptyList()
                    val isSearching = hbResult.data.owner_is_searching
                    val isStolen = com.zex.tracker.service.ServiceController.isStolen
                    
                    if (isSearching) {
                        searchModeManager.enterSearchMode("hourly_sync", hbResult.data.search_interval_seconds)
                    }

                    if (pending.isEmpty() && !isSearching && !isStolen) {
                        networkForcer.releaseNetwork()
                    }
                }

                searchModeManager.checkOwnerSearching()
            } catch (e: Exception) {
                ZexLogger.e("HourlyAlarmReceiver", "Failed search check", e)
            } finally {
                scheduler.scheduleHourlyChecks() // reschedule next alarm
                pendingResult.finish()
            }
        }
    }
}

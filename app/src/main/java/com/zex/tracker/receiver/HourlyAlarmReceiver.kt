package com.zex.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.security.NetworkForcer
import com.zex.tracker.security.SearchModeManager
import com.zex.tracker.security.Scheduler
import com.zex.tracker.security.location.LocationTracker
import com.zex.tracker.data.repository.DeviceRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HourlyAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var searchModeManager: SearchModeManager
    @Inject lateinit var networkForcer: NetworkForcer
    @Inject lateinit var locationTracker: LocationTracker
    @Inject lateinit var deviceRepo: DeviceRepository
    @Inject lateinit var scheduler: Scheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.zex.tracker.RESCHEDULE_ALARM") {
            scheduler.scheduleHourlyChecks()
            return
        }

        ZexLogger.i("HourlyAlarmReceiver", "Woke up via AlarmManager")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                networkForcer.forceNetwork()
                val isSearching = searchModeManager.checkOwnerSearching()
                if (!isSearching) {
                    val loc = locationTracker.getCurrentLocation()
                    if (loc != null) deviceRepo.sendLocation(loc)
                }
            } catch (e: Exception) {
                ZexLogger.e("HourlyAlarmReceiver", "Hourly check failed", e)
            } finally {
                scheduler.scheduleHourlyChecks()
            }
        }
    }
}

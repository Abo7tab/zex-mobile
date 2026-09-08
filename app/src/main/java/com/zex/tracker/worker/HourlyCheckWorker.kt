package com.zex.tracker.worker

import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.security.NetworkForcer
import com.zex.tracker.security.SearchModeManager
import com.zex.tracker.security.location.LocationTracker
import com.zex.tracker.data.repository.DeviceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HourlyCheckWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val searchModeManager: SearchModeManager,
    private val networkForcer: NetworkForcer,
    private val locationTracker: LocationTracker,
    private val deviceRepo: DeviceRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        ZexLogger.i("HourlyCheckWorker", "Running hourly check")
        networkForcer.forceNetwork()
        val isSearching = searchModeManager.checkOwnerSearching()?.owner_is_searching ?: false

        if (!isSearching) {
            ZexLogger.i("HourlyCheckWorker", "Not searching. Taking snapshot.")
            val loc = locationTracker.getCurrentLocation()
            if (loc != null) deviceRepo.sendLocation(loc)
        }
        
        // Re-schedule exact alarm
        context.sendBroadcast(Intent("com.zex.tracker.RESCHEDULE_ALARM"))
        return Result.success()
    }
}

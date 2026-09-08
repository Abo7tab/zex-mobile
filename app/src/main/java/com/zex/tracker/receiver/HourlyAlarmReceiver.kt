package com.zex.tracker.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.security.Scheduler
import com.zex.tracker.security.SearchModeManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HourlyAlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var searchModeManager: SearchModeManager
    @Inject lateinit var scheduler: Scheduler

    override fun onReceive(context: Context, intent: Intent) {
        ZexLogger.i("HourlyAlarmReceiver", "Waking up to perform search mode check")
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
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

package com.zex.tracker.security

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.receiver.HourlyAlarmReceiver
import com.zex.tracker.worker.HourlyCheckWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Scheduler @Inject constructor(@dagger.hilt.android.qualifiers.ApplicationContext private val context: Context) {

    fun scheduleHourlyChecks() {
        scheduleAlarmManager()
        scheduleWorkManager()
    }

    private fun scheduleAlarmManager() {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, HourlyAlarmReceiver::class.java)
            val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
            val pendingIntent = PendingIntent.getBroadcast(context, 1001, intent, flags)

            val triggerTime = System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                ZexLogger.w("Scheduler", "Cannot schedule exact alarms, using inexact.")
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, triggerTime, AlarmManager.INTERVAL_HOUR, pendingIntent)
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
                ZexLogger.i("Scheduler", "Scheduled exact hourly alarm.")
            }
        } catch (e: Exception) {
            ZexLogger.e("Scheduler", "Failed scheduling alarm", e)
        }
    }

    private fun scheduleWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<HourlyCheckWorker>(1, TimeUnit.HOURS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "HourlyCheckWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        ZexLogger.i("Scheduler", "Scheduled WorkManager hourly check.")
    }
}

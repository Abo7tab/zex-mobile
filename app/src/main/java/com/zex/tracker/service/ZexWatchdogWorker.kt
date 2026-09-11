package com.zex.tracker.service

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zex.tracker.core.logging.ZexLogger
import android.content.Intent

class ZexWatchdogWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        try {
            ZexLogger.d("ZexWatchdogWorker", "Watchdog checking ZexForegroundService status...")
            if (!ZexForegroundService.isRunning) {
                ZexLogger.w("ZexWatchdogWorker", "Service is DOWN! Attempting to restart...")
                val serviceIntent = Intent(context, ZexForegroundService::class.java)
                ContextCompat.startForegroundService(context, serviceIntent)
            } else {
                ZexLogger.d("ZexWatchdogWorker", "Service is RUNNING normally.")
            }
        } catch (e: Exception) {
            ZexLogger.e("ZexWatchdogWorker", "Error in watchdog worker", e)
        }
        return Result.success()
    }
}

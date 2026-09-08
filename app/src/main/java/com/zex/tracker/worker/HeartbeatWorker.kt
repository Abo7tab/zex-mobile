package com.zex.tracker.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.repository.DeviceRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class HeartbeatWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val deviceRepo: DeviceRepository
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            ZexLogger.i("HeartbeatWorker", "Running scheduled heartbeat")
            deviceRepo.sendHeartbeat()
            Result.success()
        } catch (e: Exception) {
            ZexLogger.e("HeartbeatWorker", "Heartbeat failed", e)
            Result.retry()
        }
    }
}

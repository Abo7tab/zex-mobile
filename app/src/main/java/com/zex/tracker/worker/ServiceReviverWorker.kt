package com.zex.tracker.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.service.ServiceController

class ServiceReviverWorker(
    private val context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    override fun doWork(): Result {
        ZexLogger.i("ServiceReviverWorker", "Checking if service is alive")
        val controller = ServiceController(context)
        controller.startProtection()
        return Result.success()
    }
}

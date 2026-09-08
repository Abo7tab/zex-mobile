package com.zex.tracker.service

import android.content.Context
import android.content.Intent
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.repository.DeviceRepository
import com.zex.tracker.domain.model.Command
import com.zex.tracker.domain.model.CommandType
import com.zex.tracker.security.LockManager
import com.zex.tracker.security.NetworkForcer
import com.zex.tracker.security.ScreamManager
import com.zex.tracker.security.location.LocationTracker
import com.zex.tracker.ui.screens.scream.ScreamActivity
import com.zex.tracker.ui.screens.lock.LockActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandProcessor @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationTracker: LocationTracker,
    private val deviceRepo: DeviceRepository,
    private val networkForcer: NetworkForcer,
    private val screamManager: ScreamManager,
    private val lockManager: LockManager,
    private val serviceController: ServiceController
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun process(command: Command) {
        ZexLogger.i("CommandProcessor", "Processing command: ${command.type} (ID: ${command.id})")
        scope.launch {
            try {
                when (command.type) {
                    CommandType.LOCATE -> handleLocate()
                    CommandType.CONTINUOUS_TRACK -> handleStartTracking(command.parameters)
                    CommandType.STOP_TRACKING -> handleStopTracking()
                    CommandType.SCREAM -> handleScream()
                    CommandType.STOP_SCREAM -> handleStopScream()
                    CommandType.LOCK -> handleLock()
                    CommandType.ENABLE_NET -> networkForcer.forceNetwork()
                    CommandType.STOLEN_MODE -> handleStolenMode()
                    CommandType.FOUND_MODE -> handleFoundMode()
                    CommandType.STATUS -> handleStatus()
                    CommandType.PHOTO -> ZexLogger.w("CommandProcessor", "PHOTO ignored by rule")
                }
                // Respond to backend
                deviceRepo.sendCommandResponse(command.id, "EXECUTED")
            } catch (e: Exception) {
                ZexLogger.e("CommandProcessor", "Failed executing ${command.type}", e)
                deviceRepo.sendCommandResponse(command.id, "FAILED")
            }
        }
    }

    private suspend fun handleLocate() {
        val loc = locationTracker.getCurrentLocation()
        if (loc != null) {
            deviceRepo.sendLocation(loc)
        }
    }

    private fun handleStartTracking(params: Map<String, String>?) {
        val interval = params?.get("interval")?.toLongOrNull() ?: 30000L
        ServiceController.trackingInterval = interval
        ServiceController.isTracking = true
        serviceController.startProtection()
    }

    private fun handleStopTracking() {
        ServiceController.isTracking = false
        locationTracker.stopContinuous()
    }

    private fun handleScream() {
        ServiceController.isScreaming = true
        val intent = Intent(context, ScreamActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)
    }

    private fun handleStopScream() {
        ServiceController.isScreaming = false
        screamManager.stopScream()
        // Wait for UI pin check naturally in real usage, but backend command stops immediately
    }

    private fun handleLock() {
        if (lockManager.isDeviceAdminActive()) {
            lockManager.lockNow()
        } else {
            val intent = Intent(context, LockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)
        }
    }

    private fun handleStolenMode() {
        ServiceController.isStolen = true
        networkForcer.forceNetwork()
        handleStartTracking(mapOf("interval" to "30000"))
        // Optional scream
    }

    private fun handleFoundMode() {
        ServiceController.isStolen = false
        handleStopTracking()
        handleStopScream()
    }

    private suspend fun handleStatus() {
        deviceRepo.sendHeartbeat()
        handleLocate()
    }
}


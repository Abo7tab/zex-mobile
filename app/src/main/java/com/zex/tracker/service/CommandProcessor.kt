package com.zex.tracker.service

import android.content.Context
import android.content.Intent
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.data.repository.DeviceRepository
import com.zex.tracker.domain.model.CommandType
import com.zex.tracker.data.remote.dto.CommandDto
import com.zex.tracker.security.LockManager
import com.zex.tracker.security.NetworkForcer
import com.zex.tracker.security.SearchModeManager
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
    private val searchModeManager: SearchModeManager,
    private val prefs: SecurePrefs,
    private val serviceController: ServiceController
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    suspend fun process(command: CommandDto) {
        ZexLogger.i("CommandProcessor", "Processing command: ${command.type}")
        try {
            when (command.type) {
                "LOCATE" -> handleLocate()
                "CONTINUOUS_TRACK" -> {
                    val interval = command.parameters?.get("interval")?.toIntOrNull() ?: 30
                    searchModeManager.enterSearchMode("command_track", interval)
                }
                "STOP_TRACKING", "STOP_SEARCH" -> {
                    searchModeManager.exitSearchMode("command_stop")
                    locationTracker.stopContinuous()
                    ServiceController.isSearching = false
                }
                "SCREAM" -> handleScream()
                "STOP_SCREAM" -> handleStopScream()
                "LOCK" -> handleLock()
                "ENABLE_NET" -> networkForcer.forceNetwork()
                "STOLEN_MODE" -> {
                    prefs.putBoolean("isStolen", true)
                    ServiceController.isStolen = true
                    searchModeManager.enterSearchMode("stolen_mode", 30)
                }
                "FOUND_MODE" -> {
                    prefs.putBoolean("isStolen", false)
                    ServiceController.isTracking = false
                    ServiceController.isStolen = false
                    ServiceController.isScreaming = false
                    ServiceController.isSearching = false
                    searchModeManager.exitSearchMode("found_mode")
                    handleStopScream()
                    locationTracker.stopContinuous()
                    context.sendBroadcast(Intent(com.zex.tracker.core.constants.ZexConstants.ACTION_STOP_SCREAM))
                }
                "STATUS" -> handleStatus()
                "PHOTO" -> ZexLogger.w("CommandProcessor", "PHOTO ignored by rule")
            }
            
            // Wait for response to be sent before completing process
            val res = deviceRepo.sendCommandResponse(command.id, "EXECUTED")
            if (res is com.zex.tracker.data.remote.ApiResult.Error) {
                throw Exception("Failed to send EXECUTED response to backend: ${res.message}")
            }
        } catch (e: Exception) {
            ZexLogger.e("CommandProcessor", "Failed executing ${command.type}", e)
            val res = deviceRepo.sendCommandResponse(command.id, "FAILED", mapOf("error" to (e.message ?: "Unknown")))
            if (res is com.zex.tracker.data.remote.ApiResult.Error) {
                throw Exception("Failed to send FAILED response to backend: ${res.message}", e)
            }
        }
    }

    private suspend fun handleLocate() {
        val loc = locationTracker.getCurrentLocation()
        if (loc != null) deviceRepo.sendLocation(loc)
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
        context.sendBroadcast(Intent(com.zex.tracker.core.constants.ZexConstants.ACTION_STOP_SCREAM))
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

    private suspend fun handleStatus() {
        searchModeManager.checkOwnerSearching()
        handleLocate()
    }
}

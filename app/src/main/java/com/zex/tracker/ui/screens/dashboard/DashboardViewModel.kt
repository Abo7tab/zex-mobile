package com.zex.tracker.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zex.tracker.data.remote.api.ZexApi
import com.zex.tracker.data.remote.dto.DeviceDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.zex.tracker.core.logging.LiveTerminalLogger

import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.data.local.prefs.SecurePrefs

@HiltViewModel
class DashboardViewModel @Inject constructor(
    val terminalLogger: LiveTerminalLogger,
    private val zexApi: ZexApi,
    private val prefs: SecurePrefs
) : ViewModel() {

    private val _devices = MutableStateFlow<List<DeviceDto>>(emptyList())
    val devices: StateFlow<List<DeviceDto>> = _devices

    private val _selectedDevice = MutableStateFlow<DeviceDto?>(null)
    val selectedDevice: StateFlow<DeviceDto?> = _selectedDevice

    private val _smsLogs = MutableStateFlow(
        prefs.getString(ZexConstants.KEY_SMS_DISPATCH_LOG)
            .orEmpty()
            .split("\\n")
            .filter { it.isNotBlank() }
    )
    val smsLogs: StateFlow<List<String>> = _smsLogs.asStateFlow()

    fun appendSmsLog(message: String) {
        val updated = (_smsLogs.value + message).takeLast(100)
        _smsLogs.value = updated
        prefs.putString(ZexConstants.KEY_SMS_DISPATCH_LOG, updated.joinToString("\\n"))
    }

    fun selectDevice(device: DeviceDto) {
        _selectedDevice.value = device
        prefs.putString("selected_target_uid", device.device_uid ?: "")
    }

    fun fetchDevices() {
        viewModelScope.launch {
            try {
                val response = zexApi.getOwnerDevices()
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        _devices.value = it
                        if (_selectedDevice.value == null && it.isNotEmpty()) {
                            val savedUid = prefs.getString("selected_target_uid", "")
                            _selectedDevice.value = it.find { d -> d.device_uid == savedUid } ?: it.first()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun pingDevice(targetHash: String = "") {
        viewModelScope.launch {
            terminalLogger.log("SYS//CMD > SEND_PING -> TARGET: $targetHash")
        }
    }
}



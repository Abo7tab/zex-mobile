package com.zex.tracker.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zex.tracker.security.BleMeshManager
import com.zex.tracker.data.remote.api.ZexApi
import com.zex.tracker.data.remote.dto.DeviceDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BleRadarViewModel @Inject constructor(
    private val bleMeshManager: BleMeshManager,
    private val api: ZexApi
) : ViewModel() {
    private val _devices = kotlinx.coroutines.flow.MutableStateFlow<List<DeviceDto>>(emptyList())
    val devices: StateFlow<List<DeviceDto>> = _devices
    val lastPeer: StateFlow<BleMeshManager.Peer?> = bleMeshManager.state
    fun setEnabled(enabled: Boolean) {
        if (enabled) bleMeshManager.startRadar() else bleMeshManager.stopRadar()
    }
    fun setTargetHash(value: String) = bleMeshManager.setTargetHash(value)
    fun fetchDevices() {
        viewModelScope.launch {
            runCatching { api.getOwnerDevices() }.getOrNull()?.let { response ->
                if (response.isSuccessful) _devices.value = response.body()?.data.orEmpty()
            }
        }
    }
    fun selectDevice(device: DeviceDto?) = bleMeshManager.setTargetHash(device?.device_uid.orEmpty())
}

package com.zex.tracker.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zex.tracker.data.remote.api.ZexApi
import com.zex.tracker.data.remote.dto.DeviceDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.zex.tracker.security.ble.ZexBleManager

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val zexApi: ZexApi,
    private val bleManager: ZexBleManager
) : ViewModel() {

    private val _devices = MutableStateFlow<List<DeviceDto>>(emptyList())
    val devices: StateFlow<List<DeviceDto>> = _devices

    fun fetchDevices() {
        viewModelScope.launch {
            try {
                val response = zexApi.getOwnerDevices()
                if (response.isSuccessful) {
                    response.body()?.data?.let {
                        _devices.value = it
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startBleScan() {
        try { bleManager.startScanning() } catch (e: Exception) {}
    }

    fun stopBleScan() {
        try { bleManager.stopScanning() } catch (e: Exception) {}
    }
}

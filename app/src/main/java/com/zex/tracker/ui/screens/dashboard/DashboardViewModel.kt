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

import com.zex.tracker.security.SmsHandshakeManager
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.data.local.prefs.SecurePrefs

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val zexApi: ZexApi,
    private val bleManager: ZexBleManager,
    private val smsHandshakeManager: SmsHandshakeManager,
    private val prefs: SecurePrefs
) : ViewModel() {

    private val _devices = MutableStateFlow<List<DeviceDto>>(emptyList())
    val devices: StateFlow<List<DeviceDto>> = _devices
    
    private val _isSmsHandshakePassed = MutableStateFlow(prefs.getBoolean(ZexConstants.KEY_SMS_HANDSHAKE_PASSED, false))
    val isSmsHandshakePassed: StateFlow<Boolean> = _isSmsHandshakePassed

    private val _smsHandshakeError = MutableStateFlow(prefs.getString("sms_handshake_error"))
    val smsHandshakeError: StateFlow<String?> = _smsHandshakeError

    fun refreshHandshakeStatus() {
        _isSmsHandshakePassed.value = prefs.getBoolean(ZexConstants.KEY_SMS_HANDSHAKE_PASSED, false)
        _smsHandshakeError.value = prefs.getString("sms_handshake_error")
    }

    fun startSmsHandshake(phoneNumber: String?) {
        if (!_isSmsHandshakePassed.value) {
            smsHandshakeManager.startHandshake(phoneNumber)
            _smsHandshakeError.value = prefs.getString("sms_handshake_error")
        }
    }

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

package com.zex.tracker.ui.screens.setup

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.data.remote.ApiResult
import com.zex.tracker.data.remote.dto.DeviceRegisterRequest
import com.zex.tracker.data.remote.dto.LoginRequest
import com.zex.tracker.data.remote.dto.RegisterRequest
import com.zex.tracker.data.repository.AuthRepository
import com.zex.tracker.data.repository.DeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val authRepo: AuthRepository,
    private val deviceRepo: DeviceRepository,
    private val prefs: SecurePrefs
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState

    fun registerOwner(req: RegisterRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val res = authRepo.register(req)) {
                is ApiResult.Success -> {
                    prefs.putString(ZexConstants.KEY_OWNER_TOKEN, res.data.token)
                    prefs.putInt(ZexConstants.KEY_OWNER_ID, res.data.owner.id)
                    ZexLogger.i("Setup", "Owner registered successfully")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = res.message)
                }
            }
        }
    }

    fun loginOwner(req: LoginRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val res = authRepo.login(req)) {
                is ApiResult.Success -> {
                    prefs.putString(ZexConstants.KEY_OWNER_TOKEN, res.data.token)
                    prefs.putInt(ZexConstants.KEY_OWNER_ID, res.data.owner.id)
                    ZexLogger.i("Setup", "Owner logged in successfully")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = res.message)
                }
            }
        }
    }

    fun registerDevice(context: Context, deviceName: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            var uid = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            if (uid.isNullOrEmpty()) {
                uid = UUID.randomUUID().toString()
            }
            val finalUid = "zex-uid-$uid"
            
            val req = DeviceRegisterRequest(
                device_uid = finalUid,
                device_name = deviceName,
                device_model = Build.MODEL,
                android_version = Build.VERSION.RELEASE,
                sim_iccid = null // handled later
            )

            when (val res = deviceRepo.registerDevice(req)) {
                is ApiResult.Success -> {
                    prefs.putString(ZexConstants.KEY_DEVICE_TOKEN, res.data.device_token)
                    prefs.putString(ZexConstants.KEY_DEVICE_UID, finalUid)
                    ZexLogger.i("Setup", "Device registered successfully")
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = res.message)
                }
            }
        }
    }

    fun completeSetup() {
        prefs.putBoolean(ZexConstants.KEY_IS_SETUP_COMPLETE, true)
    }
}

data class SetupUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

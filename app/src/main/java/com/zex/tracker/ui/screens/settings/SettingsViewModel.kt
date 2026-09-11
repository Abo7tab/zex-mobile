package com.zex.tracker.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zex.tracker.data.remote.api.ZexApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val api: ZexApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState: StateFlow<SettingsUiState> = _uiState

    fun updateProfile(name: String, email: String) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            try {
                val response = api.updateProfile(mapOf("name" to name, "email" to email))
                if (response.isSuccessful) {
                    _uiState.value = SettingsUiState.Success("تم تحديث الملف الشخصي بنجاح")
                } else {
                    _uiState.value = SettingsUiState.Error("فشل التحديث: ${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e.localizedMessage ?: "حدث خطأ غير معروف")
            }
        }
    }

    fun updateSecurity(currentPass: String, newPass: String, confirmPass: String, pinCode: String) {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.Loading
            try {
                val map = mutableMapOf("current_password" to currentPass)
                if (newPass.isNotEmpty()) {
                    map["password"] = newPass
                    map["password_confirmation"] = confirmPass
                }
                if (pinCode.isNotEmpty()) {
                    map["pin_code"] = pinCode
                }
                
                val response = api.updateSecurity(map)
                if (response.isSuccessful) {
                    _uiState.value = SettingsUiState.Success("تم تحديث الأمان بنجاح")
                } else {
                    _uiState.value = SettingsUiState.Error("فشل التحديث: الرجاء التأكد من صحة كلمة المرور الحالية")
                }
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(e.localizedMessage ?: "حدث خطأ غير معروف")
            }
        }
    }
    
    fun resetState() {
        _uiState.value = SettingsUiState.Idle
    }
}

sealed class SettingsUiState {
    object Idle : SettingsUiState()
    object Loading : SettingsUiState()
    data class Success(val message: String) : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}


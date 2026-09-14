package com.zex.tracker.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zex.tracker.security.BleMeshManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class BleRadarViewModel @Inject constructor(
    private val bleMeshManager: BleMeshManager
) : ViewModel() {
    val lastPeer: StateFlow<BleMeshManager.Peer?> = bleMeshManager.state
    fun setEnabled(enabled: Boolean) {
        if (enabled) bleMeshManager.startRadar() else bleMeshManager.stopRadar()
    }
    fun setTargetHash(value: String) = bleMeshManager.setTargetHash(value)
}

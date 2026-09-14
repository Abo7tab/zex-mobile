package com.zex.tracker.security.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.data.local.prefs.SecurePrefs
import com.zex.tracker.data.remote.api.ZexApi
import com.zex.tracker.data.remote.dto.BleRelayPayload
import com.zex.tracker.security.location.LocationTracker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@SuppressLint("MissingPermission")
@Singleton
class ZexBleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: SecurePrefs,
    private val api: ZexApi,
    private val locationTracker: LocationTracker
) {

    companion object {
        val ZEX_SERVICE_UUID: ParcelUuid = ParcelUuid(UUID.fromString("0000ZEX1-0000-1000-8000-00805F9B34FB".replace("ZEX1", "23E1")))
        const val TAG = "ZexBleManager"
    }

    private val _foundDevices = MutableSharedFlow<Pair<String, Int>>(replay = 10, extraBufferCapacity = 10)
    val foundDevices = _foundDevices.asSharedFlow()

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter
    private var advertiser: BluetoothLeAdvertiser? = null
    private var scanner: BluetoothLeScanner? = null
    private var isAdvertising = false
    private var isScanning = false

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            isAdvertising = true
            ZexLogger.i(TAG, "BLE Advertise Started successfully")
        }
        override fun onStartFailure(errorCode: Int) {
            ZexLogger.e(TAG, "BLE Advertise failed with error: $errorCode")
            isAdvertising = false
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.scanRecord?.serviceData?.get(ZEX_SERVICE_UUID)?.let { data ->
                val deviceHash = String(data)
                val rssi = result.rssi
                ZexLogger.i(TAG, "Found ZEX Device via BLE Mesh: $deviceHash, RSSI: $rssi")
                _foundDevices.tryEmit(Pair(deviceHash, rssi))

                // 🚀 BLE Mesh Relay Logic 🚀
                scope.launch {
                    try {
                        val loc = locationTracker.getCurrentLocation()
                        if (loc != null) {
                            val relayPayload = com.zex.tracker.data.remote.dto.RelayTelemetryPayload(
                                target_device_uid = deviceHash,
                                latitude = loc.latitude,
                                longitude = loc.longitude,
                                relay_source = "BLE_MESH"
                            )
                            val response = api.relayTelemetry(relayPayload)
                            if (response.isSuccessful) {
                                ZexLogger.i(TAG, "Successfully relayed telemetry to C2 via BLE_MESH for $deviceHash")
                            }
                        }
                    } catch (e: Exception) {
                        ZexLogger.e(TAG, "Failed to relay BLE_MESH telemetry", e)
                    }
                }

                // reportDeviceFound(deviceHash)
            }
        }
        override fun onScanFailed(errorCode: Int) {
            ZexLogger.e(TAG, "BLE Scan failed with error: $errorCode")
        }
    }

    fun startAdvertising() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        if (isAdvertising) return

        val deviceUid = prefs.getString("device_uid") ?: return
        val hashData = deviceUid.takeLast(8).toByteArray(Charsets.UTF_8)

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
            .setConnectable(false)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .addServiceUuid(ZEX_SERVICE_UUID)
            .addServiceData(ZEX_SERVICE_UUID, hashData)
            .build()

        advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        advertiser?.startAdvertising(settings, data, advertiseCallback)
    }

    fun stopAdvertising() {
        if (!isAdvertising) return
        advertiser?.stopAdvertising(advertiseCallback)
        isAdvertising = false
        ZexLogger.i(TAG, "BLE Advertise Stopped")
    }

    fun startScanning() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        if (isScanning) return

        val filters = listOf(ScanFilter.Builder().setServiceUuid(ZEX_SERVICE_UUID).build())
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()

        scanner = bluetoothAdapter.bluetoothLeScanner
        scanner?.startScan(filters, settings, scanCallback)
        isScanning = true
        ZexLogger.i(TAG, "BLE Scan Started")
    }

    fun stopScanning() {
        if (!isScanning) return
        scanner?.stopScan(scanCallback)
        isScanning = false
        ZexLogger.i(TAG, "BLE Scan Stopped")
}
}


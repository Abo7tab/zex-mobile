package com.zex.tracker.security

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import com.zex.tracker.core.constants.ZexConstants
import com.zex.tracker.core.logging.ZexLogger
import com.zex.tracker.core.utils.BatteryUtils
import com.zex.tracker.data.remote.ApiResult
import com.zex.tracker.data.remote.api.ZexApi
import com.zex.tracker.data.remote.dto.BleRelayPayload
import com.zex.tracker.data.remote.dto.ActivityLogPayload
import com.zex.tracker.security.location.LocationTracker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleMeshManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: com.zex.tracker.data.local.prefs.SecurePrefs,
    private val locationTracker: LocationTracker,
    private val api: ZexApi
) {
    companion object {
        // A short service UUID keeps the legacy 31-byte advertisement within budget.
        private val SERVICE_UUID = UUID.fromString("0000fd6f-0000-1000-8000-00805f9b34fb")
        private val SERVICE_PARCEL_UUID = ParcelUuid(SERVICE_UUID)
        private const val PAYLOAD_SIZE = 22
        private const val SCALE = 100_000.0
    }

    data class Peer(val hash: String, val latitude: Double, val longitude: Double, val battery: Int, val distanceMeters: Double?, val uploaded: Boolean)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow<Peer?>(null)
    val state: StateFlow<Peer?> = _state.asStateFlow()
    private val uploadedAt = ConcurrentHashMap<String, Long>()
    private var advertisingJob: Job? = null
    private var advertiser: BluetoothLeAdvertiser? = null
    private var scanner: BluetoothLeScanner? = null
    private var advertiseCallback: AdvertiseCallback? = null
    private var scanCallback: ScanCallback? = null
    @Volatile private var targetHash: String? = null

    fun setTargetHash(value: String?) {
        targetHash = value?.filter { it.isLetterOrDigit() }?.takeLast(8)?.uppercase()?.ifBlank { null }
    }

    @SuppressLint("MissingPermission")
    fun startRadar() {
        if (!hasBlePermissions()) {
            ZexLogger.w("BleMeshManager", "BLE permissions are not granted")
            return
        }
        val adapter = bluetoothManager().adapter
        if (adapter?.isEnabled != true) {
            ZexLogger.w("BleMeshManager", "Bluetooth is disabled")
            return
        }
        stopRadar()
        advertiser = adapter.bluetoothLeAdvertiser
        scanner = adapter.bluetoothLeScanner
        if (advertiser == null || scanner == null) return
        startAdvertising()
        startScanning()
        advertisingJob = scope.launch {
            while (true) {
                updateAdvertisement()
                delay(30_000L)
            }
        }
        ZexLogger.i("BleMeshManager", "BLE radar started")
    }

    @SuppressLint("MissingPermission")
    fun stopRadar() {
        advertisingJob?.cancel()
        advertisingJob = null
        try { advertiseCallback?.let { advertiser?.stopAdvertising(it) } } catch (_: SecurityException) {}
        try { scanCallback?.let { scanner?.stopScan(it) } } catch (_: SecurityException) {}
        advertiseCallback = null
        scanCallback = null
    }

    private fun bluetoothManager() = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private fun hasBlePermissions(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADVERTISE)
                .all { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun startAdvertising() {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(false)
            .build()
        advertiseCallback = object : AdvertiseCallback() {
            override fun onStartFailure(errorCode: Int) { ZexLogger.w("BleMeshManager", "BLE advertising failed: $errorCode") }
        }
        try {
            advertiser?.startAdvertising(
                settings,
                AdvertiseData.Builder()
                    .addServiceUuid(SERVICE_PARCEL_UUID)
                    .addServiceData(SERVICE_PARCEL_UUID, emptyPayload())
                    .setIncludeDeviceName(false)
                    .build(),
                advertiseCallback
            )
        } catch (e: SecurityException) { ZexLogger.e("BleMeshManager", "BLE advertising permission failure", e) }
    }

    @SuppressLint("MissingPermission")
    private fun updateAdvertisement() {
        val location = kotlinx.coroutines.runBlocking { locationTracker.getCurrentLocation() } ?: return
        val payload = encode(location.latitude, location.longitude, BatteryUtils.getBatteryLevel(context), location.accuracy)
        try {
            advertiseCallback?.let { advertiser?.stopAdvertising(it) }
            advertiser?.startAdvertising(
                AdvertiseSettings.Builder().setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY).setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM).setConnectable(false).build(),
                AdvertiseData.Builder().addServiceUuid(SERVICE_PARCEL_UUID).addServiceData(SERVICE_PARCEL_UUID, payload).setIncludeDeviceName(false).build(),
                advertiseCallback
            )
        } catch (e: SecurityException) { ZexLogger.e("BleMeshManager", "BLE advertisement update failed", e) }
    }

    @SuppressLint("MissingPermission")
    private fun startScanning() {
        val filters = listOf(ScanFilter.Builder().setServiceUuid(SERVICE_PARCEL_UUID).build())
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                result.scanRecord?.getServiceData(SERVICE_PARCEL_UUID)?.let { processPeer(it, result.rssi) }
            }
            override fun onScanFailed(errorCode: Int) { ZexLogger.w("BleMeshManager", "BLE scan failed: $errorCode") }
        }
        try { scanner?.startScan(filters, settings, scanCallback) } catch (e: SecurityException) { ZexLogger.e("BleMeshManager", "BLE scan permission failure", e) }
    }

    private fun processPeer(bytes: ByteArray, rssi: Int) {
        val decoded = decode(bytes) ?: return
        val own = deviceHash()
        if (decoded.hash == own) return
        val requestedTarget = targetHash
        if (requestedTarget != null && decoded.hash != requestedTarget) return
        val now = System.currentTimeMillis()
        if (now - (uploadedAt[decoded.hash] ?: 0L) < 30_000L) return
        uploadedAt[decoded.hash] = now
        scope.launch {
            val result = try {
                api.sendBleRelayLocation(BleRelayPayload(decoded.hash, decoded.latitude, decoded.longitude, decoded.accuracy, decoded.battery))
            } catch (e: Exception) { ZexLogger.e("BleMeshManager", "BLE relay failed", e); null }
            val uploaded = result is retrofit2.Response<*> && result.isSuccessful
            if (uploaded) {
                try {
                    api.sendActivityLog(ActivityLogPayload(
                        message = "BLE peer discovered and location relayed",
                        severity = "info",
                        payload = mapOf(
                            "lat" to decoded.latitude.toString(),
                            "lng" to decoded.longitude.toString(),
                            "target_uid" to decoded.hash,
                            "source" to "BLE_RELAY",
                            "battery" to decoded.battery.toString(),
                            "accuracy" to decoded.accuracy.toString()
                        )
                    ))
                } catch (e: Exception) {
                    ZexLogger.w("BleMeshManager", "BLE activity log failed", e)
                }
            }
            _state.value = Peer(decoded.hash, decoded.latitude, decoded.longitude, decoded.battery, estimateDistance(rssi), uploaded)
        }
    }

    private fun estimateDistance(rssi: Int): Double? {
        if (rssi >= 0) return null
        // BLE RSSI gives an approximate range only; walls and phone orientation affect it.
        return 10.0.pow((-59 - rssi) / 20.0).coerceIn(0.5, 150.0)
    }

    private data class Decoded(val hash: String, val latitude: Double, val longitude: Double, val battery: Int, val accuracy: Float)

    private fun encode(latitude: Double, longitude: Double, battery: Int, accuracy: Float): ByteArray {
        val hash = deviceHash().toByteArray(Charsets.US_ASCII).copyOf(8)
        return ByteBuffer.allocate(PAYLOAD_SIZE).order(ByteOrder.LITTLE_ENDIAN).apply {
            put(0x5A); put(0x58); put(1); put(hash)
            putInt((latitude * SCALE).toInt()); putInt((longitude * SCALE).toInt())
            put(battery.coerceIn(0, 100).toByte()); putShort((accuracy.coerceIn(0f, 6553.5f) * 10).toInt().toShort())
        }.array()
    }

    private fun emptyPayload() = encode(0.0, 0.0, 0, 0f)

    private fun decode(bytes: ByteArray): Decoded? {
        if (bytes.size < PAYLOAD_SIZE || bytes[0].toInt() != 0x5A || bytes[1].toInt() != 0x58 || bytes[2].toInt() != 1) return null
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(3)
        val hash = ByteArray(8).also(buffer::get).toString(Charsets.US_ASCII).trimEnd('\u0000', '_')
        val latitude = buffer.int / SCALE
        val longitude = buffer.int / SCALE
        val battery = buffer.get().toInt() and 0xFF
        val accuracy = (buffer.short.toInt() and 0xFFFF) / 10f
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0 || hash.isBlank()) return null
        return Decoded(hash, latitude, longitude, battery, accuracy)
    }

    private fun deviceHash(): String = prefs.getString(ZexConstants.KEY_DEVICE_UID).orEmpty().takeLast(8).padEnd(8, '_')
}

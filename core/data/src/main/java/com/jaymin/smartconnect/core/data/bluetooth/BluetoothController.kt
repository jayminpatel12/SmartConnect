package com.jaymin.smartconnect.core.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import com.jaymin.smartconnect.core.common.util.Constants
import com.jaymin.smartconnect.core.common.util.Resource
import com.jaymin.smartconnect.core.domain.model.BluetoothDeviceInfo
import com.jaymin.smartconnect.core.domain.model.DeviceType
import com.jaymin.smartconnect.core.domain.repository.BluetoothRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BluetoothController @Inject constructor(
    @ApplicationContext private val context: Context
) : BluetoothRepository {

    private val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDeviceInfo>> = _scannedDevices.asStateFlow()

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothDeviceInfo>> = _pairedDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private var currentSocket: BluetoothSocket? = null

    private val foundDeviceReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(ctx: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE).toInt()
                    device?.let {
                        val info = it.toDeviceInfo(rssi)
                        _scannedDevices.update { list ->
                            if (list.none { d -> d.address == info.address }) {
                                list + info
                            } else {
                                list.map { d -> if (d.address == info.address) info else d }
                            }
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _isScanning.value = true
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun startScan(): Flow<Resource<List<BluetoothDeviceInfo>>> = flow {
        emit(Resource.Loading)
        if (bluetoothAdapter == null) {
            emit(Resource.Error("Bluetooth not available on this device"))
            return@flow
        }
        if (!bluetoothAdapter.isEnabled) {
            emit(Resource.Error("Bluetooth is disabled. Please enable it in settings."))
            return@flow
        }

        // Check if location services are enabled (required for BT scanning on most Android versions)
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        if (!isGpsEnabled && !isNetworkEnabled) {
            emit(Resource.Error("Location services are disabled. Please enable them to scan for devices."))
            return@flow
        }

        _scannedDevices.value = emptyList()
        // Discovery is asynchronous, so we wait for ACTION_DISCOVERY_STARTED via receiver
        // but we set it here as well for immediate UI feedback
        _isScanning.value = true

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(foundDeviceReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(foundDeviceReceiver, filter)
        }

        val success = bluetoothAdapter.startDiscovery()
        if (!success) {
            _isScanning.value = false
            emit(Resource.Error("Failed to start Bluetooth discovery"))
            return@flow
        }

        // Also refresh paired devices
        _pairedDevices.value = getPairedDevices()

        // Wait for scan to finish or timeout
        kotlinx.coroutines.delay(Constants.SCAN_DURATION_MS)
        stopScan()
        emit(Resource.Success(_scannedDevices.value))
    }

    @SuppressLint("MissingPermission")
    override fun stopScan() {
        bluetoothAdapter?.cancelDiscovery()
        _isScanning.value = false
        try {
            context.unregisterReceiver(foundDeviceReceiver)
        } catch (_: IllegalArgumentException) { }
    }

    @SuppressLint("MissingPermission")
    override fun getPairedDevices(): List<BluetoothDeviceInfo> {
        return bluetoothAdapter?.bondedDevices?.map { it.toDeviceInfo(isPaired = true) } ?: emptyList()
    }

    @SuppressLint("MissingPermission")
    override suspend fun connectToDevice(address: String): Resource<BluetoothDeviceInfo> =
        withContext(Dispatchers.IO) {
            try {
                bluetoothAdapter?.cancelDiscovery()
                val device = bluetoothAdapter?.getRemoteDevice(address)
                    ?: return@withContext Resource.Error("Device not found")

                val uuid = UUID.fromString(Constants.BT_UUID)
                currentSocket = device.createRfcommSocketToServiceRecord(uuid)
                currentSocket?.connect()

                Resource.Success(device.toDeviceInfo(isConnected = true))
            } catch (e: IOException) {
                currentSocket?.close()
                currentSocket = null
                Resource.Error("Connection failed: ${e.message}")
            }
        }

    override suspend fun sendData(address: String, data: ByteArray): Resource<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val outputStream = currentSocket?.outputStream
                    ?: return@withContext Resource.Error("Not connected to any device")
                outputStream.write(data)
                outputStream.flush()
                Resource.Success(Unit)
            } catch (e: IOException) {
                Resource.Error("Failed to send data: ${e.message}")
            }
        }

    override fun release() {
        stopScan()
        currentSocket?.close()
        currentSocket = null
    }

    @SuppressLint("MissingPermission")
    private fun BluetoothDevice.toDeviceInfo(
        rssi: Int = 0,
        isPaired: Boolean = false,
        isConnected: Boolean = false
    ): BluetoothDeviceInfo = BluetoothDeviceInfo(
        name = name ?: "Unknown Device",
        address = address,
        rssi = rssi,
        type = mapDeviceType(bluetoothClass?.majorDeviceClass),
        isPaired = isPaired || bondState == BluetoothDevice.BOND_BONDED,
        isConnected = isConnected
    )

    private fun mapDeviceType(majorClass: Int?): DeviceType = when (majorClass) {
        0x0200 -> DeviceType.PHONE
        0x0100 -> DeviceType.COMPUTER
        0x0400 -> DeviceType.AUDIO
        0x0500 -> DeviceType.PERIPHERAL
        0x0700 -> DeviceType.WEARABLE
        else -> DeviceType.UNKNOWN
    }
}

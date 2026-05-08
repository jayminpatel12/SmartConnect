package com.jaymin.smartconnect.core.domain.repository

import com.jaymin.smartconnect.core.common.util.Resource
import com.jaymin.smartconnect.core.domain.model.BluetoothDeviceInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BluetoothRepository {
    val scannedDevices: StateFlow<List<BluetoothDeviceInfo>>
    val pairedDevices: StateFlow<List<BluetoothDeviceInfo>>
    val isScanning: StateFlow<Boolean>
    fun startScan(): Flow<Resource<List<BluetoothDeviceInfo>>>
    fun stopScan()
    fun getPairedDevices(): List<BluetoothDeviceInfo>
    suspend fun connectToDevice(address: String): Resource<BluetoothDeviceInfo>
    suspend fun sendData(address: String, data: ByteArray): Resource<Unit>
    fun release()
}

package com.jaymin.smartconnect.core.domain.usecase

import com.jaymin.smartconnect.core.common.util.Resource
import com.jaymin.smartconnect.core.domain.model.BluetoothDeviceInfo
import com.jaymin.smartconnect.core.domain.repository.BluetoothRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ScanDevicesUseCase @Inject constructor(
    private val repository: BluetoothRepository
) {
    operator fun invoke(): Flow<Resource<List<BluetoothDeviceInfo>>> = repository.startScan()
    fun stop() = repository.stopScan()
    val scannedDevices: StateFlow<List<BluetoothDeviceInfo>> = repository.scannedDevices
    val isScanning: StateFlow<Boolean> = repository.isScanning
}

class GetPairedDevicesUseCase @Inject constructor(
    private val repository: BluetoothRepository
) {
    operator fun invoke(): List<BluetoothDeviceInfo> = repository.getPairedDevices()
    val pairedDevices: StateFlow<List<BluetoothDeviceInfo>> = repository.pairedDevices
}

class ConnectDeviceUseCase @Inject constructor(
    private val repository: BluetoothRepository
) {
    suspend operator fun invoke(address: String): Resource<BluetoothDeviceInfo> =
        repository.connectToDevice(address)
}

class SendDataUseCase @Inject constructor(
    private val repository: BluetoothRepository
) {
    suspend operator fun invoke(address: String, data: ByteArray): Resource<Unit> =
        repository.sendData(address, data)
}

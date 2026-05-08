package com.jaymin.smartconnect.ui.screens.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaymin.smartconnect.core.domain.model.BluetoothDeviceInfo
import com.jaymin.smartconnect.core.domain.model.TransferDirection
import com.jaymin.smartconnect.core.domain.model.TransferRecord
import com.jaymin.smartconnect.core.domain.model.TransferStatus
import com.jaymin.smartconnect.core.domain.usecase.GetPairedDevicesUseCase
import com.jaymin.smartconnect.core.domain.usecase.SaveTransferUseCase
import com.jaymin.smartconnect.core.domain.usecase.SendDataUseCase
import com.jaymin.smartconnect.core.common.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val getPairedDevices: GetPairedDevicesUseCase,
    private val sendData: SendDataUseCase,
    private val saveTransfer: SaveTransferUseCase
) : ViewModel() {

    private val _pairedDevices = MutableStateFlow<List<BluetoothDeviceInfo>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDeviceInfo>> = _pairedDevices.asStateFlow()

    init {
        _pairedDevices.value = getPairedDevices()
        viewModelScope.launch {
            getPairedDevices.pairedDevices.collect { _pairedDevices.value = it }
        }
    }

    fun sendTestData(address: String) {
        viewModelScope.launch {
            val device = _pairedDevices.value.find { it.address == address } ?: return@launch
            val testPayload = "Hello from SmartConnect! ${System.currentTimeMillis()}"

            val result = sendData(address, testPayload.toByteArray())
            val status = if (result is Resource.Success) TransferStatus.SUCCESS else TransferStatus.FAILED

            saveTransfer(
                TransferRecord(
                    deviceName = device.name,
                    deviceAddress = device.address,
                    payloadType = "TEXT",
                    content = testPayload,
                    direction = TransferDirection.SENT,
                    status = status
                )
            )
        }
    }
}

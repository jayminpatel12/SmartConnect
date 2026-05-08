package com.jaymin.smartconnect.ui.screens.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaymin.smartconnect.core.common.util.Resource
import com.jaymin.smartconnect.core.domain.model.BluetoothDeviceInfo
import com.jaymin.smartconnect.core.domain.usecase.ConnectDeviceUseCase
import com.jaymin.smartconnect.core.domain.usecase.ScanDevicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ScannerUiState(
    val scannedDevices: List<BluetoothDeviceInfo> = emptyList(),
    val isScanning: Boolean = false,
    val error: String? = null,
    val connectingTo: String? = null,
    val connectedDevice: BluetoothDeviceInfo? = null
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val scanDevices: ScanDevicesUseCase,
    private val connectDevice: ConnectDeviceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            scanDevices.scannedDevices.collect { devices ->
                _uiState.update { it.copy(scannedDevices = devices) }
            }
        }
        viewModelScope.launch {
            scanDevices.isScanning.collect { scanning ->
                _uiState.update { it.copy(isScanning = scanning) }
            }
        }
    }

    fun startScan() {
        viewModelScope.launch {
            scanDevices().collect { result ->
                when (result) {
                    is Resource.Loading -> _uiState.update { it.copy(isScanning = true, error = null) }
                    is Resource.Error -> _uiState.update { it.copy(error = result.message, isScanning = false) }
                    is Resource.Success -> _uiState.update { it.copy(scannedDevices = result.data, isScanning = false) }
                }
            }
        }
    }

    fun stopScan() {
        scanDevices.stop()
    }

    fun connectTo(device: BluetoothDeviceInfo) {
        viewModelScope.launch {
            _uiState.update { it.copy(connectingTo = device.address) }
            when (val result = connectDevice(device.address)) {
                is Resource.Success -> _uiState.update {
                    it.copy(connectedDevice = result.data, connectingTo = null)
                }
                is Resource.Error -> _uiState.update {
                    it.copy(error = result.message, connectingTo = null)
                }
                else -> {}
            }
        }
    }
}

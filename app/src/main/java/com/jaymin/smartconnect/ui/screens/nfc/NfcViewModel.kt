package com.jaymin.smartconnect.ui.screens.nfc

import androidx.lifecycle.ViewModel
import com.jaymin.smartconnect.core.domain.model.NfcPayload
import com.jaymin.smartconnect.core.domain.model.PayloadType
import com.jaymin.smartconnect.core.domain.usecase.CheckNfcUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class NfcUiState(
    val isNfcEnabled: Boolean = false,
    val isNfcAvailable: Boolean = false,
    val mode: NfcMode = NfcMode.IDLE,
    val lastRead: NfcPayload? = null,
    val pendingWrite: NfcPayload? = null,
    val error: String? = null,
    val successMessage: String? = null
)

enum class NfcMode { IDLE, READING, WRITING }

@HiltViewModel
class NfcViewModel @Inject constructor(
    private val checkNfc: CheckNfcUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NfcUiState())
    val uiState: StateFlow<NfcUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                isNfcAvailable = checkNfc.isAvailable(),
                isNfcEnabled = checkNfc.isEnabled()
            )
        }
    }

    fun readMode() {
        _uiState.update { it.copy(mode = NfcMode.READING, error = null) }
    }

    fun prepareWrite(type: PayloadType, content: String) {
        val payload = NfcPayload(type = type, content = content)
        _uiState.update { it.copy(mode = NfcMode.WRITING, pendingWrite = payload, error = null) }
    }

    fun onTagRead(payload: NfcPayload) {
        _uiState.update { it.copy(lastRead = payload, mode = NfcMode.IDLE) }
    }

    fun onWriteSuccess() {
        _uiState.update {
            it.copy(mode = NfcMode.IDLE, pendingWrite = null, successMessage = "Tag written successfully!")
        }
    }

    fun onError(message: String) {
        _uiState.update { it.copy(error = message, mode = NfcMode.IDLE) }
    }

    fun refreshNfcStatus() {
        _uiState.update { it.copy(isNfcEnabled = checkNfc.isEnabled()) }
    }
}

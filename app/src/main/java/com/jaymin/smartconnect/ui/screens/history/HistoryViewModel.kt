package com.jaymin.smartconnect.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaymin.smartconnect.core.domain.model.TransferRecord
import com.jaymin.smartconnect.core.domain.repository.TransferRepository
import com.jaymin.smartconnect.core.domain.usecase.ClearHistoryUseCase
import com.jaymin.smartconnect.core.domain.usecase.GetTransferHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    getHistory: GetTransferHistoryUseCase,
    private val clearHistory: ClearHistoryUseCase,
    private val transferRepository: TransferRepository
) : ViewModel() {

    val transfers: StateFlow<List<TransferRecord>> = getHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clearHistory() {
        viewModelScope.launch { clearHistory.invoke() }
    }

    fun syncToCloud() {
        viewModelScope.launch {
            transferRepository.syncToFirebase(transfers.value)
        }
    }
}

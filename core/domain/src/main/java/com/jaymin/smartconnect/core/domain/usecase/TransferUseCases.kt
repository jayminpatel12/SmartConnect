package com.jaymin.smartconnect.core.domain.usecase

import com.jaymin.smartconnect.core.domain.model.TransferRecord
import com.jaymin.smartconnect.core.domain.repository.TransferRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransferHistoryUseCase @Inject constructor(
    private val repository: TransferRepository
) {
    operator fun invoke(): Flow<List<TransferRecord>> = repository.getAllTransfers()
}

class SaveTransferUseCase @Inject constructor(
    private val repository: TransferRepository
) {
    suspend operator fun invoke(record: TransferRecord) = repository.saveTransfer(record)
}

class ClearHistoryUseCase @Inject constructor(
    private val repository: TransferRepository
) {
    suspend operator fun invoke() = repository.clearHistory()
}

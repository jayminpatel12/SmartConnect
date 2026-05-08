package com.jaymin.smartconnect.core.domain.repository

import com.jaymin.smartconnect.core.domain.model.TransferRecord
import kotlinx.coroutines.flow.Flow

interface TransferRepository {
    fun getAllTransfers(): Flow<List<TransferRecord>>
    suspend fun saveTransfer(record: TransferRecord)
    suspend fun deleteTransfer(id: String)
    suspend fun clearHistory()
    suspend fun syncToFirebase(records: List<TransferRecord>)
}

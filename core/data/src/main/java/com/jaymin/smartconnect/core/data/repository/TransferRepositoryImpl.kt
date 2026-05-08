package com.jaymin.smartconnect.core.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.jaymin.smartconnect.core.data.local.dao.TransferDao
import com.jaymin.smartconnect.core.data.mapper.toDomain
import com.jaymin.smartconnect.core.data.mapper.toEntity
import com.jaymin.smartconnect.core.domain.model.TransferRecord
import com.jaymin.smartconnect.core.domain.repository.TransferRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferRepositoryImpl @Inject constructor(
    private val transferDao: TransferDao,
    private val firestore: FirebaseFirestore
) : TransferRepository {

    private val collection = firestore.collection("transfer_history")

    override fun getAllTransfers(): Flow<List<TransferRecord>> =
        transferDao.getAllTransfers().map { entities -> entities.map { it.toDomain() } }

    override suspend fun saveTransfer(record: TransferRecord) {
        transferDao.insert(record.toEntity())
    }

    override suspend fun deleteTransfer(id: String) {
        transferDao.delete(id)
    }

    override suspend fun clearHistory() {
        transferDao.clearAll()
    }

    override suspend fun syncToFirebase(records: List<TransferRecord>) {
        try {
            val batch = firestore.batch()
            records.forEach { record ->
                val doc = collection.document(record.id)
                batch.set(doc, mapOf(
                    "deviceName" to record.deviceName,
                    "deviceAddress" to record.deviceAddress,
                    "payloadType" to record.payloadType,
                    "content" to record.content,
                    "direction" to record.direction.name,
                    "status" to record.status.name,
                    "timestamp" to record.timestamp
                ))
            }
            batch.commit().await()
        } catch (_: Exception) {
            // Firebase sync is best-effort; local Room DB is the source of truth
        }
    }
}

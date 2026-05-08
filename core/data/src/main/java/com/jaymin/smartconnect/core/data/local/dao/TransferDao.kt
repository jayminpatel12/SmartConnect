package com.jaymin.smartconnect.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jaymin.smartconnect.core.data.local.entity.TransferEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfer_history ORDER BY timestamp DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transfer: TransferEntity)

    @Query("DELETE FROM transfer_history WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM transfer_history")
    suspend fun clearAll()
}

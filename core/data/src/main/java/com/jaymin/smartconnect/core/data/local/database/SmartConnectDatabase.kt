package com.jaymin.smartconnect.core.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jaymin.smartconnect.core.data.local.dao.TransferDao
import com.jaymin.smartconnect.core.data.local.entity.TransferEntity

@Database(entities = [TransferEntity::class], version = 1, exportSchema = false)
abstract class SmartConnectDatabase : RoomDatabase() {
    abstract fun transferDao(): TransferDao
}

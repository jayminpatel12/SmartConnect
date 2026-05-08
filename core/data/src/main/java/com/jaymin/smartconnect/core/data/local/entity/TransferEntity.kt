package com.jaymin.smartconnect.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfer_history")
data class TransferEntity(
    @PrimaryKey val id: String,
    val deviceName: String,
    val deviceAddress: String,
    val payloadType: String,
    val content: String,
    val direction: String,
    val status: String,
    val timestamp: Long
)

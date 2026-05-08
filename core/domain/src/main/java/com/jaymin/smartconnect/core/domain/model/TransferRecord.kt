package com.jaymin.smartconnect.core.domain.model

data class TransferRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val deviceName: String,
    val deviceAddress: String,
    val payloadType: String,
    val content: String,
    val direction: TransferDirection,
    val status: TransferStatus,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TransferDirection { SENT, RECEIVED }
enum class TransferStatus { SUCCESS, FAILED, PENDING }

package com.jaymin.smartconnect.core.data.mapper

import com.jaymin.smartconnect.core.data.local.entity.TransferEntity
import com.jaymin.smartconnect.core.domain.model.TransferDirection
import com.jaymin.smartconnect.core.domain.model.TransferRecord
import com.jaymin.smartconnect.core.domain.model.TransferStatus

fun TransferEntity.toDomain(): TransferRecord = TransferRecord(
    id = id,
    deviceName = deviceName,
    deviceAddress = deviceAddress,
    payloadType = payloadType,
    content = content,
    direction = TransferDirection.valueOf(direction),
    status = TransferStatus.valueOf(status),
    timestamp = timestamp
)

fun TransferRecord.toEntity(): TransferEntity = TransferEntity(
    id = id,
    deviceName = deviceName,
    deviceAddress = deviceAddress,
    payloadType = payloadType,
    content = content,
    direction = direction.name,
    status = status.name,
    timestamp = timestamp
)

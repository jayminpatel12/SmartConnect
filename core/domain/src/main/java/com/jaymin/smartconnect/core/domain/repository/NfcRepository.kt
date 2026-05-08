package com.jaymin.smartconnect.core.domain.repository

import com.jaymin.smartconnect.core.common.util.Resource
import com.jaymin.smartconnect.core.domain.model.NfcPayload
import kotlinx.coroutines.flow.Flow

interface NfcRepository {
    fun readTag(): Flow<Resource<NfcPayload>>
    suspend fun writeTag(payload: NfcPayload): Resource<Unit>
    suspend fun shareViaNfc(payload: NfcPayload): Resource<Unit>
    fun isNfcAvailable(): Boolean
    fun isNfcEnabled(): Boolean
}

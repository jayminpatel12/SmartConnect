package com.jaymin.smartconnect.core.domain.usecase

import com.jaymin.smartconnect.core.common.util.Resource
import com.jaymin.smartconnect.core.domain.model.NfcPayload
import com.jaymin.smartconnect.core.domain.repository.NfcRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReadNfcTagUseCase @Inject constructor(
    private val repository: NfcRepository
) {
    operator fun invoke(): Flow<Resource<NfcPayload>> = repository.readTag()
}

class WriteNfcTagUseCase @Inject constructor(
    private val repository: NfcRepository
) {
    suspend operator fun invoke(payload: NfcPayload): Resource<Unit> =
        repository.writeTag(payload)
}

class ShareViaNfcUseCase @Inject constructor(
    private val repository: NfcRepository
) {
    suspend operator fun invoke(payload: NfcPayload): Resource<Unit> =
        repository.shareViaNfc(payload)
}

class CheckNfcUseCase @Inject constructor(
    private val repository: NfcRepository
) {
    fun isAvailable(): Boolean = repository.isNfcAvailable()
    fun isEnabled(): Boolean = repository.isNfcEnabled()
}

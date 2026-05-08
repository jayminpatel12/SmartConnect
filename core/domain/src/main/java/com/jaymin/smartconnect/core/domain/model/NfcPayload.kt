package com.jaymin.smartconnect.core.domain.model

data class NfcPayload(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: PayloadType,
    val content: String,
    val senderDevice: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

enum class PayloadType {
    TEXT, URL, CONTACT, WIFI_CREDENTIALS, CUSTOM_DATA
}

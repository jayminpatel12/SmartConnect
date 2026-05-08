package com.jaymin.smartconnect.core.domain.model

data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val rssi: Int = 0,
    val type: DeviceType = DeviceType.UNKNOWN,
    val isPaired: Boolean = false,
    val isConnected: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
) {
    val signalStrength: SignalStrength
        get() = when {
            rssi >= -50 -> SignalStrength.EXCELLENT
            rssi >= -70 -> SignalStrength.GOOD
            rssi >= -85 -> SignalStrength.FAIR
            else -> SignalStrength.WEAK
        }
}

enum class DeviceType {
    PHONE, COMPUTER, AUDIO, PERIPHERAL, WEARABLE, UNKNOWN
}

enum class SignalStrength {
    EXCELLENT, GOOD, FAIR, WEAK
}

package com.wreck2053.essentialkey

enum class PressAction {
    SINGLE,
    DOUBLE,
    LONG,
}

data class ActionConfig(
    val method: String = "GET",
    val url: String = "",
)

data class KeyIdentity(
    val keyCode: Int,
    val scanCode: Int,
    val source: Int,
    val deviceId: Int,
    val descriptor: String,
    val vendorId: Int,
    val productId: Int,
) {
    fun matches(other: KeyIdentity): Boolean {
        if (keyCode != other.keyCode) return false
        if (scanCode != 0 || other.scanCode != 0) {
            return scanCode == other.scanCode && source == other.source
        }
        if (descriptor.isNotBlank() && other.descriptor.isNotBlank()) {
            return descriptor == other.descriptor && source == other.source
        }
        return deviceId == other.deviceId && source == other.source
    }

    fun displayText(): String = buildString {
        append("keyCode=").append(keyCode)
        append(", scanCode=").append(scanCode)
        append(", source=0x").append(source.toString(16))
        append("\ndeviceId=").append(deviceId)
        append(", vendor=").append(vendorId)
        append(", product=").append(productId)
        if (descriptor.isNotBlank()) append("\ndescriptor=").append(descriptor)
    }
}


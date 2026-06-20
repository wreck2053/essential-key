package com.wreck2053.essentialkey.domain

enum class PressAction {
    SINGLE,
    DOUBLE,
    LONG,
}

enum class RequestMethod {
    GET,
    POST,
}

enum class HapticStrength {
    OFF,
    LIGHT,
    MEDIUM,
    STRONG,
}

data class ActionSettings(
    val method: RequestMethod = RequestMethod.GET,
    val url: String = "",
    val hapticStrength: HapticStrength = HapticStrength.MEDIUM,
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

    fun summary(): String = if (keyCode == 0) {
        "Unknown key · scan code $scanCode"
    } else {
        "Key code $keyCode · scan code $scanCode"
    }

    fun technicalDetails(): String = buildString {
        append("keyCode=").append(keyCode)
        append(", scanCode=").append(scanCode)
        append(", source=0x").append(source.toString(16))
        append("\ndeviceId=").append(deviceId)
        append(", vendor=").append(vendorId)
        append(", product=").append(productId)
        if (descriptor.isNotBlank()) append("\ndescriptor=").append(descriptor)
    }
}

data class AppSettings(
    val mappedKey: KeyIdentity? = null,
    val actions: Map<PressAction, ActionSettings> = defaultActions(),
    val results: Map<PressAction, String?> = PressAction.entries.associateWith { null },
    val learning: Boolean = false,
) {
    companion object {
        fun defaultActions(): Map<PressAction, ActionSettings> =
            PressAction.entries.associateWith { ActionSettings() }
    }
}


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
    val baseUrl: String = DEFAULT_BASE_URL,
    val hapticStrength: HapticStrength = HapticStrength.MEDIUM,
    val actions: Map<PressAction, ActionSettings> = defaultActions(),
    val results: Map<PressAction, String?> = PressAction.entries.associateWith { null },
    val learning: Boolean = false,
) {
    companion object {
        const val DEFAULT_BASE_URL = "http://home-automation.local"

        fun defaultActions(): Map<PressAction, ActionSettings> =
            mapOf(
                PressAction.SINGLE to ActionSettings(url = "/toggle-light"),
                PressAction.DOUBLE to ActionSettings(url = "/preset-ac"),
                PressAction.LONG to ActionSettings(url = "/toggle-fan"),
            )
    }
}

object ActionUrlResolver {
    fun resolve(baseUrl: String, endpoint: String): String {
        val trimmedEndpoint = endpoint.trim()
        if (trimmedEndpoint.isBlank()) return ""
        if (trimmedEndpoint.startsWith("http://") || trimmedEndpoint.startsWith("https://")) {
            return trimmedEndpoint
        }
        return "${baseUrl.trim().trimEnd('/')}/${trimmedEndpoint.trimStart('/')}"
    }
}

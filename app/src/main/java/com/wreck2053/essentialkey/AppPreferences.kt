package com.wreck2053.essentialkey

import android.content.Context

class AppPreferences(context: Context) {
    private val preferences = context.getSharedPreferences("essential_key", Context.MODE_PRIVATE)

    var learning: Boolean
        get() = preferences.getBoolean("learning", false)
        set(value) = preferences.edit().putBoolean("learning", value).apply()

    fun saveKey(identity: KeyIdentity) {
        preferences.edit()
            .putBoolean(KEY_PRESENT, true)
            .putInt("key_code", identity.keyCode)
            .putInt("scan_code", identity.scanCode)
            .putInt("source", identity.source)
            .putInt("device_id", identity.deviceId)
            .putString("descriptor", identity.descriptor)
            .putInt("vendor_id", identity.vendorId)
            .putInt("product_id", identity.productId)
            .putBoolean("learning", false)
            .apply()
    }

    fun loadKey(): KeyIdentity? {
        if (!preferences.getBoolean(KEY_PRESENT, false)) return null
        return KeyIdentity(
            keyCode = preferences.getInt("key_code", 0),
            scanCode = preferences.getInt("scan_code", 0),
            source = preferences.getInt("source", 0),
            deviceId = preferences.getInt("device_id", -1),
            descriptor = preferences.getString("descriptor", "").orEmpty(),
            vendorId = preferences.getInt("vendor_id", 0),
            productId = preferences.getInt("product_id", 0),
        )
    }

    fun saveAction(action: PressAction, config: ActionConfig) {
        preferences.edit()
            .putString("${action.name}_method", config.method)
            .putString("${action.name}_url", config.url.trim())
            .apply()
    }

    fun loadAction(action: PressAction): ActionConfig = ActionConfig(
        method = preferences.getString("${action.name}_method", "GET") ?: "GET",
        url = preferences.getString("${action.name}_url", "").orEmpty(),
    )

    fun saveResult(action: PressAction, result: String) {
        preferences.edit().putString("${action.name}_result", result).apply()
    }

    fun loadResult(action: PressAction): String? =
        preferences.getString("${action.name}_result", null)

    companion object {
        private const val KEY_PRESENT = "key_present"
    }
}


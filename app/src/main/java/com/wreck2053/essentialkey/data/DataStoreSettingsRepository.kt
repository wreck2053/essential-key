package com.wreck2053.essentialkey.data

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.toMutablePreferences
import androidx.datastore.preferences.preferencesDataStore
import com.wreck2053.essentialkey.domain.ActionSettings
import com.wreck2053.essentialkey.domain.AppSettings
import com.wreck2053.essentialkey.domain.HapticStrength
import com.wreck2053.essentialkey.domain.KeyIdentity
import com.wreck2053.essentialkey.domain.PressAction
import com.wreck2053.essentialkey.domain.RequestMethod
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private const val STORE_NAME = "essential_key_settings"
private const val LEGACY_STORE_NAME = "essential_key"

private val Context.settingsDataStore by preferencesDataStore(
    name = STORE_NAME,
    produceMigrations = { context -> listOf(LegacySettingsMigration(context)) },
)

class DataStoreSettingsRepository(context: Context) : SettingsRepository {
    private val dataStore = context.applicationContext.settingsDataStore

    override val settings: Flow<AppSettings> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map(::preferencesToSettings)

    override suspend fun saveActions(actions: Map<PressAction, ActionSettings>) {
        dataStore.edit { preferences ->
            PressAction.entries.forEach { action ->
                val config = actions.getValue(action)
                preferences[Keys.method(action)] = config.method.name
                preferences[Keys.url(action)] = config.url.trim()
                preferences[Keys.haptic(action)] = config.hapticStrength.name
            }
        }
    }

    override suspend fun setLearning(learning: Boolean) {
        dataStore.edit { it[Keys.LEARNING] = learning }
    }

    override suspend fun saveMappedKey(identity: KeyIdentity) {
        dataStore.edit { preferences ->
            preferences[Keys.KEY_PRESENT] = true
            preferences[Keys.KEY_CODE] = identity.keyCode
            preferences[Keys.SCAN_CODE] = identity.scanCode
            preferences[Keys.SOURCE] = identity.source
            preferences[Keys.DEVICE_ID] = identity.deviceId
            preferences[Keys.DESCRIPTOR] = identity.descriptor
            preferences[Keys.VENDOR_ID] = identity.vendorId
            preferences[Keys.PRODUCT_ID] = identity.productId
            preferences[Keys.LEARNING] = false
        }
    }

    override suspend fun saveResult(action: PressAction, result: String) {
        dataStore.edit { it[Keys.result(action)] = result }
    }

}

internal fun preferencesToSettings(preferences: Preferences): AppSettings {
    val mappedKey = if (preferences[Keys.KEY_PRESENT] == true) {
        KeyIdentity(
            keyCode = preferences[Keys.KEY_CODE] ?: 0,
            scanCode = preferences[Keys.SCAN_CODE] ?: 0,
            source = preferences[Keys.SOURCE] ?: 0,
            deviceId = preferences[Keys.DEVICE_ID] ?: -1,
            descriptor = preferences[Keys.DESCRIPTOR].orEmpty(),
            vendorId = preferences[Keys.VENDOR_ID] ?: 0,
            productId = preferences[Keys.PRODUCT_ID] ?: 0,
        )
    } else {
        null
    }
    val actions = PressAction.entries.associateWith { action ->
        ActionSettings(
            method = preferences[Keys.method(action)].toEnumOrDefault(RequestMethod.GET),
            url = preferences[Keys.url(action)].orEmpty(),
            hapticStrength = preferences[Keys.haptic(action)].toEnumOrDefault(HapticStrength.MEDIUM),
        )
    }
    val results = PressAction.entries.associateWith { preferences[Keys.result(it)] }
    return AppSettings(mappedKey, actions, results, preferences[Keys.LEARNING] ?: false)
}

private object Keys {
    val MIGRATED = booleanPreferencesKey("legacy_migrated")
    val LEARNING = booleanPreferencesKey("learning")
    val KEY_PRESENT = booleanPreferencesKey("key_present")
    val KEY_CODE = intPreferencesKey("key_code")
    val SCAN_CODE = intPreferencesKey("scan_code")
    val SOURCE = intPreferencesKey("source")
    val DEVICE_ID = intPreferencesKey("device_id")
    val DESCRIPTOR = stringPreferencesKey("descriptor")
    val VENDOR_ID = intPreferencesKey("vendor_id")
    val PRODUCT_ID = intPreferencesKey("product_id")
    fun method(action: PressAction) = stringPreferencesKey("${action.name}_method")
    fun url(action: PressAction) = stringPreferencesKey("${action.name}_url")
    fun haptic(action: PressAction) = stringPreferencesKey("${action.name}_haptic")
    fun result(action: PressAction) = stringPreferencesKey("${action.name}_result")
}

private class LegacySettingsMigration(context: Context) : DataMigration<Preferences> {
    private val legacy = context.getSharedPreferences(LEGACY_STORE_NAME, Context.MODE_PRIVATE)

    override suspend fun shouldMigrate(currentData: Preferences): Boolean =
        currentData[Keys.MIGRATED] != true && legacy.all.isNotEmpty()

    override suspend fun migrate(currentData: Preferences): Preferences {
        val migrated = currentData.toMutablePreferences()
        migrated[Keys.MIGRATED] = true
        copyBoolean(migrated, "learning", Keys.LEARNING)
        copyBoolean(migrated, "key_present", Keys.KEY_PRESENT)
        copyInt(migrated, "key_code", Keys.KEY_CODE)
        copyInt(migrated, "scan_code", Keys.SCAN_CODE)
        copyInt(migrated, "source", Keys.SOURCE)
        copyInt(migrated, "device_id", Keys.DEVICE_ID)
        copyString(migrated, "descriptor", Keys.DESCRIPTOR)
        copyInt(migrated, "vendor_id", Keys.VENDOR_ID)
        copyInt(migrated, "product_id", Keys.PRODUCT_ID)
        PressAction.entries.forEach { action ->
            copyString(migrated, "${action.name}_method", Keys.method(action))
            copyString(migrated, "${action.name}_url", Keys.url(action))
            copyString(migrated, "${action.name}_result", Keys.result(action))
        }
        return migrated
    }

    override suspend fun cleanUp() = Unit

    private fun copyBoolean(target: androidx.datastore.preferences.core.MutablePreferences, old: String, new: Preferences.Key<Boolean>) {
        if (legacy.contains(old)) target[new] = legacy.getBoolean(old, false)
    }

    private fun copyInt(target: androidx.datastore.preferences.core.MutablePreferences, old: String, new: Preferences.Key<Int>) {
        if (legacy.contains(old)) target[new] = legacy.getInt(old, 0)
    }

    private fun copyString(target: androidx.datastore.preferences.core.MutablePreferences, old: String, new: Preferences.Key<String>) {
        legacy.getString(old, null)?.let { target[new] = it }
    }
}

private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
    this?.let { value -> enumValues<T>().firstOrNull { it.name == value } } ?: default

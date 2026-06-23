package com.wreck2053.essentialkey

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wreck2053.essentialkey.data.preferencesToSettings
import com.wreck2053.essentialkey.domain.ConfiguredAction
import com.wreck2053.essentialkey.domain.HapticStrength
import com.wreck2053.essentialkey.domain.PressAction
import com.wreck2053.essentialkey.domain.RequestMethod
import com.wreck2053.essentialkey.domain.SoundMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class DataStoreSettingsMapperTest {
    @Test
    fun storedValuesMapToTypedSettings() {
        val preferences = mutablePreferencesOf(
            booleanPreferencesKey("key_present") to true,
            intPreferencesKey("key_code") to 0,
            intPreferencesKey("scan_code") to 703,
            stringPreferencesKey("SINGLE_method") to "POST",
            stringPreferencesKey("SINGLE_url") to "http://192.168.1.5/hook",
            stringPreferencesKey("SINGLE_haptic") to "STRONG",
            stringPreferencesKey("base_url") to "http://home-automation.local",
        )

        val settings = preferencesToSettings(preferences)

        assertEquals(703, settings.mappedKey?.scanCode)
        val single = settings.actions.getValue(PressAction.SINGLE) as ConfiguredAction.Http
        assertEquals(RequestMethod.POST, single.method)
        assertEquals("http://home-automation.local", single.baseUrl)
        assertEquals(HapticStrength.STRONG, settings.hapticStrength)
        val double = settings.actions.getValue(PressAction.DOUBLE) as ConfiguredAction.Http
        assertEquals("/preset-ac", double.endpoint)
        assertEquals("http://home-automation.local", double.baseUrl)
        assertNotNull(settings.results)
    }

    @Test
    fun typedActionValuesMapFromPreferences() {
        val preferences = mutablePreferencesOf(
            stringPreferencesKey("DOUBLE_action_type") to "LAUNCH_APP",
            stringPreferencesKey("DOUBLE_action_value") to "com.example.camera",
            stringPreferencesKey("DOUBLE_action_label") to "Camera",
            stringPreferencesKey("SINGLE_action_type") to "HTTP",
            stringPreferencesKey("SINGLE_action_value") to "/single",
            stringPreferencesKey("SINGLE_http_base_url") to "https://single.example",
        )

        val settings = preferencesToSettings(preferences)

        assertEquals(
            ConfiguredAction.LaunchApp("com.example.camera", "Camera"),
            settings.actions.getValue(PressAction.DOUBLE),
        )
        assertEquals(
            ConfiguredAction.Http(baseUrl = "https://single.example", endpoint = "/single"),
            settings.actions.getValue(PressAction.SINGLE),
        )
    }

    @Test
    fun legacyToggleSilentActionMigratesIntoSoundMode() {
        val preferences = mutablePreferencesOf(
            stringPreferencesKey("LONG_action_type") to "TOGGLE_SILENT",
        )

        val settings = preferencesToSettings(preferences)

        assertEquals(
            ConfiguredAction.SetSoundMode(SoundMode.TOGGLE_SILENT_NORMAL),
            settings.actions.getValue(PressAction.LONG),
        )
    }
}

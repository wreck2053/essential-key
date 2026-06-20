package com.wreck2053.essentialkey

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.wreck2053.essentialkey.data.preferencesToSettings
import com.wreck2053.essentialkey.domain.HapticStrength
import com.wreck2053.essentialkey.domain.PressAction
import com.wreck2053.essentialkey.domain.RequestMethod
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
        )

        val settings = preferencesToSettings(preferences)

        assertEquals(703, settings.mappedKey?.scanCode)
        assertEquals(RequestMethod.POST, settings.actions.getValue(PressAction.SINGLE).method)
        assertEquals(HapticStrength.STRONG, settings.actions.getValue(PressAction.SINGLE).hapticStrength)
        assertEquals(HapticStrength.MEDIUM, settings.actions.getValue(PressAction.DOUBLE).hapticStrength)
        assertNotNull(settings.results)
    }
}


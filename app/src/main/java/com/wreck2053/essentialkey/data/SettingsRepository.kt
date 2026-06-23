package com.wreck2053.essentialkey.data

import com.wreck2053.essentialkey.domain.AppSettings
import com.wreck2053.essentialkey.domain.ConfiguredAction
import com.wreck2053.essentialkey.domain.HapticStrength
import com.wreck2053.essentialkey.domain.KeyIdentity
import com.wreck2053.essentialkey.domain.PressAction
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun saveConfiguration(
        hapticStrength: HapticStrength,
        actions: Map<PressAction, ConfiguredAction>,
    )
    suspend fun setLearning(learning: Boolean)
    suspend fun saveMappedKey(identity: KeyIdentity)
    suspend fun saveResult(action: PressAction, result: String)
}

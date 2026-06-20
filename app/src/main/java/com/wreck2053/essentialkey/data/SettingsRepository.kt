package com.wreck2053.essentialkey.data

import com.wreck2053.essentialkey.domain.ActionSettings
import com.wreck2053.essentialkey.domain.AppSettings
import com.wreck2053.essentialkey.domain.KeyIdentity
import com.wreck2053.essentialkey.domain.PressAction
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun saveActions(actions: Map<PressAction, ActionSettings>)
    suspend fun setLearning(learning: Boolean)
    suspend fun saveMappedKey(identity: KeyIdentity)
    suspend fun saveResult(action: PressAction, result: String)
}


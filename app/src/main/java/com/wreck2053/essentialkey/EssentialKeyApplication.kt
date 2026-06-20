package com.wreck2053.essentialkey

import android.app.Application
import com.wreck2053.essentialkey.data.DataStoreSettingsRepository
import com.wreck2053.essentialkey.data.SettingsRepository
import com.wreck2053.essentialkey.haptics.AndroidHapticEngine
import com.wreck2053.essentialkey.haptics.HapticEngine

class EssentialKeyApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(
            repository = DataStoreSettingsRepository(this),
            hapticEngine = AndroidHapticEngine(this),
        )
    }
}

data class AppContainer(
    val repository: SettingsRepository,
    val hapticEngine: HapticEngine,
)


package com.wreck2053.essentialkey

import android.app.Application
import com.wreck2053.essentialkey.data.DataStoreSettingsRepository
import com.wreck2053.essentialkey.data.SettingsRepository
import com.wreck2053.essentialkey.haptics.AndroidHapticEngine
import com.wreck2053.essentialkey.haptics.HapticEngine
import com.wreck2053.essentialkey.platform.LaunchableAppsReader
import com.wreck2053.essentialkey.platform.TorchController
import com.wreck2053.essentialkey.setup.EssentialKeySetupCoordinator

class EssentialKeyApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val torchController = TorchController(this)
        container = AppContainer(
            repository = DataStoreSettingsRepository(this),
            hapticEngine = AndroidHapticEngine(this),
            torchController = torchController,
            setupCoordinator = EssentialKeySetupCoordinator(this),
            launchableAppsReader = LaunchableAppsReader(this),
        )
    }
}

data class AppContainer(
    val repository: SettingsRepository,
    val hapticEngine: HapticEngine,
    val torchController: TorchController,
    val setupCoordinator: EssentialKeySetupCoordinator,
    val launchableAppsReader: LaunchableAppsReader,
)

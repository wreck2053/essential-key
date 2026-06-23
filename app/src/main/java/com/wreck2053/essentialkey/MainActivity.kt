package com.wreck2053.essentialkey

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import com.wreck2053.essentialkey.platform.AccessibilityStatusReader
import com.wreck2053.essentialkey.setup.PackageOperation
import com.wreck2053.essentialkey.ui.EssentialKeyTheme
import com.wreck2053.essentialkey.ui.MapperRoute
import com.wreck2053.essentialkey.ui.MapperViewModel

class MainActivity : ComponentActivity() {
    private val container get() = (application as EssentialKeyApplication).container
    private val viewModel: MapperViewModel by viewModels {
        MapperViewModel.Factory(
            container.repository,
            container.hapticEngine,
            container.setupCoordinator,
            container.launchableAppsReader,
        )
    }
    private val accessibilityStatusReader by lazy { AccessibilityStatusReader(this) }
    private var pendingPackageOperation: PackageOperation? = null

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        val operation = pendingPackageOperation.also { pendingPackageOperation = null }
        if (granted && operation != null) {
            startWirelessSetup(operation)
        } else if (!granted) {
            viewModel.showMessage("Notification permission is required to enter the pairing code without closing Android’s pairing dialog")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EssentialKeyTheme {
                MapperRoute(
                    viewModel = viewModel,
                    openAppInfo = {
                        startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:$packageName")
                            },
                        )
                    },
                    openAccessibilitySettings = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    },
                    openNotificationPolicySettings = {
                        openNotificationPolicySettings()
                    },
                    openAboutPhone = {
                        startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS))
                    },
                    openDeveloperOptions = {
                        startActivity(developerOptionsIntent())
                    },
                    beginPackageSetup = ::beginPackageSetup,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshSetup()
        viewModel.updateAccessibilityStatus(accessibilityStatusReader.read())
        val notificationManager = getSystemService(NotificationManager::class.java)
        viewModel.updateNotificationPolicyAccess(
            notificationManager.isNotificationPolicyAccessGranted,
        )
        viewModel.updateDeveloperOptionsStatus(
            Settings.Global.getInt(
                contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0,
            ) == 1,
        )
    }

    private fun beginPackageSetup(operation: PackageOperation) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingPackageOperation = operation
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        startWirelessSetup(operation)
    }

    private fun startWirelessSetup(operation: PackageOperation) {
        viewModel.startPackageSetup(operation)
        val wirelessDebugging = Intent(ACTION_WIRELESS_DEBUGGING_SETTINGS)
        val intent = if (wirelessDebugging.resolveActivity(packageManager) != null) {
            wirelessDebugging
        } else {
            developerOptionsIntent()
        }
        startActivity(intent)
    }

    private fun developerOptionsIntent() =
        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).putExtra(
            SETTINGS_FRAGMENT_ARGUMENT_KEY,
            WIRELESS_DEBUGGING_PREFERENCE_KEY,
        )

    private fun openNotificationPolicySettings() {
        val detailIntent = Intent(
            ACTION_NOTIFICATION_POLICY_ACCESS_DETAIL_SETTINGS,
            Uri.parse("package:$packageName"),
        ).putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        if (detailIntent.resolveActivity(packageManager) != null &&
            runCatching { startActivity(detailIntent) }.isSuccess
        ) {
            return
        }
        startActivity(
            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, packageName),
        )
    }

    private companion object {
        const val ACTION_WIRELESS_DEBUGGING_SETTINGS = "android.settings.WIRELESS_DEBUGGING_SETTINGS"
        const val ACTION_NOTIFICATION_POLICY_ACCESS_DETAIL_SETTINGS =
            "android.settings.NOTIFICATION_POLICY_ACCESS_DETAIL_SETTINGS"
        const val SETTINGS_FRAGMENT_ARGUMENT_KEY = ":settings:fragment_args_key"
        const val WIRELESS_DEBUGGING_PREFERENCE_KEY = "toggle_adb_wireless"
    }
}

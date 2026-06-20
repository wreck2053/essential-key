package com.wreck2053.essentialkey

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.wreck2053.essentialkey.platform.AccessibilityStatusReader
import com.wreck2053.essentialkey.ui.EssentialKeyTheme
import com.wreck2053.essentialkey.ui.MapperRoute
import com.wreck2053.essentialkey.ui.MapperViewModel

class MainActivity : ComponentActivity() {
    private val container get() = (application as EssentialKeyApplication).container
    private val viewModel: MapperViewModel by viewModels {
        MapperViewModel.Factory(container.repository, container.hapticEngine)
    }
    private val accessibilityStatusReader by lazy { AccessibilityStatusReader(this) }

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
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateAccessibilityStatus(accessibilityStatusReader.read())
    }
}

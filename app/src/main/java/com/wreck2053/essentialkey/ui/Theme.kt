package com.wreck2053.essentialkey.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val FallbackLightColors = lightColorScheme(
    primary = Color(0xFF006A67),
    secondary = Color(0xFF4A6361),
    tertiary = Color(0xFF49617A),
)

private val FallbackDarkColors = darkColorScheme(
    primary = Color(0xFF4FDAD5),
    secondary = Color(0xFFB1CCC9),
    tertiary = Color(0xFFB1C9E8),
)

@Composable
fun EssentialKeyTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val context = LocalContext.current
    val colors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) FallbackDarkColors else FallbackLightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}


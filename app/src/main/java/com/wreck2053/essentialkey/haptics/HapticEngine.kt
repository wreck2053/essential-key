package com.wreck2053.essentialkey.haptics

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import com.wreck2053.essentialkey.domain.HapticStrength

enum class HapticResult {
    PLAYED,
    OFF,
    UNAVAILABLE,
    SYSTEM_DISABLED,
    FAILED,
}

interface HapticEngine {
    fun perform(strength: HapticStrength): HapticResult
}

class AndroidHapticEngine(context: Context) : HapticEngine {
    private val appContext = context.applicationContext

    @Suppress("DEPRECATION")
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        appContext.getSystemService(VibratorManager::class.java).defaultVibrator
    } else {
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun perform(strength: HapticStrength): HapticResult {
        if (strength == HapticStrength.OFF) return HapticResult.OFF
        if (!vibrator.hasVibrator()) return HapticResult.UNAVAILABLE
        val enabled = Settings.System.getInt(
            appContext.contentResolver,
            Settings.System.HAPTIC_FEEDBACK_ENABLED,
            1,
        ) != 0
        if (!enabled) return HapticResult.SYSTEM_DISABLED

        return runCatching {
            val effect = createEffect(strength)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                vibrator.vibrate(
                    effect,
                    VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ACCESSIBILITY),
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(
                    effect,
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .build(),
                )
            }
            HapticResult.PLAYED
        }.getOrDefault(HapticResult.FAILED)
    }

    private fun createEffect(strength: HapticStrength): VibrationEffect {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(HapticEffectSelector.effectId(strength))
        } else {
            val (duration, amplitude) = when (strength) {
                HapticStrength.LIGHT -> 10L to 80
                HapticStrength.MEDIUM -> 15L to 160
                HapticStrength.STRONG -> 20L to 255
                HapticStrength.OFF -> error("Off has no vibration effect")
            }
            VibrationEffect.createOneShot(duration, amplitude)
        }
    }
}

object HapticEffectSelector {
    fun effectId(strength: HapticStrength): Int = when (strength) {
        HapticStrength.OFF -> error("Off has no vibration effect")
        HapticStrength.LIGHT -> VibrationEffect.EFFECT_TICK
        HapticStrength.MEDIUM -> VibrationEffect.EFFECT_CLICK
        HapticStrength.STRONG -> VibrationEffect.EFFECT_HEAVY_CLICK
    }
}

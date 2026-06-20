package com.wreck2053.essentialkey

import android.os.VibrationEffect
import com.wreck2053.essentialkey.domain.HapticStrength
import com.wreck2053.essentialkey.haptics.HapticEffectSelector
import org.junit.Assert.assertEquals
import org.junit.Test

class HapticEffectSelectorTest {
    @Test
    fun strengthsMapToPlatformEffects() {
        assertEquals(VibrationEffect.EFFECT_TICK, HapticEffectSelector.effectId(HapticStrength.LIGHT))
        assertEquals(VibrationEffect.EFFECT_CLICK, HapticEffectSelector.effectId(HapticStrength.MEDIUM))
        assertEquals(VibrationEffect.EFFECT_HEAVY_CLICK, HapticEffectSelector.effectId(HapticStrength.STRONG))
    }

    @Test(expected = IllegalStateException::class)
    fun offHasNoEffect() {
        HapticEffectSelector.effectId(HapticStrength.OFF)
    }
}

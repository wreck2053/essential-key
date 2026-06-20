package com.wreck2053.essentialkey

import com.wreck2053.essentialkey.domain.ActionUrlResolver
import com.wreck2053.essentialkey.domain.AppSettings
import com.wreck2053.essentialkey.domain.PressAction
import org.junit.Assert.assertEquals
import org.junit.Test

class ActionUrlResolverTest {
    @Test
    fun defaultsMatchHomeAutomationRoutes() {
        val settings = AppSettings()

        assertEquals("/toggle-light", settings.actions.getValue(PressAction.SINGLE).url)
        assertEquals("/preset-ac", settings.actions.getValue(PressAction.DOUBLE).url)
        assertEquals("/toggle-fan", settings.actions.getValue(PressAction.LONG).url)
        assertEquals("http://192.168.0.108", settings.baseUrl)
    }

    @Test
    fun relativePathIsCombinedWithBaseUrl() {
        assertEquals(
            "http://192.168.0.108/toggle-light",
            ActionUrlResolver.resolve("http://192.168.0.108/", "/toggle-light"),
        )
    }

    @Test
    fun completeUrlOverridesBaseUrl() {
        assertEquals(
            "http://192.168.1.10/custom",
            ActionUrlResolver.resolve(
                "http://192.168.0.108",
                "http://192.168.1.10/custom",
            ),
        )
    }
}

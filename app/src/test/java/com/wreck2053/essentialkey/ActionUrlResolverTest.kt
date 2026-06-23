package com.wreck2053.essentialkey

import com.wreck2053.essentialkey.domain.ActionUrlResolver
import com.wreck2053.essentialkey.domain.AppSettings
import com.wreck2053.essentialkey.domain.ConfiguredAction
import com.wreck2053.essentialkey.domain.PressAction
import org.junit.Assert.assertEquals
import org.junit.Test

class ActionUrlResolverTest {
    @Test
    fun defaultsMatchHomeAutomationRoutes() {
        val settings = AppSettings()

        assertEquals(
            "/toggle-light",
            (settings.actions.getValue(PressAction.SINGLE) as ConfiguredAction.Http).endpoint,
        )
        assertEquals(
            "/preset-ac",
            (settings.actions.getValue(PressAction.DOUBLE) as ConfiguredAction.Http).endpoint,
        )
        assertEquals(
            "/toggle-fan",
            (settings.actions.getValue(PressAction.LONG) as ConfiguredAction.Http).endpoint,
        )
        assertEquals(
            "http://192.168.0.108",
            (settings.actions.getValue(PressAction.SINGLE) as ConfiguredAction.Http).baseUrl,
        )
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

package com.wreck2053.essentialkey

import com.wreck2053.essentialkey.setup.NothingPackageCommands
import com.wreck2053.essentialkey.setup.PackageOperation
import org.junit.Assert.assertEquals
import org.junit.Test

class NothingPackageCommandsTest {
    @Test
    fun disableCommandsAreStrictlyAllowlisted() {
        assertEquals(
            listOf(
                "pm disable-user --user 0 com.nothing.ntessentialspace",
                "pm disable-user --user 0 com.nothing.ntessentialrecorder",
            ),
            NothingPackageCommands.commands(PackageOperation.DISABLE),
        )
    }

    @Test
    fun restoreCommandsAreStrictlyAllowlisted() {
        assertEquals(
            listOf(
                "pm enable --user 0 com.nothing.ntessentialspace",
                "pm enable --user 0 com.nothing.ntessentialrecorder",
            ),
            NothingPackageCommands.commands(PackageOperation.RESTORE),
        )
    }
}

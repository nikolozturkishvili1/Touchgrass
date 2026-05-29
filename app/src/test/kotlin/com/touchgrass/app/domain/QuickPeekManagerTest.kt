package com.touchgrass.app.domain

import com.touchgrass.app.data.local.PauseRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class QuickPeekManagerTest {
    private fun manager(enabled: MutableStateFlow<Boolean>): QuickPeekManager {
        val repo = mockk<PauseRepository>()
        every { repo.quickPeekEnabledFlow } returns enabled
        val scope = CoroutineScope(UnconfinedTestDispatcher())
        return QuickPeekManager(repo, scope)
    }

    @Test
    fun `disabled — never grants a peek`() =
        runTest {
            val manager = manager(MutableStateFlow(false))

            assertFalse(manager.checkAndConsumeQuickPeek("com.google.android.youtube"))
            assertFalse(manager.checkAndConsumeQuickPeek("com.google.android.youtube"))
        }

    @Test
    fun `enabled — first call for a package grants a peek`() =
        runTest {
            val manager = manager(MutableStateFlow(true))

            assertTrue(manager.checkAndConsumeQuickPeek("com.google.android.youtube"))
        }

    @Test
    fun `enabled — second call for the same package does not grant a peek`() =
        runTest {
            val manager = manager(MutableStateFlow(true))

            assertTrue(manager.checkAndConsumeQuickPeek("com.google.android.youtube"))
            assertFalse(manager.checkAndConsumeQuickPeek("com.google.android.youtube"))
        }

    @Test
    fun `enabled — different packages get independent peeks`() =
        runTest {
            val manager = manager(MutableStateFlow(true))

            assertTrue(manager.checkAndConsumeQuickPeek("com.google.android.youtube"))
            assertTrue(manager.checkAndConsumeQuickPeek("com.instagram.android"))
        }

    @Test
    fun `switching packages resets the prior package's peek state`() =
        runTest {
            val manager = manager(MutableStateFlow(true))

            assertTrue(manager.checkAndConsumeQuickPeek("com.google.android.youtube"))
            assertTrue(manager.checkAndConsumeQuickPeek("com.instagram.android"))
            // Coming back to YouTube — the IG switch reset the YT flag.
            assertTrue(manager.checkAndConsumeQuickPeek("com.google.android.youtube"))
            assertFalse(manager.checkAndConsumeQuickPeek("com.google.android.youtube"))
        }

    @Test
    fun `flipping enabled off clears existing peek state so flipping back on starts fresh`() =
        runTest {
            val enabled = MutableStateFlow(true)
            val manager = manager(enabled)

            assertTrue(manager.checkAndConsumeQuickPeek("com.instagram.android"))
            assertFalse(manager.checkAndConsumeQuickPeek("com.instagram.android"))

            enabled.value = false
            assertFalse(manager.checkAndConsumeQuickPeek("com.instagram.android"))

            enabled.value = true
            // Toggle-off cleared the consumption map; first call after re-enable is a fresh peek.
            assertTrue(manager.checkAndConsumeQuickPeek("com.instagram.android"))
        }
}

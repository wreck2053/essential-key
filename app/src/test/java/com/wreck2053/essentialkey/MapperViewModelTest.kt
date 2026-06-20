package com.wreck2053.essentialkey

import com.wreck2053.essentialkey.data.SettingsRepository
import com.wreck2053.essentialkey.domain.ActionSettings
import com.wreck2053.essentialkey.domain.AppSettings
import com.wreck2053.essentialkey.domain.HapticStrength
import com.wreck2053.essentialkey.domain.KeyIdentity
import com.wreck2053.essentialkey.domain.PressAction
import com.wreck2053.essentialkey.haptics.HapticEngine
import com.wreck2053.essentialkey.haptics.HapticResult
import com.wreck2053.essentialkey.ui.MapperViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapperViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun editsRemainDraftUntilSave() = runTest(dispatcher) {
        val repository = FakeRepository()
        val viewModel = MapperViewModel(repository, FakeHapticEngine())
        advanceUntilIdle()

        viewModel.updateUrl(PressAction.SINGLE, "http://192.168.1.10/hook")
        assertTrue(viewModel.uiState.value.dirty)
        assertEquals("", repository.state.value.actions.getValue(PressAction.SINGLE).url)

        viewModel.save()
        advanceUntilIdle()
        assertEquals("http://192.168.1.10/hook", repository.state.value.actions.getValue(PressAction.SINGLE).url)
        assertFalse(viewModel.uiState.value.dirty)
    }

    @Test
    fun invalidUrlIsNotSaved() = runTest(dispatcher) {
        val repository = FakeRepository()
        val viewModel = MapperViewModel(repository, FakeHapticEngine())
        advanceUntilIdle()

        viewModel.updateUrl(PressAction.LONG, "not a url")
        viewModel.save()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.validationErrors.containsKey(PressAction.LONG))
        assertEquals("", repository.state.value.actions.getValue(PressAction.LONG).url)
    }

    private class FakeRepository : SettingsRepository {
        val state = MutableStateFlow(AppSettings())
        override val settings = state
        override suspend fun saveActions(actions: Map<PressAction, ActionSettings>) {
            state.value = state.value.copy(actions = actions)
        }
        override suspend fun setLearning(learning: Boolean) {
            state.value = state.value.copy(learning = learning)
        }
        override suspend fun saveMappedKey(identity: KeyIdentity) {
            state.value = state.value.copy(mappedKey = identity, learning = false)
        }
        override suspend fun saveResult(action: PressAction, result: String) {
            state.value = state.value.copy(results = state.value.results + (action to result))
        }
    }

    private class FakeHapticEngine : HapticEngine {
        override fun perform(strength: HapticStrength) = HapticResult.PLAYED
    }
}

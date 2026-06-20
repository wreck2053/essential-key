package com.wreck2053.essentialkey.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wreck2053.essentialkey.data.SettingsRepository
import com.wreck2053.essentialkey.domain.ActionSettings
import com.wreck2053.essentialkey.domain.AppSettings
import com.wreck2053.essentialkey.domain.HapticStrength
import com.wreck2053.essentialkey.domain.PressAction
import com.wreck2053.essentialkey.domain.RequestMethod
import com.wreck2053.essentialkey.haptics.HapticEngine
import com.wreck2053.essentialkey.haptics.HapticResult
import com.wreck2053.essentialkey.platform.AccessibilityStatus
import java.net.URI
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapperUiState(
    val settings: AppSettings = AppSettings(),
    val draftActions: Map<PressAction, ActionSettings> = AppSettings.defaultActions(),
    val serviceEnabled: Boolean = false,
    val competingServices: List<String> = emptyList(),
    val validationErrors: Map<PressAction, String> = emptyMap(),
    val initialized: Boolean = false,
    val saving: Boolean = false,
) {
    val dirty: Boolean get() = draftActions != settings.actions
}

class MapperViewModel(
    private val repository: SettingsRepository,
    private val hapticEngine: HapticEngine,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MapperUiState())
    val uiState: StateFlow<MapperUiState> = _uiState.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val messages: SharedFlow<String> = _messages.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.settings.collect { settings ->
                _uiState.update { current ->
                    val draft = if (!current.initialized || !current.dirty) {
                        settings.actions
                    } else {
                        current.draftActions
                    }
                    current.copy(settings = settings, draftActions = draft, initialized = true)
                }
            }
        }
    }

    fun updateAccessibilityStatus(status: AccessibilityStatus) {
        _uiState.update {
            it.copy(
                serviceEnabled = status.serviceEnabled,
                competingServices = status.competingKeyServices,
            )
        }
        if (!status.serviceEnabled && _uiState.value.settings.learning) cancelLearning()
    }

    fun updateMethod(action: PressAction, method: RequestMethod) = updateAction(action) {
        copy(method = method)
    }

    fun updateUrl(action: PressAction, url: String) {
        updateAction(action) { copy(url = url) }
        _uiState.update { it.copy(validationErrors = it.validationErrors - action) }
    }

    fun updateHaptic(action: PressAction, strength: HapticStrength) = updateAction(action) {
        copy(hapticStrength = strength)
    }

    fun save() {
        val actions = _uiState.value.draftActions
        val errors = actions.mapNotNull { (action, config) ->
            validateUrl(config.url)?.let { action to it }
        }.toMap()
        if (errors.isNotEmpty()) {
            _uiState.update { it.copy(validationErrors = errors) }
            _messages.tryEmit("Fix the highlighted URLs before saving")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, validationErrors = emptyMap()) }
            runCatching { repository.saveActions(actions) }
                .onSuccess { _messages.emit("Actions saved") }
                .onFailure { _messages.emit("Could not save actions") }
            _uiState.update { it.copy(saving = false) }
        }
    }

    fun startLearning() {
        if (!_uiState.value.serviceEnabled) {
            _messages.tryEmit("Enable the accessibility service first")
            return
        }
        viewModelScope.launch { repository.setLearning(true) }
    }

    fun cancelLearning() {
        viewModelScope.launch { repository.setLearning(false) }
    }

    fun previewHaptic(strength: HapticStrength) {
        val message = when (hapticEngine.perform(strength)) {
            HapticResult.PLAYED -> null
            HapticResult.OFF -> "Haptic feedback is off"
            HapticResult.UNAVAILABLE -> "This device has no vibrator"
            HapticResult.SYSTEM_DISABLED -> "Enable touch feedback in system settings"
            HapticResult.FAILED -> "Haptic feedback could not be played"
        }
        if (message != null) _messages.tryEmit(message)
    }

    private fun updateAction(action: PressAction, transform: ActionSettings.() -> ActionSettings) {
        _uiState.update { current ->
            current.copy(
                draftActions = current.draftActions.toMutableMap().apply {
                    this[action] = getValue(action).transform()
                },
            )
        }
    }

    internal fun validateUrl(value: String): String? {
        if (value.isBlank()) return null
        return try {
            val uri = URI(value.trim())
            if (uri.scheme !in setOf("http", "https") || uri.host.isNullOrBlank()) {
                "Use a complete http:// or https:// URL"
            } else {
                null
            }
        } catch (_: Exception) {
            "Enter a valid URL"
        }
    }

    class Factory(
        private val repository: SettingsRepository,
        private val hapticEngine: HapticEngine,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            MapperViewModel(repository, hapticEngine) as T
    }
}


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
    val draftBaseUrl: String = AppSettings.DEFAULT_BASE_URL,
    val draftHapticStrength: HapticStrength = HapticStrength.MEDIUM,
    val draftActions: Map<PressAction, ActionSettings> = AppSettings.defaultActions(),
    val serviceEnabled: Boolean = false,
    val competingServices: List<String> = emptyList(),
    val baseUrlError: String? = null,
    val validationErrors: Map<PressAction, String> = emptyMap(),
    val initialized: Boolean = false,
    val saving: Boolean = false,
) {
    val dirty: Boolean get() =
        draftBaseUrl != settings.baseUrl ||
            draftHapticStrength != settings.hapticStrength ||
            draftActions != settings.actions

    val setupStatus: SetupStatus get() = when {
        competingServices.isNotEmpty() -> SetupStatus.CONFLICT
        serviceEnabled -> SetupStatus.READY
        else -> SetupStatus.REQUIRED
    }
}

enum class SetupStatus {
    REQUIRED,
    CONFLICT,
    READY,
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
                    current.copy(
                        settings = settings,
                        draftBaseUrl = if (!current.initialized || !current.dirty) settings.baseUrl else current.draftBaseUrl,
                        draftHapticStrength = if (!current.initialized || !current.dirty) settings.hapticStrength else current.draftHapticStrength,
                        draftActions = draft,
                        initialized = true,
                    )
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

    fun updateBaseUrl(baseUrl: String) {
        _uiState.update { it.copy(draftBaseUrl = baseUrl, baseUrlError = null) }
    }

    fun updateHaptic(strength: HapticStrength) {
        _uiState.update { it.copy(draftHapticStrength = strength) }
    }

    fun save() {
        val actions = _uiState.value.draftActions
        val baseUrlError = validateBaseUrl(_uiState.value.draftBaseUrl)
        val errors = actions.mapNotNull { (action, config) ->
            validateEndpoint(config.url)?.let { action to it }
        }.toMap()
        if (baseUrlError != null || errors.isNotEmpty()) {
            _uiState.update { it.copy(baseUrlError = baseUrlError, validationErrors = errors) }
            _messages.tryEmit(baseUrlError ?: "Fix the highlighted paths before saving")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, baseUrlError = null, validationErrors = emptyMap()) }
            runCatching {
                repository.saveConfiguration(
                    baseUrl = _uiState.value.draftBaseUrl,
                    hapticStrength = _uiState.value.draftHapticStrength,
                    actions = actions,
                )
            }
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
            HapticResult.PLAYED -> "${strength.displayName()} haptic played"
            HapticResult.OFF -> "Haptic feedback is off"
            HapticResult.UNAVAILABLE -> "This device has no vibrator"
            HapticResult.SYSTEM_DISABLED -> "Enable touch feedback in system settings"
            HapticResult.FAILED -> "Haptic feedback could not be played"
        }
        _messages.tryEmit(message)
    }

    private fun HapticStrength.displayName(): String =
        name.lowercase().replaceFirstChar(Char::uppercase)

    private fun updateAction(action: PressAction, transform: ActionSettings.() -> ActionSettings) {
        _uiState.update { current ->
            current.copy(
                draftActions = current.draftActions.toMutableMap().apply {
                    this[action] = getValue(action).transform()
                },
            )
        }
    }

    internal fun validateBaseUrl(value: String): String? {
        if (value.isBlank()) return "Enter the controller base URL"
        return validateAbsoluteUrl(value)
    }

    internal fun validateEndpoint(value: String): String? {
        if (value.isBlank()) return null
        if (value.startsWith("/")) return null
        if (!value.startsWith("http://") && !value.startsWith("https://")) {
            return "Start paths with / or enter a complete URL"
        }
        return validateAbsoluteUrl(value)
    }

    private fun validateAbsoluteUrl(value: String): String? {
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

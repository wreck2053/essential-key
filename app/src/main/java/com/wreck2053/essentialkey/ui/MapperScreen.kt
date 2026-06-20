package com.wreck2053.essentialkey.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import com.wreck2053.essentialkey.domain.ActionSettings
import com.wreck2053.essentialkey.domain.HapticStrength
import com.wreck2053.essentialkey.domain.PressAction
import com.wreck2053.essentialkey.domain.RequestMethod

@Composable
fun MapperRoute(
    viewModel: MapperViewModel,
    openAppInfo: () -> Unit,
    openAccessibilitySettings: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(viewModel) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }
    MapperScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        openAppInfo = openAppInfo,
        openAccessibilitySettings = openAccessibilitySettings,
        startLearning = viewModel::startLearning,
        cancelLearning = viewModel::cancelLearning,
        updateMethod = viewModel::updateMethod,
        updateUrl = viewModel::updateUrl,
        updateHaptic = viewModel::updateHaptic,
        previewHaptic = viewModel::previewHaptic,
        save = viewModel::save,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapperScreen(
    state: MapperUiState,
    snackbarHostState: SnackbarHostState,
    openAppInfo: () -> Unit,
    openAccessibilitySettings: () -> Unit,
    startLearning: () -> Unit,
    cancelLearning: () -> Unit,
    updateMethod: (PressAction, RequestMethod) -> Unit,
    updateUrl: (PressAction, String) -> Unit,
    updateHaptic: (PressAction, HapticStrength) -> Unit,
    previewHaptic: (HapticStrength) -> Unit,
    save: () -> Unit,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val expanded = windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND,
    )
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Essential Key") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            SaveBar(
                dirty = state.dirty,
                saving = state.saving,
                onSave = save,
            )
        },
    ) { scaffoldPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
        ) {
            val gutter = when {
                maxWidth >= 840.dp -> 32.dp
                maxWidth >= 600.dp -> 24.dp
                else -> 16.dp
            }
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 1200.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = gutter, vertical = 16.dp),
            ) {
                if (expanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column(
                            modifier = Modifier.weight(0.42f),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            SetupCard(state, openAppInfo, openAccessibilitySettings)
                            MappedKeyCard(state, startLearning, cancelLearning)
                        }
                        ActionColumn(
                            modifier = Modifier.weight(0.58f),
                            state = state,
                            updateMethod = updateMethod,
                            updateUrl = updateUrl,
                            updateHaptic = updateHaptic,
                            previewHaptic = previewHaptic,
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SetupCard(state, openAppInfo, openAccessibilitySettings)
                        MappedKeyCard(state, startLearning, cancelLearning)
                        ActionColumn(
                            state = state,
                            updateMethod = updateMethod,
                            updateUrl = updateUrl,
                            updateHaptic = updateHaptic,
                            previewHaptic = previewHaptic,
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SetupCard(
    state: MapperUiState,
    openAppInfo: () -> Unit,
    openAccessibilitySettings: () -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("Setup")
            StatusLine(
                success = state.serviceEnabled,
                successText = "Accessibility service enabled",
                failureText = "Accessibility service disabled",
            )
            if (!state.serviceEnabled) {
                Text(
                    "Sideloaded apps must first be allowed under restricted settings, then enabled in Accessibility.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedButton(onClick = openAppInfo, modifier = Modifier.fillMaxWidth()) {
                    Text("1. Allow restricted settings")
                }
                Button(onClick = openAccessibilitySettings, modifier = Modifier.fillMaxWidth()) {
                    Text("2. Enable accessibility service")
                }
            }
            if (state.competingServices.isNotEmpty()) {
                WarningPanel(
                    "Turn off ${state.competingServices.joinToString()} while using this app. Android sends hardware key events to only one filtering service.",
                )
            }
        }
    }
}

@Composable
private fun MappedKeyCard(
    state: MapperUiState,
    startLearning: () -> Unit,
    cancelLearning: () -> Unit,
) {
    var detailsExpanded by rememberSaveable { mutableStateOf(false) }
    OutlinedCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("Mapped button")
            if (state.settings.learning) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text("Press the hardware button once", style = MaterialTheme.typography.titleMedium)
                Text(
                    "The detected key will replace the current mapping.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = cancelLearning) { Text("Cancel") }
            } else {
                val key = state.settings.mappedKey
                Text(
                    key?.summary() ?: "No hardware button mapped",
                    style = MaterialTheme.typography.titleMedium,
                )
                Button(
                    onClick = startLearning,
                    enabled = state.serviceEnabled && state.competingServices.isEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (key == null) "Detect hardware button" else "Change hardware button")
                }
                if (key != null) {
                    TextButton(onClick = { detailsExpanded = !detailsExpanded }) {
                        Text(if (detailsExpanded) "Hide technical details" else "Show technical details")
                    }
                    if (detailsExpanded) {
                        Text(
                            key.technicalDetails(),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionColumn(
    state: MapperUiState,
    updateMethod: (PressAction, RequestMethod) -> Unit,
    updateUrl: (PressAction, String) -> Unit,
    updateHaptic: (PressAction, HapticStrength) -> Unit,
    previewHaptic: (HapticStrength) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SectionTitle("Actions")
        PressAction.entries.forEach { action ->
            ActionCard(
                action = action,
                settings = state.draftActions.getValue(action),
                result = state.settings.results[action],
                error = state.validationErrors[action],
                updateMethod = { updateMethod(action, it) },
                updateUrl = { updateUrl(action, it) },
                updateHaptic = { updateHaptic(action, it) },
                previewHaptic = previewHaptic,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionCard(
    action: PressAction,
    settings: ActionSettings,
    result: String?,
    error: String?,
    updateMethod: (RequestMethod) -> Unit,
    updateUrl: (String) -> Unit,
    updateHaptic: (HapticStrength) -> Unit,
    previewHaptic: (HapticStrength) -> Unit,
) {
    var hapticMenuExpanded by rememberSaveable(action) { mutableStateOf(false) }
    Card {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        action.badge(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Text(action.title(), style = MaterialTheme.typography.titleLarge)
            }
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                RequestMethod.entries.forEachIndexed { index, method ->
                    SegmentedButton(
                        selected = settings.method == method,
                        onClick = { updateMethod(method) },
                        shape = SegmentedButtonDefaults.itemShape(index, RequestMethod.entries.size),
                    ) {
                        Text(method.name)
                    }
                }
            }
            OutlinedTextField(
                value = settings.url,
                onValueChange = updateUrl,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Local URL") },
                placeholder = { Text("http://192.168.1.10/action") },
                supportingText = {
                    Text(error ?: "Leave blank to disable the HTTP request")
                },
                isError = error != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )
            ExposedDropdownMenuBox(
                expanded = hapticMenuExpanded,
                onExpandedChange = { hapticMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = settings.hapticStrength.displayName(),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text("Haptic feedback") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = hapticMenuExpanded)
                    },
                )
                ExposedDropdownMenu(
                    expanded = hapticMenuExpanded,
                    onDismissRequest = { hapticMenuExpanded = false },
                ) {
                    HapticStrength.entries.forEach { strength ->
                        DropdownMenuItem(
                            text = { Text(strength.displayName()) },
                            onClick = {
                                updateHaptic(strength)
                                hapticMenuExpanded = false
                            },
                        )
                    }
                }
            }
            TextButton(
                onClick = { previewHaptic(settings.hapticStrength) },
                enabled = settings.hapticStrength != HapticStrength.OFF,
                modifier = Modifier.align(Alignment.End),
            ) {
                Text("Preview haptic")
            }
            if (result != null) {
                HorizontalDivider()
                ResultLine(result)
            }
        }
    }
}

@Composable
private fun SaveBar(dirty: Boolean, saving: Boolean, onSave: () -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Box(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Button(
                onClick = onSave,
                enabled = dirty && !saving,
                modifier = Modifier.align(Alignment.Center).widthIn(max = 520.dp).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp, horizontal = 24.dp),
            ) {
                if (saving) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (dirty) "Save actions" else "All changes saved")
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.semantics { heading() },
    )
}

@Composable
private fun StatusLine(success: Boolean, successText: String, failureText: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (success) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.width(8.dp))
        Text(if (success) successText else failureText, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun WarningPanel(message: String) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(Modifier.width(8.dp))
            Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}

@Composable
private fun ResultLine(result: String) {
    val isError = result.contains("Error:")
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isError) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
        )
        Spacer(Modifier.width(8.dp))
        Text(result, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun PressAction.title(): String = when (this) {
    PressAction.SINGLE -> "Single press"
    PressAction.DOUBLE -> "Double press"
    PressAction.LONG -> "Long press"
}

private fun PressAction.badge(): String = when (this) {
    PressAction.SINGLE -> "1×"
    PressAction.DOUBLE -> "2×"
    PressAction.LONG -> "HOLD"
}

private fun HapticStrength.displayName(): String = name.lowercase().replaceFirstChar(Char::uppercase)

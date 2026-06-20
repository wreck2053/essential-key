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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
        startLearning = viewModel::startLearning,
        cancelLearning = viewModel::cancelLearning,
        updateMethod = viewModel::updateMethod,
        updateUrl = viewModel::updateUrl,
        updateBaseUrl = viewModel::updateBaseUrl,
        updateHaptic = viewModel::updateHaptic,
        previewHaptic = viewModel::previewHaptic,
        save = viewModel::save,
        setupClick = {
            when {
                state.serviceEnabled || state.competingServices.isNotEmpty() -> openAccessibilitySettings()
                state.setupStage == SetupStage.TRIGGER_RESTRICTION -> {
                    viewModel.advanceSetupStage()
                    openAccessibilitySettings()
                }
                state.setupStage == SetupStage.ALLOW_RESTRICTED -> {
                    viewModel.advanceSetupStage()
                    openAppInfo()
                }
                else -> openAccessibilitySettings()
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapperScreen(
    state: MapperUiState,
    snackbarHostState: SnackbarHostState,
    startLearning: () -> Unit,
    cancelLearning: () -> Unit,
    updateMethod: (PressAction, RequestMethod) -> Unit,
    updateUrl: (PressAction, String) -> Unit,
    updateBaseUrl: (String) -> Unit,
    updateHaptic: (HapticStrength) -> Unit,
    previewHaptic: (HapticStrength) -> Unit,
    save: () -> Unit,
    setupClick: () -> Unit,
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
                            SetupBar(state, setupClick)
                            MappedKeyCard(state, startLearning, cancelLearning)
                        }
                        ActionColumn(
                            modifier = Modifier.weight(0.58f),
                            state = state,
                            updateMethod = updateMethod,
                            updateUrl = updateUrl,
                            updateBaseUrl = updateBaseUrl,
                            updateHaptic = updateHaptic,
                            previewHaptic = previewHaptic,
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SetupBar(state, setupClick)
                        MappedKeyCard(state, startLearning, cancelLearning)
                        ActionColumn(
                            state = state,
                            updateMethod = updateMethod,
                            updateUrl = updateUrl,
                            updateBaseUrl = updateBaseUrl,
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
private fun SetupBar(state: MapperUiState, onClick: () -> Unit) {
    val complete = state.serviceEnabled && state.competingServices.isEmpty()
    val conflict = state.competingServices.isNotEmpty()
    val containerColor = when {
        complete -> Color(0xFF1B5E20)
        conflict -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val contentColor = when {
        complete -> Color.White
        conflict -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onTertiaryContainer
    }
    val title = when {
        complete -> "Setup complete"
        conflict -> "Turn off ${state.competingServices.joinToString()}"
        state.setupStage == SetupStage.TRIGGER_RESTRICTION -> "Trigger the access request"
        state.setupStage == SetupStage.ALLOW_RESTRICTED -> "Allow restricted settings"
        else -> "Enable accessibility service"
    }
    val subtitle = when {
        complete -> "Hardware button listener is active"
        conflict -> "Another service currently receives hardware keys"
        state.setupStage == SetupStage.TRIGGER_RESTRICTION -> "Step 1 of 3 · Tap the blocked listener once"
        state.setupStage == SetupStage.ALLOW_RESTRICTED -> "Step 2 of 3 · Use the App info ⋮ menu"
        else -> "Step 3 of 3 · Turn on the listener"
    }
    val step = when {
        complete -> 3
        state.setupStage == SetupStage.TRIGGER_RESTRICTION -> 1
        state.setupStage == SetupStage.ALLOW_RESTRICTED -> 2
        else -> 3
    }
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (complete) Icons.Default.CheckCircle else if (conflict) Icons.Default.Warning else Icons.Default.Error,
                    contentDescription = null,
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall)
                }
                SetupProgress(step = step, complete = complete, color = contentColor)
            }
            LinearProgressIndicator(
                progress = { step / 3f },
                modifier = Modifier.fillMaxWidth(),
                color = contentColor,
                trackColor = contentColor.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
private fun SetupProgress(step: Int, complete: Boolean, color: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { index ->
            Box(
                Modifier
                    .size(if (complete || index < step) 8.dp else 6.dp)
                    .background(
                        color.copy(alpha = if (complete || index < step) 1f else 0.35f),
                        MaterialTheme.shapes.extraSmall,
                    ),
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionColumn(
    state: MapperUiState,
    updateMethod: (PressAction, RequestMethod) -> Unit,
    updateUrl: (PressAction, String) -> Unit,
    updateBaseUrl: (String) -> Unit,
    updateHaptic: (HapticStrength) -> Unit,
    previewHaptic: (HapticStrength) -> Unit,
    modifier: Modifier = Modifier,
) {
    var hapticMenuExpanded by rememberSaveable { mutableStateOf(false) }
    Card(modifier) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            SectionTitle("Actions")
            OutlinedTextField(
                value = state.draftBaseUrl,
                onValueChange = updateBaseUrl,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Controller base URL") },
                placeholder = { Text("http://home-automation.local") },
                supportingText = state.baseUrlError?.let { error -> { Text(error) } },
                isError = state.baseUrlError != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ExposedDropdownMenuBox(
                    expanded = hapticMenuExpanded,
                    onExpandedChange = { hapticMenuExpanded = it },
                    modifier = Modifier.weight(1f),
                ) {
                    OutlinedTextField(
                        value = state.draftHapticStrength.displayName(),
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
                    onClick = { previewHaptic(state.draftHapticStrength) },
                    enabled = state.draftHapticStrength != HapticStrength.OFF,
                ) {
                    Text("Preview")
                }
            }
            PressAction.entries.forEach { action ->
                HorizontalDivider()
                CompactActionEditor(
                    action = action,
                    settings = state.draftActions.getValue(action),
                    result = state.settings.results[action],
                    error = state.validationErrors[action],
                    updateMethod = { updateMethod(action, it) },
                    updateUrl = { updateUrl(action, it) },
                )
            }
        }
    }
}

@Composable
private fun CompactActionEditor(
    action: PressAction,
    settings: ActionSettings,
    result: String?,
    error: String?,
    updateMethod: (RequestMethod) -> Unit,
    updateUrl: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            if (maxWidth >= 360.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ActionIdentity(action, Modifier.weight(1f))
                    MethodSelector(settings.method, updateMethod, Modifier.width(144.dp))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionIdentity(action)
                    MethodSelector(settings.method, updateMethod, Modifier.fillMaxWidth())
                }
            }
        }
        OutlinedTextField(
            value = settings.url,
            onValueChange = updateUrl,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Path or complete URL") },
            placeholder = { Text(action.defaultPath()) },
            supportingText = if (error != null) ({ Text(error) }) else null,
            isError = error != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        )
        if (result != null) {
            ResultLine(result)
        }
    }
}

@Composable
private fun ActionIdentity(action: PressAction, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                action.badge(),
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            action.title(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun MethodSelector(
    selectedMethod: RequestMethod,
    updateMethod: (RequestMethod) -> Unit,
    modifier: Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier) {
        RequestMethod.entries.forEachIndexed { index, method ->
            SegmentedButton(
                selected = selectedMethod == method,
                onClick = { updateMethod(method) },
                shape = SegmentedButtonDefaults.itemShape(index, RequestMethod.entries.size),
            ) {
                Text(method.name, style = MaterialTheme.typography.labelMedium)
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

private fun PressAction.defaultPath(): String = when (this) {
    PressAction.SINGLE -> "/toggle-light"
    PressAction.DOUBLE -> "/preset-ac"
    PressAction.LONG -> "/toggle-fan"
}

private fun HapticStrength.displayName(): String = name.lowercase().replaceFirstChar(Char::uppercase)

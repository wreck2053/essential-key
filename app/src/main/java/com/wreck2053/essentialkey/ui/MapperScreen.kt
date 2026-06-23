package com.wreck2053.essentialkey.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.graphics.drawable.toBitmap
import com.wreck2053.essentialkey.domain.ActionKind
import com.wreck2053.essentialkey.domain.ConfiguredAction
import com.wreck2053.essentialkey.domain.HapticStrength
import com.wreck2053.essentialkey.domain.PressAction
import com.wreck2053.essentialkey.domain.RequestMethod
import com.wreck2053.essentialkey.domain.SoundMode
import com.wreck2053.essentialkey.domain.SystemAction
import com.wreck2053.essentialkey.platform.LaunchableApp
import com.wreck2053.essentialkey.setup.NothingPackageStatus
import com.wreck2053.essentialkey.setup.PackageOperation
import com.wreck2053.essentialkey.setup.SetupPhase

@Composable
fun MapperRoute(
    viewModel: MapperViewModel,
    openAppInfo: () -> Unit,
    openAccessibilitySettings: () -> Unit,
    openNotificationPolicySettings: () -> Unit,
    openAboutPhone: () -> Unit,
    openDeveloperOptions: () -> Unit,
    beginPackageSetup: (PackageOperation) -> Unit,
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
        openNotificationPolicySettings = openNotificationPolicySettings,
        openAboutPhone = openAboutPhone,
        openDeveloperOptions = openDeveloperOptions,
        beginPackageSetup = beginPackageSetup,
        submitPairingCode = viewModel::submitPairingCode,
        cancelPackageSetup = viewModel::cancelPackageSetup,
        startLearning = viewModel::startLearning,
        cancelLearning = viewModel::cancelLearning,
        updateActionKind = viewModel::updateActionKind,
        updateHttpBaseUrl = viewModel::updateHttpBaseUrl,
        updateHttpMethod = viewModel::updateHttpMethod,
        updateActionValue = viewModel::updateActionValue,
        updateSoundMode = viewModel::updateSoundMode,
        updateSystemAction = viewModel::updateSystemAction,
        updateLaunchApp = viewModel::updateLaunchApp,
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
    openNotificationPolicySettings: () -> Unit,
    openAboutPhone: () -> Unit,
    openDeveloperOptions: () -> Unit,
    beginPackageSetup: (PackageOperation) -> Unit,
    submitPairingCode: (String) -> Unit,
    cancelPackageSetup: () -> Unit,
    startLearning: () -> Unit,
    cancelLearning: () -> Unit,
    updateActionKind: (PressAction, ActionKind) -> Unit,
    updateHttpBaseUrl: (PressAction, String) -> Unit,
    updateHttpMethod: (PressAction, RequestMethod) -> Unit,
    updateActionValue: (PressAction, String) -> Unit,
    updateSoundMode: (PressAction, SoundMode) -> Unit,
    updateSystemAction: (PressAction, SystemAction) -> Unit,
    updateLaunchApp: (PressAction, LaunchableApp) -> Unit,
    updateHaptic: (HapticStrength) -> Unit,
    previewHaptic: (HapticStrength) -> Unit,
    save: () -> Unit,
) {
    var packageConfirmation by remember { mutableStateOf<PackageOperation?>(null) }
    var showAccessibilityDisclosure by remember { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableStateOf(MapperTab.SETUP) }

    packageConfirmation?.let { operation ->
        PackageConfirmationDialog(
            operation = operation,
            confirm = {
                packageConfirmation = null
                beginPackageSetup(operation)
            },
            dismiss = { packageConfirmation = null },
        )
    }
    if (showAccessibilityDisclosure) {
        AccessibilityDisclosureDialog(
            confirm = {
                showAccessibilityDisclosure = false
                openAccessibilitySettings()
            },
            dismiss = { showAccessibilityDisclosure = false },
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Essential Key Remapper",
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Column {
                if (selectedTab == MapperTab.ACTIONS) {
                    SaveBar(state.dirty, state.saving, save)
                }
                NavigationBar {
                    NavigationBarItem(
                        selected = selectedTab == MapperTab.SETUP,
                        onClick = { selectedTab = MapperTab.SETUP },
                        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                        label = { Text("Setup") },
                    )
                    NavigationBarItem(
                        selected = selectedTab == MapperTab.ACTIONS,
                        onClick = { selectedTab = MapperTab.ACTIONS },
                        icon = { Icon(Icons.Default.Tune, contentDescription = null) },
                        label = { Text("Actions") },
                    )
                }
            }
        },
    ) { padding ->
        when (selectedTab) {
            MapperTab.SETUP -> SetupPage(
                modifier = Modifier.padding(padding),
                state = state,
                openAboutPhone = openAboutPhone,
                openDeveloperOptions = openDeveloperOptions,
                requestOperation = { packageConfirmation = it },
                submitPairingCode = submitPairingCode,
                cancelPackageSetup = cancelPackageSetup,
                openAppInfo = openAppInfo,
                openAccessibility = { showAccessibilityDisclosure = true },
                startLearning = startLearning,
                cancelLearning = cancelLearning,
            )
            MapperTab.ACTIONS -> ActionsPage(
                modifier = Modifier.padding(padding),
                state = state,
                openNotificationPolicySettings = openNotificationPolicySettings,
                updateActionKind = updateActionKind,
                updateHttpBaseUrl = updateHttpBaseUrl,
                updateHttpMethod = updateHttpMethod,
                updateActionValue = updateActionValue,
                updateSoundMode = updateSoundMode,
                updateSystemAction = updateSystemAction,
                updateLaunchApp = updateLaunchApp,
                updateHaptic = updateHaptic,
                previewHaptic = previewHaptic,
            )
        }
    }
}

private enum class MapperTab {
    SETUP,
    ACTIONS,
}

@Composable
private fun SetupPage(
    modifier: Modifier,
    state: MapperUiState,
    openAboutPhone: () -> Unit,
    openDeveloperOptions: () -> Unit,
    requestOperation: (PackageOperation) -> Unit,
    submitPairingCode: (String) -> Unit,
    cancelPackageSetup: () -> Unit,
    openAppInfo: () -> Unit,
    openAccessibility: () -> Unit,
    startLearning: () -> Unit,
    cancelLearning: () -> Unit,
) {
    PageColumn(modifier) {
        RestoreReminder()
        DeveloperOptionsCard(state, openAboutPhone, openDeveloperOptions)
        ReleaseKeyCard(
            state,
            requestOperation,
            submitPairingCode,
            cancelPackageSetup,
        )
        AccessibilityCard(state, openAppInfo, openAccessibility)
        MappedKeyCard(state, startLearning, cancelLearning)
    }
}

@Composable
private fun ActionsPage(
    modifier: Modifier,
    state: MapperUiState,
    openNotificationPolicySettings: () -> Unit,
    updateActionKind: (PressAction, ActionKind) -> Unit,
    updateHttpBaseUrl: (PressAction, String) -> Unit,
    updateHttpMethod: (PressAction, RequestMethod) -> Unit,
    updateActionValue: (PressAction, String) -> Unit,
    updateSoundMode: (PressAction, SoundMode) -> Unit,
    updateSystemAction: (PressAction, SystemAction) -> Unit,
    updateLaunchApp: (PressAction, LaunchableApp) -> Unit,
    updateHaptic: (HapticStrength) -> Unit,
    previewHaptic: (HapticStrength) -> Unit,
) {
    PageColumn(modifier) {
        CommonSettingsCard(
            state,
            updateHaptic,
            previewHaptic,
        )
        PressAction.entries.forEach { gesture ->
            GestureActionCard(
                gesture = gesture,
                state = state,
                initiallyExpanded = gesture == PressAction.SINGLE,
                openNotificationPolicySettings = openNotificationPolicySettings,
                updateActionKind = updateActionKind,
                updateHttpBaseUrl = updateHttpBaseUrl,
                updateHttpMethod = updateHttpMethod,
                updateActionValue = updateActionValue,
                updateSoundMode = updateSoundMode,
                updateSystemAction = updateSystemAction,
                updateLaunchApp = updateLaunchApp,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommonSettingsCard(
    state: MapperUiState,
    updateHaptic: (HapticStrength) -> Unit,
    previewHaptic: (HapticStrength) -> Unit,
) {
    var hapticExpanded by rememberSaveable { mutableStateOf(false) }
    OutlinedCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                "Common settings",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            Text(
                "Applied to every Essential Key gesture.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(
                    expanded = hapticExpanded,
                    onExpandedChange = { hapticExpanded = it },
                    modifier = Modifier.weight(1f),
                ) {
                    OutlinedTextField(
                        value = state.draftHapticStrength.displayName(),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        label = { Text("Haptic feedback") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(hapticExpanded)
                        },
                    )
                    ExposedDropdownMenu(
                        expanded = hapticExpanded,
                        onDismissRequest = { hapticExpanded = false },
                    ) {
                        HapticStrength.entries.forEach { strength ->
                            DropdownMenuItem(
                                text = { Text(strength.displayName()) },
                                onClick = {
                                    updateHaptic(strength)
                                    hapticExpanded = false
                                },
                            )
                        }
                    }
                }
                TextButton(
                    onClick = { previewHaptic(state.draftHapticStrength) },
                    enabled = state.draftHapticStrength != HapticStrength.OFF,
                ) { Text("Preview") }
            }
        }
    }
}

@Composable
private fun GestureActionCard(
    gesture: PressAction,
    state: MapperUiState,
    initiallyExpanded: Boolean,
    openNotificationPolicySettings: () -> Unit,
    updateActionKind: (PressAction, ActionKind) -> Unit,
    updateHttpBaseUrl: (PressAction, String) -> Unit,
    updateHttpMethod: (PressAction, RequestMethod) -> Unit,
    updateActionValue: (PressAction, String) -> Unit,
    updateSoundMode: (PressAction, SoundMode) -> Unit,
    updateSystemAction: (PressAction, SystemAction) -> Unit,
    updateLaunchApp: (PressAction, LaunchableApp) -> Unit,
) {
    var expanded by rememberSaveable(gesture) { mutableStateOf(initiallyExpanded) }
    val action = state.draftActions.getValue(gesture)
    OutlinedCard {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        gesture.displayName(),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        action.summary(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                )
            }
            if (expanded) {
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ActionEditor(
                        action = action,
                        apps = state.launchableApps,
                        notificationPolicyAccess = state.notificationPolicyAccess,
                        baseUrlError = state.baseUrlErrors[gesture],
                        error = state.validationErrors[gesture],
                        result = state.settings.results[gesture],
                        updateKind = { updateActionKind(gesture, it) },
                        updateHttpBaseUrl = { updateHttpBaseUrl(gesture, it) },
                        updateHttpMethod = { updateHttpMethod(gesture, it) },
                        updateValue = { updateActionValue(gesture, it) },
                        updateSoundMode = { updateSoundMode(gesture, it) },
                        updateSystemAction = { updateSystemAction(gesture, it) },
                        updateLaunchApp = { updateLaunchApp(gesture, it) },
                        openNotificationPolicySettings = openNotificationPolicySettings,
                    )
                }
            }
        }
    }
}

@Composable
private fun PageColumn(
    modifier: Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .widthIn(max = 760.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}

@Composable
private fun DeveloperOptionsCard(
    state: MapperUiState,
    openAboutPhone: () -> Unit,
    openDeveloperOptions: () -> Unit,
) {
    OutlinedCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StepTitle("1", "Enable Developer options")
            if (state.developerOptionsEnabled) {
                StatusLine(true, "Developer options enabled")
                Text(
                    "Open Developer options and keep its main switch enabled. The next step uses Wireless debugging; Nothing also recommends enabling USB debugging.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = openDeveloperOptions, modifier = Modifier.fillMaxWidth()) {
                    Text("Open Developer options")
                }
            } else {
                StatusLine(false, "Developer options are still hidden")
                Text(
                    "On Nothing OS: open About phone → Software info → Build number. Tap Build number 7 times, enter your screen-lock PIN if asked, and return here after “You are now a developer” appears.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = openAboutPhone, modifier = Modifier.fillMaxWidth()) {
                    Text("Open About phone")
                }
            }
        }
    }
}

@Composable
private fun ReleaseKeyCard(
    state: MapperUiState,
    requestOperation: (PackageOperation) -> Unit,
    submitPairingCode: (String) -> Unit,
    cancel: () -> Unit,
) {
    var pairingCode by rememberSaveable { mutableStateOf("") }
    val setup = state.setup
    OutlinedCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StepTitle("2", "Release Essential Key")
            when (setup.packageStatus) {
                NothingPackageStatus.DISABLED -> StatusLine(true, "Nothing’s handlers are disabled")
                NothingPackageStatus.ENABLED -> StatusLine(false, "Nothing currently owns the Essential Key")
                NothingPackageStatus.PARTIAL -> StatusLine(false, "Nothing’s handlers are only partially disabled")
                NothingPackageStatus.UNSUPPORTED -> StatusLine(
                    false,
                    "Essential Space packages were not found on this device",
                )
                NothingPackageStatus.UNKNOWN -> StatusLine(false, "Checking Nothing package state")
            }
            Text(
                "The app uses Android’s local Wireless Debugging connection to change only the two Essential Space packages. No terminal is required.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (setup.busy) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                val message = setup.message.orEmpty()
                Text(message)
                if (setup.phase == SetupPhase.DISCOVERING ||
                    setup.phase == SetupPhase.WAITING_FOR_CODE
                ) {
                    OutlinedTextField(
                        value = pairingCode,
                        onValueChange = { pairingCode = it.filter(Char::isDigit).take(6) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Pairing code") },
                        supportingText = {
                            Text("Enter here in split-screen, or reply from the setup notification.")
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { submitPairingCode(pairingCode) },
                            enabled = pairingCode.length == 6,
                            modifier = Modifier.weight(1f),
                        ) { Text("Submit code") }
                        TextButton(onClick = cancel) { Text("Cancel") }
                    }
                }
            } else {
                setup.message?.let { message ->
                    Text(
                        message,
                        color = if (setup.phase == SetupPhase.ERROR) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                when (setup.packageStatus) {
                    NothingPackageStatus.DISABLED -> OutlinedButton(
                        onClick = { requestOperation(PackageOperation.RESTORE) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Restore Essential Space") }
                    NothingPackageStatus.ENABLED,
                    NothingPackageStatus.PARTIAL,
                    -> Button(
                        onClick = { requestOperation(PackageOperation.DISABLE) },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Open Wireless debugging setup") }
                    NothingPackageStatus.UNSUPPORTED,
                    NothingPackageStatus.UNKNOWN,
                    -> Unit
                }
            }
        }
    }
}

@Composable
private fun AccessibilityCard(
    state: MapperUiState,
    openAppInfo: () -> Unit,
    openAccessibility: () -> Unit,
) {
    OutlinedCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StepTitle("3", "Enable button listener")
            when {
                state.competingServices.isNotEmpty() -> {
                    StatusLine(false, "Turn off ${state.competingServices.joinToString()}")
                    Text("Another accessibility service currently requests hardware key events.")
                    OutlinedButton(onClick = openAccessibility, modifier = Modifier.fillMaxWidth()) {
                        Text("Manage Accessibility services")
                    }
                }
                state.serviceEnabled -> {
                    StatusLine(true, "Accessibility listener enabled")
                    Text(
                        "The listener receives hardware key events only; it does not read screen content.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    OutlinedButton(onClick = openAccessibility, modifier = Modifier.fillMaxWidth()) {
                        Text("Manage Accessibility")
                    }
                }
                else -> {
                    StatusLine(false, "Accessibility listener disabled")
                    Text(
                        "For sideloaded APKs, Android reveals “Allow restricted settings” only after it blocks the service once. Follow these steps in order:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    InstructionStep(
                        "1",
                        "Open Accessibility, select “Essential Key button listener,” and try to turn it on. Android should show a “Restricted setting” dialog. Dismiss it and return here; this blocked attempt is required.",
                    )
                    Button(onClick = openAccessibility, modifier = Modifier.fillMaxWidth()) {
                        Text("1. Open Accessibility")
                    }
                    InstructionStep(
                        "2",
                        "Open App info, tap ⋮ in the top-right corner, choose “Allow restricted settings,” then confirm.",
                    )
                    Button(onClick = openAppInfo, modifier = Modifier.fillMaxWidth()) {
                        Text("2. Open App info")
                    }
                    InstructionStep(
                        "3",
                        "Open Accessibility again, select the listener, and turn it on successfully.",
                    )
                    Button(onClick = openAccessibility, modifier = Modifier.fillMaxWidth()) {
                        Text("3. Open Accessibility again")
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructionStep(number: String, text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.extraSmall,
        ) {
            Text(
                number,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun MappedKeyCard(
    state: MapperUiState,
    startLearning: () -> Unit,
    cancelLearning: () -> Unit,
) {
    OutlinedCard {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            StepTitle("4", "Detect Essential Key")
            if (state.settings.learning) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text("Press the Essential Key now", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Waiting for one press…",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                TextButton(onClick = cancelLearning) { Text("Cancel") }
            } else {
                val key = state.settings.mappedKey
                if (key == null) {
                    StatusLine(false, "No Essential Key press detected yet")
                } else {
                    StatusLine(true, "Essential Key press detected")
                }
                Button(
                    onClick = startLearning,
                    enabled = state.readyToMap,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (key == null) "Detect Essential Key press" else "Detect again")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ActionEditor(
    action: ConfiguredAction,
    apps: List<LaunchableApp>,
    notificationPolicyAccess: Boolean,
    baseUrlError: String?,
    error: String?,
    result: String?,
    updateKind: (ActionKind) -> Unit,
    updateHttpBaseUrl: (String) -> Unit,
    updateHttpMethod: (RequestMethod) -> Unit,
    updateValue: (String) -> Unit,
    updateSoundMode: (SoundMode) -> Unit,
    updateSystemAction: (SystemAction) -> Unit,
    updateLaunchApp: (LaunchableApp) -> Unit,
    openNotificationPolicySettings: () -> Unit,
) {
    var typeExpanded by rememberSaveable { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = it },
        ) {
            OutlinedTextField(
                value = action.displayName(),
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                label = { Text("Action") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false },
            ) {
                ActionKind.entries.filterNot { it == ActionKind.TOGGLE_SILENT }.forEach { kind ->
                    DropdownMenuItem(
                        text = { Text(kind.displayName()) },
                        onClick = {
                            updateKind(kind)
                            typeExpanded = false
                        },
                    )
                }
            }
        }
        when (action) {
            ConfiguredAction.None,
            ConfiguredAction.Flashlight,
            -> Unit
            is ConfiguredAction.Http -> {
                OutlinedTextField(
                    value = action.baseUrl,
                    onValueChange = updateHttpBaseUrl,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("HTTP base URL") },
                    isError = baseUrlError != null,
                    supportingText = baseUrlError?.let { message -> { Text(message) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true,
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    RequestMethod.entries.forEachIndexed { index, method ->
                        SegmentedButton(
                            selected = action.method == method,
                            onClick = { updateHttpMethod(method) },
                            shape = SegmentedButtonDefaults.itemShape(index, RequestMethod.entries.size),
                            modifier = Modifier.weight(1f),
                        ) { Text(method.name) }
                    }
                }
                OutlinedTextField(
                    value = action.endpoint,
                    onValueChange = updateValue,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Path or complete URL") },
                    isError = error != null,
                    supportingText = error?.let { message -> { Text(message) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    singleLine = true,
                )
            }
            ConfiguredAction.ToggleSilent -> {
                if (!notificationPolicyAccess) {
                    SilentModeAccessPrompt(openNotificationPolicySettings)
                }
            }
            is ConfiguredAction.SetSoundMode -> {
                EnumDropdown(
                    label = "Sound mode",
                    value = action.mode,
                    entries = SoundMode.entries,
                    name = { it.displayName() },
                    select = updateSoundMode,
                )
                if (action.mode.requiresSilentAccess() && !notificationPolicyAccess) {
                    SilentModeAccessPrompt(openNotificationPolicySettings)
                }
            }
            is ConfiguredAction.LaunchApp -> AppDropdown(
                selected = action,
                apps = apps,
                error = error,
                select = updateLaunchApp,
            )
            is ConfiguredAction.OpenUrl -> OutlinedTextField(
                value = action.url,
                onValueChange = updateValue,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("URL") },
                placeholder = { Text("https://example.com") },
                isError = error != null,
                supportingText = error?.let { message -> { Text(message) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                singleLine = true,
            )
            is ConfiguredAction.PerformSystemAction -> EnumDropdown(
                label = "System action",
                value = action.action,
                entries = SystemAction.entries,
                name = { it.displayName() },
                select = updateSystemAction,
            )
        }
        result?.let { ResultLine(it) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    value: T,
    entries: List<T>,
    name: (T) -> String,
    select: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = name(value),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            entries.forEach { entry ->
                DropdownMenuItem(
                    text = { Text(name(entry)) },
                    onClick = {
                        select(entry)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppDropdown(
    selected: ConfiguredAction.LaunchApp,
    apps: List<LaunchableApp>,
    error: String?,
    select: (LaunchableApp) -> Unit,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val selectedApp = apps.firstOrNull { it.packageName == selected.packageName }
    OutlinedButton(
        onClick = { open = true },
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
    ) {
        selectedApp?.let {
            AppIcon(it.packageName)
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.Start) {
            Text(selected.label.ifBlank { "Choose an app" })
            if (selected.packageName.isNotBlank()) {
                Text(
                    selected.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    if (open) {
        val filteredApps = remember(apps, query) {
            if (query.isBlank()) {
                apps
            } else {
                apps.filter {
                    it.label.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
                }
            }
        }
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text("Choose app") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Search apps") },
                        singleLine = true,
                    )
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        select(app)
                                        open = false
                                        query = ""
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                AppIcon(app.packageName)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(app.label)
                                    Text(
                                        app.packageName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { open = false }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun AppIcon(packageName: String) {
    val packageManager = LocalContext.current.packageManager
    val bitmap = remember(packageName) {
        runCatching {
            packageManager.getApplicationIcon(packageName)
                .toBitmap(width = 96, height = 96)
                .asImageBitmap()
        }.getOrNull()
    }
    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = null,
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp)),
        )
    }
}

@Composable
private fun SilentModeAccessPrompt(openSettings: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
            )
            Column(Modifier.weight(1f)) {
                Text("Silent mode may need access", fontWeight = FontWeight.SemiBold)
                Text(
                    "Nothing OS may require Do Not Disturb access only for changes involving Silent.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Button(onClick = openSettings) { Text("Allow") }
        }
    }
}

@Composable
private fun RestoreReminder() {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Warning, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text(
                "Before uninstalling, restore Essential Space here. Uninstalling alone will not restore the key.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun PackageConfirmationDialog(
    operation: PackageOperation,
    confirm: () -> Unit,
    dismiss: () -> Unit,
) {
    val disabling = operation == PackageOperation.DISABLE
    AlertDialog(
        onDismissRequest = dismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null) },
        title = { Text(if (disabling) "Release Essential Key?" else "Restore Essential Space?") },
        text = {
            Text(
                if (disabling) {
                    "This will use local ADB to disable Essential Space and Essential Recorder for your user. Their data is not deleted. Important: uninstalling this app will not restore the Essential Key. Restore Essential Space from this app before uninstalling."
                } else {
                    "This will re-enable Essential Space and Essential Recorder. Nothing will own the Essential Key again."
                },
            )
        },
        confirmButton = {
            Button(onClick = confirm) {
                Text(if (disabling) "Continue" else "Restore")
            }
        },
        dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } },
    )
}

@Composable
private fun AccessibilityDisclosureDialog(confirm: () -> Unit, dismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = dismiss,
        icon = { Icon(Icons.Default.Settings, contentDescription = null) },
        title = { Text("Hardware-key accessibility service") },
        text = {
            Text(
                "Essential Key Remapper uses AccessibilityService only to receive and consume the hardware key you select, including while other apps are open. It does not read screen content, type text, collect personal data, or send accessibility data off the device.",
            )
        },
        confirmButton = { Button(onClick = confirm) { Text("I understand") } },
        dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } },
    )
}

@Composable
private fun StepTitle(number: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.small,
        ) {
            Text(
                number,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() },
        )
    }
}

@Composable
private fun StatusLine(success: Boolean, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (success) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (success) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
        )
        Spacer(Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
private fun ResultLine(result: String) {
    val error = result.contains("Error:")
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            if (error) Icons.Default.Error else Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (error) MaterialTheme.colorScheme.error else Color(0xFF2E7D32),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            result,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SaveBar(dirty: Boolean, saving: Boolean, onSave: () -> Unit) {
    Surface(tonalElevation = 3.dp) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Button(
                onClick = onSave,
                enabled = dirty && !saving,
                modifier = Modifier.align(Alignment.Center).widthIn(max = 520.dp).fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 14.dp, horizontal = 24.dp),
            ) {
                if (saving) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Save, contentDescription = null)
                }
                Spacer(Modifier.width(8.dp))
                Text(if (dirty) "Save actions" else "All changes saved")
            }
        }
    }
}

private fun ActionKind.displayName(): String = when (this) {
    ActionKind.NONE -> "No action"
    ActionKind.HTTP -> "HTTP request"
    ActionKind.FLASHLIGHT -> "Toggle flashlight"
    ActionKind.SOUND_MODE -> "Sound mode"
    ActionKind.TOGGLE_SILENT -> "Sound mode"
    ActionKind.LAUNCH_APP -> "Launch app"
    ActionKind.OPEN_URL -> "Open URL"
    ActionKind.SYSTEM -> "Device control"
}

private fun ConfiguredAction.displayName(): String = kind.displayName()

private fun ConfiguredAction.summary(): String = when (this) {
    ConfiguredAction.None -> "No action"
    ConfiguredAction.Flashlight -> "Toggle flashlight"
    ConfiguredAction.ToggleSilent -> "Toggle silent / normal"
    is ConfiguredAction.Http -> "${method.name} ${endpoint.ifBlank { "request" }}"
    is ConfiguredAction.SetSoundMode -> mode.displayName()
    is ConfiguredAction.LaunchApp -> label.ifBlank { "Choose an app" }
    is ConfiguredAction.OpenUrl -> url.ifBlank { "Open a URL" }
    is ConfiguredAction.PerformSystemAction -> action.displayName()
}

private fun PressAction.displayName(): String = when (this) {
    PressAction.SINGLE -> "Single press"
    PressAction.DOUBLE -> "Double press"
    PressAction.LONG -> "Long press"
}

private fun HapticStrength.displayName(): String =
    name.lowercase().replaceFirstChar(Char::uppercase)

private fun SoundMode.displayName(): String = when (this) {
    SoundMode.NORMAL -> "Normal"
    SoundMode.VIBRATE -> "Vibrate"
    SoundMode.SILENT -> "Silent"
    SoundMode.TOGGLE_SILENT_NORMAL -> "Toggle silent / normal"
}

private fun SoundMode.requiresSilentAccess(): Boolean =
    this == SoundMode.SILENT || this == SoundMode.TOGGLE_SILENT_NORMAL

private fun SystemAction.displayName(): String = when (this) {
    SystemAction.SCREENSHOT -> "Take screenshot"
    SystemAction.LOCK_SCREEN -> "Lock screen"
    SystemAction.POWER_MENU -> "Show power menu"
    SystemAction.NOTIFICATIONS -> "Open notifications"
    SystemAction.QUICK_SETTINGS -> "Open Quick Settings"
    SystemAction.HOME -> "Home"
    SystemAction.BACK -> "Back"
    SystemAction.RECENTS -> "Recent apps"
    SystemAction.MEDIA_PLAY_PAUSE -> "Play / pause media"
    SystemAction.MEDIA_NEXT -> "Next media"
    SystemAction.MEDIA_PREVIOUS -> "Previous media"
    SystemAction.CAMERA -> "Open camera"
    SystemAction.ASSISTANT -> "Open assistant"
}

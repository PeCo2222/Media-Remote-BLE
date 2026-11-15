package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BleConfig
import com.example.model.BlePreset
import com.example.model.PcKeyboardLayout
import com.example.model.RemoteActionId
import com.example.ui.theme.BleConnectedCyan
import com.example.ui.theme.BleErrorCoral
import com.example.ui.theme.MediaCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    config: BleConfig,
    onBack: () -> Unit,
    onApplyPreset: (BlePreset) -> Unit,
    onSaveUuids: (service: String, write: String, notify: String) -> Unit,
    onUpdateFlags: (autoDetect: Boolean, appendNewline: Boolean, writeNoResp: Boolean, filterNamed: Boolean) -> Unit,
    onSensitivityChange: (Float) -> Unit,
    onPcLayoutChange: (PcKeyboardLayout) -> Unit,
    onSendEnterAfterTextChange: (Boolean) -> Unit,
    onPayloadChange: (RemoteActionId, String) -> Unit,
    onTestSkipCalibration: (x: Int, y: Int) -> Unit,
    onSaveSkipCalibration: (screen: String, x: Int, y: Int) -> Unit,
    onForgetSavedDevice: () -> Unit,
    onResetDefaults: () -> Unit,
) {
    var serviceUuid by remember(config.serviceUuid) { mutableStateOf(config.serviceUuid) }
    var writeUuid by remember(config.writeCharacteristicUuid) { mutableStateOf(config.writeCharacteristicUuid) }
    var notifyUuid by remember(config.notifyCharacteristicUuid) { mutableStateOf(config.notifyCharacteristicUuid) }

    val activePreset = config.activePreset()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onResetDefaults,
                        modifier = Modifier.testTag("settings_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = "Reset defaults",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Column(modifier = Modifier.widthIn(max = 640.dp)) {
                    SectionTitle("BLE GATT Profile")
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianSurface,
                        border = BorderStroke(1.dp, ObsidianBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Quick Presets",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextSecondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(BlePreset.NORDIC_UART, BlePreset.ESP32_EXAMPLE).forEach { preset ->
                                    val selected = activePreset == preset
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onApplyPreset(preset) },
                                        label = { Text(preset.label, maxLines = 1) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MediaCyan.copy(alpha = 0.2f),
                                            selectedLabelColor = MediaCyan,
                                            labelColor = TextSecondary
                                        ),
                                        modifier = Modifier.testTag("preset_${preset.name.lowercase()}")
                                    )
                                }
                            }

                            UuidField(
                                label = "Service UUID",
                                value = serviceUuid,
                                onValueChange = { serviceUuid = it },
                                testTag = "input_service_uuid"
                            )
                            UuidField(
                                label = "Write (RX) Characteristic UUID",
                                value = writeUuid,
                                onValueChange = { writeUuid = it },
                                testTag = "input_write_uuid"
                            )
                            UuidField(
                                label = "Notify (TX) Characteristic UUID",
                                value = notifyUuid,
                                onValueChange = { notifyUuid = it },
                                testTag = "input_notify_uuid"
                            )

                            Button(
                                onClick = { onSaveUuids(serviceUuid, writeUuid, notifyUuid) },
                                colors = ButtonDefaults.buttonColors(containerColor = MediaCyan),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_uuids_button")
                            ) {
                                Text("Save BLE UUIDs", color = TextPrimary)
                            }
                        }
                    }
                }
            }

            item {
                Column(modifier = Modifier.widthIn(max = 640.dp)) {
                    SectionTitle("Transport & Trackpad")
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianSurface,
                        border = BorderStroke(1.dp, ObsidianBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SettingToggleRow(
                                title = "Auto-detect writable characteristic",
                                subtitle = "Fallback to first writable GATT characteristic if UUID doesn't match",
                                checked = config.autoDetectWritable,
                                testTag = "switch_auto_detect",
                                onCheckedChange = {
                                    onUpdateFlags(it, config.appendNewline, config.writeWithoutResponse, config.filterNamedOnly)
                                }
                            )
                            SettingToggleRow(
                                title = "Append newline (Enter) after text",
                                subtitle = "Automatically press Enter on PC after sending typed text",
                                checked = config.sendEnterAfterText,
                                testTag = "switch_send_enter_after_text",
                                onCheckedChange = onSendEnterAfterTextChange
                            )
                            SettingToggleRow(
                                title = "BLE packet delimiter (\\n)",
                                subtitle = "Terminate each BLE command packet with a line feed for ESP32 parsing",
                                checked = config.appendNewline,
                                testTag = "switch_append_newline",
                                onCheckedChange = {
                                    onUpdateFlags(config.autoDetectWritable, it, config.writeWithoutResponse, config.filterNamedOnly)
                                }
                            )
                            SettingToggleRow(
                                title = "Write without response",
                                subtitle = "Lower latency for button taps when supported by ESP32",
                                checked = config.writeWithoutResponse,
                                testTag = "switch_write_no_resp",
                                onCheckedChange = {
                                    onUpdateFlags(config.autoDetectWritable, config.appendNewline, it, config.filterNamedOnly)
                                }
                            )
                            SettingToggleRow(
                                title = "Show named BLE devices only",
                                subtitle = "Hide unnamed beacons during device scan",
                                checked = config.filterNamedOnly,
                                testTag = "switch_filter_named",
                                onCheckedChange = {
                                    onUpdateFlags(config.autoDetectWritable, config.appendNewline, config.writeWithoutResponse, it)
                                }
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            val pct = ((config.trackpadSensitivity * 10f).roundToInt() / 10f)
                            Text(
                                text = "Trackpad Sensitivity: ${pct}x",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Slider(
                                value = config.trackpadSensitivity,
                                onValueChange = onSensitivityChange,
                                valueRange = 0.5f..3.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MediaCyan,
                                    activeTrackColor = MediaCyan
                                ),
                                modifier = Modifier.testTag("slider_trackpad_sensitivity")
                            )

                            Text(
                                text = "PC Keyboard Layout (Windows HID)",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = "ESP32 USB HID uses US QWERTY scancodes. Pick your PC's Windows layout so AZERTY/QWERTY keys match.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PcKeyboardLayout.entries.forEach { layout ->
                                    FilterChip(
                                        selected = config.pcKeyboardLayout == layout,
                                        onClick = { onPcLayoutChange(layout) },
                                        label = { Text(layout.shortLabel, maxLines = 1) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MediaCyan.copy(alpha = 0.2f),
                                            selectedLabelColor = MediaCyan,
                                            labelColor = TextSecondary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("settings_layout_${layout.name.lowercase()}")
                                    )
                                }
                            }

                            if (config.savedDeviceAddress.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Saved Device: ${config.savedDeviceName}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = config.savedDeviceAddress,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = onForgetSavedDevice,
                                        border = BorderStroke(1.dp, BleErrorCoral.copy(alpha = 0.5f)),
                                        modifier = Modifier.testTag("forget_saved_device_button")
                                    ) {
                                        Text("Forget", color = BleErrorCoral)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.widthIn(max = 640.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SectionTitle("Skip Ad Screen Calibration (BLE)")
                    Text(
                        text = "Test and save Skip Ad coordinates on your ESP32-S3 flash memory over BLE.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    SkipCalibrationCard(
                        title = "LG Ultrawide",
                        screenKey = "lg",
                        initialX = config.lgX,
                        initialY = config.lgY,
                        isDefault = config.defaultSkipScreen == "lg",
                        onTest = onTestSkipCalibration,
                        onSaveDefault = onSaveSkipCalibration
                    )
                    SkipCalibrationCard(
                        title = "17\" ROG Laptop",
                        screenKey = "rog",
                        initialX = config.rogX,
                        initialY = config.rogY,
                        isDefault = config.defaultSkipScreen == "rog",
                        onTest = onTestSkipCalibration,
                        onSaveDefault = onSaveSkipCalibration
                    )
                    SkipCalibrationCard(
                        title = "Custom Screen",
                        screenKey = "custom",
                        initialX = config.customX,
                        initialY = config.customY,
                        isDefault = config.defaultSkipScreen == "custom",
                        onTest = onTestSkipCalibration,
                        onSaveDefault = onSaveSkipCalibration
                    )
                }
            }

            item {
                Column(modifier = Modifier.widthIn(max = 640.dp)) {
                    SectionTitle("Button Command Payloads")
                    Text(
                        text = "Customize the exact ASCII string sent over BLE for each remote button.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            items(
                items = RemoteActionId.entries,
                key = { it.name }
            ) { action ->
                Surface(
                    modifier = Modifier.widthIn(max = 640.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = ObsidianSurface,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    var draft by remember(action, config.payloadFor(action)) {
                        mutableStateOf(config.payloadFor(action))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = action.displayLabel,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Text(
                                text = action.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted
                            )
                        }
                        OutlinedTextField(
                            value = draft,
                            onValueChange = {
                                draft = it
                                onPayloadChange(action, it)
                            },
                            singleLine = true,
                            textStyle = MaterialTheme.typography.labelMedium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MediaCyan,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedContainerColor = ObsidianSurfaceHigh,
                                unfocusedContainerColor = ObsidianSurfaceHigh,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .widthIn(min = 110.dp, max = 140.dp)
                                .testTag("payload_input_${action.name.lowercase()}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MediaCyan
    )
}

@Composable
private fun UuidField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    testTag: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        textStyle = MaterialTheme.typography.labelMedium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MediaCyan,
            unfocusedBorderColor = ObsidianBorder,
            focusedLabelColor = MediaCyan,
            unfocusedLabelColor = TextSecondary,
            focusedContainerColor = ObsidianSurfaceHigh,
            unfocusedContainerColor = ObsidianSurfaceHigh,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

@Composable
private fun SkipCalibrationCard(
    title: String,
    screenKey: String,
    initialX: Int,
    initialY: Int,
    isDefault: Boolean,
    onTest: (Int, Int) -> Unit,
    onSaveDefault: (String, Int, Int) -> Unit,
) {
    var xText by remember(screenKey, initialX) { mutableStateOf(initialX.toString()) }
    var yText by remember(screenKey, initialY) { mutableStateOf(initialY.toString()) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = ObsidianSurface,
        border = BorderStroke(1.dp, if (isDefault) BleConnectedCyan else ObsidianBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                if (isDefault) {
                    Text(
                        text = "DEFAULT",
                        style = MaterialTheme.typography.labelSmall,
                        color = BleConnectedCyan
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = xText,
                    onValueChange = { xText = it.filter { ch -> ch.isDigit() || ch == '-' } },
                    label = { Text("X Steps") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.labelMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MediaCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedContainerColor = ObsidianSurfaceHigh,
                        unfocusedContainerColor = ObsidianSurfaceHigh,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.weight(1f).testTag("skip_${screenKey}_x")
                )
                OutlinedTextField(
                    value = yText,
                    onValueChange = { yText = it.filter { ch -> ch.isDigit() || ch == '-' } },
                    label = { Text("Y Steps") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    textStyle = MaterialTheme.typography.labelMedium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MediaCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedContainerColor = ObsidianSurfaceHigh,
                        unfocusedContainerColor = ObsidianSurfaceHigh,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.weight(1f).testTag("skip_${screenKey}_y")
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onTest(xText.toIntOrNull() ?: initialX, yText.toIntOrNull() ?: initialY)
                    },
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.weight(1f).testTag("skip_${screenKey}_test")
                ) {
                    Text("Test", color = TextPrimary)
                }
                Button(
                    onClick = {
                        onSaveDefault(
                            screenKey,
                            xText.toIntOrNull() ?: initialX,
                            yText.toIntOrNull() ?: initialY
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MediaCyan),
                    modifier = Modifier.weight(1.5f).testTag("skip_${screenKey}_save")
                ) {
                    Text("Save & Default", color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, color = TextPrimary)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = TextMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = TextPrimary,
                checkedTrackColor = MediaCyan
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble.BleRemoteManager
import com.example.data.SettingsRepository
import com.example.model.BleConfig
import com.example.model.BleConnectionState
import com.example.model.BleDiscoveredDevice
import com.example.model.BlePreset
import com.example.model.HidCommandChunk
import com.example.model.KeyboardLayoutMapper
import com.example.model.PcKeyboardLayout
import com.example.model.RemoteActionId
import com.example.model.RemoteMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class RemoteUiState(
    val config: BleConfig = BleConfig(),
    val connectionState: BleConnectionState = BleConnectionState.Disconnected,
    val discoveredDevices: List<BleDiscoveredDevice> = emptyList(),
    val activeMode: RemoteMode = RemoteMode.MEDIA_REMOTE,
    val statusLine: String = "Ready",
    val lastSentCommand: String? = null,
    val lastTypedText: String? = null,
    val lastRxMessage: String? = null,
    val isBluetoothEnabled: Boolean = true,
    val hasBlePermissions: Boolean = false,
    val isBleSupported: Boolean = true,
    val showScannerSheet: Boolean = false,
)

class RemoteViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application.applicationContext)
    private val bleManager = BleRemoteManager(application.applicationContext)

    private val _activeMode = MutableStateFlow(RemoteMode.MEDIA_REMOTE)
    private val _statusOverride = MutableStateFlow<String?>(null)
    private val _lastSentCommand = MutableStateFlow<String?>(null)
    private val _lastTypedText = MutableStateFlow<String?>(null)
    private val _hasPermissions = MutableStateFlow(bleManager.hasRequiredPermissions())
    private val _showScannerSheet = MutableStateFlow(false)

    private var statusResetJob: Job? = null

    // Trackpad movement accumulator for smooth 50Hz BLE coalescing
    private var accumDx = 0f
    private var accumDy = 0f
    private var lastMoveDispatchMs = 0L

    private var accumScroll = 0f
    private var lastScrollDispatchMs = 0L

    val uiState: StateFlow<RemoteUiState> = combine(
        settingsRepository.configFlow,
        bleManager.connectionState,
        bleManager.discoveredDevices,
        bleManager.isBluetoothEnabled,
        bleManager.lastRxMessage,
        _activeMode,
        _statusOverride,
        _lastSentCommand,
        _lastTypedText,
        _hasPermissions,
        _showScannerSheet,
    ) { values ->
        val config = values[0] as BleConfig
        val conn = values[1] as BleConnectionState
        @Suppress("UNCHECKED_CAST")
        val devices = values[2] as List<BleDiscoveredDevice>
        val btEnabled = values[3] as Boolean
        val rx = values[4] as String?
        val mode = values[5] as RemoteMode
        val overrideStatus = values[6] as String?
        val lastSent = values[7] as String?
        val lastTyped = values[8] as String?
        val perms = values[9] as Boolean
        val showSheet = values[10] as Boolean

        val derivedStatus = overrideStatus ?: when (conn) {
            is BleConnectionState.Connected -> "Connected · ${conn.deviceName}"
            is BleConnectionState.Connecting -> "Connecting to ${conn.deviceName}..."
            is BleConnectionState.Scanning -> "Scanning for ESP32-S3..."
            is BleConnectionState.Error -> conn.message
            BleConnectionState.Disconnected -> if (config.savedDeviceName.isNotBlank()) {
                "Ready · Tap BLE to connect ${config.savedDeviceName}"
            } else {
                "Ready"
            }
        }

        RemoteUiState(
            config = config,
            connectionState = conn,
            discoveredDevices = devices,
            activeMode = mode,
            statusLine = derivedStatus,
            lastSentCommand = lastSent,
            lastTypedText = lastTyped,
            lastRxMessage = rx,
            isBluetoothEnabled = btEnabled,
            hasBlePermissions = perms,
            isBleSupported = bleManager.isBleSupported,
            showScannerSheet = showSheet,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RemoteUiState(
            hasBlePermissions = bleManager.hasRequiredPermissions(),
            isBleSupported = bleManager.isBleSupported,
            isBluetoothEnabled = bleManager.isBluetoothEnabled.value
        )
    )

    init {
        viewModelScope.launch {
            settingsRepository.configFlow.collect { config ->
                bleManager.updateConfig(config)
            }
        }
        viewModelScope.launch {
            bleManager.connectionState.collect { state ->
                if (state is BleConnectionState.Connected) {
                    settingsRepository.saveConnectedDevice(state.address, state.deviceName)
                    _showScannerSheet.value = false
                }
            }
        }
    }

    fun requiredBlePermissions(): Array<String> = bleManager.requiredPermissions()

    fun onPermissionsResult() {
        _hasPermissions.value = bleManager.hasRequiredPermissions()
        bleManager.refreshBluetoothState()
        if (_hasPermissions.value && _showScannerSheet.value && bleManager.isBluetoothEnabled.value) {
            bleManager.startScan()
        }
    }

    fun onBluetoothStateMaybeChanged() {
        bleManager.refreshBluetoothState()
    }

    fun setMode(mode: RemoteMode) {
        _activeMode.value = mode
    }

    fun openScannerSheet() {
        _hasPermissions.value = bleManager.hasRequiredPermissions()
        bleManager.refreshBluetoothState()
        _showScannerSheet.value = true
        if (_hasPermissions.value && bleManager.isBluetoothEnabled.value) {
            bleManager.startScan()
        }
    }

    fun closeScannerSheet() {
        _showScannerSheet.value = false
        bleManager.stopScan()
    }

    fun onBottomBleButtonClick() {
        val current = uiState.value
        if (current.connectionState is BleConnectionState.Disconnected &&
            current.config.savedDeviceAddress.isNotBlank() &&
            current.hasBlePermissions &&
            current.isBluetoothEnabled
        ) {
            // Try quick connect to saved ESP32-S3, or open sheet if user wants to pick
            bleManager.connect(
                address = current.config.savedDeviceAddress,
                fallbackName = current.config.savedDeviceName.ifBlank { "ESP32-S3" }
            )
        } else {
            openScannerSheet()
        }
    }

    fun startScan() {
        _hasPermissions.value = bleManager.hasRequiredPermissions()
        bleManager.refreshBluetoothState()
        bleManager.startScan()
    }

    fun stopScan() {
        bleManager.stopScan()
    }

    fun connectToDevice(device: BleDiscoveredDevice) {
        bleManager.connect(device.address, device.name)
    }

    fun disconnect() {
        bleManager.disconnect()
        flashStatus("Disconnected")
    }

    fun forgetSavedDevice() {
        viewModelScope.launch {
            settingsRepository.clearSavedDevice()
        }
    }

    fun sendMediaAction(action: RemoteActionId) {
        val payload = uiState.value.config.payloadFor(action)
        dispatchCommand(payload, label = action.displayLabel)
    }

    fun sendRawText(text: String) {
        if (text.isEmpty()) return
        if (uiState.value.connectionState !is BleConnectionState.Connected) {
            flashStatus("Not connected — tap BLE icon below")
            openScannerSheet()
            return
        }
        val config = uiState.value.config
        val layout = config.pcKeyboardLayout
        val chunks = KeyboardLayoutMapper.encodeText(text, layout)
        var allSent = true
        for (chunk in chunks) {
            val cmd = when (chunk) {
                is HidCommandChunk.TextChunk -> "TXT:${chunk.text}"
                is HidCommandChunk.RawKeyChunk -> "KRAW:${chunk.modifier},${chunk.keycode}"
            }
            if (!bleManager.sendCommand(cmd)) {
                allSent = false
            } else {
                _lastSentCommand.value = cmd
            }
        }
        if (allSent && config.sendEnterAfterText) {
            val enterCmd = config.payloadFor(RemoteActionId.ENTER)
            if (bleManager.sendCommand(enterCmd)) {
                _lastSentCommand.value = "$enterCmd (auto)"
            }
        }
        if (allSent) {
            _lastTypedText.value = text
        }
        flashStatus(
            if (allSent) "Sent (${layout.shortLabel}): ${text.take(16)}" else "BLE write failed"
        )
    }

    fun sendTrackpadDelta(dx: Float, dy: Float) {
        if (uiState.value.connectionState !is BleConnectionState.Connected) return
        val sensitivity = uiState.value.config.trackpadSensitivity
        accumDx += dx * sensitivity
        accumDy += dy * sensitivity

        val now = System.currentTimeMillis()
        if (now - lastMoveDispatchMs < 20L) return

        val moveX = accumDx.roundToInt().coerceIn(-127, 127)
        val moveY = accumDy.roundToInt().coerceIn(-127, 127)
        if (moveX != 0 || moveY != 0) {
            accumDx -= moveX
            accumDy -= moveY
            lastMoveDispatchMs = now
            val cmd = "M:$moveX,$moveY"
            bleManager.sendCommand(cmd, preferNoResponse = true)
            _lastSentCommand.value = cmd
        }
    }

    fun flushTrackpadDelta() {
        if (uiState.value.connectionState !is BleConnectionState.Connected) {
            accumDx = 0f
            accumDy = 0f
            return
        }
        val moveX = accumDx.roundToInt().coerceIn(-127, 127)
        val moveY = accumDy.roundToInt().coerceIn(-127, 127)
        accumDx = 0f
        accumDy = 0f
        if (moveX != 0 || moveY != 0) {
            val cmd = "M:$moveX,$moveY"
            bleManager.sendCommand(cmd, preferNoResponse = true)
            _lastSentCommand.value = cmd
        }
    }

    fun sendScrollDelta(dy: Float) {
        if (uiState.value.connectionState !is BleConnectionState.Connected) return
        accumScroll += dy
        val now = System.currentTimeMillis()
        if (now - lastScrollDispatchMs < 45L) return

        // Invert sign so dragging down scrolls down naturally
        val step = (-accumScroll / 22f).roundToInt().coerceIn(-10, 10)
        if (step != 0) {
            accumScroll = 0f
            lastScrollDispatchMs = now
            val cmd = "SCR:$step"
            bleManager.sendCommand(cmd, preferNoResponse = true)
            _lastSentCommand.value = cmd
        }
    }

    fun sendClick(rightClick: Boolean) {
        val action = if (rightClick) RemoteActionId.RIGHT_CLICK else RemoteActionId.LEFT_CLICK
        sendMediaAction(action)
    }

    private fun dispatchCommand(payload: String, label: String) {
        if (uiState.value.connectionState !is BleConnectionState.Connected) {
            flashStatus("Not connected — tap BLE icon below")
            openScannerSheet()
            return
        }
        val sent = bleManager.sendCommand(payload)
        if (sent) {
            _lastSentCommand.value = payload
            flashStatus("Sent: $label ($payload)")
        } else {
            flashStatus("BLE write failed")
        }
    }

    private fun flashStatus(message: String) {
        _statusOverride.value = message
        statusResetJob?.cancel()
        statusResetJob = viewModelScope.launch {
            delay(2_000)
            _statusOverride.value = null
        }
    }

    fun applyPreset(preset: BlePreset) {
        viewModelScope.launch {
            settingsRepository.applyPreset(preset)
        }
    }

    fun updateUuids(serviceUuid: String, writeUuid: String, notifyUuid: String) {
        viewModelScope.launch {
            settingsRepository.updateUuids(serviceUuid, writeUuid, notifyUuid)
        }
    }

    fun updateFlags(
        autoDetectWritable: Boolean,
        appendNewline: Boolean,
        writeWithoutResponse: Boolean,
        filterNamedOnly: Boolean,
    ) {
        viewModelScope.launch {
            settingsRepository.updateFlags(
                autoDetectWritable = autoDetectWritable,
                appendNewline = appendNewline,
                writeWithoutResponse = writeWithoutResponse,
                filterNamedOnly = filterNamedOnly
            )
        }
    }

    fun updateTrackpadSensitivity(sensitivity: Float) {
        viewModelScope.launch {
            settingsRepository.updateTrackpadSensitivity(sensitivity)
        }
    }

    fun updatePcKeyboardLayout(layout: PcKeyboardLayout) {
        viewModelScope.launch {
            settingsRepository.updatePcKeyboardLayout(layout)
        }
    }

    fun updateSendEnterAfterText(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSendEnterAfterText(enabled)
        }
    }

    fun updateActionPayload(action: RemoteActionId, payload: String) {
        viewModelScope.launch {
            settingsRepository.updateActionPayload(action, payload)
        }
    }

    fun testSkipCalibration(x: Int, y: Int) {
        dispatchCommand("SKIP_TEST:$x,$y", label = "Test Skip ($x, $y)")
    }

    fun saveSkipCalibration(screen: String, x: Int, y: Int) {
        viewModelScope.launch {
            settingsRepository.saveSkipCalibration(screen, x, y)
        }
        dispatchCommand("SKIP_SAVE:$screen,$x,$y", label = "Save ${screen.uppercase()} ($x, $y)")
    }

    fun resetDefaults() {
        viewModelScope.launch {
            settingsRepository.resetDefaults()
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleManager.release()
    }
}

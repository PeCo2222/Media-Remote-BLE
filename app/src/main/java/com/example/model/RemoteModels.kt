package com.example.model

enum class RemoteActionId(
    val displayLabel: String,
    val defaultPayload: String,
    val description: String,
) {
    SKIP_AD("Skip Ad", "SKIP", "Click / skip ad macro on PC"),
    PLAY_PAUSE("Play / Pause", "PLAY", "Toggle media playback (Space / K)"),
    SEEK_BACK("- 5 sec", "BACK5", "Rewind 5 seconds (Left Arrow)"),
    SEEK_FWD("+ 5 sec", "FWD5", "Forward 5 seconds (Right Arrow)"),
    KEY_UP("Key Up", "UP", "Up Arrow key"),
    KEY_DOWN("Key Down", "DOWN", "Down Arrow key"),
    VOL_DOWN("Vol -", "VOLDN", "Decrease PC volume"),
    VOL_UP("Vol +", "VOLUP", "Increase PC volume"),
    FULLSCREEN("Fullscreen (F)", "FULL", "Send 'f' key for fullscreen"),
    SUBTITLES("Subtitles (C)", "SUBS", "Send 'c' key for captions"),
    ESC("Esc", "ESC", "Escape key"),
    TAB("Tab", "TAB", "Tab key"),
    ENTER("Enter", "ENTER", "Enter / Return key"),
    SPACE("Space", "SPACE", "Spacebar key"),
    LEFT_CLICK("Left Click", "CLK:L", "Mouse left click"),
    RIGHT_CLICK("Right Click", "CLK:R", "Mouse right click"),
    ;

    companion object {
        val primaryRemoteActions: List<RemoteActionId> = listOf(
            SKIP_AD, PLAY_PAUSE, SEEK_BACK, SEEK_FWD,
            KEY_UP, KEY_DOWN, VOL_DOWN, VOL_UP, FULLSCREEN, SUBTITLES
        )
    }
}

enum class BlePreset(
    val label: String,
    val serviceUuid: String,
    val writeUuid: String,
    val notifyUuid: String,
) {
    NORDIC_UART(
        label = "Nordic UART (NUS)",
        serviceUuid = "6E400001-B5A3-F393-E0A9-E50E24DCCA9E",
        writeUuid = "6E400002-B5A3-F393-E0A9-E50E24DCCA9E",
        notifyUuid = "6E400003-B5A3-F393-E0A9-E50E24DCCA9E"
    ),
    ESP32_EXAMPLE(
        label = "Espressif BLE Example",
        serviceUuid = "4FAFC201-1FB5-459E-8FCC-C5C9C331914B",
        writeUuid = "BEB5483E-36E1-4688-B7F5-EA07361B26A8",
        notifyUuid = "BEB5483E-36E1-4688-B7F5-EA07361B26A8"
    ),
    CUSTOM(
        label = "Custom UUIDs",
        serviceUuid = "",
        writeUuid = "",
        notifyUuid = ""
    )
}

data class BleConfig(
    val serviceUuid: String = BlePreset.NORDIC_UART.serviceUuid,
    val writeCharacteristicUuid: String = BlePreset.NORDIC_UART.writeUuid,
    val notifyCharacteristicUuid: String = BlePreset.NORDIC_UART.notifyUuid,
    val autoDetectWritable: Boolean = true,
    val appendNewline: Boolean = true,
    val writeWithoutResponse: Boolean = false,
    val filterNamedOnly: Boolean = true,
    val savedDeviceAddress: String = "",
    val savedDeviceName: String = "",
    val trackpadSensitivity: Float = 1.6f,
    val defaultSkipScreen: String = "lg",
    val lgX: Int = 990,
    val lgY: Int = 350,
    val rogX: Int = 1400,
    val rogY: Int = 400,
    val customX: Int = 1200,
    val customY: Int = 380,
    val commandPayloads: Map<RemoteActionId, String> = RemoteActionId.entries.associateWith { it.defaultPayload }
) {
    fun payloadFor(action: RemoteActionId): String =
        commandPayloads[action]?.takeIf { it.isNotBlank() } ?: action.defaultPayload

    fun activePreset(): BlePreset = when {
        serviceUuid.equals(BlePreset.NORDIC_UART.serviceUuid, ignoreCase = true) &&
            writeCharacteristicUuid.equals(BlePreset.NORDIC_UART.writeUuid, ignoreCase = true) -> BlePreset.NORDIC_UART
        serviceUuid.equals(BlePreset.ESP32_EXAMPLE.serviceUuid, ignoreCase = true) &&
            writeCharacteristicUuid.equals(BlePreset.ESP32_EXAMPLE.writeUuid, ignoreCase = true) -> BlePreset.ESP32_EXAMPLE
        else -> BlePreset.CUSTOM
    }
}

data class BleDiscoveredDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val isSaved: Boolean = false,
)

sealed interface BleConnectionState {
    data object Disconnected : BleConnectionState
    data object Scanning : BleConnectionState
    data class Connecting(val deviceName: String, val address: String) : BleConnectionState
    data class Connected(
        val deviceName: String,
        val address: String,
        val activeCharacteristicUuid: String,
        val mtu: Int = 23,
    ) : BleConnectionState
    data class Error(val message: String) : BleConnectionState
}

enum class RemoteMode {
    MEDIA_REMOTE,
    TRACKPAD_AND_KEYS
}

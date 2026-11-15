package com.example.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.model.BleConfig
import com.example.model.BleConnectionState
import com.example.model.BleDiscoveredDevice
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

class BleRemoteManager(private val context: Context) {

    companion object {
        private val CCCD_UUID: UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB")
        private val STANDARD_GAP_GATT_PREFIXES = setOf("00001800", "00001801")
        private const val SCAN_TIMEOUT_MS = 15_000L
        private const val WRITE_ACK_TIMEOUT_MS = 450L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager?.adapter

    val isBleSupported: Boolean
        get() = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) &&
            bluetoothAdapter != null

    private val _isBluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled == true)
    val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _connectionState = MutableStateFlow<BleConnectionState>(BleConnectionState.Disconnected)
    val connectionState: StateFlow<BleConnectionState> = _connectionState.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BleDiscoveredDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BleDiscoveredDevice>> = _discoveredDevices.asStateFlow()

    private val _lastRxMessage = MutableStateFlow<String?>(null)
    val lastRxMessage: StateFlow<String?> = _lastRxMessage.asStateFlow()

    private val deviceMap = LinkedHashMap<String, BleDiscoveredDevice>()
    private var activeConfig: BleConfig = BleConfig()
    private var activeGatt: BluetoothGatt? = null
    private var writeCharacteristic: BluetoothGattCharacteristic? = null
    private var currentMtu: Int = 23
    private var scanJob: Job? = null
    private var isScanning = false

    private data class WriteRequest(
        val bytes: ByteArray,
        val preferNoResponse: Boolean,
    )

    private val writeQueue = Channel<WriteRequest>(capacity = 64)
    private var pendingWriteAck: CompletableDeferred<Boolean>? = null

    private val btStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                val enabled = state == BluetoothAdapter.STATE_ON
                _isBluetoothEnabled.value = enabled
                if (!enabled) {
                    stopScan()
                    closeGatt()
                    _connectionState.value = BleConnectionState.Disconnected
                }
            }
        }
    }

    init {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        ContextCompat.registerReceiver(
            context,
            btStateReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        startWriteProcessor()
    }

    fun updateConfig(config: BleConfig) {
        activeConfig = config
        // If already connected, re-resolve characteristic if UUIDs changed
        activeGatt?.let { gatt ->
            if (_connectionState.value is BleConnectionState.Connected) {
                resolveCharacteristics(gatt)
            }
        }
    }

    fun refreshBluetoothState() {
        _isBluetoothEnabled.value = bluetoothAdapter?.isEnabled == true
    }

    fun hasRequiredPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) ==
                PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) ==
                PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        }
    }

    fun requiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        refreshBluetoothState()
        if (!isBleSupported) {
            _connectionState.value = BleConnectionState.Error("BLE not supported on this device")
            return
        }
        if (!hasRequiredPermissions()) {
            _connectionState.value = BleConnectionState.Error("Bluetooth permissions required")
            return
        }
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) {
            _connectionState.value = BleConnectionState.Error("Bluetooth is turned off")
            return
        }
        val scanner = adapter.bluetoothLeScanner ?: run {
            _connectionState.value = BleConnectionState.Error("BLE scanner unavailable")
            return
        }

        stopScan()
        deviceMap.clear()
        _discoveredDevices.value = emptyList()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            scanner.startScan(null, settings, scanCallback)
            isScanning = true
            if (_connectionState.value !is BleConnectionState.Connected) {
                _connectionState.value = BleConnectionState.Scanning
            }
            scanJob = scope.launch {
                delay(SCAN_TIMEOUT_MS)
                stopScan()
            }
        } catch (se: SecurityException) {
            _connectionState.value = BleConnectionState.Error("Missing scan permission: ${se.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        if (!isScanning) return
        isScanning = false
        try {
            if (hasRequiredPermissions()) {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            }
        } catch (_: SecurityException) {
        } catch (_: IllegalStateException) {
        }
        if (_connectionState.value is BleConnectionState.Scanning) {
            _connectionState.value = BleConnectionState.Disconnected
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            handleScanResult(result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            results.forEach(::handleScanResult)
        }

        override fun onScanFailed(errorCode: Int) {
            isScanning = false
            if (_connectionState.value !is BleConnectionState.Connected) {
                _connectionState.value = BleConnectionState.Error("BLE scan failed (code $errorCode)")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleScanResult(result: ScanResult) {
        val device = result.device ?: return
        val address = device.address ?: return
        val rawName = try {
            result.scanRecord?.deviceName ?: device.name
        } catch (_: SecurityException) {
            result.scanRecord?.deviceName
        }

        if (activeConfig.filterNamedOnly && rawName.isNullOrBlank()) return

        val displayName = rawName?.takeIf { it.isNotBlank() } ?: "Unknown BLE Device"
        val isSaved = activeConfig.savedDeviceAddress.equals(address, ignoreCase = true)

        deviceMap[address] = BleDiscoveredDevice(
            name = displayName,
            address = address,
            rssi = result.rssi,
            isSaved = isSaved
        )

        _discoveredDevices.value = deviceMap.values
            .sortedWith(
                compareByDescending<BleDiscoveredDevice> { it.isSaved }
                    .thenByDescending { it.name.contains("ESP", ignoreCase = true) || it.name.contains("Remote", ignoreCase = true) }
                    .thenByDescending { it.rssi }
            )
    }

    @SuppressLint("MissingPermission")
    fun connect(address: String, fallbackName: String = "ESP32-S3"): Boolean {
        refreshBluetoothState()
        if (!isBleSupported || !hasRequiredPermissions()) {
            _connectionState.value = BleConnectionState.Error("Bluetooth permissions required")
            return false
        }
        val adapter = bluetoothAdapter ?: return false
        if (!adapter.isEnabled) {
            _connectionState.value = BleConnectionState.Error("Bluetooth is turned off")
            return false
        }

        stopScan()
        closeGatt()

        return try {
            val device = adapter.getRemoteDevice(address)
            val name = try {
                device.name?.takeIf { it.isNotBlank() } ?: fallbackName
            } catch (_: SecurityException) {
                fallbackName
            }
            _connectionState.value = BleConnectionState.Connecting(name, address)
            activeGatt = device.connectGatt(
                context,
                false,
                gattCallback,
                BluetoothDevice.TRANSPORT_LE
            )
            true
        } catch (e: Exception) {
            _connectionState.value = BleConnectionState.Error(e.message ?: "Failed to connect")
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun disconnect() {
        stopScan()
        closeGatt()
        _connectionState.value = BleConnectionState.Disconnected
    }

    @SuppressLint("MissingPermission")
    private fun closeGatt() {
        writeCharacteristic = null
        pendingWriteAck?.complete(false)
        pendingWriteAck = null
        try {
            activeGatt?.disconnect()
            activeGatt?.close()
        } catch (_: SecurityException) {
        } catch (_: Exception) {
        }
        activeGatt = null
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS && newState != BluetoothProfile.STATE_CONNECTED) {
                val deviceAddr = gatt.device?.address ?: ""
                closeGatt()
                _connectionState.value = if (status == 133) {
                    BleConnectionState.Error("Connection dropped (GATT 133). Retry near ESP32-S3.")
                } else {
                    BleConnectionState.Disconnected
                }
                return
            }

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                try {
                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                    gatt.requestMtu(185)
                    gatt.discoverServices()
                } catch (se: SecurityException) {
                    _connectionState.value = BleConnectionState.Error("Permission error: ${se.message}")
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                closeGatt()
                _connectionState.value = BleConnectionState.Disconnected
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                currentMtu = mtu
                val current = _connectionState.value
                if (current is BleConnectionState.Connected) {
                    _connectionState.value = current.copy(mtu = mtu)
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = BleConnectionState.Error("Service discovery failed ($status)")
                return
            }
            resolveCharacteristics(gatt)
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            pendingWriteAck?.complete(status == BluetoothGatt.GATT_SUCCESS)
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            val text = value.toString(Charsets.UTF_8).trim()
            if (text.isNotEmpty()) {
                _lastRxMessage.value = text
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            @Suppress("DEPRECATION")
            val text = characteristic.value?.toString(Charsets.UTF_8)?.trim().orEmpty()
            if (text.isNotEmpty()) {
                _lastRxMessage.value = text
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun resolveCharacteristics(gatt: BluetoothGatt) {
        val targetServiceUuid = parseUuidOrNull(activeConfig.serviceUuid)
        val targetWriteUuid = parseUuidOrNull(activeConfig.writeCharacteristicUuid)
        val targetNotifyUuid = parseUuidOrNull(activeConfig.notifyCharacteristicUuid)

        var resolvedWrite: BluetoothGattCharacteristic? = null
        var resolvedNotify: BluetoothGattCharacteristic? = null

        // 1. Exact service + characteristic match
        if (targetServiceUuid != null) {
            val service = gatt.getService(targetServiceUuid)
            if (service != null) {
                if (targetWriteUuid != null) {
                    resolvedWrite = service.getCharacteristic(targetWriteUuid)
                }
                if (targetNotifyUuid != null) {
                    resolvedNotify = service.getCharacteristic(targetNotifyUuid)
                }
            }
        }

        // 2. Global characteristic UUID match across all services
        if (resolvedWrite == null && targetWriteUuid != null) {
            resolvedWrite = gatt.services
                .flatMap { it.characteristics }
                .firstOrNull { it.uuid == targetWriteUuid && isWritable(it) }
        }

        // 3. Auto-detect first non-system writable characteristic if enabled
        if (resolvedWrite == null && activeConfig.autoDetectWritable) {
            resolvedWrite = gatt.services
                .filterNot { svc ->
                    val prefix = svc.uuid.toString().lowercase().take(8)
                    prefix in STANDARD_GAP_GATT_PREFIXES
                }
                .flatMap { it.characteristics }
                .firstOrNull { isWritable(it) }
        }

        if (resolvedWrite == null) {
            _connectionState.value = BleConnectionState.Error(
                "No writable BLE characteristic found. Check UUIDs in Settings."
            )
            return
        }

        writeCharacteristic = resolvedWrite

        // Enable notifications on RX/Notify characteristic if present
        val notifyChar = resolvedNotify ?: gatt.services
            .filterNot { it.uuid.toString().lowercase().take(8) in STANDARD_GAP_GATT_PREFIXES }
            .flatMap { it.characteristics }
            .firstOrNull { isNotifiable(it) }

        if (notifyChar != null) {
            enableNotifications(gatt, notifyChar)
        }

        val name = try {
            gatt.device?.name?.takeIf { it.isNotBlank() } ?: activeConfig.savedDeviceName.ifBlank { "ESP32-S3" }
        } catch (_: SecurityException) {
            activeConfig.savedDeviceName.ifBlank { "ESP32-S3" }
        }
        val address = gatt.device?.address.orEmpty()
        _connectionState.value = BleConnectionState.Connected(
            deviceName = name,
            address = address,
            activeCharacteristicUuid = resolvedWrite.uuid.toString().uppercase(),
            mtu = currentMtu
        )
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        try {
            gatt.setCharacteristicNotification(characteristic, true)
            val descriptor = characteristic.getDescriptor(CCCD_UUID) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
            } else {
                @Suppress("DEPRECATION")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                @Suppress("DEPRECATION")
                gatt.writeDescriptor(descriptor)
            }
        } catch (_: Exception) {
        }
    }

    fun sendCommand(payload: String, preferNoResponse: Boolean = activeConfig.writeWithoutResponse): Boolean {
        val char = writeCharacteristic
        val gatt = activeGatt
        if (char == null || gatt == null || _connectionState.value !is BleConnectionState.Connected) {
            return false
        }
        val formatted = if (activeConfig.appendNewline && !payload.endsWith("\n")) {
            "$payload\n"
        } else {
            payload
        }
        val bytes = formatted.toByteArray(Charsets.UTF_8)
        return writeQueue.trySend(WriteRequest(bytes, preferNoResponse)).isSuccess
    }

    private fun startWriteProcessor() {
        scope.launch {
            for (req in writeQueue) {
                performGattWrite(req.bytes, req.preferNoResponse)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun performGattWrite(bytes: ByteArray, preferNoResponse: Boolean) {
        val gatt = activeGatt ?: return
        val char = writeCharacteristic ?: return
        if (!hasRequiredPermissions()) return

        val supportsNoResponse =
            (char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0
        val supportsDefault =
            (char.properties and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0

        val writeType = when {
            preferNoResponse && supportsNoResponse -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            supportsDefault -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            supportsNoResponse -> BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        }

        val deferred = CompletableDeferred<Boolean>()
        pendingWriteAck = deferred

        val started = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                gatt.writeCharacteristic(char, bytes, writeType) == BluetoothStatusCodes.SUCCESS
            } else {
                @Suppress("DEPRECATION")
                char.writeType = writeType
                @Suppress("DEPRECATION")
                char.value = bytes
                @Suppress("DEPRECATION")
                gatt.writeCharacteristic(char)
            }
        } catch (_: Exception) {
            false
        }

        if (started) {
            withTimeoutOrNull(WRITE_ACK_TIMEOUT_MS) {
                deferred.await()
            }
        }
        pendingWriteAck = null
    }

    private fun isWritable(char: BluetoothGattCharacteristic): Boolean {
        val props = char.properties
        return (props and BluetoothGattCharacteristic.PROPERTY_WRITE) != 0 ||
            (props and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0
    }

    private fun isNotifiable(char: BluetoothGattCharacteristic): Boolean {
        val props = char.properties
        return (props and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0 ||
            (props and BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0
    }

    private fun parseUuidOrNull(raw: String): UUID? {
        return try {
            if (raw.isBlank()) null else UUID.fromString(raw.trim())
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    fun release() {
        stopScan()
        closeGatt()
        writeQueue.close()
        try {
            context.unregisterReceiver(btStateReceiver)
        } catch (_: IllegalArgumentException) {
        }
        scope.cancel()
    }
}

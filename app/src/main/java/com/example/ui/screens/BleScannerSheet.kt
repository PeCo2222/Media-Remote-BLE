package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.BluetoothDisabled
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BleConnectionState
import com.example.model.BleDiscoveredDevice
import com.example.ui.theme.BleConnectedCyan
import com.example.ui.theme.BleErrorCoral
import com.example.ui.theme.BleScanningAmber
import com.example.ui.theme.MediaCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.RemoteUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleScannerSheet(
    uiState: RemoteUiState,
    onDismiss: () -> Unit,
    onRequestPermissions: () -> Unit,
    onRequestEnableBluetooth: () -> Unit,
    onStartScan: () -> Unit,
    onStopScan: () -> Unit,
    onSelectDevice: (BleDiscoveredDevice) -> Unit,
    onDisconnect: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ObsidianSurface,
        contentColor = TextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.scanner_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.scanner_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("scanner_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close scanner",
                        tint = TextSecondary
                    )
                }
            }

            if (uiState.connectionState is BleConnectionState.Connected) {
                ConnectedBannerCard(
                    state = uiState.connectionState,
                    onDisconnect = onDisconnect
                )
            }

            when {
                !uiState.isBleSupported -> {
                    PermissionOrWarningCard(
                        icon = Icons.Rounded.BluetoothDisabled,
                        title = "BLE Hardware Unavailable",
                        body = stringResource(R.string.status_ble_unavailable),
                        actionLabel = null,
                        onAction = {}
                    )
                }
                !uiState.hasBlePermissions -> {
                    PermissionOrWarningCard(
                        icon = Icons.Rounded.Bluetooth,
                        title = "Nearby Devices Permission",
                        body = "Android requires Nearby Devices permission to scan and connect to your ESP32-S3 over Bluetooth Low Energy.",
                        actionLabel = "Grant BLE Permission",
                        onAction = onRequestPermissions
                    )
                }
                !uiState.isBluetoothEnabled -> {
                    PermissionOrWarningCard(
                        icon = Icons.Rounded.BluetoothDisabled,
                        title = "Bluetooth Is Off",
                        body = stringResource(R.string.status_bt_disabled),
                        actionLabel = "Turn On Bluetooth",
                        onAction = onRequestEnableBluetooth
                    )
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val isScanning = uiState.connectionState is BleConnectionState.Scanning
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isScanning) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = BleScanningAmber
                                )
                                Text(
                                    text = "Scanning nearby peripherals...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BleScanningAmber
                                )
                            } else {
                                Text(
                                    text = "${uiState.discoveredDevices.size} peripheral(s) found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { if (isScanning) onStopScan() else onStartScan() },
                            border = BorderStroke(1.dp, ObsidianBorder),
                            modifier = Modifier.testTag("scanner_toggle_scan_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                tint = MediaCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(
                                    if (isScanning) R.string.scanner_stop else R.string.scanner_rescan
                                ),
                                color = TextPrimary
                            )
                        }
                    }

                    if (uiState.discoveredDevices.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = ObsidianBg,
                            border = BorderStroke(1.dp, ObsidianBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Memory,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    text = stringResource(R.string.scanner_empty),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = uiState.discoveredDevices,
                                key = { it.address }
                            ) { device ->
                                DeviceRowCard(
                                    device = device,
                                    isConnecting = (uiState.connectionState as? BleConnectionState.Connecting)?.address == device.address,
                                    isConnected = (uiState.connectionState as? BleConnectionState.Connected)?.address == device.address,
                                    onClick = { onSelectDevice(device) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectedBannerCard(
    state: BleConnectionState.Connected,
    onDisconnect: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ObsidianSurfaceHigh,
        border = BorderStroke(1.dp, BleConnectedCyan.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(BleConnectedCyan.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.BluetoothConnected,
                        contentDescription = null,
                        tint = BleConnectedCyan
                    )
                }
                Column {
                    Text(
                        text = state.deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${state.address} · MTU ${state.mtu}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
            OutlinedButton(
                onClick = onDisconnect,
                border = BorderStroke(1.dp, BleErrorCoral.copy(alpha = 0.6f)),
                modifier = Modifier.testTag("scanner_disconnect_button")
            ) {
                Text(
                    text = stringResource(R.string.scanner_disconnect),
                    color = BleErrorCoral
                )
            }
        }
    }
}

@Composable
private fun PermissionOrWarningCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    actionLabel: String?,
    onAction: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ObsidianBg,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MediaCyan)
                Text(text = title, style = MaterialTheme.typography.titleMedium, color = TextPrimary)
            }
            Text(text = body, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            if (actionLabel != null) {
                Button(
                    onClick = onAction,
                    colors = ButtonDefaults.buttonColors(containerColor = MediaCyan),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scanner_permission_action_button")
                ) {
                    Text(text = actionLabel, color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun DeviceRowCard(
    device: BleDiscoveredDevice,
    isConnecting: Boolean,
    isConnected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = when {
        isConnected -> BleConnectedCyan
        device.isSaved -> MediaCyan.copy(alpha = 0.5f)
        else -> ObsidianBorder
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("ble_device_${device.address}"),
        shape = RoundedCornerShape(14.dp),
        color = ObsidianSurfaceHigh,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = device.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (device.isSaved) {
                        Text(
                            text = "SAVED",
                            style = MaterialTheme.typography.labelSmall,
                            color = BleConnectedCyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BleConnectedCyan.copy(alpha = 0.14f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
            }

            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = BleScanningAmber
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SignalCellularAlt,
                        contentDescription = "Signal strength ${device.rssi} dBm",
                        tint = if (device.rssi > -70) BleConnectedCyan else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${device.rssi} dBm",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

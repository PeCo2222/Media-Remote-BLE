package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.BluetoothSearching
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.BleConnectionState
import com.example.model.RemoteMode
import com.example.ui.theme.BleConnectedCyan
import com.example.ui.theme.BleDisconnectedSlate
import com.example.ui.theme.BleErrorCoral
import com.example.ui.theme.BleScanningAmber
import com.example.ui.theme.MediaCyan
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TactileRemoteButton(
    label: String,
    containerColor: Color,
    darkerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    height: Dp = 60.dp,
    testTag: String,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "button_press_scale"
    )

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .scale(scale)
            .defaultMinSize(minHeight = height)
            .height(height)
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        containerColor.copy(alpha = 0.96f),
                        darkerColor
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.24f),
                        Color.Black.copy(alpha = 0.25f)
                    )
                ),
                shape = shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White.copy(alpha = 0.25f)),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            )
            .padding(horizontal = 14.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(21.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ModeSegmentedPill(
    activeMode: RemoteMode,
    onSelectMode: (RemoteMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ObsidianSurface,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RemoteMode.entries.forEach { mode ->
                val selected = mode == activeMode
                val bgColor by animateColorAsState(
                    targetValue = if (selected) ObsidianSurfaceHigh else Color.Transparent,
                    label = "mode_pill_bg"
                )
                val borderColor by animateColorAsState(
                    targetValue = if (selected) MediaCyan.copy(alpha = 0.55f) else Color.Transparent,
                    label = "mode_pill_border"
                )
                val label = when (mode) {
                    RemoteMode.MEDIA_REMOTE -> "Media Remote"
                    RemoteMode.TRACKPAD_AND_KEYS -> "Trackpad & Keys"
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(10.dp))
                        .background(bgColor)
                        .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                        .clickable { onSelectMode(mode) }
                        .padding(vertical = 10.dp)
                        .testTag("mode_tab_${mode.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (selected) TextPrimary else TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun BleStatusBadge(
    connectionState: BleConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (dotColor, label) = when (connectionState) {
        is BleConnectionState.Connected -> BleConnectedCyan to "BLE · ${connectionState.deviceName}"
        is BleConnectionState.Connecting -> BleScanningAmber to "Connecting..."
        is BleConnectionState.Scanning -> BleScanningAmber to "Scanning..."
        is BleConnectionState.Error -> BleErrorCoral to "BLE Error"
        BleConnectionState.Disconnected -> BleDisconnectedSlate to "BLE Disconnected"
    }

    Surface(
        modifier = modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag("ble_status_badge"),
        shape = CircleShape,
        color = ObsidianSurfaceHigh,
        border = BorderStroke(1.dp, dotColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun BleConnectionOrbButton(
    connectionState: BleConnectionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String,
) {
    val accentColor by animateColorAsState(
        targetValue = when (connectionState) {
            is BleConnectionState.Connected -> BleConnectedCyan
            is BleConnectionState.Connecting,
            is BleConnectionState.Scanning -> BleScanningAmber
            is BleConnectionState.Error -> BleErrorCoral
            BleConnectionState.Disconnected -> BleDisconnectedSlate
        },
        label = "ble_orb_color"
    )

    val icon = when (connectionState) {
        is BleConnectionState.Connected -> Icons.Rounded.BluetoothConnected
        is BleConnectionState.Connecting,
        is BleConnectionState.Scanning -> Icons.Rounded.BluetoothSearching
        is BleConnectionState.Error -> Icons.Rounded.WarningAmber
        BleConnectionState.Disconnected -> Icons.Rounded.Bluetooth
    }

    val shape = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier
            .size(56.dp)
            .clip(shape)
            .clickable(onClick = onClick)
            .testTag("ble_connect_button"),
        shape = shape,
        color = ObsidianSurfaceHigh,
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.7f)),
        tonalElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (connectionState is BleConnectionState.Disconnected) TextSecondary else accentColor,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

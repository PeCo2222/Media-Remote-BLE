package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.ClosedCaption
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Mouse
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.BleConnectionState
import com.example.model.PcKeyboardLayout
import com.example.model.RemoteActionId
import com.example.model.RemoteMode
import com.example.ui.components.BleConnectionOrbButton
import com.example.ui.components.BleStatusBadge
import com.example.ui.components.ModeSegmentedPill
import com.example.ui.components.TactileRemoteButton
import com.example.ui.theme.BleConnectedCyan
import com.example.ui.theme.ControlSlate
import com.example.ui.theme.ControlSlateDark
import com.example.ui.theme.MediaCyan
import com.example.ui.theme.MediaCyanDark
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceHigh
import com.example.ui.theme.SkipAdPurple
import com.example.ui.theme.SkipAdPurpleDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VolumeEmerald
import com.example.ui.theme.VolumeEmeraldDark
import com.example.viewmodel.RemoteUiState

@Composable
fun RemoteMainScreen(
    uiState: RemoteUiState,
    onAction: (RemoteActionId) -> Unit,
    onSelectMode: (RemoteMode) -> Unit,
    onTrackpadDrag: (Float, Float) -> Unit,
    onTrackpadDragEnd: () -> Unit,
    onTrackpadClick: (Boolean) -> Unit,
    onScrollDrag: (Float) -> Unit,
    onSendText: (String) -> Unit,
    onSelectPcLayout: (PcKeyboardLayout) -> Unit,
    onToggleAutoEnter: (Boolean) -> Unit,
    onBleOrbClick: () -> Unit,
    onOpenScanner: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHelp: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ObsidianBg,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MediaCyan.copy(alpha = 0.08f),
                            ObsidianBg
                        ),
                        radius = 1100f
                    )
                )
                .padding(innerPadding)
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                val isWideLayout = maxWidth >= 700.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = if (isWideLayout) 960.dp else 520.dp)
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar (? | Media Remote | Settings)
                    RemoteHeaderBar(
                        connectionState = uiState.connectionState,
                        onOpenHelp = onOpenHelp,
                        onOpenSettings = onOpenSettings,
                        onOpenScanner = onOpenScanner
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isWideLayout) {
                        // Adaptive split-pane layout on tablets / foldables
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MediaRemoteGrid(onAction = onAction)
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                TrackpadAndKeysPanel(
                                    onTrackpadDrag = onTrackpadDrag,
                                    onTrackpadDragEnd = onTrackpadDragEnd,
                                    onTrackpadClick = onTrackpadClick,
                                    onScrollDrag = onScrollDrag,
                                    onAction = onAction,
                                    onSendText = onSendText,
                                    lastTypedText = uiState.lastTypedText,
                                    selectedPcLayout = uiState.config.pcKeyboardLayout,
                                    autoEnterEnabled = uiState.config.sendEnterAfterText,
                                    onSelectPcLayout = onSelectPcLayout,
                                    onToggleAutoEnter = onToggleAutoEnter,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    } else {
                        // Phone layout: Mode Switcher + Hero Remote / Trackpad
                        ModeSegmentedPill(
                            activeMode = uiState.activeMode,
                            onSelectMode = onSelectMode
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            when (uiState.activeMode) {
                                RemoteMode.MEDIA_REMOTE -> {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        MediaRemoteGrid(onAction = onAction)
                                    }
                                }
                                RemoteMode.TRACKPAD_AND_KEYS -> {
                                    TrackpadAndKeysPanel(
                                        onTrackpadDrag = onTrackpadDrag,
                                        onTrackpadDragEnd = onTrackpadDragEnd,
                                        onTrackpadClick = onTrackpadClick,
                                        onScrollDrag = onScrollDrag,
                                        onAction = onAction,
                                        onSendText = onSendText,
                                        lastTypedText = uiState.lastTypedText,
                                        selectedPcLayout = uiState.config.pcKeyboardLayout,
                                        autoEnterEnabled = uiState.config.sendEnterAfterText,
                                        onSelectPcLayout = onSelectPcLayout,
                                        onToggleAutoEnter = onToggleAutoEnter,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }

                    val imeOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
                    if (!imeOpen) {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Bottom Status & BLE Connect Footer
                        RemoteBottomFooter(
                            uiState = uiState,
                            onBleOrbClick = onBleOrbClick,
                            onOpenScanner = onOpenScanner
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RemoteHeaderBar(
    connectionState: BleConnectionState,
    onOpenHelp: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenScanner: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HeaderIconButton(
            icon = Icons.Rounded.QuestionMark,
            contentDescription = stringResource(R.string.cd_help),
            testTag = "help_button",
            onClick = onOpenHelp
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            BleStatusBadge(
                connectionState = connectionState,
                onClick = onOpenScanner
            )
        }

        HeaderIconButton(
            icon = Icons.Rounded.Settings,
            contentDescription = stringResource(R.string.cd_settings),
            testTag = "settings_button",
            onClick = onOpenSettings
        )
    }
}

@Composable
private fun HeaderIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clip(shape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = shape,
        color = ObsidianSurfaceHigh,
        border = BorderStroke(1.dp, ObsidianBorder)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun MediaRemoteGrid(
    onAction: (RemoteActionId) -> Unit,
) {
    // 1. Skip Ad (full width Purple)
    TactileRemoteButton(
        label = stringResource(R.string.action_skip_ad),
        icon = Icons.Rounded.SkipNext,
        containerColor = SkipAdPurple,
        darkerColor = SkipAdPurpleDark,
        height = 64.dp,
        testTag = "btn_skip_ad",
        onClick = { onAction(RemoteActionId.SKIP_AD) },
        modifier = Modifier.fillMaxWidth()
    )

    // 2. Play / Pause (full width Cyan)
    TactileRemoteButton(
        label = stringResource(R.string.action_play_pause),
        icon = Icons.Rounded.PlayArrow,
        containerColor = MediaCyan,
        darkerColor = MediaCyanDark,
        height = 66.dp,
        testTag = "btn_play_pause",
        onClick = { onAction(RemoteActionId.PLAY_PAUSE) },
        modifier = Modifier.fillMaxWidth()
    )

    // 3. - 5 sec | + 5 sec
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TactileRemoteButton(
            label = stringResource(R.string.action_seek_back),
            icon = Icons.Rounded.FastRewind,
            containerColor = MediaCyan,
            darkerColor = MediaCyanDark,
            testTag = "btn_seek_back",
            onClick = { onAction(RemoteActionId.SEEK_BACK) },
            modifier = Modifier.weight(1f)
        )
        TactileRemoteButton(
            label = stringResource(R.string.action_seek_fwd),
            icon = Icons.Rounded.FastForward,
            containerColor = MediaCyan,
            darkerColor = MediaCyanDark,
            testTag = "btn_seek_fwd",
            onClick = { onAction(RemoteActionId.SEEK_FWD) },
            modifier = Modifier.weight(1f)
        )
    }

    // 4. Key Up | Key Down
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TactileRemoteButton(
            label = stringResource(R.string.action_key_up),
            icon = Icons.Rounded.KeyboardArrowUp,
            containerColor = MediaCyan,
            darkerColor = MediaCyanDark,
            testTag = "btn_key_up",
            onClick = { onAction(RemoteActionId.KEY_UP) },
            modifier = Modifier.weight(1f)
        )
        TactileRemoteButton(
            label = stringResource(R.string.action_key_down),
            icon = Icons.Rounded.KeyboardArrowDown,
            containerColor = MediaCyan,
            darkerColor = MediaCyanDark,
            testTag = "btn_key_down",
            onClick = { onAction(RemoteActionId.KEY_DOWN) },
            modifier = Modifier.weight(1f)
        )
    }

    // 5. Vol - | Vol +
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TactileRemoteButton(
            label = stringResource(R.string.action_vol_down),
            icon = Icons.Rounded.VolumeDown,
            containerColor = VolumeEmerald,
            darkerColor = VolumeEmeraldDark,
            testTag = "btn_vol_down",
            onClick = { onAction(RemoteActionId.VOL_DOWN) },
            modifier = Modifier.weight(1f)
        )
        TactileRemoteButton(
            label = stringResource(R.string.action_vol_up),
            icon = Icons.Rounded.VolumeUp,
            containerColor = VolumeEmerald,
            darkerColor = VolumeEmeraldDark,
            testTag = "btn_vol_up",
            onClick = { onAction(RemoteActionId.VOL_UP) },
            modifier = Modifier.weight(1f)
        )
    }

    // 6. Fullscreen (F) | Subtitles (C)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TactileRemoteButton(
            label = stringResource(R.string.action_fullscreen),
            icon = Icons.Rounded.Fullscreen,
            containerColor = ControlSlate,
            darkerColor = ControlSlateDark,
            testTag = "btn_fullscreen",
            onClick = { onAction(RemoteActionId.FULLSCREEN) },
            modifier = Modifier.weight(1f)
        )
        TactileRemoteButton(
            label = stringResource(R.string.action_subtitles),
            icon = Icons.Rounded.ClosedCaption,
            containerColor = ControlSlate,
            darkerColor = ControlSlateDark,
            testTag = "btn_subtitles",
            onClick = { onAction(RemoteActionId.SUBTITLES) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TrackpadAndKeysPanel(
    onTrackpadDrag: (Float, Float) -> Unit,
    onTrackpadDragEnd: () -> Unit,
    onTrackpadClick: (Boolean) -> Unit,
    onScrollDrag: (Float) -> Unit,
    onAction: (RemoteActionId) -> Unit,
    onSendText: (String) -> Unit,
    lastTypedText: String?,
    selectedPcLayout: PcKeyboardLayout,
    autoEnterEnabled: Boolean,
    onSelectPcLayout: (PcKeyboardLayout) -> Unit,
    onToggleAutoEnter: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var textDraft by remember { mutableStateOf(TextFieldValue("")) }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Trackpad + Edge Scroll Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Main Mouse Touchpad Surface
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ObsidianSurface)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(18.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { onTrackpadDragEnd() },
                            onDragCancel = { onTrackpadDragEnd() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onTrackpadDrag(dragAmount.x, dragAmount.y)
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTrackpadClick(false)
                            },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTrackpadClick(true)
                            }
                        )
                    }
                    .testTag("mouse_trackpad_surface"),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val step = 36.dp.toPx()
                    val dotColor = Color.White.copy(alpha = 0.05f)
                    var x = step
                    while (x < size.width) {
                        var y = step
                        while (y < size.height) {
                            drawCircle(color = dotColor, radius = 1.5.dp.toPx(), center = Offset(x, y))
                            y += step
                        }
                        x += step
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Mouse,
                        contentDescription = null,
                        tint = MediaCyan.copy(alpha = 0.7f),
                        modifier = Modifier.size(30.dp)
                    )
                    Text(
                        text = "HID Trackpad",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "Drag to move · Tap = Left click · Hold = Right click",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Vertical Mouse Scroll Wheel Strip
            Box(
                modifier = Modifier
                    .width(54.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianSurfaceHigh)
                    .border(1.dp, ObsidianBorder, RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onScrollDrag(dragAmount.y)
                        }
                    }
                    .testTag("mouse_scroll_strip"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SwapVert,
                        contentDescription = "Scroll wheel",
                        tint = MediaCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "SCR",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }

        // Left / Right Mouse Click Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TactileRemoteButton(
                label = "Left Click",
                containerColor = ControlSlate,
                darkerColor = ControlSlateDark,
                height = 50.dp,
                testTag = "btn_left_click",
                onClick = { onTrackpadClick(false) },
                modifier = Modifier.weight(1f)
            )
            TactileRemoteButton(
                label = "Right Click",
                containerColor = ControlSlate,
                darkerColor = ControlSlateDark,
                height = 50.dp,
                testTag = "btn_right_click",
                onClick = { onTrackpadClick(true) },
                modifier = Modifier.weight(1f)
            )
        }

        // Quick PC Keys Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                RemoteActionId.ESC to "Esc",
                RemoteActionId.SPACE to "Space",
                RemoteActionId.TAB to "Tab",
                RemoteActionId.ENTER to "Enter"
            ).forEach { (action, title) ->
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onAction(action)
                        }
                        .testTag("btn_key_${action.name.lowercase()}"),
                    shape = RoundedCornerShape(12.dp),
                    color = ObsidianSurfaceHigh,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // PC Keyboard Layout Selector (matches Windows PC layout, independent of phone keyboard)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "PC:",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            PcKeyboardLayout.entries.forEach { layout ->
                val selected = layout == selectedPcLayout
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSelectPcLayout(layout) }
                        .testTag("pc_layout_${layout.name.lowercase()}"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) MediaCyan.copy(alpha = 0.22f) else ObsidianSurface,
                    border = BorderStroke(
                        1.dp,
                        if (selected) MediaCyan else ObsidianBorder
                    )
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = layout.shortLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) BleConnectedCyan else TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onToggleAutoEnter(!autoEnterEnabled) }
                    .testTag("toggle_auto_enter"),
                shape = RoundedCornerShape(10.dp),
                color = if (autoEnterEnabled) MediaCyan.copy(alpha = 0.22f) else ObsidianSurface,
                border = BorderStroke(
                    1.dp,
                    if (autoEnterEnabled) MediaCyan else ObsidianBorder
                )
            ) {
                Box(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (autoEnterEnabled) "↵ ON" else "↵ OFF",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (autoEnterEnabled) BleConnectedCyan else TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        if (!lastTypedText.isNullOrEmpty()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = ObsidianSurface,
                border = BorderStroke(1.dp, ObsidianBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Last sent: $lastTypedText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BleConnectedCyan,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        // Send Text to PC USB Keyboard
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = textDraft,
                onValueChange = { textDraft = it },
                placeholder = { Text("Type text to PC keyboard...", color = TextMuted) },
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 16.sp,
                    color = TextPrimary
                ),
                keyboardOptions = KeyboardOptions(
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (textDraft.text.isNotEmpty()) {
                            onSendText(textDraft.text)
                            textDraft = TextFieldValue("", TextRange.Zero)
                        }
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MediaCyan,
                    unfocusedBorderColor = ObsidianBorder,
                    focusedContainerColor = ObsidianSurface,
                    unfocusedContainerColor = ObsidianSurface,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = MediaCyan
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("keyboard_text_input")
            )
            Surface(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        if (textDraft.text.isNotEmpty()) {
                            onSendText(textDraft.text)
                            textDraft = TextFieldValue("", TextRange.Zero)
                        }
                    }
                    .testTag("keyboard_send_button"),
                shape = RoundedCornerShape(14.dp),
                color = MediaCyan
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "Send text to PC",
                        tint = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun RemoteBottomFooter(
    uiState: RemoteUiState,
    onBleOrbClick: () -> Unit,
    onOpenScanner: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = uiState.statusLine,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        if (!uiState.lastSentCommand.isNullOrBlank() || !uiState.lastRxMessage.isNullOrBlank()) {
            val rxPart = uiState.lastRxMessage?.let { "  ·  RX: $it" }.orEmpty()
            val txPart = uiState.lastSentCommand?.let { "TX: $it" }.orEmpty()
            Text(
                text = "$txPart$rxPart",
                style = MaterialTheme.typography.labelSmall,
                color = BleConnectedCyan.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BleConnectionOrbButton(
                connectionState = uiState.connectionState,
                onClick = onBleOrbClick,
                contentDescription = stringResource(R.string.cd_ble_connect)
            )

            Surface(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenScanner)
                    .testTag("open_ble_devices_button"),
                shape = RoundedCornerShape(12.dp),
                color = ObsidianSurfaceHigh,
                border = BorderStroke(1.dp, ObsidianBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Bluetooth,
                        contentDescription = null,
                        tint = MediaCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (uiState.connectionState is BleConnectionState.Connected) "Manage BLE" else "Scan ESP32",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

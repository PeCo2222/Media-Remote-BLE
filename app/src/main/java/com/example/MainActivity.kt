package com.example

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.BleScannerSheet
import com.example.ui.screens.HelpScreen
import com.example.ui.screens.RemoteMainScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MediaRemoteTheme
import com.example.viewmodel.RemoteViewModel

const val ROUTE_REMOTE = "remote"
const val ROUTE_SETTINGS = "settings"
const val ROUTE_HELP = "help"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MediaRemoteTheme {
                MediaRemoteApp()
            }
        }
    }
}

@Composable
fun MediaRemoteApp(
    viewModel: RemoteViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val navController = rememberNavController()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.onPermissionsResult()
    }

    val enableBluetoothLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.onBluetoothStateMaybeChanged()
    }

    NavHost(
        navController = navController,
        startDestination = ROUTE_REMOTE
    ) {
        composable(ROUTE_REMOTE) {
            RemoteMainScreen(
                uiState = uiState,
                onAction = viewModel::sendMediaAction,
                onSelectMode = viewModel::setMode,
                onTrackpadDrag = viewModel::sendTrackpadDelta,
                onTrackpadDragEnd = viewModel::flushTrackpadDelta,
                onTrackpadClick = viewModel::sendClick,
                onScrollDrag = viewModel::sendScrollDelta,
                onSendText = viewModel::sendRawText,
                onSelectPcLayout = viewModel::updatePcKeyboardLayout,
                onToggleAutoEnter = viewModel::updateSendEnterAfterText,
                onBleOrbClick = {
                    if (!uiState.hasBlePermissions) {
                        viewModel.openScannerSheet()
                        permissionLauncher.launch(viewModel.requiredBlePermissions())
                    } else if (!uiState.isBluetoothEnabled) {
                        viewModel.openScannerSheet()
                        enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    } else {
                        viewModel.onBottomBleButtonClick()
                    }
                },
                onOpenScanner = {
                    viewModel.openScannerSheet()
                    if (!uiState.hasBlePermissions) {
                        permissionLauncher.launch(viewModel.requiredBlePermissions())
                    }
                },
                onOpenSettings = { navController.navigate(ROUTE_SETTINGS) },
                onOpenHelp = { navController.navigate(ROUTE_HELP) }
            )

            if (uiState.showScannerSheet) {
                BleScannerSheet(
                    uiState = uiState,
                    onDismiss = viewModel::closeScannerSheet,
                    onRequestPermissions = {
                        permissionLauncher.launch(viewModel.requiredBlePermissions())
                    },
                    onRequestEnableBluetooth = {
                        enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
                    },
                    onStartScan = viewModel::startScan,
                    onStopScan = viewModel::stopScan,
                    onSelectDevice = viewModel::connectToDevice,
                    onDisconnect = viewModel::disconnect
                )
            }
        }

        composable(ROUTE_SETTINGS) {
            SettingsScreen(
                config = uiState.config,
                onBack = { navController.popBackStack() },
                onApplyPreset = viewModel::applyPreset,
                onSaveUuids = viewModel::updateUuids,
                onUpdateFlags = viewModel::updateFlags,
                onSensitivityChange = viewModel::updateTrackpadSensitivity,
                onPcLayoutChange = viewModel::updatePcKeyboardLayout,
                onSendEnterAfterTextChange = viewModel::updateSendEnterAfterText,
                onPayloadChange = viewModel::updateActionPayload,
                onTestSkipCalibration = viewModel::testSkipCalibration,
                onSaveSkipCalibration = viewModel::saveSkipCalibration,
                onForgetSavedDevice = viewModel::forgetSavedDevice,
                onResetDefaults = viewModel::resetDefaults
            )
        }

        composable(ROUTE_HELP) {
            HelpScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

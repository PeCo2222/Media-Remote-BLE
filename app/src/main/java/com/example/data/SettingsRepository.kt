package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.BleConfig
import com.example.model.BlePreset
import com.example.model.RemoteActionId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "media_remote_prefs")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val SERVICE_UUID = stringPreferencesKey("service_uuid")
        val WRITE_UUID = stringPreferencesKey("write_uuid")
        val NOTIFY_UUID = stringPreferencesKey("notify_uuid")
        val AUTO_DETECT = booleanPreferencesKey("auto_detect_writable")
        val APPEND_NEWLINE = booleanPreferencesKey("append_newline")
        val WRITE_NO_RESPONSE = booleanPreferencesKey("write_no_response")
        val FILTER_NAMED = booleanPreferencesKey("filter_named_only")
        val SAVED_ADDRESS = stringPreferencesKey("saved_device_address")
        val SAVED_NAME = stringPreferencesKey("saved_device_name")
        val TRACKPAD_SENSITIVITY = floatPreferencesKey("trackpad_sensitivity")
        val DEFAULT_SKIP_SCREEN = stringPreferencesKey("default_skip_screen")
        val LG_X = intPreferencesKey("lg_x")
        val LG_Y = intPreferencesKey("lg_y")
        val ROG_X = intPreferencesKey("rog_x")
        val ROG_Y = intPreferencesKey("rog_y")
        val CUST_X = intPreferencesKey("cust_x")
        val CUST_Y = intPreferencesKey("cust_y")

        fun payloadKey(action: RemoteActionId) = stringPreferencesKey("payload_${action.name}")
    }

    val configFlow: Flow<BleConfig> = context.dataStore.data.map { prefs ->
        val payloads = RemoteActionId.entries.associateWith { action ->
            prefs[Keys.payloadKey(action)] ?: action.defaultPayload
        }
        BleConfig(
            serviceUuid = prefs[Keys.SERVICE_UUID] ?: BlePreset.NORDIC_UART.serviceUuid,
            writeCharacteristicUuid = prefs[Keys.WRITE_UUID] ?: BlePreset.NORDIC_UART.writeUuid,
            notifyCharacteristicUuid = prefs[Keys.NOTIFY_UUID] ?: BlePreset.NORDIC_UART.notifyUuid,
            autoDetectWritable = prefs[Keys.AUTO_DETECT] ?: true,
            appendNewline = prefs[Keys.APPEND_NEWLINE] ?: true,
            writeWithoutResponse = prefs[Keys.WRITE_NO_RESPONSE] ?: false,
            filterNamedOnly = prefs[Keys.FILTER_NAMED] ?: true,
            savedDeviceAddress = prefs[Keys.SAVED_ADDRESS] ?: "",
            savedDeviceName = prefs[Keys.SAVED_NAME] ?: "",
            trackpadSensitivity = prefs[Keys.TRACKPAD_SENSITIVITY] ?: 1.6f,
            defaultSkipScreen = prefs[Keys.DEFAULT_SKIP_SCREEN] ?: "lg",
            lgX = prefs[Keys.LG_X] ?: 990,
            lgY = prefs[Keys.LG_Y] ?: 350,
            rogX = prefs[Keys.ROG_X] ?: 1400,
            rogY = prefs[Keys.ROG_Y] ?: 400,
            customX = prefs[Keys.CUST_X] ?: 1200,
            customY = prefs[Keys.CUST_Y] ?: 380,
            commandPayloads = payloads
        )
    }

    suspend fun updateUuids(serviceUuid: String, writeUuid: String, notifyUuid: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SERVICE_UUID] = serviceUuid.trim()
            prefs[Keys.WRITE_UUID] = writeUuid.trim()
            prefs[Keys.NOTIFY_UUID] = notifyUuid.trim()
        }
    }

    suspend fun applyPreset(preset: BlePreset) {
        if (preset == BlePreset.CUSTOM) return
        updateUuids(preset.serviceUuid, preset.writeUuid, preset.notifyUuid)
    }

    suspend fun updateFlags(
        autoDetectWritable: Boolean,
        appendNewline: Boolean,
        writeWithoutResponse: Boolean,
        filterNamedOnly: Boolean,
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_DETECT] = autoDetectWritable
            prefs[Keys.APPEND_NEWLINE] = appendNewline
            prefs[Keys.WRITE_NO_RESPONSE] = writeWithoutResponse
            prefs[Keys.FILTER_NAMED] = filterNamedOnly
        }
    }

    suspend fun saveConnectedDevice(address: String, name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SAVED_ADDRESS] = address
            prefs[Keys.SAVED_NAME] = name
        }
    }

    suspend fun clearSavedDevice() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.SAVED_ADDRESS)
            prefs.remove(Keys.SAVED_NAME)
        }
    }

    suspend fun updateTrackpadSensitivity(sensitivity: Float) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TRACKPAD_SENSITIVITY] = sensitivity.coerceIn(0.5f, 3.5f)
        }
    }

    suspend fun updateActionPayload(action: RemoteActionId, payload: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.payloadKey(action)] = payload
        }
    }

    suspend fun saveSkipCalibration(screen: String, x: Int, y: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_SKIP_SCREEN] = screen
            when (screen) {
                "rog" -> {
                    prefs[Keys.ROG_X] = x
                    prefs[Keys.ROG_Y] = y
                }
                "custom" -> {
                    prefs[Keys.CUST_X] = x
                    prefs[Keys.CUST_Y] = y
                }
                else -> {
                    prefs[Keys.LG_X] = x
                    prefs[Keys.LG_Y] = y
                }
            }
        }
    }

    suspend fun resetDefaults() {
        context.dataStore.edit { prefs ->
            val savedAddr = prefs[Keys.SAVED_ADDRESS]
            val savedName = prefs[Keys.SAVED_NAME]
            prefs.clear()
            if (!savedAddr.isNullOrBlank()) {
                prefs[Keys.SAVED_ADDRESS] = savedAddr
            }
            if (!savedName.isNullOrBlank()) {
                prefs[Keys.SAVED_NAME] = savedName
            }
        }
    }
}

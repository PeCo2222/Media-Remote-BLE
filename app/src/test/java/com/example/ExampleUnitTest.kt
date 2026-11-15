package com.example

import com.example.model.BleConfig
import com.example.model.BlePreset
import com.example.model.HidCommandChunk
import com.example.model.KeyboardLayoutMapper
import com.example.model.PcKeyboardLayout
import com.example.model.RemoteActionId
import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun defaultConfigUsesNordicUartPreset() {
        val config = BleConfig()
        assertEquals(BlePreset.NORDIC_UART, config.activePreset())
        assertEquals("PLAY", config.payloadFor(RemoteActionId.PLAY_PAUSE))
        assertEquals("SKIP", config.payloadFor(RemoteActionId.SKIP_AD))
    }

    @Test
    fun customPayloadFallbackWorksWhenBlank() {
        val config = BleConfig(
            commandPayloads = mapOf(
                RemoteActionId.VOL_UP to "CUSTOM_VOL+",
                RemoteActionId.VOL_DOWN to "   "
            )
        )
        assertEquals("CUSTOM_VOL+", config.payloadFor(RemoteActionId.VOL_UP))
        assertEquals(RemoteActionId.VOL_DOWN.defaultPayload, config.payloadFor(RemoteActionId.VOL_DOWN))
    }

    @Test
    fun azertyBelgiumEncodesLettersNumbersAndAltGr() {
        val chunks = KeyboardLayoutMapper.encodeText("azerty 123@", PcKeyboardLayout.AZERTY_BE)
        assertEquals(
            listOf(
                HidCommandChunk.TextChunk("qwerty !@#"),
                HidCommandChunk.RawKeyChunk(modifier = 0x40, keycode = 0x1F)
            ),
            chunks
        )
    }

    @Test
    fun azertyFranceEncodesHyphenUnderscoreAndAt() {
        val chunks = KeyboardLayoutMapper.encodeText("a-z_@", PcKeyboardLayout.AZERTY_FR)
        assertEquals(
            listOf(
                HidCommandChunk.TextChunk("q6w8"),
                HidCommandChunk.RawKeyChunk(modifier = 0x40, keycode = 0x27)
            ),
            chunks
        )
    }
}

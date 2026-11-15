package com.example.model

enum class PcKeyboardLayout(
    val displayName: String,
    val shortLabel: String,
) {
    US_QWERTY("US QWERTY", "QWERTY US"),
    AZERTY_BE("Belgian AZERTY (BE)", "AZERTY BE"),
    AZERTY_FR("French AZERTY (FR)", "AZERTY FR"),
}

sealed interface HidCommandChunk {
    data class TextChunk(val text: String) : HidCommandChunk
    data class RawKeyChunk(val modifier: Int, val keycode: Int) : HidCommandChunk
}

/**
 * Translates intended characters into the US-QWERTY ASCII characters (or raw HID keycodes
 * for AltGr / ISO keys) that Arduino-ESP32 USBHIDKeyboard needs to send so a Windows/Linux PC
 * configured with the selected physical layout outputs the expected characters.
 */
object KeyboardLayoutMapper {

    private const val MOD_SHIFT = 0x02
    private const val MOD_ALTGR = 0x40

    private sealed interface MappedChar {
        data class UsAscii(val ch: Char) : MappedChar
        data class Raw(val modifier: Int, val keycode: Int) : MappedChar
    }

    fun encodeText(input: String, layout: PcKeyboardLayout): List<HidCommandChunk> {
        if (input.isEmpty()) return emptyList()
        if (layout == PcKeyboardLayout.US_QWERTY) {
            return listOf(HidCommandChunk.TextChunk(input))
        }

        val chunks = mutableListOf<HidCommandChunk>()
        val textBuffer = StringBuilder()

        fun flushText() {
            if (textBuffer.isNotEmpty()) {
                chunks.add(HidCommandChunk.TextChunk(textBuffer.toString()))
                textBuffer.clear()
            }
        }

        for (ch in input) {
            when (val mapped = mapChar(ch, layout)) {
                is MappedChar.UsAscii -> textBuffer.append(mapped.ch)
                is MappedChar.Raw -> {
                    flushText()
                    chunks.add(HidCommandChunk.RawKeyChunk(mapped.modifier, mapped.keycode))
                }
            }
        }
        flushText()
        return chunks
    }

    private fun mapChar(ch: Char, layout: PcKeyboardLayout): MappedChar {
        // Common letter swaps shared by Belgian & French AZERTY
        val commonAzertyAscii: Char? = when (ch) {
            'a' -> 'q'
            'A' -> 'Q'
            'q' -> 'a'
            'Q' -> 'A'
            'z' -> 'w'
            'Z' -> 'W'
            'w' -> 'z'
            'W' -> 'Z'
            'm' -> ';'
            'M' -> ':'
            // Digits require Shift on AZERTY
            '1' -> '!'
            '2' -> '@'
            '3' -> '#'
            '4' -> '$'
            '5' -> '%'
            '6' -> '^'
            '7' -> '&'
            '8' -> '*'
            '9' -> '('
            '0' -> ')'
            // Shared punctuation
            '&' -> '1'
            '"' -> '3'
            '\'' -> '4'
            '(' -> '5'
            ',' -> 'm'
            '?' -> 'M'
            ';' -> ','
            '.' -> '<'
            ':' -> '.'
            '/' -> '>'
            else -> null
        }
        if (commonAzertyAscii != null) return MappedChar.UsAscii(commonAzertyAscii)

        return when (layout) {
            PcKeyboardLayout.AZERTY_BE -> mapBelgianSpecific(ch)
            PcKeyboardLayout.AZERTY_FR -> mapFrenchSpecific(ch)
            PcKeyboardLayout.US_QWERTY -> MappedChar.UsAscii(ch)
        }
    }

    private fun mapBelgianSpecific(ch: Char): MappedChar = when (ch) {
        '!' -> MappedChar.UsAscii('8')
        ')' -> MappedChar.UsAscii('-')
        '-' -> MappedChar.UsAscii('=')
        '_' -> MappedChar.UsAscii('+')
        '$' -> MappedChar.UsAscii(']')
        '*' -> MappedChar.UsAscii('}')
        '%' -> MappedChar.UsAscii('"')
        '=' -> MappedChar.UsAscii('/')
        '+' -> MappedChar.UsAscii('?')
        // AltGr & ISO-102 keys on Belgian AZERTY
        '|' -> MappedChar.Raw(MOD_ALTGR, 0x1E)
        '@' -> MappedChar.Raw(MOD_ALTGR, 0x1F)
        '#' -> MappedChar.Raw(MOD_ALTGR, 0x20)
        '^' -> MappedChar.Raw(MOD_ALTGR, 0x23)
        '{' -> MappedChar.Raw(MOD_ALTGR, 0x26)
        '}' -> MappedChar.Raw(MOD_ALTGR, 0x27)
        '[' -> MappedChar.Raw(MOD_ALTGR, 0x2F)
        ']' -> MappedChar.Raw(MOD_ALTGR, 0x30)
        '<' -> MappedChar.Raw(0, 0x64)
        '>' -> MappedChar.Raw(MOD_SHIFT, 0x64)
        '\\' -> MappedChar.Raw(MOD_ALTGR, 0x64)
        else -> MappedChar.UsAscii(ch)
    }

    private fun mapFrenchSpecific(ch: Char): MappedChar = when (ch) {
        '-' -> MappedChar.UsAscii('6')
        '_' -> MappedChar.UsAscii('8')
        ')' -> MappedChar.UsAscii('-')
        '=' -> MappedChar.UsAscii('=')
        '+' -> MappedChar.UsAscii('+')
        '!' -> MappedChar.UsAscii('/')
        '$' -> MappedChar.UsAscii(']')
        '*' -> MappedChar.UsAscii('\\')
        '%' -> MappedChar.UsAscii('"')
        // AltGr & ISO-102 keys on French AZERTY
        '~' -> MappedChar.Raw(MOD_ALTGR, 0x1F)
        '#' -> MappedChar.Raw(MOD_ALTGR, 0x20)
        '{' -> MappedChar.Raw(MOD_ALTGR, 0x21)
        '[' -> MappedChar.Raw(MOD_ALTGR, 0x22)
        '|' -> MappedChar.Raw(MOD_ALTGR, 0x23)
        '\\' -> MappedChar.Raw(MOD_ALTGR, 0x25)
        '^' -> MappedChar.Raw(MOD_ALTGR, 0x26)
        '@' -> MappedChar.Raw(MOD_ALTGR, 0x27)
        ']' -> MappedChar.Raw(MOD_ALTGR, 0x2D)
        '}' -> MappedChar.Raw(MOD_ALTGR, 0x2E)
        '<' -> MappedChar.Raw(0, 0x64)
        '>' -> MappedChar.Raw(MOD_SHIFT, 0x64)
        else -> MappedChar.UsAscii(ch)
    }
}

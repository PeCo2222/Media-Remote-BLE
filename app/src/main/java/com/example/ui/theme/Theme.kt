package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val ObsidianColorScheme = darkColorScheme(
    primary = MediaCyan,
    onPrimary = TextPrimary,
    primaryContainer = MediaCyanDark,
    onPrimaryContainer = TextPrimary,
    secondary = SkipAdPurple,
    onSecondary = TextPrimary,
    secondaryContainer = SkipAdPurpleDark,
    onSecondaryContainer = TextPrimary,
    tertiary = VolumeEmerald,
    onTertiary = TextPrimary,
    tertiaryContainer = VolumeEmeraldDark,
    onTertiaryContainer = TextPrimary,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurfaceHigh,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianBorder,
    outlineVariant = ObsidianBorder,
    error = BleErrorCoral,
    onError = TextPrimary,
)

@Composable
fun MediaRemoteTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = ObsidianColorScheme,
        typography = Typography,
        content = content
    )
}

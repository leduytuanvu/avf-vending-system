package com.avf.vending.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Runtime theme engine that can be updated from remote config.
 * Inject as a singleton and call [applyRemoteConfig] when config changes.
 */
@Stable
class VendingThemeEngine {

    var colorScheme: ColorScheme by mutableStateOf(defaultScheme())
        private set

    var fontScale: Float by mutableStateOf(1f)
        private set

    /**
     * Apply remote-config values to update the running theme.
     * All parameters are optional; only provided values are changed.
     */
    fun applyRemoteConfig(
        primaryHex: String? = null,
        secondaryHex: String? = null,
        fontScaleOverride: Float? = null,
    ) {
        val primary = primaryHex?.parseHexColor() ?: colorScheme.primary
        val secondary = secondaryHex?.parseHexColor() ?: colorScheme.secondary
        colorScheme = colorScheme.copy(primary = primary, secondary = secondary)
        fontScaleOverride?.let { fontScale = it.coerceIn(0.8f, 1.4f) }
    }

    fun reset() {
        colorScheme = defaultScheme()
        fontScale = 1f
    }

    private fun defaultScheme(): ColorScheme = darkColorScheme(
        primary = VendingBlue,
        onPrimary = OnSurfaceDark,
        primaryContainer = VendingBlueDark,
        secondary = VendingGreen,
        error = VendingRed,
        background = BackgroundDark,
        surface = SurfaceDark,
        surfaceVariant = SurfaceVariantDark,
        onBackground = OnSurfaceDark,
        onSurface = OnSurfaceDark,
        onSurfaceVariant = OnSurfaceVariantDark,
    )
}

private fun String.parseHexColor(): androidx.compose.ui.graphics.Color {
    val hex = removePrefix("#").removePrefix("0x")
    val value = hex.toLongOrNull(16) ?: return VendingBlue
    return if (hex.length == 6) {
        androidx.compose.ui.graphics.Color(0xFF_000000L or value)
    } else {
        androidx.compose.ui.graphics.Color(value)
    }
}

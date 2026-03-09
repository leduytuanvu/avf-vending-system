package com.avf.vending.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VendingColorScheme = darkColorScheme(
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

@Composable
fun VendingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VendingColorScheme,
        typography = VendingTypography,
        content = content,
    )
}

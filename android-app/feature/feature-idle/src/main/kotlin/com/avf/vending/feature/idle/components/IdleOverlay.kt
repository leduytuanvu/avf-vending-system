package com.avf.vending.feature.idle.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * Full-screen idle overlay. Tapping anywhere wakes up the storefront.
 * The invisible 5-tap admin corner is handled by [onAdminTap].
 */
@Composable
fun IdleOverlay(
    onWake: () -> Unit,
    onAdminTap: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) { detectTapGestures { onWake() } },
    ) {
        content()

        // Invisible admin tap target — top-right corner, 80×80 dp
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(80.dp)
                .pointerInput(Unit) {
                    detectTapGestures { onAdminTap() }
                },
        )
    }
}

package com.avf.vending.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Animated circular countdown ring.
 * [remainingMs] and [totalMs] drive the sweep angle.
 */
@Composable
fun CountdownTimer(
    remainingMs: Long,
    totalMs: Long,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 6.dp,
) {
    val fraction = if (totalMs > 0) remainingMs.toFloat() / totalMs.toFloat() else 0f
    val sweep by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f) * 360f,
        animationSpec = tween(durationMillis = 300),
        label = "countdown_sweep",
    )

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val arcColor = if (fraction > 0.3f) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.error

    val remainingSeconds = (remainingMs / 1000).toInt()

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            drawArc(
                color = arcColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
        }
        Text(
            text = "$remainingSeconds",
            style = MaterialTheme.typography.labelLarge,
            color = arcColor,
        )
    }
}

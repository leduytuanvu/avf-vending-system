package com.avf.vending.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.avf.vending.ui.theme.Dimen

@Composable
fun SkeletonBox(
    width: Dp = Dp.Unspecified,
    height: Dp = 16.dp,
    cornerRadius: Dp = 4.dp,
    modifier: Modifier = Modifier,
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surfaceVariant,
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "shimmer_translate",
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim, 0f),
    )
    val sizeModifier = if (width != Dp.Unspecified) modifier.width(width).height(height)
    else modifier.fillMaxWidth().height(height)
    Box(
        modifier = sizeModifier
            .background(brush, RoundedCornerShape(cornerRadius))
    )
}

@Composable
fun ProductCardSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(Dimen.SpaceMd)) {
        SkeletonBox(height = 120.dp, cornerRadius = Dimen.CardCorner)
        Spacer(Modifier.height(Dimen.SpaceSm))
        SkeletonBox(height = 16.dp, width = 120.dp)
        Spacer(Modifier.height(Dimen.SpaceXs))
        SkeletonBox(height = 12.dp, width = 80.dp)
    }
}

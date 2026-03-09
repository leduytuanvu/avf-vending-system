package com.avf.vending.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.avf.vending.ui.theme.VendingAmber
import com.avf.vending.ui.theme.VendingGreen
import com.avf.vending.ui.theme.VendingRed

enum class BadgeStatus { OK, WARNING, ERROR, INFO }

@Composable
fun StatusBadge(
    label: String,
    status: BadgeStatus,
    modifier: Modifier = Modifier,
) {
    val color = when (status) {
        BadgeStatus.OK -> VendingGreen
        BadgeStatus.WARNING -> VendingAmber
        BadgeStatus.ERROR -> VendingRed
        BadgeStatus.INFO -> MaterialTheme.colorScheme.primary
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

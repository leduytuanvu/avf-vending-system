package com.avf.vending.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.avf.vending.hardware.api.strategy.StrategyType
import com.avf.vending.ui.theme.Dimen
import com.avf.vending.ui.theme.VendingAmber
import com.avf.vending.ui.theme.VendingGreen
import com.avf.vending.ui.theme.VendingRed

data class StrategyIndicatorState(
    val activeStrategyId: String,
    val activeType: StrategyType,
    val isConnected: Boolean,
)

@Composable
fun StrategyStatusIndicator(
    state: StrategyIndicatorState,
    modifier: Modifier = Modifier,
) {
    val color = when {
        !state.isConnected -> VendingRed
        else -> VendingGreen
    }
    val icon = when (state.activeType) {
        StrategyType.SERIAL_RS232, StrategyType.RS485 -> Icons.Default.Cable
        StrategyType.TCP_IP -> Icons.Default.Cable
        StrategyType.USB_SERIAL -> Icons.Default.Usb
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(Dimen.IconSm),
        )
        Spacer(Modifier.width(Dimen.SpaceXs))
        Text(
            text = state.activeStrategyId,
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}

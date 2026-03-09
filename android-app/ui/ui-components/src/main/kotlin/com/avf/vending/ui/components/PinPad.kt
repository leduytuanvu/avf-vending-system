package com.avf.vending.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.avf.vending.ui.theme.Dimen

private val keys = listOf(
    listOf("1", "2", "3"),
    listOf("4", "5", "6"),
    listOf("7", "8", "9"),
    listOf("", "0", "⌫"),
)

@Composable
fun PinPad(
    enteredPin: String,
    maxLength: Int = 6,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // PIN dots display
        Row(
            horizontalArrangement = Arrangement.spacedBy(Dimen.SpaceMd),
            modifier = Modifier.padding(bottom = Dimen.SpaceLg),
        ) {
            repeat(maxLength) { index ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (index < enteredPin.length)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(Dimen.SpaceMd),
                ) {}
            }
        }

        // Keys
        keys.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(Dimen.PinPadKeySpacing)) {
                row.forEach { key ->
                    when {
                        key.isEmpty() -> Spacer(modifier = Modifier.size(Dimen.PinPadKeySize))
                        key == "⌫" -> FilledTonalIconButton(
                            onClick = onBackspace,
                            modifier = Modifier.size(Dimen.PinPadKeySize),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Backspace,
                                contentDescription = "Backspace",
                            )
                        }
                        else -> FilledTonalButton(
                            onClick = {
                                if (enteredPin.length < maxLength) onKeyPress(key)
                            },
                            modifier = Modifier.size(Dimen.PinPadKeySize),
                            shape = RoundedCornerShape(Dimen.ButtonCorner),
                            contentPadding = PaddingValues(),
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(Dimen.PinPadKeySpacing))
        }
    }
}

package com.avf.vending.feature.admin.pin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.avf.vending.ui.components.PinPad
import com.avf.vending.ui.theme.VendingRed
import java.security.MessageDigest

@Composable
fun AdminPinScreen(
    storedPinHash: String,
    onUnlocked: () -> Unit,
    onCancel: () -> Unit,
) {
    var enteredPin by remember { mutableStateOf("") }
    var attempts by remember { mutableIntStateOf(0) }
    var showError by remember { mutableStateOf(false) }

    val maxAttempts = 5

    fun verify() {
        val hash = sha256(enteredPin)
        if (hash == storedPinHash) {
            onUnlocked()
        } else {
            attempts++
            showError = true
            enteredPin = ""
            if (attempts >= maxAttempts) onCancel()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        modifier = Modifier.fillMaxSize().padding(32.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp),
        )
        Text(
            text = "Admin Access",
            style = MaterialTheme.typography.headlineMedium,
        )
        if (showError) {
            Text(
                text = "Incorrect PIN. ${maxAttempts - attempts} attempts remaining.",
                style = MaterialTheme.typography.bodySmall,
                color = VendingRed,
            )
        }
        PinPad(
            enteredPin = enteredPin,
            maxLength = 6,
            onKeyPress = {
                enteredPin += it
                showError = false
                if (enteredPin.length == 6) verify()
            },
            onBackspace = {
                if (enteredPin.isNotEmpty()) enteredPin = enteredPin.dropLast(1)
            },
        )
        TextButton(onClick = onCancel) {
            Text("Cancel")
        }
    }
}

fun sha256(input: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

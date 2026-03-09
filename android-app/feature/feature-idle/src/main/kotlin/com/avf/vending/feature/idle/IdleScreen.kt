package com.avf.vending.feature.idle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun IdleScreen(
    onWakeUp: () -> Unit,
    onAdminAccess: () -> Unit,
    viewModel: IdleViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                IdleEffect.NavigateToStorefront -> onWakeUp()
                IdleEffect.NavigateToAdmin -> onAdminAccess()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { viewModel.handleIntent(IdleIntent.Tap) },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "AVF Vending",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Chạm để bắt đầu",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            state.machineStatus?.let { status ->
                if (!status.isOnline || status.errors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bảo trì hệ thống",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }

        // Hidden admin tap zone — top-right corner, 5 taps
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(80.dp)
                .clickable { viewModel.handleIntent(IdleIntent.AdminTap) },
        )
    }
}

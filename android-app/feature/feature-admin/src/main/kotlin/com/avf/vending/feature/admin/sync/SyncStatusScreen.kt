package com.avf.vending.feature.admin.sync

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.avf.vending.ui.components.BadgeStatus
import com.avf.vending.ui.components.StatusBadge
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncStatusScreen(
    viewModel: SyncStatusViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync Status") },
                actions = {
                    IconButton(onClick = { viewModel.triggerSync() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Sync Now")
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        ) {
            InfoRow(
                label = "Last sync",
                value = state.lastSyncAt?.let { formatter.format(Date(it)) } ?: "Never",
            )
            InfoRow(
                label = "Pending items",
                value = state.pendingCount.toString(),
            )
            InfoRow(
                label = "Status",
                value = state.statusLabel,
                badge = {
                    StatusBadge(
                        label = state.statusLabel,
                        status = state.badgeStatus,
                    )
                },
            )
            if (state.isSyncing) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Syncing…", style = MaterialTheme.typography.bodyMedium)
                }
            }
            state.error?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    badge: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        if (badge != null) {
            badge()
        } else {
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

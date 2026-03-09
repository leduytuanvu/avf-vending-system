package com.avf.vending.feature.admin.strategy

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrategyManagerScreen(
    viewModel: StrategyManagerViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Strategy Manager") },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
        modifier = modifier,
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            items(state.strategies) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    ) {
                        Column {
                            Text(entry.id, style = MaterialTheme.typography.titleSmall)
                            Text(entry.type, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusBadge(
                            label = if (entry.isActive) "Active" else "Standby",
                            status = if (entry.isActive) BadgeStatus.OK else BadgeStatus.INFO,
                        )
                    }
                    if (!entry.isActive) {
                        Row(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
                            OutlinedButton(
                                onClick = { viewModel.forceStrategy(entry.id) },
                                modifier = Modifier.fillMaxWidth(),
                            ) { Text("Force to this strategy") }
                        }
                    }
                }
            }
            if (state.forcedStrategyId != null) {
                item {
                    OutlinedButton(
                        onClick = { viewModel.clearForce() },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Clear forced strategy") }
                }
            }
        }
    }
}

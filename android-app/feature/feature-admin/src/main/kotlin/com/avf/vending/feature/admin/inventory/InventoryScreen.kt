package com.avf.vending.feature.admin.inventory

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.avf.vending.domain.model.Slot
import com.avf.vending.ui.components.BadgeStatus
import com.avf.vending.ui.components.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Inventory") }) },
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
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            items(state.slots, key = { it.id }) { slot ->
                InventoryRow(slot = slot)
            }
        }
    }
}

@Composable
private fun InventoryRow(slot: Slot, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Slot ${slot.address}", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "ID: ${slot.id}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            StatusBadge(
                label = "${slot.stock}/${slot.capacity}",
                status = when {
                    slot.stock == 0 -> BadgeStatus.ERROR
                    slot.stock <= 3 -> BadgeStatus.WARNING
                    else -> BadgeStatus.OK
                },
            )
        }
    }
}

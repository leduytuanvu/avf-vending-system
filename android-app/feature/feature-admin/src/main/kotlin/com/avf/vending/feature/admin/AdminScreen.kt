package com.avf.vending.feature.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.avf.vending.domain.model.ProductSlot
import com.avf.vending.domain.model.Transaction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBack: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AdminEffect.NavigateBack -> onBack()
            }
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Giao dịch", "Kho hàng", "Máy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleIntent(AdminIntent.Logout) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                    )
                }
            }

            when (selectedTab) {
                0 -> TransactionsTab(transactions = state.transactions, isLoading = state.isLoading)
                1 -> StockTab(slots = state.slots)
                2 -> SummaryTab(
                    refundRequiredCount = state.refundRequiredCount,
                    totalSlots = state.slots.size,
                    error = state.error,
                )
            }
        }
    }
}

@Composable
private fun TransactionsTab(transactions: List<Transaction>, isLoading: Boolean) {
    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(transactions, key = { it.id }) { tx ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "ID: ${tx.id.take(8)}…", style = MaterialTheme.typography.labelLarge)
                    Text(text = "Slot: ${tx.slotId}  •  ${tx.paymentMethod}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "${tx.amount.toVnd()}  •  ${tx.status}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun StockTab(slots: List<ProductSlot>) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(slots, key = { it.slot.id }) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(text = item.slot.address.toString(), style = MaterialTheme.typography.labelLarge)
                        Text(text = item.product.name, style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = "${item.slot.stock}/${item.slot.capacity}",
                        style = MaterialTheme.typography.titleLarge,
                        color = if (item.slot.stock == 0) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryTab(
    refundRequiredCount: Int,
    totalSlots: Int,
    error: String?,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatusRow(label = "Refund required", value = refundRequiredCount.toString())
        StatusRow(label = "Available slots", value = totalSlots.toString())
        if (error != null) {
            Text(text = "Last error:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
            Text(text = error, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

private fun Long.toVnd(): String {
    val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
    return "${formatter.format(this)} đ"
}

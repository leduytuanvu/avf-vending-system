package com.avf.vending.feature.storefront

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.avf.vending.feature.storefront.components.ProductGrid
import com.avf.vending.ui.components.ErrorBanner
import com.avf.vending.ui.components.LoadingOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorefrontScreen(
    onProductSelected: (slotId: String, productId: String, amount: Long) -> Unit,
    onAdminAccess: () -> Unit,
    viewModel: StorefrontViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is StorefrontEffect.NavigateToPayment ->
                    onProductSelected(effect.slotId, effect.productId, effect.amount)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chọn sản phẩm") },
                actions = {
                    IconButton(onClick = onAdminAccess) {
                        Icon(Icons.Default.Settings, contentDescription = "Admin")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                state.error?.let { ErrorBanner(message = it) }

                ProductGrid(
                    items = state.items,
                    isLoading = isLoading,
                    onProductSelected = { item ->
                        viewModel.handleIntent(
                            StorefrontIntent.SelectProduct(
                                slotId = item.slotId,
                                productId = item.productId,
                                amount = item.price,
                            )
                        )
                    },
                    modifier = Modifier.fillMaxSize(),
                )
            }

            if (isLoading) LoadingOverlay()
        }
    }
}

package com.avf.vending.feature.payment

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.avf.vending.domain.model.PaymentMethod
import com.avf.vending.ui.components.LoadingOverlay
import com.avf.vending.ui.components.PriceText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    slotId: String,
    productId: String,
    amount: Long,
    onPaymentComplete: (transactionId: String, slotAddress: String) -> Unit,
    onCancel: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(slotId, productId, amount) {
        viewModel.handleIntent(PaymentIntent.Init(slotId, productId, amount))
    }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PaymentEffect.NavigateToDispensing ->
                    onPaymentComplete(effect.transactionId, effect.slotAddress)
                PaymentEffect.NavigateBack -> onCancel()
                is PaymentEffect.ShowError -> { /* handled inline via state */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.handleIntent(PaymentIntent.Cancel) }) {
                        Icon(Icons.Default.Close, contentDescription = "Huỷ")
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                state.product?.let { product ->
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                PriceText(price = state.requiredAmount)

                // Method selector
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PaymentMethodChip(
                        label = "Tiền mặt",
                        selected = state.selectedMethod == PaymentMethod.CASH,
                        onClick = { viewModel.handleIntent(PaymentIntent.SelectMethod(PaymentMethod.CASH)) },
                    )
                    PaymentMethodChip(
                        label = "Ví QR",
                        selected = state.selectedMethod == PaymentMethod.QR_WALLET,
                        onClick = { viewModel.handleIntent(PaymentIntent.SelectMethod(PaymentMethod.QR_WALLET)) },
                    )
                }

                when (state.selectedMethod) {
                    PaymentMethod.CASH -> CashPaymentContent(
                        inserted = state.insertedAmount,
                        required = state.requiredAmount,
                    )
                    PaymentMethod.QR_WALLET -> QrPaymentContent(qrData = state.qrData)
                    else -> Unit
                }

                state.error?.let { error ->
                    Text(text = error, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
                }
            }

            if (state.isProcessing) LoadingOverlay()
        }
    }
}

@Composable
private fun PaymentMethodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
    )
}

@Composable
private fun CashPaymentContent(inserted: Long, required: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Vui lòng bỏ tiền vào máy", style = MaterialTheme.typography.bodyLarge)
        LinearProgressIndicator(
            progress = { (inserted.toFloat() / required.coerceAtLeast(1)).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp),
        )
        Text(
            text = "${inserted.toVnd()} / ${required.toVnd()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun QrPaymentContent(qrData: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (qrData != null) {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .border(2.dp, Color.White, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "QR: $qrData", fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Quét mã QR để thanh toán", style = MaterialTheme.typography.bodyLarge)
        } else {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Đang tạo mã QR...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun Long.toVnd(): String {
    val formatter = java.text.NumberFormat.getNumberInstance(java.util.Locale("vi", "VN"))
    return "${formatter.format(this)} đ"
}

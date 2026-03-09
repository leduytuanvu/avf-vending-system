package com.avf.vending.feature.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avf.vending.common.di.ApplicationScope
import com.avf.vending.domain.model.*
import com.avf.vending.domain.service.CatalogService
import com.avf.vending.domain.transaction.TransactionCoordinator
import com.avf.vending.payment.api.PaymentEvent
import com.avf.vending.payment.api.PaymentType
import com.avf.vending.payment.orchestrator.PaymentOrchestrator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val catalogService: CatalogService,
    private val transactionCoordinator: TransactionCoordinator,
    private val paymentOrchestrator: PaymentOrchestrator,
    @ApplicationScope private val appScope: CoroutineScope,
) : ViewModel() {

    private val _state = MutableStateFlow(PaymentState())
    val state: StateFlow<PaymentState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<PaymentEffect>()
    val effects: SharedFlow<PaymentEffect> = _effects.asSharedFlow()

    fun handleIntent(intent: PaymentIntent) {
        when (intent) {
            is PaymentIntent.Init -> init(intent.slotId, intent.productId, intent.amount)
            is PaymentIntent.SelectMethod -> selectMethod(intent.method)
            PaymentIntent.Cancel -> cancel()
            PaymentIntent.Confirm -> confirm()
        }
    }

    private fun init(slotId: String, productId: String, amount: Long) {
        viewModelScope.launch {
            val product = catalogService.getProductDetail(productId)
            _state.update { it.copy(product = product, slotId = slotId, requiredAmount = amount) }
            startPaymentSession(amount, PaymentMethod.CASH)
        }
    }

    private fun selectMethod(method: PaymentMethod) {
        _state.update { it.copy(selectedMethod = method, insertedAmount = 0L, qrData = null) }
        viewModelScope.launch {
            paymentOrchestrator.cancel()
            startPaymentSession(_state.value.requiredAmount, method)
        }
    }

    private fun startPaymentSession(amount: Long, method: PaymentMethod) {
        val types = when (method) {
            PaymentMethod.CASH -> listOf(PaymentType.CASH)
            PaymentMethod.QR_WALLET -> listOf(PaymentType.QR_WALLET)
            else -> listOf(PaymentType.CASH)
        }
        viewModelScope.launch {
            paymentOrchestrator.startSession(amount, types)
                .catch { e -> _effects.emit(PaymentEffect.ShowError(e.message ?: "Payment error")) }
                .collect { event -> handlePaymentEvent(event) }
        }
    }

    private fun handlePaymentEvent(event: PaymentEvent) {
        when (event) {
            is PaymentEvent.CashInserted ->
                _state.update { it.copy(insertedAmount = event.total) }
            is PaymentEvent.CashSufficient ->
                _state.update { it.copy(insertedAmount = event.total, change = event.change) }
                    .also { viewModelScope.launch { completeTransaction() } }
            is PaymentEvent.CashRejected ->
                viewModelScope.launch { _effects.emit(PaymentEffect.ShowError("Bill rejected: ${event.reason}")) }
            is PaymentEvent.WalletScanReady ->
                _state.update { it.copy(qrData = event.qrData) }
            is PaymentEvent.WalletConfirmed ->
                viewModelScope.launch { completeTransaction() }
            is PaymentEvent.WalletFailed ->
                viewModelScope.launch { _effects.emit(PaymentEffect.ShowError("Wallet payment failed: ${event.reason}")) }
            PaymentEvent.WalletTimeout ->
                viewModelScope.launch { _effects.emit(PaymentEffect.ShowError("Payment timed out")) }
            else -> Unit
        }
    }

    private suspend fun completeTransaction() {
        if (_state.value.isProcessing) return
        _state.update { it.copy(isProcessing = true) }
        try {
            val current = _state.value
            val slot = catalogService.getSlotById(current.slotId)
                ?: throw IllegalStateException("Slot not found: ${current.slotId}")

            val authorizedTransaction = transactionCoordinator.authorizePayment(
                slotId = current.slotId,
                productId = current.product?.id.orEmpty(),
                amount = current.requiredAmount,
                paymentMethod = current.selectedMethod,
                change = current.change,
            )

            _effects.emit(
                PaymentEffect.NavigateToDispensing(
                    authorizedTransaction.transactionId,
                    slot.address.toString(),
                )
            )
        } catch (e: Exception) {
            _effects.emit(PaymentEffect.ShowError(e.message ?: "Transaction failed"))
        } finally {
            _state.update { it.copy(isProcessing = false) }
        }
    }

    private fun cancel() {
        viewModelScope.launch {
            paymentOrchestrator.cancel()
            _effects.emit(PaymentEffect.NavigateBack)
        }
    }

    private fun confirm() {
        viewModelScope.launch { completeTransaction() }
    }

    override fun onCleared() {
        super.onCleared()
        appScope.launch { paymentOrchestrator.cancel() }
    }
}

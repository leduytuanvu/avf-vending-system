package com.avf.vending.feature.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avf.vending.domain.service.CatalogService
import com.avf.vending.domain.service.TransactionService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val catalogService: CatalogService,
    private val transactionService: TransactionService,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<AdminEffect>()
    val effects: SharedFlow<AdminEffect> = _effects.asSharedFlow()

    init {
        handleIntent(AdminIntent.Load)
    }

    fun handleIntent(intent: AdminIntent) {
        when (intent) {
            AdminIntent.Load -> load()
            AdminIntent.Logout -> viewModelScope.launch { _effects.emit(AdminEffect.NavigateBack) }
        }
    }

    private fun load() {
        // Transaction history
        transactionService.observeHistory()
            .catch { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
            .onEach { txns -> _state.update { it.copy(transactions = txns, isLoading = false) } }
            .launchIn(viewModelScope)

        // Refund alert badge — drives the red counter in the admin header
        transactionService.observeRefundRequired()
            .onEach { pending -> _state.update { it.copy(refundRequiredCount = pending.size) } }
            .catch { }
            .launchIn(viewModelScope)

        // Slot catalogue — reuses the same join logic as StorefrontViewModel
        catalogService.observeAvailableProducts()
            .catch { }
            .onEach { items -> _state.update { it.copy(slots = items) } }
            .launchIn(viewModelScope)
    }
}

package com.avf.vending.feature.storefront

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avf.vending.domain.service.CatalogService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorefrontViewModel @Inject constructor(
    private val catalogService: CatalogService,
) : ViewModel() {

    private val _state = MutableStateFlow(StorefrontState())
    val state: StateFlow<StorefrontState> = _state.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _effects = MutableSharedFlow<StorefrontEffect>()
    val effects: SharedFlow<StorefrontEffect> = _effects.asSharedFlow()
    private var loadJob: Job? = null

    init {
        handleIntent(StorefrontIntent.Load)
    }

    fun handleIntent(intent: StorefrontIntent) {
        when (intent) {
            is StorefrontIntent.Load -> loadProducts()
            is StorefrontIntent.SelectProduct -> viewModelScope.launch {
                _effects.emit(
                    StorefrontEffect.NavigateToPayment(intent.slotId, intent.productId, intent.amount)
                )
            }
        }
    }

    private fun loadProducts() {
        loadJob?.cancel()
        loadJob = catalogService.observeAvailableProducts()
            .map { items ->
                items.map { item ->
                    StorefrontItemUiModel(
                        slotId = item.slot.id,
                        productId = item.product.id,
                        name = item.product.name,
                        price = item.product.price,
                        slotAddress = item.slot.address.toString(),
                        stock = item.slot.stock,
                    )
                }
            }
            .distinctUntilChanged()
            .onStart {
                _isLoading.value = true
                _state.update { it.copy(error = null) }
            }
            .catch { e ->
                _isLoading.value = false
                _state.update { it.copy(error = e.message) }
            }
            .onEach { items ->
                _isLoading.value = false
                _state.update { it.copy(items = items, error = null) }
            }
            .launchIn(viewModelScope)
    }
}

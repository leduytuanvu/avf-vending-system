package com.avf.vending.feature.admin.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avf.vending.domain.model.Slot
import com.avf.vending.domain.service.CatalogService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class InventoryState(
    val isLoading: Boolean = true,
    val slots: List<Slot> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val catalogService: CatalogService,
) : ViewModel() {

    private val _state = MutableStateFlow(InventoryState())
    val state: StateFlow<InventoryState> = _state.asStateFlow()

    init {
        catalogService.observeSlots()
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { slots -> _state.update { it.copy(isLoading = false, slots = slots, error = null) } }
            .catch { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            .launchIn(viewModelScope)
    }
}

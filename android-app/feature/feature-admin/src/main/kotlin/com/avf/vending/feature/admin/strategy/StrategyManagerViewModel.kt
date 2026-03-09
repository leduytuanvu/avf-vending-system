package com.avf.vending.feature.admin.strategy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avf.vending.hardware.transport.StrategyManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StrategyEntry(
    val id: String,
    val type: String,
    val isActive: Boolean,
)

data class StrategyManagerState(
    val isLoading: Boolean = true,
    val strategies: List<StrategyEntry> = emptyList(),
    val forcedStrategyId: String? = null,
    val error: String? = null,
)

@HiltViewModel
class StrategyManagerViewModel @Inject constructor(
    private val strategyManager: StrategyManager,
) : ViewModel() {

    private val _state = MutableStateFlow(StrategyManagerState())
    val state: StateFlow<StrategyManagerState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val active = strategyManager.getActive()
                _state.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun forceStrategy(id: String) {
        strategyManager.forceStrategy(id)
        _state.update { it.copy(forcedStrategyId = id) }
    }

    fun clearForce() {
        strategyManager.clearForce()
        _state.update { it.copy(forcedStrategyId = null) }
    }
}

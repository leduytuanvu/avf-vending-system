package com.avf.vending.feature.idle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avf.vending.domain.repository.HardwareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ADMIN_TAP_THRESHOLD = 5

@HiltViewModel
class IdleViewModel @Inject constructor(
    private val hardwareRepository: HardwareRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(IdleState())
    val state: StateFlow<IdleState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<IdleEffect>()
    val effects: SharedFlow<IdleEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            hardwareRepository.observeMachineStatus()
                .catch { /* ignore, show last known */ }
                .collect { status -> _state.update { it.copy(machineStatus = status) } }
        }
    }

    fun handleIntent(intent: IdleIntent) {
        when (intent) {
            IdleIntent.Tap -> viewModelScope.launch {
                _state.update { it.copy(adminTapCount = 0) }
                _effects.emit(IdleEffect.NavigateToStorefront)
            }
            IdleIntent.AdminTap -> {
                val newCount = _state.value.adminTapCount + 1
                _state.update { it.copy(adminTapCount = newCount) }
                if (newCount >= ADMIN_TAP_THRESHOLD) {
                    _state.update { it.copy(adminTapCount = 0) }
                    viewModelScope.launch { _effects.emit(IdleEffect.NavigateToAdmin) }
                }
            }
        }
    }
}

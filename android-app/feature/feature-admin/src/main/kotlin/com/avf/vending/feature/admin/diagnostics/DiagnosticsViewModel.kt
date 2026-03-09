package com.avf.vending.feature.admin.diagnostics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avf.vending.hardware.api.driver.VendingMachineDriver
import com.avf.vending.hardware.api.model.DispenseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DiagnosticsState(
    val rows: Int = 6,
    val cols: Int = 10,
    // key = (rowIndex 1-based, col 1-based), value = null=pending, true=ok, false=fail
    val testResults: Map<Pair<Int, Int>, Boolean?> = emptyMap(),
    val hardwareStatus: Map<String, Boolean> = emptyMap(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class DiagnosticsViewModel @Inject constructor(
    private val driver: VendingMachineDriver,
) : ViewModel() {

    private val _state = MutableStateFlow(DiagnosticsState())
    val state: StateFlow<DiagnosticsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val status = driver.getStatus()
            _state.update { it.copy(
                hardwareStatus = mapOf(
                    "Door" to !status.doorOpen,
                    "Connection" to (status.connectionState.name == "CONNECTED"),
                    "Errors" to status.errorCodes.isEmpty(),
                )
            ) }
        }
    }

    fun testMotor(row: Int, col: Int) {
        viewModelScope.launch {
            _state.update { s -> s.copy(testResults = s.testResults + ((row to col) to null)) }
            val rowChar = ('A' + row - 1)
            val success = try {
                driver.dispense(rowChar, col) is DispenseResult.Success
            } catch (e: Exception) {
                false
            }
            _state.update { s -> s.copy(testResults = s.testResults + ((row to col) to success)) }
        }
    }
}

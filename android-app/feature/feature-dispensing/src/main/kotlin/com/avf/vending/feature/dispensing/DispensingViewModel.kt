package com.avf.vending.feature.dispensing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avf.vending.domain.model.DispenseJournalEntry
import com.avf.vending.domain.model.DispenseOutcome
import com.avf.vending.domain.repository.DispenseJournalRepository
import com.avf.vending.domain.transaction.TransactionCoordinator
import com.avf.vending.hardware.api.driver.VendingMachineDriver
import com.avf.vending.hardware.api.model.DispenseResult
import com.avf.vending.hardware.api.validation.SensorTimingGuard
import com.avf.vending.hardware.api.validation.SensorValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class DispensingViewModel @Inject constructor(
    private val vendingMachineDriver: VendingMachineDriver,
    private val transactionCoordinator: TransactionCoordinator,
    private val dispenseJournalRepository: DispenseJournalRepository,
    private val sensorTimingGuard: SensorTimingGuard,
) : ViewModel() {

    private val _state = MutableStateFlow(DispensingState())
    val state: StateFlow<DispensingState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<DispensingEffect>()
    val effects: SharedFlow<DispensingEffect> = _effects.asSharedFlow()

    fun handleIntent(intent: DispensingIntent) {
        when (intent) {
            is DispensingIntent.Start -> startDispensing(intent.transactionId, intent.slotAddress)
            DispensingIntent.Confirm -> viewModelScope.launch {
                _effects.emit(DispensingEffect.NavigateToIdle)
            }
        }
    }

    private fun startDispensing(transactionId: String, slotAddress: String) {
        _state.update { it.copy(transactionId = transactionId, slotAddress = slotAddress, isDispensing = true) }
        viewModelScope.launch {
            try {
                val row = slotAddress.firstOrNull() ?: 'A'
                val col = slotAddress.drop(1).toIntOrNull() ?: 1

                // Write-ahead log BEFORE the hardware command so crash recovery can
                // decide completed vs refund-required without risking double-dispense.
                val dispenseId = UUID.randomUUID().toString()
                dispenseJournalRepository.create(
                    DispenseJournalEntry(
                        dispenseId = dispenseId,
                        transactionId = transactionId,
                        slotId = slotAddress,
                        sensorTriggered = false,
                        completed = false,
                        createdAt = System.currentTimeMillis(),
                    )
                )
                transactionCoordinator.markDispensing(transactionId)

                val result = vendingMachineDriver.dispense(row, col)

                val validatedResult: DispenseResult = when (result) {
                    is DispenseResult.Success -> {
                        val signal = result.sensorSignal
                        if (signal != null) {
                            when (val validation = sensorTimingGuard.validate(signal)) {
                                SensorValidationResult.Valid -> {
                                    dispenseJournalRepository.markSensorTriggered(dispenseId)
                                    result
                                }
                                is SensorValidationResult.TooEarly ->
                                    DispenseResult.Failed(
                                        reason = "sensor_drift_early",
                                        code = -1,
                                    )
                                is SensorValidationResult.TooLate ->
                                    DispenseResult.Failed(
                                        reason = "sensor_drift_late",
                                        code = -2,
                                    )
                                SensorValidationResult.NoSignal ->
                                    DispenseResult.Failed(
                                        reason = "sensor_no_signal",
                                        code = -3,
                                    )
                            }
                        } else {
                            result
                        }
                    }
                    else -> result
                }

                val outcome: DispenseOutcome = when (validatedResult) {
                    is DispenseResult.Success -> DispenseOutcome.Success(slotId = validatedResult.slotId)
                    is DispenseResult.Failed -> DispenseOutcome.Failed(
                        reason = validatedResult.reason,
                        code = validatedResult.code,
                    )
                    is DispenseResult.Timeout -> DispenseOutcome.Timeout(waitedMs = validatedResult.waitedMs)
                }
                transactionCoordinator.recordDispenseOutcome(transactionId, outcome)

                if (outcome is DispenseOutcome.Success) {
                    dispenseJournalRepository.markCompleted(dispenseId)
                }

                _state.update {
                    it.copy(
                        isDispensing = false,
                        result = validatedResult,
                        error = if (validatedResult is DispenseResult.Success) null
                        else resultMessage(validatedResult),
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isDispensing = false,
                        error = e.message ?: "Dispense failed",
                    )
                }
            }
        }
    }

    private fun resultMessage(result: DispenseResult): String = when (result) {
        is DispenseResult.Failed -> "Dispense failed: ${result.reason} (code ${result.code})"
        is DispenseResult.Timeout -> "Dispense timed out after ${result.waitedMs}ms"
        else -> ""
    }
}

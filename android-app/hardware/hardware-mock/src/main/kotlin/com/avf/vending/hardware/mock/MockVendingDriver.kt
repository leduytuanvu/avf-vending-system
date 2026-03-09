package com.avf.vending.hardware.mock

import com.avf.vending.hardware.api.driver.VendingMachineDriver
import com.avf.vending.hardware.api.model.ConnectionState
import com.avf.vending.hardware.api.model.DispenseResult
import com.avf.vending.hardware.api.model.MachineStatus
import com.avf.vending.hardware.api.model.SlotStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlin.random.Random

class MockVendingDriver @Inject constructor() : VendingMachineDriver {
    var scenario: MockScenarios = MockScenarios.DEFAULT
    var responseDelayMs: Long = 200

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: Flow<ConnectionState> = _connectionState

    override suspend fun connect(): Boolean {
        delay(responseDelayMs)
        _connectionState.value = ConnectionState.CONNECTED
        return true
    }

    override suspend fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override suspend fun dispense(row: Char, col: Int): DispenseResult {
        delay(responseDelayMs)
        return when (scenario) {
            MockScenarios.ALWAYS_FAIL -> DispenseResult.Failed("Mock always fail", -1)
            MockScenarios.RANDOM_EMPTY -> if (Random.nextBoolean()) DispenseResult.Failed("Slot empty", 0x02)
            else DispenseResult.Success("$row$col")
            MockScenarios.SLOW_RESPONSE -> { delay(3000); DispenseResult.Success("$row$col") }
            MockScenarios.DEFAULT -> DispenseResult.Success("$row$col")
        }
    }

    override suspend fun getStatus(): MachineStatus = MachineStatus()
    override suspend fun getInventory(): List<SlotStatus> =
        listOf(SlotStatus("A1", stock = 10, motorOk = true), SlotStatus("A2", stock = 5, motorOk = true))
}

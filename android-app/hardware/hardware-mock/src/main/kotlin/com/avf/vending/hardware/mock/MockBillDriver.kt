package com.avf.vending.hardware.mock

import com.avf.vending.hardware.api.driver.BillValidatorDriver
import com.avf.vending.hardware.api.event.BillEvent
import com.avf.vending.hardware.api.model.BillStatus
import com.avf.vending.hardware.api.model.ConnectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class MockBillDriver @Inject constructor() : BillValidatorDriver {
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: Flow<ConnectionState> = _connectionState

    private val _billEvents = MutableSharedFlow<BillEvent>(extraBufferCapacity = 16)
    override val billEvents: Flow<BillEvent> = _billEvents

    override suspend fun connect(): Boolean {
        _connectionState.value = ConnectionState.CONNECTED
        return true
    }

    override suspend fun disconnect() { _connectionState.value = ConnectionState.DISCONNECTED }

    override suspend fun pollStatus(): BillStatus = BillStatus(ready = true, stackerCount = 0)

    /** Simulate bill insertion — call this from tests/demos */
    suspend fun simulateBillInsert(denomination: Long) {
        _billEvents.emit(BillEvent.BillInserted(denomination))
        delay(500)
        _billEvents.emit(BillEvent.Accepted(denomination, denomination))
    }

    override suspend fun acceptBill() {}
    override suspend fun rejectBill() { _billEvents.emit(BillEvent.Rejected("Mock rejected")) }
    override suspend fun dispenseChange(amount: Long) { _billEvents.emit(BillEvent.ChangeDispensed(amount)) }
}

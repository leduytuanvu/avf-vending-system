package com.avf.vending.testing

import com.avf.vending.hardware.api.driver.BillValidatorDriver
import com.avf.vending.hardware.api.event.BillEvent
import com.avf.vending.hardware.api.model.BillStatus
import com.avf.vending.hardware.api.model.ConnectionState
import kotlinx.coroutines.flow.*

class FakeBillValidatorDriver : BillValidatorDriver {

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: Flow<ConnectionState> = _connectionState

    private val _events = MutableSharedFlow<BillEvent>(extraBufferCapacity = 16)
    override val billEvents: Flow<BillEvent> = _events

    var connectResult = true
    var acceptBillCalled = false
    var rejectBillCalled = false
    val dispensedChanges = mutableListOf<Long>()

    override suspend fun connect(): Boolean {
        _connectionState.value = ConnectionState.CONNECTED
        return connectResult
    }

    override suspend fun disconnect() {
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    override suspend fun pollStatus(): BillStatus = BillStatus(
        ready = true, stackerCount = 0, errorCode = 0
    )

    override suspend fun acceptBill() {
        acceptBillCalled = true
    }

    override suspend fun rejectBill() {
        rejectBillCalled = true
        _events.emit(BillEvent.Rejected("test_reject"))
    }

    override suspend fun dispenseChange(amount: Long) {
        dispensedChanges += amount
        _events.emit(BillEvent.ChangeDispensed(amount))
    }

    // Test helpers
    suspend fun simulateInsert(denomination: Long) {
        _events.emit(BillEvent.BillInserted(denomination))
    }

    suspend fun simulateAccepted(denomination: Long, total: Long) {
        _events.emit(BillEvent.Accepted(denomination, total))
    }

    suspend fun simulateJam() {
        _events.emit(BillEvent.Jammed)
    }
}

package com.avf.vending.hardware.api.driver

import com.avf.vending.hardware.api.event.BillEvent
import com.avf.vending.hardware.api.model.BillStatus
import com.avf.vending.hardware.api.model.ConnectionState
import kotlinx.coroutines.flow.Flow

interface BillValidatorDriver {
    val connectionState: Flow<ConnectionState>
    val billEvents: Flow<BillEvent>
    suspend fun connect(): Boolean
    suspend fun disconnect()
    suspend fun pollStatus(): BillStatus
    suspend fun acceptBill()
    suspend fun rejectBill()
    suspend fun dispenseChange(amount: Long)
}

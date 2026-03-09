package com.avf.vending.hardware.api.driver

import com.avf.vending.hardware.api.model.ConnectionState
import com.avf.vending.hardware.api.model.DispenseResult
import com.avf.vending.hardware.api.model.MachineStatus
import com.avf.vending.hardware.api.model.SlotStatus
import kotlinx.coroutines.flow.Flow

interface VendingMachineDriver {
    val connectionState: Flow<ConnectionState>
    suspend fun connect(): Boolean
    suspend fun disconnect()
    suspend fun dispense(row: Char, col: Int): DispenseResult
    suspend fun getStatus(): MachineStatus
    suspend fun getInventory(): List<SlotStatus>
}

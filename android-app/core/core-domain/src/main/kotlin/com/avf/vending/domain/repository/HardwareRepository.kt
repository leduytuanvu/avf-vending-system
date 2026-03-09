package com.avf.vending.domain.repository

import com.avf.vending.domain.model.MachineStatus
import kotlinx.coroutines.flow.Flow

interface HardwareRepository {
    fun observeMachineStatus(): Flow<MachineStatus>
    suspend fun getLastKnownStatus(): MachineStatus?
    suspend fun recordStatus(status: MachineStatus)
}

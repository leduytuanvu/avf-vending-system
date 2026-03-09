package com.avf.vending.testing

import com.avf.vending.domain.model.MachineStatus
import com.avf.vending.domain.repository.HardwareRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull

class FakeHardwareRepository : HardwareRepository {
    private val status = MutableStateFlow<MachineStatus?>(null)

    fun setStatus(s: MachineStatus) { status.value = s }

    override fun observeMachineStatus(): Flow<MachineStatus> = status.filterNotNull()

    override suspend fun getLastKnownStatus(): MachineStatus = requireNotNull(status.value)
    override suspend fun recordStatus(s: MachineStatus) { status.value = s }
}

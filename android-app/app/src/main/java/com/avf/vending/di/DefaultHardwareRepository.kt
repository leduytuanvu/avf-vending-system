package com.avf.vending.di

import com.avf.vending.domain.model.MachineStatus
import com.avf.vending.domain.repository.HardwareRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultHardwareRepository @Inject constructor() : HardwareRepository {
    private val status = MutableStateFlow(
        MachineStatus(
            temperature = 0f,
            doorOpen = false,
            errors = emptyList(),
            isOnline = true,
        )
    )

    override fun observeMachineStatus(): Flow<MachineStatus> = status

    override suspend fun getLastKnownStatus(): MachineStatus = status.value

    override suspend fun recordStatus(status: MachineStatus) {
        this.status.value = status
    }
}

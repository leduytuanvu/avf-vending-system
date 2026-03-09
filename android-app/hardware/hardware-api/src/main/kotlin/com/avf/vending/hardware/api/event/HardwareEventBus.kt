package com.avf.vending.hardware.api.event

import kotlinx.coroutines.flow.Flow

interface HardwareEventBus {
    val events: Flow<HardwareEvent>

    suspend fun publish(event: HardwareEvent)

    fun tryPublish(event: HardwareEvent): Boolean
}

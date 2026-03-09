package com.avf.vending.hardware.transport

import com.avf.vending.hardware.api.event.HardwareEvent
import com.avf.vending.hardware.api.event.HardwareEventBus
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultHardwareEventBus @Inject constructor() : HardwareEventBus {
    private val _events = MutableSharedFlow<HardwareEvent>(
        extraBufferCapacity = 512,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override val events: Flow<HardwareEvent> = _events.asSharedFlow()

    override suspend fun publish(event: HardwareEvent) {
        if (!_events.tryEmit(event)) {
            _events.emit(event)
        }
    }

    override fun tryPublish(event: HardwareEvent): Boolean = _events.tryEmit(event)
}

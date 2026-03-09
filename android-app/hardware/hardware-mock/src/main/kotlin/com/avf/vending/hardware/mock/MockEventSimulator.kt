package com.avf.vending.hardware.mock

import com.avf.vending.hardware.api.event.MachineEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import kotlin.random.Random

class MockEventSimulator @Inject constructor() {
    fun simulateEvents(): Flow<MachineEvent> = flow {
        while (true) {
            delay(5000)
            when (Random.nextInt(4)) {
                0 -> emit(MachineEvent.SlotEmpty("A${Random.nextInt(1, 5)}"))
                1 -> emit(MachineEvent.TemperatureAlert(Random.nextFloat() * 10 + 25))
                2 -> emit(MachineEvent.DoorClosed)
                else -> { /* no event */ }
            }
        }
    }
}

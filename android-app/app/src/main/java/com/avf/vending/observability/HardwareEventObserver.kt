package com.avf.vending.observability

import com.avf.vending.hardware.api.event.HardwareEvent
import com.avf.vending.hardware.api.event.HardwareEventBus
import com.avf.vending.logger.EventLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HardwareEventObserver @Inject constructor(
    private val hardwareEventBus: HardwareEventBus,
    private val eventLogger: EventLogger,
) {
    fun start(scope: CoroutineScope) {
        hardwareEventBus.events
            .onEach { event ->
                eventLogger.log(
                    screen = "hardware",
                    action = event.actionName(),
                    metadata = event.toString(),
                )
            }
            .launchIn(scope)
    }

    private fun HardwareEvent.actionName(): String = when (this) {
        is HardwareEvent.BillEventObserved -> "bill.$eventName"
        is HardwareEvent.CommandFailed -> "command.failed"
        is HardwareEvent.CommandQueued -> "command.queued"
        is HardwareEvent.CommandStarted -> "command.started"
        is HardwareEvent.CommandSucceeded -> "command.succeeded"
        is HardwareEvent.ConnectionChanged -> "connection.$state"
        is HardwareEvent.DispenseEventObserved -> "dispense.$eventName"
        is HardwareEvent.StrategyFallback -> "strategy.fallback"
    }
}

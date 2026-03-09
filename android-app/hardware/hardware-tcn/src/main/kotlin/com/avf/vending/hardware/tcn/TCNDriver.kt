package com.avf.vending.hardware.tcn

import com.avf.vending.hardware.api.command.HardwareCommand
import com.avf.vending.hardware.api.command.HardwareCommandQueue
import com.avf.vending.hardware.api.driver.VendingMachineDriver
import com.avf.vending.hardware.api.event.HardwareEvent
import com.avf.vending.hardware.api.event.HardwareEventBus
import com.avf.vending.hardware.api.model.ConnectionState
import com.avf.vending.hardware.api.model.DispenseResult
import com.avf.vending.hardware.api.model.MachineStatus
import com.avf.vending.hardware.api.model.SlotStatus
import com.avf.vending.common.di.ApplicationScope
import com.avf.vending.hardware.transport.DriverConnectionWatchdog
import com.avf.vending.hardware.transport.StrategyManager
import com.avf.vending.hardware.tcn.strategy.TCNStrategyDefaults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class TCNDriver @Inject constructor(
    private val strategyManager: StrategyManager,
    private val commandQueue: HardwareCommandQueue,
    private val eventBus: HardwareEventBus,
    @ApplicationScope private val appScope: CoroutineScope,
) : VendingMachineDriver {
    private val portId = TCNStrategyDefaults.PORT_ID

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: Flow<ConnectionState> = _connectionState
    private val watchdog = DriverConnectionWatchdog(
        scope = appScope,
        reconnect = { connectInternal() },
    )

    override suspend fun connect(): Boolean {
        return connectInternal()
    }

    override suspend fun disconnect() {
        watchdog.cancel()
        try {
            commandQueue.submit(
                HardwareCommand(
                    name = "tcn.disconnect",
                    busKey = portId,
                    timeoutMs = 5_000L,
                )
            ) {
                strategyManager.getActive().disconnect()
            }
        } finally {
            updateConnectionState(ConnectionState.DISCONNECTED)
        }
    }

    override suspend fun dispense(row: Char, col: Int): DispenseResult {
        val slotId = "$row$col"
        eventBus.publish(
            HardwareEvent.DispenseEventObserved(
                source = "TCNDriver",
                slotId = slotId,
                eventName = "started",
            )
        )
        return try {
            ensureConnected()
            val accumulator = TCNAccumulator()
            val response = commandQueue.submit(
                HardwareCommand(
                    name = "tcn.dispense",
                    busKey = portId,
                    payloadDescription = slotId,
                    timeoutMs = 5_000L,
                )
            ) {
                strategyManager.request(
                    data = TCNFrameCodec.encode(TCNCommand.VendRequest(row, col)),
                    timeoutMs = 5_000L,
                    busKey = portId,
                    parseChunk = { chunk -> accumulator.feed(chunk) },
                    accept = { it.cmdCode == TCNCommands.VEND_REQUEST || it.cmdCode == TCNCommands.ACK || it.cmdCode == TCNCommands.NAK },
                )
            }
            val result = TCNStatusMapper.toDispenseResult(response)
            eventBus.publish(
                HardwareEvent.DispenseEventObserved(
                    source = "TCNDriver",
                    slotId = slotId,
                    eventName = if (result is DispenseResult.Success) "succeeded" else "failed",
                    detail = (result as? DispenseResult.Failed)?.reason.orEmpty(),
                )
            )
            result
        } catch (e: Exception) {
            handleConnectionFailure()
            eventBus.publish(
                HardwareEvent.DispenseEventObserved(
                    source = "TCNDriver",
                    slotId = slotId,
                    eventName = "failed",
                    detail = e.message ?: "Unknown",
                )
            )
            DispenseResult.Failed(e.message ?: "Unknown", -1)
        }
    }

    override suspend fun getStatus(): MachineStatus = try {
        ensureConnected()
        val accumulator = TCNAccumulator()
        val response = commandQueue.submit(
            HardwareCommand(
                name = "tcn.status",
                busKey = portId,
                timeoutMs = 3_000L,
            )
        ) {
            strategyManager.request(
                data = TCNFrameCodec.encode(TCNCommand.StatusPoll),
                timeoutMs = 3_000L,
                busKey = portId,
                parseChunk = { chunk -> accumulator.feed(chunk) },
                accept = { it.cmdCode == TCNCommands.STATUS_POLL || it.cmdCode == TCNCommands.ACK || it.cmdCode == TCNCommands.NAK },
            )
        }
        MachineStatus(
            errorCodes = if (response.isSuccess) emptyList() else listOf(response.data.firstOrNull()?.toInt() ?: -1),
            connectionState = _connectionState.value,
        )
    } catch (e: Exception) {
        handleConnectionFailure()
        MachineStatus(connectionState = ConnectionState.DISCONNECTED, errorCodes = listOf(-1))
    }

    override suspend fun getInventory(): List<SlotStatus> = try {
        ensureConnected()
        val accumulator = TCNAccumulator()
        commandQueue.submit(
            HardwareCommand(
                name = "tcn.inventory",
                busKey = portId,
                timeoutMs = 3_000L,
            )
        ) {
            strategyManager.request(
                data = TCNFrameCodec.encode(TCNCommand.GetInventory),
                timeoutMs = 3_000L,
                busKey = portId,
                parseChunk = { chunk -> accumulator.feed(chunk) },
                accept = { it.cmdCode == TCNCommands.GET_INVENTORY || it.cmdCode == TCNCommands.ACK || it.cmdCode == TCNCommands.NAK },
            )
        }
        emptyList()
    } catch (e: Exception) {
        handleConnectionFailure()
        emptyList()
    }

    private suspend fun connectInternal(): Boolean {
        strategyManager.configure(TCNStrategyDefaults.buildStrategies())
        strategyManager.restoreFromPersistence()
        updateConnectionState(ConnectionState.CONNECTING)
        return try {
            val result = commandQueue.submit(
                HardwareCommand(
                    name = "tcn.connect",
                    busKey = portId,
                    timeoutMs = 5_000L,
                )
            ) {
                strategyManager.getActive().connect()
            }
            updateConnectionState(if (result) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED)
            if (!result) watchdog.scheduleReconnect()
            result
        } catch (e: Exception) {
            handleConnectionFailure()
            false
        }
    }

    private suspend fun ensureConnected() {
        if (_connectionState.value == ConnectionState.CONNECTED) return
        check(connectInternal()) { "TCN driver is disconnected" }
    }

    private suspend fun handleConnectionFailure() {
        updateConnectionState(ConnectionState.DISCONNECTED)
        watchdog.scheduleReconnect()
    }

    private suspend fun updateConnectionState(state: ConnectionState) {
        _connectionState.value = state
        eventBus.publish(HardwareEvent.ConnectionChanged("TCNDriver", portId, state.name))
    }
}

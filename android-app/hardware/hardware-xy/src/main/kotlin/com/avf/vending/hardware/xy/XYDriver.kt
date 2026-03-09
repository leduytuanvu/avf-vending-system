package com.avf.vending.hardware.xy

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
import com.avf.vending.hardware.transport.VendingDispatcher
import com.avf.vending.hardware.xy.strategy.XYStrategyDefaults
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Concurrency design:
 * - All send operations run on [dispatcher] (limitedParallelism=1), keeping
 *   vending commands off the shared IO pool and naturally serialised.
 * - [busArbiter] provides a cross-driver lock. Use the same portId as
 *   ICTBillDriver only when both devices share the same physical RS485 bus.
 *   When they use separate ports (the common case), they get independent locks
 *   and never block each other.
 */
class XYDriver @Inject constructor(
    private val strategyManager: StrategyManager,
    private val commandQueue: HardwareCommandQueue,
    private val eventBus: HardwareEventBus,
    @ApplicationScope private val appScope: CoroutineScope,
    @VendingDispatcher private val dispatcher: CoroutineDispatcher,
) : VendingMachineDriver {

    // Change to BillStrategyDefaults.PORT_ID if the machine controller and
    // bill acceptor share the same physical RS485 bus.
    private val portId = XYStrategyDefaults.PORT_ID

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: Flow<ConnectionState> = _connectionState
    private val watchdog = DriverConnectionWatchdog(
        scope = appScope,
        reconnect = { connectInternal() },
    )

    override suspend fun connect(): Boolean = withContext(dispatcher) {
        connectInternal()
    }

    override suspend fun disconnect() = withContext(dispatcher) {
        watchdog.cancel()
        try {
            commandQueue.submit(
                HardwareCommand(
                    name = "xy.disconnect",
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

    override suspend fun dispense(row: Char, col: Int): DispenseResult = withContext(dispatcher) {
        val cmd = XYCommand.Dispense(row, col)
        val slotId = "$row$col"
        eventBus.publish(
            HardwareEvent.DispenseEventObserved(
                source = "XYDriver",
                slotId = slotId,
                eventName = "started",
            )
        )
        try {
            ensureConnected()
            val accumulator = XYAccumulator()
            val response = commandQueue.submit(
                HardwareCommand(
                    name = "xy.dispense",
                    busKey = portId,
                    payloadDescription = slotId,
                    timeoutMs = 5_000L,
                )
            ) {
                strategyManager.request(
                    data = XYFrameCodec.encode(cmd),
                    timeoutMs = 5_000L,
                    busKey = portId,
                    parseChunk = { chunk -> accumulator.feed(chunk) },
                    accept = { response -> response.cmdCode == XYCommands.DISPENSE },
                )
            }
            val result = XYStatusMapper.toDispenseResult(response)
            eventBus.publish(
                HardwareEvent.DispenseEventObserved(
                    source = "XYDriver",
                    slotId = slotId,
                    eventName = if (result is DispenseResult.Success) "succeeded" else "failed",
                    detail = (result as? DispenseResult.Failed)?.reason.orEmpty(),
                )
            )
            result
        } catch (e: Exception) {
            handleConnectionFailure(e)
            eventBus.publish(
                HardwareEvent.DispenseEventObserved(
                    source = "XYDriver",
                    slotId = slotId,
                    eventName = "failed",
                    detail = e.message ?: "Request failed",
                )
            )
            DispenseResult.Failed(e.message ?: "Request failed", -1)
        }
    }

    override suspend fun getStatus(): MachineStatus = withContext(dispatcher) {
        try {
            ensureConnected()
            val accumulator = XYAccumulator()
            val response = commandQueue.submit(
                HardwareCommand(
                    name = "xy.status",
                    busKey = portId,
                    timeoutMs = 3_000L,
                )
            ) {
                strategyManager.request(
                    data = XYFrameCodec.encode(XYCommand.GetStatus),
                    timeoutMs = 3_000L,
                    busKey = portId,
                    parseChunk = { chunk -> accumulator.feed(chunk) },
                    accept = { it.cmdCode == XYCommands.GET_STATUS },
                )
            }
            MachineStatus(
                errorCodes = if (response.isSuccess) emptyList() else listOf(response.status.toInt() and 0xFF),
                connectionState = _connectionState.value,
            )
        } catch (e: Exception) {
            handleConnectionFailure(e)
            MachineStatus(
                errorCodes = listOf(-1),
                connectionState = ConnectionState.DISCONNECTED,
            )
        }
    }

    override suspend fun getInventory(): List<SlotStatus> = withContext(dispatcher) {
        try {
            ensureConnected()
            val accumulator = XYAccumulator()
            val response = commandQueue.submit(
                HardwareCommand(
                    name = "xy.inventory",
                    busKey = portId,
                    timeoutMs = 3_000L,
                )
            ) {
                strategyManager.request(
                    data = XYFrameCodec.encode(XYCommand.GetInventory),
                    timeoutMs = 3_000L,
                    busKey = portId,
                    parseChunk = { chunk -> accumulator.feed(chunk) },
                    accept = { it.cmdCode == XYCommands.GET_INVENTORY },
                )
            }
            XYInventoryParser.parse(response.data)
        } catch (e: Exception) {
            handleConnectionFailure(e)
            emptyList()
        }
    }

    private suspend fun connectInternal(): Boolean {
        strategyManager.configure(XYStrategyDefaults.buildStrategies())
        strategyManager.restoreFromPersistence()
        updateConnectionState(ConnectionState.CONNECTING)
        return try {
            val result = commandQueue.submit(
                HardwareCommand(
                    name = "xy.connect",
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
            handleConnectionFailure(e)
            false
        }
    }

    private suspend fun ensureConnected() {
        if (_connectionState.value == ConnectionState.CONNECTED) return
        check(connectInternal()) { "XY driver is disconnected" }
    }

    private suspend fun handleConnectionFailure(error: Exception) {
        updateConnectionState(ConnectionState.DISCONNECTED)
        eventBus.publish(
            HardwareEvent.DispenseEventObserved(
                source = "XYDriver",
                slotId = portId,
                eventName = "connection_failed",
                detail = error.message ?: error::class.java.simpleName,
            )
        )
        watchdog.scheduleReconnect()
    }

    private suspend fun updateConnectionState(state: ConnectionState) {
        _connectionState.value = state
        eventBus.publish(HardwareEvent.ConnectionChanged("XYDriver", portId, state.name))
    }
}

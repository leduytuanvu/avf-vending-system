package com.avf.vending.hardware.bill

import com.avf.vending.hardware.api.command.HardwareCommand
import com.avf.vending.hardware.api.command.HardwareCommandQueue
import com.avf.vending.hardware.api.driver.BillValidatorDriver
import com.avf.vending.hardware.api.event.BillEvent
import com.avf.vending.hardware.api.event.HardwareEvent
import com.avf.vending.hardware.api.event.HardwareEventBus
import com.avf.vending.hardware.api.model.BillStatus
import com.avf.vending.hardware.api.model.ConnectionState
import com.avf.vending.common.di.ApplicationScope
import com.avf.vending.hardware.transport.BillDispatcher
import com.avf.vending.hardware.transport.DriverConnectionWatchdog
import com.avf.vending.hardware.transport.StrategyManager
import com.avf.vending.hardware.bill.strategy.BillStrategyDefaults
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * ICT-BC Bill Validator Driver
 * Protocol: 9600/8E1 — EVEN parity is mandatory
 * Polls CMD=25 (POLL_STATUS) every 500ms
 *
 * Concurrency design:
 * - All send operations run on [dispatcher] (limitedParallelism=1), keeping
 *   bill commands off the shared IO pool and naturally serialised.
 * - [_billEvents] uses SUSPEND overflow so the poll loop back-pressures instead
 *   of silently dropping events when the consumer is slow (e.g., during dispense).
 */
class ICTBillDriver @Inject constructor(
    private val strategyManager: StrategyManager,
    private val commandQueue: HardwareCommandQueue,
    private val eventBus: HardwareEventBus,
    @ApplicationScope private val appScope: CoroutineScope,
    @BillDispatcher private val dispatcher: CoroutineDispatcher,
) : BillValidatorDriver {

    // Port ID used to acquire the bus lock.  Change to match the vending
    // machine driver's portId if both devices share the same RS485 bus.
    private val portId = BillStrategyDefaults.PORT_ID

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    override val connectionState: Flow<ConnectionState> = _connectionState

    // Buffer = 64 (up from 16) + SUSPEND: the poll coroutine will back-pressure
    // instead of dropping events when the payment collector is occupied.
    private val _billEvents = MutableSharedFlow<BillEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )
    override val billEvents: Flow<BillEvent> = _billEvents
    private val watchdog = DriverConnectionWatchdog(
        scope = appScope,
        reconnect = { connectInternal() },
    )
    private var pollJob: Job? = null
    private var lastPollSignature: String? = null

    override suspend fun connect(): Boolean = withContext(dispatcher) {
        connectInternal()
    }

    override suspend fun disconnect() = withContext(dispatcher) {
        pollJob?.cancel()
        watchdog.cancel()
        try {
            commandQueue.submit(
                HardwareCommand(
                    name = "bill.disconnect",
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

    override suspend fun pollStatus(): BillStatus = withContext(dispatcher) {
        try {
            ensureConnected()
            val pollFrame = ICTFrame(ICTAddresses.CONTROLLER, ICTCommands.POLL_STATUS, byteArrayOf())
            val accumulator = ICTAccumulator()
            val response = commandQueue.submit(
                HardwareCommand(
                    name = "bill.poll_status",
                    busKey = portId,
                    timeoutMs = 2_000L,
                )
            ) {
                strategyManager.request(
                    data = ICTFrameCodec.encode(pollFrame),
                    timeoutMs = 2_000L,
                    busKey = portId,
                    parseChunk = { chunk -> accumulator.feed(chunk) },
                    accept = { it.cmd == ICTCommands.POLL_STATUS },
                )
            }
            emitPollEventIfChanged(response)
            ICTStatusParser.parse(response)
        } catch (e: Exception) {
            handleConnectionFailure(e)
            BillStatus(ready = false, stackerCount = 0, errorCode = -1)
        }
    }

    override suspend fun acceptBill() = withContext(dispatcher) {
        try {
            ensureConnected()
            val frame = ICTFrame(ICTAddresses.CONTROLLER, ICTCommands.ESCROW_DECISION, byteArrayOf(ICTCommands.ACCEPT))
            val accumulator = ICTAccumulator()
            commandQueue.submit(
                HardwareCommand(
                    name = "bill.accept",
                    busKey = portId,
                    timeoutMs = 2_000L,
                )
            ) {
                strategyManager.request(
                    data = ICTFrameCodec.encode(frame),
                    timeoutMs = 2_000L,
                    busKey = portId,
                    parseChunk = { chunk -> accumulator.feed(chunk) },
                    accept = { it.cmd == ICTCommands.ESCROW_DECISION },
                )
            }
            eventBus.publish(HardwareEvent.BillEventObserved("ICTBillDriver", "accepted"))
        } catch (e: Exception) {
            handleConnectionFailure(e)
            throw e
        }
    }

    override suspend fun rejectBill() = withContext(dispatcher) {
        try {
            ensureConnected()
            val frame = ICTFrame(ICTAddresses.CONTROLLER, ICTCommands.ESCROW_DECISION, byteArrayOf(ICTCommands.REJECT))
            val accumulator = ICTAccumulator()
            commandQueue.submit(
                HardwareCommand(
                    name = "bill.reject",
                    busKey = portId,
                    timeoutMs = 2_000L,
                )
            ) {
                strategyManager.request(
                    data = ICTFrameCodec.encode(frame),
                    timeoutMs = 2_000L,
                    busKey = portId,
                    parseChunk = { chunk -> accumulator.feed(chunk) },
                    accept = { it.cmd == ICTCommands.ESCROW_DECISION },
                )
            }
            eventBus.publish(HardwareEvent.BillEventObserved("ICTBillDriver", "rejected"))
        } catch (e: Exception) {
            handleConnectionFailure(e)
            throw e
        }
    }

    override suspend fun dispenseChange(amount: Long) = withContext(dispatcher) {
        try {
            ensureConnected()
            val amountBytes = byteArrayOf(((amount shr 8) and 0xFF).toByte(), (amount and 0xFF).toByte())
            val frame = ICTFrame(ICTAddresses.CONTROLLER, ICTCommands.DISPENSE_CHANGE, amountBytes)
            val accumulator = ICTAccumulator()
            commandQueue.submit(
                HardwareCommand(
                    name = "bill.dispense_change",
                    busKey = portId,
                    payloadDescription = amount.toString(),
                    timeoutMs = 5_000L,
                )
            ) {
                strategyManager.request(
                    data = ICTFrameCodec.encode(frame),
                    timeoutMs = 5_000L,
                    busKey = portId,
                    parseChunk = { chunk -> accumulator.feed(chunk) },
                    accept = { it.cmd == ICTCommands.DISPENSE_CHANGE },
                )
            }
            eventBus.publish(HardwareEvent.BillEventObserved("ICTBillDriver", "change_dispensed", amount))
        } catch (e: Exception) {
            handleConnectionFailure(e)
            throw e
        }
    }

    private suspend fun sendReset() {
        val frame = ICTFrame(ICTAddresses.CONTROLLER, ICTCommands.RESET, byteArrayOf())
        val accumulator = ICTAccumulator()
        commandQueue.submit(
            HardwareCommand(
                name = "bill.reset",
                busKey = portId,
                timeoutMs = 5_000L,
            )
        ) {
            strategyManager.request(
                data = ICTFrameCodec.encode(frame),
                timeoutMs = 5_000L,
                busKey = portId,
                parseChunk = { chunk -> accumulator.feed(chunk) },
                accept = { it.cmd == ICTCommands.RESET },
            )
        }
        delay(500)
    }

    private suspend fun connectInternal(): Boolean {
        strategyManager.configure(BillStrategyDefaults.buildStrategies())
        strategyManager.restoreFromPersistence()
        updateConnectionState(ConnectionState.CONNECTING)
        return try {
            val result = commandQueue.submit(
                HardwareCommand(
                    name = "bill.connect",
                    busKey = portId,
                    timeoutMs = 5_000L,
                )
            ) {
                strategyManager.getActive().connect()
            }
            updateConnectionState(if (result) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED)
            if (result) {
                sendReset()
                startPollingLoop()
            } else {
                watchdog.scheduleReconnect()
            }
            result
        } catch (e: Exception) {
            handleConnectionFailure(e)
            false
        }
    }

    private fun startPollingLoop() {
        if (pollJob?.isActive == true) return
        pollJob = appScope.launch {
            while (isActive && _connectionState.value == ConnectionState.CONNECTED) {
                pollStatus()
                delay(500L)
            }
        }
    }

    private suspend fun ensureConnected() {
        if (_connectionState.value == ConnectionState.CONNECTED) return
        check(connectInternal()) { "ICT bill driver is disconnected" }
    }

    private suspend fun emitPollEventIfChanged(frame: ICTFrame) {
        val signature = frame.data.joinToString(separator = ",") { byte -> (byte.toInt() and 0xFF).toString() }
        if (signature == lastPollSignature) return
        lastPollSignature = signature
        val billEvent = ICTEventMapper.mapPollResponse(frame) ?: return
        _billEvents.emit(billEvent)
        when (billEvent) {
            is BillEvent.BillInserted -> eventBus.publish(
                HardwareEvent.BillEventObserved(
                    source = "ICTBillDriver",
                    eventName = "bill_inserted",
                    amount = billEvent.denomination,
                )
            )
            is BillEvent.Rejected -> eventBus.publish(
                HardwareEvent.BillEventObserved(
                    source = "ICTBillDriver",
                    eventName = "bill_rejected",
                    detail = billEvent.reason,
                )
            )
            BillEvent.Jammed -> eventBus.publish(
                HardwareEvent.BillEventObserved(
                    source = "ICTBillDriver",
                    eventName = "bill_jammed",
                )
            )
            is BillEvent.Accepted -> eventBus.publish(
                HardwareEvent.BillEventObserved(
                    source = "ICTBillDriver",
                    eventName = "bill_accepted",
                    amount = billEvent.total,
                )
            )
            is BillEvent.ChangeDispensed -> eventBus.publish(
                HardwareEvent.BillEventObserved(
                    source = "ICTBillDriver",
                    eventName = "change_dispensed",
                    amount = billEvent.amount,
                )
            )
        }
    }

    private suspend fun handleConnectionFailure(error: Exception) {
        updateConnectionState(ConnectionState.DISCONNECTED)
        eventBus.publish(
            HardwareEvent.BillEventObserved(
                source = "ICTBillDriver",
                eventName = "connection_failed",
                detail = error.message ?: error::class.java.simpleName,
            )
        )
        pollJob?.cancel()
        watchdog.scheduleReconnect()
    }

    private suspend fun updateConnectionState(state: ConnectionState) {
        _connectionState.value = state
        eventBus.publish(HardwareEvent.ConnectionChanged("ICTBillDriver", portId, state.name))
    }
}

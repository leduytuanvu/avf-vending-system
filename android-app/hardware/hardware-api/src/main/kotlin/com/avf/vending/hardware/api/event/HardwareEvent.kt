package com.avf.vending.hardware.api.event

import com.avf.vending.hardware.api.command.HardwareCommand

sealed interface HardwareEvent {
    val timestamp: Long

    data class CommandQueued(
        val command: HardwareCommand,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : HardwareEvent

    data class CommandStarted(
        val command: HardwareCommand,
        val attempt: Int,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : HardwareEvent

    data class CommandSucceeded(
        val command: HardwareCommand,
        val attempt: Int,
        val durationMs: Long,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : HardwareEvent

    data class CommandFailed(
        val command: HardwareCommand,
        val attempt: Int,
        val reason: String,
        val willRetry: Boolean,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : HardwareEvent

    data class StrategyFallback(
        val busKey: String,
        val fromStrategyId: String,
        val toStrategyId: String,
        val reason: String,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : HardwareEvent

    data class ConnectionChanged(
        val source: String,
        val busKey: String,
        val state: String,
        override val timestamp: Long = System.currentTimeMillis(),
    ) : HardwareEvent

    data class BillEventObserved(
        val source: String,
        val eventName: String,
        val amount: Long? = null,
        val detail: String = "",
        override val timestamp: Long = System.currentTimeMillis(),
    ) : HardwareEvent

    data class DispenseEventObserved(
        val source: String,
        val slotId: String,
        val eventName: String,
        val transactionId: String? = null,
        val detail: String = "",
        override val timestamp: Long = System.currentTimeMillis(),
    ) : HardwareEvent
}

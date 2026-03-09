package com.avf.vending.hardware.api.event

sealed class ConnectionEvent {
    object Connected : ConnectionEvent()
    object Disconnected : ConnectionEvent()
    data class Error(val message: String, val cause: Throwable? = null) : ConnectionEvent()
    data class StrategyFallback(val fromId: String, val toId: String) : ConnectionEvent()
}

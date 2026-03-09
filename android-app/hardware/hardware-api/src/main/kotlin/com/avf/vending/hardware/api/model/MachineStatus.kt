package com.avf.vending.hardware.api.model

data class MachineStatus(
    val temperature: Float = 0f,
    val doorOpen: Boolean = false,
    val errorCodes: List<Int> = emptyList(),
    val connectionState: ConnectionState = ConnectionState.DISCONNECTED,
)

package com.avf.vending.hardware.api.config

data class Rs485Config(
    val portName: String,
    val baudRate: Int = 19200,
    val dataBits: Int = 8,
    val stopBits: Int = 1,
    val lineDelayMs: Long = 2,
    val hardwareFlowControl: Boolean = false,
)

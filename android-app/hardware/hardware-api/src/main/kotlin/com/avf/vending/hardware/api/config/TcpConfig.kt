package com.avf.vending.hardware.api.config

data class TcpConfig(
    val host: String,
    val port: Int,
    val connectTimeoutMs: Int = 5000,
    val readTimeoutMs: Int = 3000,
)

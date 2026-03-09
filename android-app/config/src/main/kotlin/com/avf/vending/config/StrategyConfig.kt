package com.avf.vending.config

data class StrategyConfig(
    val machineType: String = "unknown",
    val primaryPort: String = "/dev/ttyS0",
    val backupPort: String = "/dev/ttyS1",
    val tcpHost: String? = null,
    val tcpPort: Int = 4001,
    val baudRate: Int = 9600,
)

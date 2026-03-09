package com.avf.vending.config

object StrategyDefaults {
    val xyDefault = StrategyConfig(
        machineType = "xy",
        primaryPort = "/dev/ttyS0",
        backupPort = "/dev/ttyS1",
        baudRate = 9600,
    )

    val tcnDefault = StrategyConfig(
        machineType = "tcn",
        primaryPort = "/dev/ttyS0",
        backupPort = "/dev/ttyS1",
        tcpHost = "192.168.1.100",
        tcpPort = 4001,
        baudRate = 19200,
    )

    val coDefault = StrategyConfig(
        machineType = "co",
        primaryPort = "/dev/ttyS0",
        backupPort = "/dev/ttyS2",
        baudRate = 9600,
    )

    val ictBillDefault = StrategyConfig(
        machineType = "bill",
        primaryPort = "/dev/ttyS1",
        backupPort = "/dev/ttyUSB0",
        baudRate = 9600,
    )

    fun forMachineType(type: String): StrategyConfig = when (type) {
        "xy" -> xyDefault
        "tcn" -> tcnDefault
        "co" -> coDefault
        else -> xyDefault
    }
}

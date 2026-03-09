package com.avf.vending.config

data class PerformanceConfig(
    val imageCacheMb: Int = 50,
    val syncIntervalMinutes: Int = 15,
    val logLevel: String = "INFO",          // DEBUG | INFO | WARN | ERROR | NONE
    val maxSyncRetries: Int = 5,
    val syncBatchSize: Int = 100,
    val memCacheTtlSeconds: Long = 60L,
)

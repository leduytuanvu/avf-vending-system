package com.avf.vending.config

/**
 * Root config object. Fetched from remote, validated, then stored locally.
 * Exposed as StateFlow<MachineConfig> by ConfigRepositoryImpl for hot-reload.
 */
data class MachineConfig(
    val machineId: String = "",
    val machineType: String = "unknown",
    val apiBaseUrl: String = "",
    val hardware: HardwareConfig = HardwareConfig(),
    val ui: UIConfig = UIConfig(),
    val business: BusinessConfig = BusinessConfig(),
    val performance: PerformanceConfig = PerformanceConfig(),
    val features: Map<String, Boolean> = emptyMap(),
    val strategy: StrategyConfig = StrategyConfig(),
    val updatedAt: Long = 0L,
)

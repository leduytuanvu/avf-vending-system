package com.avf.vending.config

object ConfigValidator {
    data class ValidationResult(val isValid: Boolean, val errors: List<String>)

    fun validate(config: MachineConfig): ValidationResult {
        val errors = mutableListOf<String>()

        if (config.machineId.isBlank()) errors += "machineId must not be blank"
        if (config.apiBaseUrl.isBlank()) errors += "apiBaseUrl must not be blank"
        if (!config.apiBaseUrl.startsWith("http")) errors += "apiBaseUrl must start with http/https"

        with(config.hardware) {
            if (dispenseTimeoutMs < 1_000) errors += "dispenseTimeoutMs must be >= 1000"
            if (dispenseRetryCount !in 0..10) errors += "dispenseRetryCount must be 0–10"
            if (temperatureWarningCelsius >= temperatureCriticalCelsius)
                errors += "warningTemp must be < criticalTemp"
        }

        with(config.ui) {
            if (idleTimeoutMs < 5_000) errors += "idleTimeoutMs must be >= 5000"
            if (paymentTimeoutMs < 30_000) errors += "paymentTimeoutMs must be >= 30000"
            if (brightness !in 0..100) errors += "brightness must be 0–100"
            if (gridColumns !in 1..8) errors += "gridColumns must be 1–8"
        }

        with(config.strategy) {
            if (machineType.isBlank()) errors += "strategy.machineType must not be blank"
            if (primaryPort.isBlank()) errors += "strategy.primaryPort must not be blank"
            if (backupPort.isBlank()) errors += "strategy.backupPort must not be blank"
            if (tcpHost != null && tcpHost.isBlank()) errors += "strategy.tcpHost must not be blank when provided"
            if (tcpPort !in 1..65535) errors += "strategy.tcpPort must be 1..65535"
            if (baudRate <= 0) errors += "strategy.baudRate must be > 0"
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    /** Returns a sanitized config with defaults applied for out-of-range values. */
    fun sanitize(config: MachineConfig): MachineConfig = config.copy(
        hardware = config.hardware.copy(
            dispenseTimeoutMs = config.hardware.dispenseTimeoutMs.coerceAtLeast(1_000),
            dispenseRetryCount = config.hardware.dispenseRetryCount.coerceIn(0, 10),
        ),
        ui = config.ui.copy(
            idleTimeoutMs = config.ui.idleTimeoutMs.coerceAtLeast(5_000),
            brightness = config.ui.brightness.coerceIn(0, 100),
            gridColumns = config.ui.gridColumns.coerceIn(1, 8),
        ),
    )
}

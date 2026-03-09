package com.avf.vending.domain.model

/**
 * Domain-level result of a hardware dispense attempt.
 * Feature layer maps hardware-api DispenseResult → this type so that
 * core-domain remains independent of the hardware module.
 */
sealed class DispenseOutcome {
    data class Success(val slotId: String) : DispenseOutcome()
    data class Failed(val reason: String, val code: Int = 0) : DispenseOutcome()
    data class Timeout(val waitedMs: Long) : DispenseOutcome()
}

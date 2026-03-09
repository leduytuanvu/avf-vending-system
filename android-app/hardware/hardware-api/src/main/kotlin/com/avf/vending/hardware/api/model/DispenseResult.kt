package com.avf.vending.hardware.api.model

sealed class DispenseResult {
    /**
     * Hardware protocol reported success.
     *
     * @param sensorSignal  Timing data for the drop sensor, or null if the driver
     *                      does not implement multi-signal confirmation.  Feature
     *                      layer feeds this into [SensorTimingGuard] to detect drift.
     */
    data class Success(
        val slotId: String,
        val sensorSignal: DispenseSignal? = null,
    ) : DispenseResult()

    data class Failed(val reason: String, val code: Int = 0) : DispenseResult()
    data class Timeout(val waitedMs: Long) : DispenseResult()
}

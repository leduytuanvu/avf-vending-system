package com.avf.vending.hardware.api.model

/**
 * Timing data collected during a single dispense attempt.
 *
 * Drivers record monotonic timestamps (e.g. SystemClock.elapsedRealtime())
 * at each hardware event so the [SensorTimingGuard] can validate that the
 * sensor triggered within a physically plausible window.
 *
 * @param motorStartedElapsed   Elapsed-realtime ms when the motor command was sent.
 * @param sensorTriggeredElapsed Elapsed-realtime ms when the drop sensor fired,
 *                               or null if the sensor never triggered.
 */
data class DispenseSignal(
    val motorStartedElapsed: Long,
    val sensorTriggeredElapsed: Long?,
) {
    /** True if the sensor fired at all during this attempt. */
    val sensorTriggered: Boolean get() = sensorTriggeredElapsed != null
}

package com.avf.vending.hardware.api.validation

import com.avf.vending.hardware.api.model.DispenseSignal

/**
 * Validates that a drop-sensor trigger occurred within a physically plausible
 * time window after the motor started.
 *
 * This guards against **sensor drift** — a common failure mode after thousands
 * of cycles where dust, heat, or mechanical wear causes sensors to:
 *  - Trigger too early  (before the motor could physically move the product)
 *  - Trigger too late   (after the product should have already fallen)
 *
 * Default timing window tuned for typical gravity-drop vending mechanisms.
 * Drivers or config can override per-machine type.
 *
 * @param minWindowMs  Minimum time after motor start before a sensor trigger
 *                     is considered valid.  Triggers before this = sensor drift.
 * @param maxWindowMs  Maximum time the sensor has to trigger after motor start.
 *                     Triggers after this = product stuck / sensor miss.
 */
class SensorTimingGuard(
    val minWindowMs: Long = MIN_WINDOW_MS,
    val maxWindowMs: Long = MAX_WINDOW_MS,
) {
    /**
     * Returns [SensorValidationResult.Valid] if the signal is within the timing
     * window, or a specific failure reason otherwise.
     *
     * If [signal.sensorTriggeredElapsed] is null the sensor never fired at all,
     * which is treated as [SensorValidationResult.NoSignal].
     */
    fun validate(signal: DispenseSignal): SensorValidationResult {
        val triggered = signal.sensorTriggeredElapsed
            ?: return SensorValidationResult.NoSignal

        val deltaMs = triggered - signal.motorStartedElapsed
        return when {
            deltaMs < minWindowMs -> SensorValidationResult.TooEarly(deltaMs)
            deltaMs > maxWindowMs -> SensorValidationResult.TooLate(deltaMs)
            else -> SensorValidationResult.Valid
        }
    }

    companion object {
        /** 300 ms — motor cannot physically move the carrier this fast. */
        const val MIN_WINDOW_MS = 300L
        /** 5 000 ms — product must have dropped within 5 s or something is wrong. */
        const val MAX_WINDOW_MS = 5_000L
    }
}

sealed class SensorValidationResult {
    /** Sensor triggered within the valid timing window. */
    object Valid : SensorValidationResult()

    /** Sensor triggered before the motor could physically move the product.
     *  Likely a false positive from vibration or dust on the sensor. */
    data class TooEarly(val deltaMs: Long) : SensorValidationResult()

    /** Sensor triggered after the maximum expected drop time.
     *  Product may be stuck or sensor alignment is off. */
    data class TooLate(val deltaMs: Long) : SensorValidationResult()

    /** Sensor never triggered at all. */
    object NoSignal : SensorValidationResult()
}

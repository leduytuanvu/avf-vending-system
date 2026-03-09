package com.avf.vending.common.time

/**
 * Monotonic clock interface — immune to NTP/wall-clock adjustments.
 *
 * Unlike [Clock] (wall clock), this counts milliseconds elapsed since device
 * boot. It cannot jump forward or backward due to NTP sync, so it is safe
 * to use for retry scheduling and timeout windows within a session.
 *
 * Note: the value resets to 0 on device reboot. Do NOT store values across
 * reboots for absolute comparison — use wall clock for DB persistence and
 * reset nextRetryAt on startup (see SyncRepository.resetStuckTasks).
 */
interface MonotonicClock {
    fun elapsed(): Long
}

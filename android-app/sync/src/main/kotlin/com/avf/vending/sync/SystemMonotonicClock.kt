package com.avf.vending.sync

import android.os.SystemClock
import com.avf.vending.common.time.MonotonicClock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Production implementation of [MonotonicClock] backed by
 * [SystemClock.elapsedRealtime] — milliseconds since device boot.
 *
 * This value is guaranteed to be monotonically increasing within a boot
 * session and is immune to NTP adjustments, user clock changes, or DST
 * transitions that affect [System.currentTimeMillis].
 */
@Singleton
class SystemMonotonicClock @Inject constructor() : MonotonicClock {
    override fun elapsed(): Long = SystemClock.elapsedRealtime()
}

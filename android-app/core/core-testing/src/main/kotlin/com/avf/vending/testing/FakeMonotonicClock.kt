package com.avf.vending.testing

import com.avf.vending.common.time.MonotonicClock

class FakeMonotonicClock(private var currentElapsed: Long = 0L) : MonotonicClock {
    override fun elapsed(): Long = currentElapsed
    fun advance(ms: Long) { currentElapsed += ms }
    fun set(elapsed: Long) { currentElapsed = elapsed }
}

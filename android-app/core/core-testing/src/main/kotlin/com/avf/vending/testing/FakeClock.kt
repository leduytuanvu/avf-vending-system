package com.avf.vending.testing

import com.avf.vending.common.time.Clock

class FakeClock(private var currentTime: Long = 0L) : Clock {
    override fun now(): Long = currentTime
    fun advance(ms: Long) { currentTime += ms }
    fun set(time: Long) { currentTime = time }
}

package com.avf.vending.common.time

import javax.inject.Inject

class SystemClock @Inject constructor() : Clock {
    override fun now(): Long = System.currentTimeMillis()
}

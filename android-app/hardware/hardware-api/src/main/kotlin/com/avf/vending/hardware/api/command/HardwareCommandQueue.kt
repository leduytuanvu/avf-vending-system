package com.avf.vending.hardware.api.command

interface HardwareCommandQueue {
    suspend fun <T> submit(command: HardwareCommand, execute: suspend () -> T): T
}

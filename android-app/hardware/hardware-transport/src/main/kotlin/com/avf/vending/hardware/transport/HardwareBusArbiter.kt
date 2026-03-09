package com.avf.vending.hardware.transport

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared mutex registry for physical serial/RS485 buses.
 *
 * When two drivers communicate over the same physical port (e.g., both on
 * /dev/ttyS1), they must serialize their sends through the same Mutex.
 * Each driver calls [withBusLock] with the same [portId]; unrelated ports
 * get independent locks and never block each other.
 *
 * Usage in a driver:
 *   busArbiter.withBusLock(config.portId) { strategyManager.send(frame) }
 */
@Singleton
class HardwareBusArbiter @Inject constructor() {

    private val locks = ConcurrentHashMap<String, Mutex>()

    fun busLock(portId: String): Mutex = locks.getOrPut(portId) { Mutex() }

    suspend fun <T> withBusLock(portId: String, block: suspend () -> T): T =
        busLock(portId).withLock { block() }
}

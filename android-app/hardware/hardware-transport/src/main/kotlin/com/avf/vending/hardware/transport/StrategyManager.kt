package com.avf.vending.hardware.transport

import com.avf.vending.hardware.api.exception.NoStrategyAvailableException
import com.avf.vending.hardware.api.event.HardwareEvent
import com.avf.vending.hardware.api.event.HardwareEventBus
import com.avf.vending.hardware.api.strategy.CommunicationStrategy
import com.avf.vending.local.datastore.StrategyDataStore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

class StrategyManager @Inject constructor(
    private val eventBus: HardwareEventBus,
    private val strategyDataStore: StrategyDataStore,
) {
    private val strategies = mutableListOf<CommunicationStrategy>()
    private var activeIndex = 0
    private val mutex = Mutex()
    private var forcedStrategy: String? = null

    fun configure(orderedStrategies: List<CommunicationStrategy>) {
        strategies.clear()
        strategies.addAll(orderedStrategies)
    }

    /** Call after configure() to restore activeIndex from persisted last-successful strategy. */
    suspend fun restoreFromPersistence() {
        if (strategies.isEmpty()) return
        val savedId = strategyDataStore.lastSuccessfulStrategyId.first()
        if (savedId != null) {
            val idx = strategies.indexOfFirst { it.id == savedId }
            if (idx >= 0) activeIndex = idx
        }
    }

    fun forceStrategy(strategyId: String) { forcedStrategy = strategyId }
    fun clearForce() { forcedStrategy = null }

    // --- non-locking helpers (call only while mutex is held) ---

    private fun requireStrategies() {
        if (strategies.isEmpty()) throw NoStrategyAvailableException()
    }

    private fun resolveActive(): CommunicationStrategy {
        requireStrategies()
        return forcedStrategy?.let { id -> strategies.find { it.id == id } }
            ?: strategies.getOrElse(activeIndex) { strategies.first() }
    }

    private fun advanceIndex() {
        activeIndex = (activeIndex + 1) % strategies.size
    }

    // --- public API (all mutex-protected) ---

    suspend fun getActive(): CommunicationStrategy = mutex.withLock { resolveActive() }

    suspend fun fallbackToNext(): CommunicationStrategy = mutex.withLock {
        requireStrategies()
        advanceIndex()
        resolveActive()
    }

    /**
     * Sends [data] through the active strategy, with automatic fallback.
     *
     * The entire "pick strategy → send" sequence runs under [mutex] so no
     * concurrent coroutine can switch the active strategy mid-flight.
     * Holding the lock during I/O is intentional: it serialises all commands
     * through this manager, which is correct for a half-duplex serial device.
     */
    suspend fun send(data: ByteArray, busKey: String = "unknown"): Unit = mutex.withLock {
        requireStrategies()
        var lastError: Throwable? = null
        repeat(strategies.size) {
            val strategy = resolveActive()
            try {
                if (strategy.isAvailable()) {
                    strategy.send(data)
                    saveLastSuccessful(strategy.id)
                    return@withLock
                }
            } catch (e: Exception) {
                lastError = e
                val failedStrategy = strategy
                advanceIndex()
                val fallbackStrategy = resolveActive()
                eventBus.publish(
                    HardwareEvent.StrategyFallback(
                        busKey = busKey,
                        fromStrategyId = failedStrategy.id,
                        toStrategyId = fallbackStrategy.id,
                        reason = e.message ?: "send_failed",
                    )
                )
            }
        }
        throw lastError ?: NoStrategyAvailableException()
    }

    suspend fun <FRAME> request(
        data: ByteArray,
        timeoutMs: Long,
        busKey: String = "unknown",
        parseChunk: (ByteArray) -> List<FRAME>,
        accept: (FRAME) -> Boolean = { true },
    ): FRAME = mutex.withLock {
        requireStrategies()
        var lastError: Throwable? = null
        repeat(strategies.size) {
            val strategy = resolveActive()
            try {
                if (!strategy.isAvailable()) {
                    throw IllegalStateException("Strategy ${strategy.id} is unavailable")
                }

                return@withLock withTimeout(timeoutMs) {
                    coroutineScope {
                        val response = async {
                            strategy.receiveStream()
                                .transform { chunk ->
                                    parseChunk(chunk).forEach { emit(it) }
                                }
                                .first { accept(it) }
                        }
                        strategy.send(data)
                        val result = response.await()
                        saveLastSuccessful(strategy.id)
                        result
                    }
                }
            } catch (e: Exception) {
                lastError = e
                if (strategies.size > 1) {
                    val failedStrategy = strategy
                    advanceIndex()
                    val fallbackStrategy = resolveActive()
                    eventBus.publish(
                        HardwareEvent.StrategyFallback(
                            busKey = busKey,
                            fromStrategyId = failedStrategy.id,
                            toStrategyId = fallbackStrategy.id,
                            reason = e.message ?: "request_failed",
                        )
                    )
                }
            }
        }
        throw lastError ?: NoStrategyAvailableException()
    }

    private suspend fun saveLastSuccessful(strategyId: String) {
        strategyDataStore.saveLastSuccessfulStrategyId(strategyId)
    }
}

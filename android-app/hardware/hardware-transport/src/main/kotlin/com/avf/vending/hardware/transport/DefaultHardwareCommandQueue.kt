package com.avf.vending.hardware.transport

import com.avf.vending.common.di.ApplicationScope
import com.avf.vending.hardware.api.command.HardwareCommand
import com.avf.vending.hardware.api.command.HardwareCommandQueue
import com.avf.vending.hardware.api.event.HardwareEvent
import com.avf.vending.hardware.api.event.HardwareEventBus
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultHardwareCommandQueue @Inject constructor(
    private val busArbiter: HardwareBusArbiter,
    private val eventBus: HardwareEventBus,
    @param:ApplicationScope private val scope: CoroutineScope,
) : HardwareCommandQueue {
    private val workers = ConcurrentHashMap<String, SendChannel<QueuedCommand>>()

    override suspend fun <T> submit(command: HardwareCommand, execute: suspend () -> T): T {
        val deferred = CompletableDeferred<Any?>()
        eventBus.publish(HardwareEvent.CommandQueued(command))
        workerFor(command.busKey).send(
            QueuedCommand(
                command = command,
                execute = { execute() as Any? },
                deferred = deferred,
            )
        )

        @Suppress("UNCHECKED_CAST")
        return deferred.await() as T
    }

    private fun workerFor(busKey: String): SendChannel<QueuedCommand> =
        workers.getOrPut(busKey) { createWorker(busKey) }

    private fun createWorker(busKey: String): SendChannel<QueuedCommand> {
        val channel = Channel<QueuedCommand>(Channel.UNLIMITED)
        scope.launch {
            for (queued in channel) {
                process(busKey = busKey, queued = queued)
            }
        }
        return channel
    }

    private suspend fun process(busKey: String, queued: QueuedCommand) {
        var lastError: Exception? = null

        repeat(queued.command.maxRetries + 1) { index ->
            val attempt = index + 1
            val startedAt = System.currentTimeMillis()
            eventBus.publish(HardwareEvent.CommandStarted(queued.command, attempt))

            try {
                val result = busArbiter.withBusLock(busKey) {
                    withTimeout(queued.command.timeoutMs) {
                        queued.execute()
                    }
                }
                eventBus.publish(
                    HardwareEvent.CommandSucceeded(
                        command = queued.command,
                        attempt = attempt,
                        durationMs = System.currentTimeMillis() - startedAt,
                    )
                )
                queued.deferred.complete(result)
                return
            } catch (e: Exception) {
                lastError = e
                val willRetry = attempt <= queued.command.maxRetries
                eventBus.publish(
                    HardwareEvent.CommandFailed(
                        command = queued.command,
                        attempt = attempt,
                        reason = e.message ?: e::class.java.simpleName,
                        willRetry = willRetry,
                    )
                )
            }
        }

        queued.deferred.completeExceptionally(
            lastError ?: IllegalStateException("Command failed without an exception")
        )
    }

    private data class QueuedCommand(
        val command: HardwareCommand,
        val execute: suspend () -> Any?,
        val deferred: CompletableDeferred<Any?>,
    )
}

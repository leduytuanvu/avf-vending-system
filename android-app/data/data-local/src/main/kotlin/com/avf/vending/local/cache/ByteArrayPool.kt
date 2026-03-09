package com.avf.vending.local.cache

import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ByteArrayPool @Inject constructor() {
    private val pool = ConcurrentLinkedQueue<ByteArray>()
    private val bufferSize = 256

    fun acquire(): ByteArray = pool.poll() ?: ByteArray(bufferSize)

    fun release(buffer: ByteArray) {
        if (buffer.size == bufferSize) pool.offer(buffer)
    }
}

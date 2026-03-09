package com.avf.vending.hardware.transport

import com.avf.vending.hardware.api.config.TcpConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.net.Socket

class TcpTransport(private val config: TcpConfig) {
    private var socket: Socket? = null
    private val writeMutex = Mutex()

    suspend fun connect(): Boolean = withContext(Dispatchers.IO) {
        try {
            socket = Socket(config.host, config.port).apply {
                tcpNoDelay = true
                keepAlive = true
                soTimeout = config.readTimeoutMs
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun readStream(): Flow<ByteArray> = flow {
        val s = socket ?: return@flow
        val buf = ByteArray(256)
        while (true) {
            val n = withContext(Dispatchers.IO) { s.getInputStream().read(buf) }
            if (n < 0) break
            emit(buf.copyOf(n))
        }
    }

    suspend fun write(data: ByteArray) = writeMutex.withLock {
        withContext(Dispatchers.IO) { socket?.getOutputStream()?.write(data) }
    }

    fun close() { socket?.close(); socket = null }
}

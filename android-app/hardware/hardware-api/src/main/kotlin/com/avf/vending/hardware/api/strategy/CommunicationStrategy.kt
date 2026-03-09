package com.avf.vending.hardware.api.strategy

import kotlinx.coroutines.flow.Flow

interface CommunicationStrategy {
    val id: String
    val type: StrategyType
    suspend fun isAvailable(): Boolean
    suspend fun send(data: ByteArray)
    fun receiveStream(): Flow<ByteArray>
    suspend fun connect(): Boolean
    suspend fun disconnect()
}

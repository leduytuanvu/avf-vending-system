package com.avf.vending.hardware.api.codec

abstract class FrameAccumulator<FRAME> {
    private val buffer = mutableListOf<Byte>()

    fun feed(bytes: ByteArray): List<FRAME> {
        buffer.addAll(bytes.toList())
        return extractFrames()
    }

    protected abstract fun extractFrames(): List<FRAME>

    protected fun consumeBuffer(): ByteArray {
        val result = buffer.toByteArray()
        buffer.clear()
        return result
    }

    protected fun peekBuffer(): List<Byte> = buffer.toList()

    protected fun removeUpTo(index: Int) {
        repeat(index + 1) { if (buffer.isNotEmpty()) buffer.removeAt(0) }
    }
}

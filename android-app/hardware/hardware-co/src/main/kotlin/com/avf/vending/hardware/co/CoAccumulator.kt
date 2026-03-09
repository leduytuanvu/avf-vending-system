package com.avf.vending.hardware.co

import com.avf.vending.hardware.api.codec.FrameAccumulator

class CoAccumulator : FrameAccumulator<CoResponse>() {
    override fun extractFrames(): List<CoResponse> {
        val buf = peekBuffer()
        val frames = mutableListOf<CoResponse>()
        var i = 0
        while (i < buf.size) {
            if (buf[i] != 0xFE.toByte()) { i++; continue }
            val sofPos = i
            val eofPos = (sofPos + 1 until buf.size).firstOrNull { buf[it] == 0xFF.toByte() } ?: break
            val raw = buf.subList(sofPos, eofPos + 1).toByteArray()
            CoFrameCodec.decode(raw)?.let { frames.add(it) }
            i = eofPos + 1
        }
        if (frames.isNotEmpty()) removeUpTo(buf.indexOfLast { it == 0xFF.toByte() })
        return frames
    }
}

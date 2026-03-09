package com.avf.vending.hardware.tcn

import com.avf.vending.hardware.api.codec.FrameAccumulator

class TCNAccumulator : FrameAccumulator<TCNResponse>() {
    override fun extractFrames(): List<TCNResponse> {
        val buf = peekBuffer()
        val frames = mutableListOf<TCNResponse>()
        var i = 0
        while (i < buf.size - 1) {
            if (buf[i] != 0xAA.toByte() || buf[i + 1] != 0x55.toByte()) { i++; continue }
            if (i + 4 >= buf.size) break
            val len = ((buf[i + 2].toInt() and 0xFF) shl 8) or (buf[i + 3].toInt() and 0xFF)
            val frameEnd = i + 4 + len + 2
            if (frameEnd > buf.size) break
            val raw = buf.subList(i, frameEnd).toByteArray()
            TCNFrameCodec.decode(raw)?.let { frames.add(it) }
            i = frameEnd
        }
        if (frames.isNotEmpty()) removeUpTo(i - 1)
        return frames
    }
}

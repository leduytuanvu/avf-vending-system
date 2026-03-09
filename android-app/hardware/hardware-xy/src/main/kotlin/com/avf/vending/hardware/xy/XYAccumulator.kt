package com.avf.vending.hardware.xy

import com.avf.vending.hardware.api.codec.FrameAccumulator

class XYAccumulator : FrameAccumulator<XYResponse>() {
    override fun extractFrames(): List<XYResponse> {
        val buf = peekBuffer()
        val frames = mutableListOf<XYResponse>()
        var i = 0
        while (i < buf.size) {
            if (buf[i] != XYCommands.STX) { i++; continue }
            val stxPos = i
            val etxPos = (stxPos + 1 until buf.size).firstOrNull { pos -> buf[pos] == XYCommands.ETX } ?: -1
            if (etxPos < 0) break
            val raw = buf.subList(stxPos, etxPos + 1).toByteArray()
            XYFrameCodec.decode(raw)?.let { frames.add(it) }
            i = etxPos + 1
        }
        if (frames.isNotEmpty()) removeUpTo(buf.indexOfLast { it == XYCommands.ETX })
        return frames
    }
}

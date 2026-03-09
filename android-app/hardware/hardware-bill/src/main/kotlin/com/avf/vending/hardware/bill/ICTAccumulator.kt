package com.avf.vending.hardware.bill

import com.avf.vending.hardware.api.codec.FrameAccumulator

class ICTAccumulator : FrameAccumulator<ICTFrame>() {
    override fun extractFrames(): List<ICTFrame> {
        val buf = peekBuffer()
        val frames = mutableListOf<ICTFrame>()
        var i = 0
        while (i < buf.size) {
            // DA byte identifies start of ICT frame
            if (buf[i] != ICTAddresses.BILL) { i++; continue }
            if (i + 1 >= buf.size) break
            val lng = buf[i + 1].toInt() and 0xFF
            val frameEnd = i + 1 + lng + 1
            if (frameEnd > buf.size) break
            val raw = buf.subList(i, frameEnd).toByteArray()
            ICTFrameCodec.decode(raw)?.let { frames.add(it) }
            i = frameEnd
        }
        if (frames.isNotEmpty()) removeUpTo(i - 1)
        return frames
    }
}

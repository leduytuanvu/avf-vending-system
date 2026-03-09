package com.avf.vending.hardware.me

import com.avf.vending.hardware.api.codec.FrameAccumulator

class MeAccumulator : FrameAccumulator<MeResponse>() {
    override fun extractFrames(): List<MeResponse> {
        val buf = peekBuffer()
        val frames = mutableListOf<MeResponse>()
        var i = 0
        while (i < buf.size) {
            if (buf[i] != 0xFE.toByte()) { i++; continue }
            val sofPos = i
            val eofIdx = buf.indexOf(0xFF.toByte().also { })
            val eofPos = buf.indexOfFirst { pos ->
                buf.getOrNull(pos)?.let { it == 0xFF.toByte() } == true && pos > sofPos
            }
            if (eofPos < 0) break
            val raw = buf.subList(sofPos, eofPos + 1).toByteArray()
            MeFrameCodec.decode(raw)?.let { frames.add(it) }
            i = eofPos + 1
        }
        if (frames.isNotEmpty()) removeUpTo(buf.indexOfLast { it == 0xFF.toByte() })
        return frames
    }

    private fun List<Byte>.indexOf(target: Byte): Int = indexOfFirst { it == target }
    private fun List<Byte>.indexOfFirst(predicate: (Int) -> Boolean): Int {
        forEachIndexed { index, _ -> if (predicate(index)) return index }
        return -1
    }
}

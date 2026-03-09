package com.avf.vending.hardware.api.codec

interface FrameCodec<CMD, FRAME> {
    fun encode(command: CMD): ByteArray
    fun decode(bytes: ByteArray): FRAME?
}

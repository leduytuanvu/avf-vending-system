package com.avf.vending.hardware.co

import com.avf.vending.hardware.api.codec.FrameCodec

/**
 * Máy Cơ custom serial protocol.
 * Frame: [0xFE][CMD][LEN][DATA...][XOR_CHECKSUM][0xFF]
 */
object CoFrameCodec : FrameCodec<CoCommand, CoResponse> {
    private const val SOF: Byte = 0xFE.toByte()
    private const val EOF: Byte = 0xFF.toByte()

    override fun encode(command: CoCommand): ByteArray {
        val (cmd, data) = when (command) {
            is CoCommand.Dispense -> Pair(
                CoCommands.DISPENSE,
                byteArrayOf(command.row.code.toByte(), command.col.toByte())
            )
            CoCommand.Poll -> Pair(CoCommands.POLL, byteArrayOf())
            CoCommand.GetInventory -> Pair(CoCommands.GET_INVENTORY, byteArrayOf())
            CoCommand.GetStatus -> Pair(CoCommands.GET_STATUS, byteArrayOf())
        }
        val payload = byteArrayOf(cmd, data.size.toByte()) + data
        val xor = payload.fold(0.toByte()) { acc, b -> (acc.toInt() xor b.toInt()).toByte() }
        return byteArrayOf(SOF) + payload + byteArrayOf(xor, EOF)
    }

    override fun decode(bytes: ByteArray): CoResponse? {
        if (bytes.size < 5) return null
        if (bytes.first() != SOF || bytes.last() != EOF) return null
        val cmd = bytes[1]
        val len = bytes[2].toInt() and 0xFF
        if (bytes.size < 4 + len) return null
        val status = bytes[3]
        val data = bytes.copyOfRange(4, 4 + len)
        return CoResponse(cmd, status, data)
    }
}

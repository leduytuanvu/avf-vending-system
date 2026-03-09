package com.avf.vending.hardware.me

import com.avf.vending.hardware.api.codec.FrameCodec

/**
 * ME Machine custom serial protocol.
 * Frame: [0xFE][CMD][LEN][DATA...][XOR_CHECKSUM][0xFF]
 */
object MeFrameCodec : FrameCodec<MeCommand, MeResponse> {
    private const val SOF: Byte = 0xFE.toByte()
    private const val EOF: Byte = 0xFF.toByte()

    override fun encode(command: MeCommand): ByteArray {
        val (cmd, data) = when (command) {
            is MeCommand.Dispense -> Pair(
                MeCommands.DISPENSE,
                byteArrayOf(command.row.code.toByte(), command.col.toByte())
            )
            MeCommand.Poll -> Pair(MeCommands.POLL, byteArrayOf())
            MeCommand.GetInventory -> Pair(MeCommands.GET_INVENTORY, byteArrayOf())
            MeCommand.GetStatus -> Pair(MeCommands.GET_STATUS, byteArrayOf())
        }
        val payload = byteArrayOf(cmd, data.size.toByte()) + data
        val xor = payload.fold(0.toByte()) { acc, b -> (acc.toInt() xor b.toInt()).toByte() }
        return byteArrayOf(SOF) + payload + byteArrayOf(xor, EOF)
    }

    override fun decode(bytes: ByteArray): MeResponse? {
        if (bytes.size < 5) return null
        if (bytes.first() != SOF || bytes.last() != EOF) return null
        val cmd = bytes[1]
        val len = bytes[2].toInt() and 0xFF
        if (bytes.size < 4 + len) return null
        val status = bytes[3]
        val data = bytes.copyOfRange(4, 4 + len)
        return MeResponse(cmd, status, data)
    }
}

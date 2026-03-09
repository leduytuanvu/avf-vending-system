package com.avf.vending.hardware.xy

import com.avf.vending.hardware.api.codec.FrameCodec

/**
 * XY Frame: [STX 0x02][LEN][ADDR][CMD][DATA...][XOR checksum][ETX 0x03]
 */
object XYFrameCodec : FrameCodec<XYCommand, XYResponse> {

    override fun encode(command: XYCommand): ByteArray {
        val (addr, cmd, data) = when (command) {
            is XYCommand.Dispense -> Triple(
                XYCommands.ADDR_CONTROLLER,
                XYCommands.DISPENSE,
                byteArrayOf(command.row.code.toByte(), command.col.toByte())
            )
            is XYCommand.Poll -> Triple(XYCommands.ADDR_CONTROLLER, XYCommands.POLL, byteArrayOf())
            is XYCommand.GetInventory -> Triple(XYCommands.ADDR_CONTROLLER, XYCommands.GET_INVENTORY, byteArrayOf())
            is XYCommand.GetStatus -> Triple(XYCommands.ADDR_CONTROLLER, XYCommands.GET_STATUS, byteArrayOf())
        }
        val len = (3 + data.size).toByte()
        val payload = byteArrayOf(len, addr, cmd) + data
        val xor = payload.fold(0.toByte()) { acc, b -> (acc.toInt() xor b.toInt()).toByte() }
        return byteArrayOf(XYCommands.STX) + payload + byteArrayOf(xor, XYCommands.ETX)
    }

    override fun decode(bytes: ByteArray): XYResponse? {
        if (bytes.size < 6) return null
        if (bytes.first() != XYCommands.STX || bytes.last() != XYCommands.ETX) return null
        val len = bytes[1].toInt() and 0xFF
        if (bytes.size < len + 3) return null
        val addr = bytes[2]
        val cmd = bytes[3]
        val status = bytes[4]
        val data = bytes.copyOfRange(5, 2 + len)
        return XYResponse(addr, cmd, status, data)
    }
}

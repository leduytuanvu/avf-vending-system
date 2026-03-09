package com.avf.vending.hardware.transport

import android.content.Context
import android.hardware.usb.UsbManager
import com.avf.vending.hardware.api.config.SerialConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UsbSerialTransport @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val usbManager by lazy { context.getSystemService(Context.USB_SERVICE) as UsbManager }

    fun readStream(): Flow<ByteArray> = flow {
        // usb-serial-for-android adapter
        // TODO: enumerate USB devices, open connection, read stream
    }

    suspend fun write(data: ByteArray) {
        // TODO: write via UsbDeviceConnection
    }

    suspend fun open(config: SerialConfig): Boolean = false // TODO

    fun close() {} // TODO
}

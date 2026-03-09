package com.avf.vending.testing

import com.avf.vending.common.coroutine.DispatcherProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class TestDispatcherProvider : DispatcherProvider {
    private val dispatcher = UnconfinedTestDispatcher()
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
    override val hardware: CoroutineDispatcher = dispatcher
}

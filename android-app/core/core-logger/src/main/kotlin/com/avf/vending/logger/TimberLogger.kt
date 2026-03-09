package com.avf.vending.logger

import timber.log.Timber
import javax.inject.Inject

class TimberLogger @Inject constructor() : AppLogger {
    override fun d(tag: String, message: String) = Timber.tag(tag).d(message)
    override fun i(tag: String, message: String) = Timber.tag(tag).i(message)
    override fun w(tag: String, message: String, throwable: Throwable?) =
        Timber.tag(tag).w(throwable, message)
    override fun e(tag: String, message: String, throwable: Throwable?) =
        Timber.tag(tag).e(throwable, message)
}

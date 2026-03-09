package com.avf.vending

import android.app.Application
import com.avf.vending.domain.repository.SyncRepository
import com.avf.vending.domain.usecase.ReconcileTransactionsUseCase
import com.avf.vending.common.di.ApplicationScope
import com.avf.vending.observability.HardwareEventObserver
import com.avf.vending.sync.ConnectivityAwareSyncManager
import com.avf.vending.sync.SyncEngine
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class VendingApp : Application() {

    @Inject lateinit var syncRepository: SyncRepository
    @Inject lateinit var reconcileTransactionsUseCase: ReconcileTransactionsUseCase
    @Inject lateinit var hardwareEventObserver: HardwareEventObserver
    @Inject lateinit var syncEngine: SyncEngine
    @Inject lateinit var connectivityAwareSyncManager: ConnectivityAwareSyncManager
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        try {
            SentryAndroid.init(this) { options ->
                options.dsn = BuildConfig.SENTRY_DSN
                options.environment = if (BuildConfig.DEBUG) "debug" else "release"
                options.isEnableAutoSessionTracking = true
            }
        } catch (_: Throwable) { /* Sentry init failed — app continues without crash reporting */ }
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        hardwareEventObserver.start(appScope)
        syncEngine.start(appScope)
        connectivityAwareSyncManager.start(appScope)
        appScope.launch {
            // Reset sync tasks stuck in PROCESSING from a previous crash
            syncRepository.resetStuckTasks()
            // Find payment-success / dispensing transactions that were never confirmed
            // and mark them REFUND_REQUIRED so the operator is alerted
            reconcileTransactionsUseCase()
        }
    }
}

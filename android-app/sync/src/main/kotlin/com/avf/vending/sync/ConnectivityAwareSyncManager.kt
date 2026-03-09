package com.avf.vending.sync

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Watches network state and triggers both the delta sync and the queue processor
 * whenever connectivity is (re-)established. Debounces 500 ms to avoid rapid re-triggers.
 */
@Singleton
@OptIn(FlowPreview::class)
class ConnectivityAwareSyncManager @Inject constructor(
    private val syncEngine: SyncEngine,
    private val networkMonitor: NetworkMonitor,
) {
    private var job: Job? = null

    fun start(scope: CoroutineScope) {
        // Establish the long-lived network observer ONCE.
        // SyncEngine.start() subscribes to the same monitor; calling it again here would
        // create duplicate subscriptions. Use runOnce() so each reconnect triggers exactly
        // one drain pass without adding a new Flow subscriber.
        job = networkMonitor.isOnline
            .distinctUntilChanged()
            .filter { it }
            .debounce(500L)
            .onEach { scope.launch { syncEngine.runOnce() } }
            .launchIn(scope)
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}

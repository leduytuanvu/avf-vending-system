package com.avf.vending.sync.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.avf.vending.sync.SyncEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * One-shot worker triggered when network becomes available.
 * Immediately drains the pending sync queue.
 */
@HiltWorker
class ImmediateSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncEngine: SyncEngine,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            syncEngine.runOnce()
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "immediate_sync"

        fun enqueue(workManager: WorkManager) {
            val request = OneTimeWorkRequestBuilder<ImmediateSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            workManager.enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
}

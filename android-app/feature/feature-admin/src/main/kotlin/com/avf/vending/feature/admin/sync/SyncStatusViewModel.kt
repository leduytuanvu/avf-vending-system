package com.avf.vending.feature.admin.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avf.vending.sync.SyncEngine
import com.avf.vending.ui.components.BadgeStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SyncStatusState(
    val lastSyncAt: Long? = null,
    val pendingCount: Int = 0,
    val isSyncing: Boolean = false,
    val statusLabel: String = "Idle",
    val badgeStatus: BadgeStatus = BadgeStatus.INFO,
    val error: String? = null,
)

@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    private val syncEngine: SyncEngine,
) : ViewModel() {

    private val _state = MutableStateFlow(SyncStatusState())
    val state: StateFlow<SyncStatusState> = _state.asStateFlow()

    fun triggerSync() {
        viewModelScope.launch {
            _state.update { it.copy(isSyncing = true, error = null, statusLabel = "Syncing…", badgeStatus = BadgeStatus.INFO) }
            try {
                syncEngine.start(viewModelScope)
                _state.update { it.copy(
                    isSyncing = false,
                    lastSyncAt = System.currentTimeMillis(),
                    statusLabel = "OK",
                    badgeStatus = BadgeStatus.OK,
                ) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    isSyncing = false,
                    error = e.message,
                    statusLabel = "Failed",
                    badgeStatus = BadgeStatus.ERROR,
                ) }
            }
        }
    }
}

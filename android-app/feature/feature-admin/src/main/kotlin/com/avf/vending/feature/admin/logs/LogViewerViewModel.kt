package com.avf.vending.feature.admin.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

data class LogViewerState(
    val entries: List<String> = emptyList(),
    val isLoading: Boolean = false,
)

@HiltViewModel
class LogViewerViewModel @Inject constructor() : ViewModel() {

    private val _state = MutableStateFlow(LogViewerState())
    val state: StateFlow<LogViewerState> = _state.asStateFlow()

    init { loadLogs() }

    private fun loadLogs() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val process = Runtime.getRuntime().exec(arrayOf("logcat", "-d", "-v", "time", "-t", "500"))
                val lines = BufferedReader(InputStreamReader(process.inputStream))
                    .readLines()
                _state.update { it.copy(entries = lines, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(
                    entries = listOf("Failed to read logcat: ${e.message}"),
                    isLoading = false,
                ) }
            }
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            try {
                Runtime.getRuntime().exec(arrayOf("logcat", "-c"))
                _state.update { it.copy(entries = emptyList()) }
            } catch (_: Exception) {}
        }
    }
}

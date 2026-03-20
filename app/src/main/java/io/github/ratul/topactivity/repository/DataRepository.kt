package io.github.ratul.topactivity.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HistoryItem(val pkg: String, val cls: String)

data class ServiceState(
    val running: Boolean = false,
    val pkg: String = "",
    val cls: String = "",
    val history: List<HistoryItem> = emptyList()
)

object DataRepository {
    private val _appState = MutableStateFlow(ServiceState())
    val appState: StateFlow<ServiceState> = _appState.asStateFlow()
    private val historyDeque = ArrayDeque<HistoryItem>(50)

    fun updateStatus(isRunning: Boolean) {
        _appState.value = _appState.value.copy(running = isRunning)
    }

    fun updateData(newPkg: String, newCls: String) {
        val currentState = _appState.value
        // Prevent rapid duplicate emissions
        if (currentState.pkg == newPkg && currentState.cls == newCls) return

        val newItem = HistoryItem(newPkg, newCls)
        historyDeque.addLast(newItem)
        if (historyDeque.size > 50) {
            historyDeque.removeFirst()
        }

        _appState.value = currentState.copy(
            pkg = newPkg,
            cls = newCls,
            history = historyDeque.toList()
        )
    }

    fun clearHistory() {
        historyDeque.clear()
        _appState.value = _appState.value.copy(
            history = emptyList()
        )
    }
}
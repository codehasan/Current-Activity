/*
 *   Copyright (C) 2022 Ratul Hasan
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
        historyDeque.addFirst(newItem)
        if (historyDeque.size >= 50) historyDeque.removeLast()

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
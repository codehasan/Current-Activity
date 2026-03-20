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
package io.github.ratul.topactivity.manager

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.ratul.topactivity.R
import io.github.ratul.topactivity.extensions.getScreenSize
import io.github.ratul.topactivity.repository.DataRepository
import io.github.ratul.topactivity.repository.HistoryItem
import io.github.ratul.topactivity.utils.DatabaseUtil
import io.github.ratul.topactivity.utils.WindowManagerUtil.getLayoutParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryManager(private val context: Context) {

    private val popupScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var collectionJob: Job? = null

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var baseView: View? = null
    private var historyAdapter: HistoryAdapter? = null

    fun show() {
        if (baseView != null) return

        val wrapper = ContextThemeWrapper(
            context,
            if (DatabaseUtil.useSystemFont) R.style.AppTheme_SystemFont
            else R.style.AppTheme
        )
        val view = LayoutInflater.from(wrapper)
            .inflate(R.layout.layout_activity_history, null)
        baseView = view

        val (displayWidth, _) = windowManager.getScreenSize()
        val scaleFactor = mapPreferenceToWindowSize(DatabaseUtil.windowSize)
        val viewSize = (displayWidth * scaleFactor).toInt()

        val layoutParams = getLayoutParams()
        layoutParams.gravity = Gravity.CENTER
        layoutParams.width = viewSize

        windowManager.addView(baseView, layoutParams)

        val closeBtn = view.findViewById<ImageView>(R.id.closeBtn)
        val clearBtn = view.findViewById<ImageView>(R.id.clearBtn)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_history)

        val serviceState = DataRepository.appState.value
        val historySize = mapPreferenceToHistorySize(DatabaseUtil.historySize)
        historyAdapter = HistoryAdapter(serviceState.history, historySize)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = historyAdapter

        clearBtn.setOnClickListener {
            DataRepository.clearHistory()
            historyAdapter?.clearAll()
        }
        closeBtn.setOnClickListener { hide() }
        view.setOnTouchListener(DragTouchManager(windowManager, layoutParams))

        collectionJob = popupScope.launch {
            DataRepository.appState.collectLatest { state ->
                if (!state.running) {
                    hide()
                    return@collectLatest
                }

                historyAdapter?.addItem(HistoryItem(state.pkg, state.cls))
                recyclerView.scrollToPosition(0)
            }
        }
    }

    fun hide() {
        baseView?.let {
            windowManager.removeView(it)
            baseView = null
        }
        collectionJob?.cancel()
        collectionJob = null
    }

    private fun mapPreferenceToWindowSize(value: String): Double {
        return when (value) {
            "0" -> 0.65
            "1" -> 0.50
            else -> 0.45
        }
    }

    private fun mapPreferenceToHistorySize(value: String): Int {
        return when (value) {
            "0" -> 50
            "1" -> 20
            else -> 10
        }
    }

    private class HistoryAdapter(
        initialList: List<HistoryItem> = emptyList(),
        private val historySize: Int
    ) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

        private val items: MutableList<HistoryItem> = initialList.toMutableList()

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val pkgText: TextView = view.findViewById(R.id.item_package_name)
            val clsText: TextView = view.findViewById(R.id.item_class_name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.content_activity_history_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.pkgText.text = item.pkg
            holder.clsText.text = item.cls
        }

        override fun getItemCount(): Int = items.size

        fun addItem(newItem: HistoryItem) {
            if (items.isNotEmpty() &&
                items[0].pkg == newItem.pkg &&
                items[0].cls == newItem.cls
            ) return

            if (items.size >= historySize) {
                val lastIndex = items.size - 1
                items.removeAt(lastIndex)
                notifyItemRemoved(lastIndex)
            }
            items.add(0, newItem)
            notifyItemInserted(0)
        }

        fun clearAll() {
            val size = items.size
            if (size > 0) {
                items.clear()
                notifyItemRangeRemoved(0, size)
            }
        }
    }
}
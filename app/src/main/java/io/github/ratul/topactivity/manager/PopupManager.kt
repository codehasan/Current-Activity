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

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.view.ContextThemeWrapper
import io.github.ratul.topactivity.App
import io.github.ratul.topactivity.R
import io.github.ratul.topactivity.extensions.getScreenSize
import io.github.ratul.topactivity.extensions.value
import io.github.ratul.topactivity.repository.DataRepository
import io.github.ratul.topactivity.utils.DatabaseUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PopupManager(private val context: Context) {

    private val popupScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var collectionJob: Job? = null

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var baseView: View? = null

    val isShown: Boolean
        get() = baseView?.isAttachedToWindow == true

    fun show() {
        if (baseView != null) return

        val wrapper = ContextThemeWrapper(
            context,
            if (DatabaseUtil.useSystemFont) R.style.AppTheme_SystemFont
            else R.style.AppTheme
        )
        val view = LayoutInflater.from(wrapper)
            .inflate(R.layout.layout_activity_info, null)
        baseView = view

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        val (displayWidth, displayHeight) = windowManager.getScreenSize()
        val viewSize = (displayWidth * 0.65).toInt()
        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.width = viewSize
        layoutParams.x = (displayWidth / 2) - (viewSize / 2)
        layoutParams.y = (displayHeight / 2)

        windowManager.addView(baseView, layoutParams)

        val appName = view.findViewById<TextView>(R.id.app_name)
        val packageName = view.findViewById<TextView>(R.id.package_name)
        val className = view.findViewById<TextView>(R.id.class_name)
        val closeBtn = view.findViewById<ImageView>(R.id.closeBtn)

        val copyListener = View.OnLongClickListener { v ->
            val textView = v as TextView
            val message = when (v.id) {
                R.id.package_name -> context.getString(R.string.package_copied)
                R.id.class_name -> context.getString(R.string.class_copied)
                else -> context.getString(R.string.copied)
            }
            App.copyString(context, textView.value(), message)
            true
        }

        closeBtn.setOnClickListener { DataRepository.updateStatus(false) }
        packageName.setOnLongClickListener(copyListener)
        className.setOnLongClickListener(copyListener)
        view.setOnTouchListener(DragTouchListener(windowManager, layoutParams))

        val serviceState = DataRepository.appState.value
        className.text = serviceState.cls
        packageName.text = serviceState.pkg
        appName.text = getAppName(serviceState.pkg) ?: context.getString(R.string.unknown)

        collectionJob = popupScope.launch {
            DataRepository.appState.collectLatest { state ->
                if (!state.running) {
                    hide()
                    return@collectLatest
                }

                val isPackageChanged = state.pkg != packageName.value()
                val isClassChanged = state.cls != className.value()

                if (isClassChanged) {
                    className.text = state.cls
                }

                if (isPackageChanged) {
                    packageName.text = state.pkg
                    val fetchedData = withContext(Dispatchers.IO) {
                        getAppName(state.pkg) ?: context.getString(R.string.unknown)
                    }
                    appName.text = fetchedData
                }
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

    private fun getAppName(pkg: String): String? {
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(pkg, 0)
            pm.getApplicationLabel(info).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    private class DragTouchListener(
        private val windowManager: WindowManager,
        private val params: WindowManager.LayoutParams
    ) :
        View.OnTouchListener {
        private var xInitCord = 0
        private var yInitCord = 0
        private var xInitMargin = 0
        private var yInitMargin = 0

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val xCord = event.rawX.toInt()
            val yCord = event.rawY.toInt()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    xInitCord = xCord
                    yInitCord = yCord
                    xInitMargin = params.x
                    yInitMargin = params.y
                    return true
                }

                MotionEvent.ACTION_MOVE -> {
                    params.x = xInitMargin + (xCord - xInitCord)
                    params.y = yInitMargin + (yCord - yInitCord)
                    windowManager.updateViewLayout(view, params)
                    return true
                }
            }
            return false
        }
    }
}
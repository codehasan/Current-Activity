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
package io.github.ratul.topactivity.managers

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
import io.github.ratul.topactivity.services.QuickSettingsTileService
import io.github.ratul.topactivity.utils.DatabaseUtil
import kotlin.math.min

interface PopupStateListener {
    fun onPopupShown() {}
    fun onPopupDismissed() {}
    fun onActivityInfoChanged(packageName: String, className: String) {}
}

object PopupManager {

    private var windowManager: WindowManager? = null
    private var packageManager: PackageManager? = null
    private var baseView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var appNameView: TextView? = null
    private var packageNameView: TextView? = null
    private var classNameView: TextView? = null

    private val listeners = mutableSetOf<PopupStateListener>()

    val isActive: Boolean
        get() = baseView?.isAttachedToWindow == true

    fun addListener(listener: PopupStateListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: PopupStateListener) {
        listeners.remove(listener)
    }

    fun show(context: Context, pkg: String, cls: String) {
        if (windowManager == null || baseView == null) {
            initView(context.applicationContext)
        }

        val wasInactive = !isActive
        if (wasInactive) {
            attachView()
            DatabaseUtil.wasShowingWindow = true
            notifyListeners { it.onPopupShown() }
            QuickSettingsTileService.requestUpdate(context)
        }

        updateContent(pkg, cls)
    }

    fun dismiss() {
        if (isActive) {
            detachView()
        }
        releaseResources()
        DatabaseUtil.wasShowingWindow = false
        notifyListeners { it.onPopupDismissed() }
        QuickSettingsTileService.requestUpdate(App.instance)
    }

    private fun notifyListeners(action: (PopupStateListener) -> Unit) {
        listeners.forEach(action)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView(context: Context) {
        windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        packageManager = context.packageManager

        layoutParams = createLayoutParams()

        val wrapper = ContextThemeWrapper(
            context,
            if (DatabaseUtil.useSystemFont) R.style.AppTheme_SystemFont else R.style.AppTheme
        )
        baseView = LayoutInflater.from(wrapper)
            .inflate(R.layout.layout_activity_info, null, false)

        appNameView = baseView!!.findViewById(R.id.app_name)
        packageNameView = baseView!!.findViewById(R.id.package_name)
        classNameView = baseView!!.findViewById(R.id.class_name)
        val closeBtn = baseView!!.findViewById<ImageView>(R.id.closeBtn)

        val copyListener = View.OnLongClickListener { v ->
            val textView = v as TextView
            val message = when (v.id) {
                R.id.package_name -> context.getString(R.string.package_copied)
                R.id.class_name -> context.getString(R.string.class_copied)
                else -> context.getString(R.string.copied)
            }
            App.copyString(context, textView.text.toString(), message)
            true
        }

        closeBtn.setOnClickListener { dismiss() }
        packageNameView!!.setOnLongClickListener(copyListener)
        classNameView!!.setOnLongClickListener(copyListener)
        baseView!!.setOnTouchListener(DragTouchListener())
    }

    private fun createLayoutParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.CENTER
        windowAnimations = android.R.style.Animation_Toast
    }

    private fun attachView() {
        val params = layoutParams ?: return
        val userWidth = DatabaseUtil.userWidth
        if (userWidth != -1) {
            params.width = userWidth
        } else {
            val displaySize = min(1100, DatabaseUtil.displayWidth)
            params.width = (displaySize * 0.65).toInt()
        }
        windowManager?.addView(baseView, params)
    }

    private fun detachView() {
        try {
            windowManager?.removeView(baseView)
        } catch (_: Exception) {
        }
    }

    private fun releaseResources() {
        layoutParams = null
        windowManager = null
        packageManager = null
        baseView = null
        appNameView = null
        packageNameView = null
        classNameView = null
    }

    private fun updateContent(pkg: String, cls: String) {
        val pkgView = packageNameView ?: return
        val clsView = classNameView ?: return

        val isPackageChanged = pkgView.text.toString() != pkg
        val isClassChanged = clsView.text.toString() != cls

        if (isPackageChanged) {
            appNameView?.text = getAppName(pkg) ?: App.instance.getString(R.string.unknown)
            pkgView.text = pkg
        }

        if (isClassChanged) {
            clsView.text = cls
        }

        if (isPackageChanged || isClassChanged) {
            notifyListeners { it.onActivityInfoChanged(pkg, cls) }
        }
    }

    private fun getAppName(pkg: String): String? {
        return try {
            packageManager?.getApplicationLabel(
                packageManager!!.getApplicationInfo(pkg, 0)
            )?.toString()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    private inner class DragTouchListener : View.OnTouchListener {
        private var xInitCord = 0
        private var yInitCord = 0
        private var xInitMargin = 0
        private var yInitMargin = 0

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            val params = layoutParams ?: return false
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
                    windowManager?.updateViewLayout(view, params)
                    return true
                }
            }
            return false
        }
    }
}

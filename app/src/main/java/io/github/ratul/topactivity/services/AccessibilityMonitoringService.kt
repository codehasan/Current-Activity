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
package io.github.ratul.topactivity.services

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Intent
import android.util.LruCache
import android.view.accessibility.AccessibilityEvent
import io.github.ratul.topactivity.extensions.isActivity
import io.github.ratul.topactivity.repository.DataRepository

@SuppressLint("AccessibilityPolicy")
class AccessibilityMonitoringService : AccessibilityService() {

    private val activityCache = LruCache<String, Boolean>(500)

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!DataRepository.appState.value.running) return

        val pkg = event.packageName?.toString() ?: return
        val cls = event.className?.toString() ?: return
        val cacheKey = "$pkg/$cls"

        val isCachedActivity = activityCache[cacheKey]
        if (isCachedActivity != null) {
            if (isCachedActivity) DataRepository.updateData(pkg, cls)
            return
        }

        val isActivity = packageManager.isActivity(pkg, cls)
        activityCache.put(cacheKey, isActivity)

        if (isActivity) DataRepository.updateData(pkg, cls)
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        instance = this
        super.onServiceConnected()
    }

    override fun onRebind(intent: Intent) {
        instance = this
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        instance = null
        return true
    }

    override fun onDestroy() {
        instance = null
        super.onDestroy()
    }

    companion object {
        var instance: AccessibilityMonitoringService? = null
            private set
    }
}

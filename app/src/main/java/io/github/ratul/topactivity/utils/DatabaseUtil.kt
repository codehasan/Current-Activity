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
package io.github.ratul.topactivity.utils

import io.github.ratul.topactivity.App

object DatabaseUtil {
    private val prefs get() = App.instance.sharedPreferences

    var displayWidth: Int
        get() = prefs.getInt("width", 720)
        set(value) = prefs.edit().putInt("width", value).apply()

    var userWidth: Int
        get() = prefs.getInt("user_width", -1)
        set(value) = prefs.edit().putInt("user_width", value).apply()

    var useAccessibility: Boolean
        get() = prefs.getBoolean("has_access", false)
        set(value) = prefs.edit().putBoolean("has_access", value).apply()

    var showNotification: Boolean
        get() = prefs.getBoolean("show_notification", false)
        set(value) = prefs.edit().putBoolean("show_notification", value).apply()

    var autoUpdate: Boolean
        get() = prefs.getBoolean("auto_update", false)
        set(value) = prefs.edit().putBoolean("auto_update", value).apply()

    var useSystemFont: Boolean
        get() = prefs.getBoolean("system_font", false)
        set(value) = prefs.edit().putBoolean("system_font", value).apply()

    var isFirstRun: Boolean
        get() = prefs.getBoolean("first_run", true)
        set(value) = prefs.edit().putBoolean("first_run", value).apply()

    /**
     * Persistence hint for restoring popup state after process death.
     * NOT the source of truth for whether the popup is currently visible —
     * use [io.github.ratul.topactivity.managers.PopupManager.isActive] for that.
     */
    var wasShowingWindow: Boolean
        get() = prefs.getBoolean("is_show_window", false)
        set(value) = prefs.edit().putBoolean("is_show_window", value).apply()
}

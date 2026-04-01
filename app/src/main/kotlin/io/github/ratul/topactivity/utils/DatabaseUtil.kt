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

import androidx.core.content.edit
import androidx.preference.PreferenceManager
import io.github.ratul.topactivity.App

object DatabaseUtil {

    private val prefs get() = PreferenceManager.getDefaultSharedPreferences(App.instance)

    var serviceMode: String
        get() = prefs.getString("service_mode", "0") ?: "0"
        set(value) = prefs.edit { putString("service_mode", value) }

    var scanSpeed: String
        get() = prefs.getString("scan_speed", "2") ?: "2"
        set(value) = prefs.edit { putString("scan_speed", value) }

    var historySize: String
        get() = prefs.getString("history_size", "1") ?: "1"
        set(value) = prefs.edit { putString("history_size", value) }

    var windowSize: String
        get() = prefs.getString("window_size", "1") ?: "1"
        set(value) = prefs.edit { putString("window_size", value) }

    var useAccessibility: Boolean
        get() = prefs.getBoolean("use_accessibility", false)
        set(value) = prefs.edit { putBoolean("use_accessibility", value) }

    var autoUpdate: Boolean
        get() = prefs.getBoolean("auto_update", false)
        set(value) = prefs.edit { putBoolean("auto_update", value) }

    var useSystemFont: Boolean
        get() = prefs.getBoolean("system_font", false)
        set(value) = prefs.edit { putBoolean("system_font", value) }

    var isFirstRun: Boolean
        get() = prefs.getBoolean("first_run", true)
        set(value) = prefs.edit { putBoolean("first_run", value) }
}

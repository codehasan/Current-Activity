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
import io.github.ratul.topactivity.utils.DatabaseUtil

class ServiceManager(private val context: Context) {
    private val popupManager by lazy { PopupManager(context) }
    private val notificationUiManager = NotificationUiManager(context)
    private val serviceMode = DatabaseUtil.serviceMode

    fun show() {
        when (serviceMode) {
            "0" -> {
                popupManager.show()
                notificationUiManager.show()
            }

            "1" -> notificationUiManager.show()
        }
    }

    fun hide() {
        when (serviceMode) {
            "0" -> {
                popupManager.hide()
                notificationUiManager.hide()
            }

            "1" -> notificationUiManager.hide()
        }
    }
}
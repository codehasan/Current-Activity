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
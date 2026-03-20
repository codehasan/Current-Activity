package io.github.ratul.topactivity.manager

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.github.ratul.topactivity.R
import io.github.ratul.topactivity.receivers.NotificationActionReceiver
import io.github.ratul.topactivity.repository.DataRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationUiManager(private val context: Context) {

    private val notificationManager = NotificationManagerCompat.from(context)
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var collectionJob: Job? = null

    private val packageLabel = context.getString(R.string.package_label)
    private val classLabel = context.getString(R.string.class_label)
    private val stopLabel = context.getString(R.string.stop)

    private val packageCopied = context.getString(R.string.package_copied)
    private val classCopied = context.getString(R.string.class_copied)

    fun show() {
        if (!notificationManager.areNotificationsEnabled()) return
        if (collectionJob?.isActive == true) return

        collectionJob = scope.launch {
            DataRepository.appState.collectLatest { state ->
                if (!state.running) {
                    hide()
                    return@collectLatest
                }

                if (state.pkg.isNotEmpty() && state.cls.isNotEmpty()) {
                    updateNotification(state.pkg, state.cls)
                }
            }
        }

        val serviceState = DataRepository.appState.value
        updateNotification(serviceState.pkg, serviceState.cls)
    }

    fun hide() {
        notificationManager.cancel(NOTIFICATION_ID)
        collectionJob?.cancel()
        collectionJob = null
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(pkg: String, cls: String) {
        val copyPkg = NotificationCompat.Action(
            R.drawable.ic_package, packageLabel,
            actionCopyPendingIntent(
                PACKAGE_COPY_ACTION_ID, pkg, packageCopied
            )
        )
        val copyClass = NotificationCompat.Action(
            R.drawable.ic_class, classLabel,
            actionCopyPendingIntent(
                CLASS_COPY_ACTION_ID, cls, classCopied
            )
        )
        val stop =
            NotificationCompat.Action(R.drawable.ic_cancel, stopLabel, actionStopPendingIntent())

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(pkg)
            .setSmallIcon(R.drawable.ic_logo)
            .setContentText(cls)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(copyPkg)
            .addAction(copyClass)
            .addAction(stop)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun actionCopyPendingIntent(
        requestCode: Int,
        text: String,
        message: String
    ): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra(
                NotificationActionReceiver.EXTRA_NOTIFICATION_ACTION,
                NotificationActionReceiver.ACTION_COPY
            )
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_ASSIST_CONTEXT, message)
        }
        return PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun actionStopPendingIntent(): PendingIntent {
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            putExtra(
                NotificationActionReceiver.EXTRA_NOTIFICATION_ACTION,
                NotificationActionReceiver.ACTION_STOP
            )
        }
        return PendingIntent.getBroadcast(
            context, STOP_ACTION_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val NOTIFICATION_ID = 62345
        const val PACKAGE_COPY_ACTION_ID = 3429872
        const val CLASS_COPY_ACTION_ID = 3429873
        const val STOP_ACTION_ID = 908435
        const val CHANNEL_ID = "activity_info"
    }
}
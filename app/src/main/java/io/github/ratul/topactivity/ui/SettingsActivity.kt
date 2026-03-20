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
package io.github.ratul.topactivity.ui

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.ComponentName
import android.content.DialogInterface
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.preference.Preference
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.ratul.topactivity.App.Companion.REPO_URL
import io.github.ratul.topactivity.R
import io.github.ratul.topactivity.extensions.isAccessibilityNotStarted
import io.github.ratul.topactivity.extensions.isNotificationGranted
import io.github.ratul.topactivity.extensions.isSystemOverlayGranted
import io.github.ratul.topactivity.extensions.isUsageStatsGranted
import io.github.ratul.topactivity.extensions.openLink
import io.github.ratul.topactivity.extensions.setStatus
import io.github.ratul.topactivity.extensions.showMessage
import io.github.ratul.topactivity.manager.AppUpdateManager
import io.github.ratul.topactivity.manager.ServiceManager
import io.github.ratul.topactivity.repository.DataRepository
import io.github.ratul.topactivity.services.PackageMonitoringService
import io.github.ratul.topactivity.utils.AutostartUtil.isAutoStartPermissionAvailable
import io.github.ratul.topactivity.utils.AutostartUtil.requestAutoStartPermission
import io.github.ratul.topactivity.utils.DatabaseUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private val popupScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    lateinit var appUpdateManager: AppUpdateManager

    lateinit var baseView: CoordinatorLayout
    lateinit var fabStart: FloatingActionButton
    lateinit var fragment: SettingsFragment

    private var monitoringService: PackageMonitoringService? = null
    private var isServiceBound = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            monitoringService = (service as PackageMonitoringService.LocalBinder).service
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            isServiceBound = false
            monitoringService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        fabStart = findViewById(R.id.start)
        baseView = findViewById(R.id.main)
        fragment =
            supportFragmentManager.findFragmentById(R.id.preferences_container) as SettingsFragment
        appUpdateManager = AppUpdateManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(baseView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (DatabaseUtil.isFirstRun) showAutoUpdatePolicyDialog()
        if (DatabaseUtil.autoUpdate) appUpdateManager.checkForUpdate(true)

        popupScope.launch {
            DataRepository.appState.collectLatest { state ->
                fabStart.setStatus(state.running)
            }
        }
        fabStart.setOnClickListener { onFabStartClicked() }

        val topAppBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        topAppBar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.github -> {
                    openLink(REPO_URL)
                    true
                }

                else -> false
            }
        }

        fragment.enableAutostart.isVisible = isAutoStartPermissionAvailable(this)
        fragment.enableAutostart.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            requestAutoStartPermission(this)
            true
        }

        fragment.checkUpdate.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            showMessage(R.string.checking_for_update)
            appUpdateManager.checkForUpdate()
            true
        }

        if (handleQsTileIntent(intent)) {
            moveTaskToBack(true)
        }
    }

    override fun onDestroy() {
        stopService()
        popupScope.cancel()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (handleQsTileIntent(intent)) {
            moveTaskToBack(true)
        }
    }

    private fun handleQsTileIntent(intent: Intent): Boolean {
        val fromQsTile = intent.getBooleanExtra(EXTRA_FROM_QS_TILE, false)

        return if (fromQsTile) {
            onFabStartClicked()
            DataRepository.appState.value.running
        } else false
    }

    private fun stopService() {
        DataRepository.updateStatus(false)
    }

    private fun onFabStartClicked() {
        val serviceState = DataRepository.appState.value
        if (serviceState.running) {
            stopService()
            return
        }

        val accessibilityNot = isAccessibilityNotStarted()
        val usageStats = isUsageStatsGranted()
        val notification = isNotificationGranted()
        val systemOverlay = isSystemOverlayGranted()

        if (accessibilityNot) requestAccessibilityPermission()
        if (!usageStats) requestUsageStatsPermission()
        if (!notification) requestNotificationPermission()

        val serviceMode = DatabaseUtil.serviceMode
        if (serviceMode == "0" && !systemOverlay) requestSystemOverlayPermission()

        if (accessibilityNot or
            !usageStats or
            !notification or
            !systemOverlay
        ) return

        DataRepository.updateStatus(true)
        val intent = Intent(this, PackageMonitoringService::class.java)
        applicationContext.startService(intent)
        applicationContext.bindService(intent, serviceConnection, BIND_AUTO_CREATE)
        ServiceManager(this).show()
        DataRepository.updateData(packageName, this::class.java.name)
    }

    private fun showAutoUpdatePolicyDialog() {
        val clickListener = DialogInterface.OnClickListener { dialog, btn ->
            dialog.dismiss()

            when (btn) {
                AlertDialog.BUTTON_POSITIVE -> {
                    DatabaseUtil.autoUpdate = true
                    fragment.autoUpdate.isChecked = true
                    appUpdateManager.checkForUpdate(true)
                }

                AlertDialog.BUTTON_NEUTRAL -> {
                    DatabaseUtil.autoUpdate = false
                    fragment.autoUpdate.isChecked = false
                }
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.auto_update_check)
            .setMessage(R.string.auto_update_desc)
            .setPositiveButton(R.string.enable, clickListener)
            .setNeutralButton(R.string.cancel, clickListener)
            .setOnDismissListener { DatabaseUtil.isFirstRun = false }
            .show()
    }

    private fun requestNotificationPermission() {
        notificationPermissionLauncher.launch(POST_NOTIFICATIONS)
    }

    private fun requestSystemOverlayPermission() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.system_overlay_title)
            .setMessage(
                getString(
                    R.string.system_overlay_description,
                    getString(R.string.app_name)
                )
            )
            .setPositiveButton(R.string.settings) { dialog, _ ->
                dialog.dismiss()
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    .setData("package:$packageName".toUri())
                startActivity(intent)
            }
            .setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun requestAccessibilityPermission() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.accessibility_permission_title)
            .setMessage(
                getString(
                    R.string.accessibility_permission_description,
                    getString(R.string.app_name)
                )
            )
            .setPositiveButton(R.string.settings) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            .setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun requestUsageStatsPermission() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.usage_access_title)
            .setMessage(getString(R.string.usage_access_description, getString(R.string.app_name)))
            .setPositiveButton(R.string.settings) { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            .setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun applyTheme() {
        setTheme(
            if (DatabaseUtil.useSystemFont) R.style.AppTheme_SystemFont
            else R.style.AppTheme
        )
    }

    companion object {
        const val EXTRA_FROM_QS_TILE = "from_qs_tile"
    }
}

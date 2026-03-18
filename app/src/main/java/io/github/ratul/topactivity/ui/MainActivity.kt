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

import android.Manifest.permission.PACKAGE_USAGE_STATS
import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.AppOpsManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Process
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowInsets
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.EdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Request.Method.GET
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDivider
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import io.github.ratul.topactivity.App
import io.github.ratul.topactivity.App.Companion.API_URL
import io.github.ratul.topactivity.App.Companion.REPO_URL
import io.github.ratul.topactivity.BuildConfig
import io.github.ratul.topactivity.R
import io.github.ratul.topactivity.managers.PopupManager
import io.github.ratul.topactivity.managers.PopupStateListener
import io.github.ratul.topactivity.services.AccessibilityMonitoringService
import io.github.ratul.topactivity.services.PackageMonitoringService
import io.github.ratul.topactivity.utils.AutostartUtil
import io.github.ratul.topactivity.utils.DatabaseUtil
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var baseView: CoordinatorLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var showWindow: MaterialSwitch
    private lateinit var showNotification: MaterialSwitch
    private lateinit var useAccessibility: MaterialSwitch

    private var monitoringService: PackageMonitoringService? = null
    private var isServiceBound = false

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        DatabaseUtil.showNotification = isGranted
        showNotification.isChecked = isGranted
    }

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

    private val popupListener = object : PopupStateListener {
        override fun onPopupShown() = refreshWindowSwitch()
        override fun onPopupDismissed() = refreshWindowSwitch()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        EdgeToEdge.enable(this)
        setContentView(R.layout.activity_main)

        baseView = findViewById(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(baseView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        registerListeners()
        checkForUpdateIfAllowed()
        DatabaseUtil.displayWidth = getScreenWidth()

        toolbar = findViewById(R.id.topAppBar)
        showWindow = findViewById(R.id.show_window)
        showNotification = findViewById(R.id.show_notification)
        useAccessibility = findViewById(R.id.use_accessibility)
        val downloadAccessibility = findViewById<Button>(R.id.download_accessibility)
        val configureWidth = findViewById<Button>(R.id.configure_width)
        val autostartLayout = findViewById<LinearLayout>(R.id.autostart_layout)
        val autostartDivider = findViewById<MaterialDivider>(R.id.autostart_divider)
        val allowAutostart = findViewById<Button>(R.id.allow_autostart)

        setupSwitches()
        setupToolbarMenu()
        downloadAccessibility.setOnClickListener { showGlobalVersionDownloadDialog() }
        configureWidth.setOnClickListener { showConfigureWidthDialog() }

        setupAutostartSection(autostartLayout, autostartDivider, allowAutostart)

        if (DatabaseUtil.isFirstRun) {
            showAutoUpdatePolicyDialog()
        }

        if (handleQsTileIntent(intent)) {
            moveTaskToBack(true)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (handleQsTileIntent(intent)) {
            moveTaskToBack(true)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshMenu()
        refreshWindowSwitch()
        refreshNotificationSwitch()
        refreshAccessibilitySwitch()
    }

    override fun onDestroy() {
        PopupManager.removeListener(popupListener)
        super.onDestroy()
    }

    // -- Setup helpers --

    private fun registerListeners() {
        PopupManager.addListener(popupListener)
    }

    private fun setupSwitches() {
        useAccessibility.setOnCheckedChangeListener { _, isChecked ->
            DatabaseUtil.useAccessibility = isChecked
        }

        showNotification.setOnCheckedChangeListener { _, isChecked ->
            DatabaseUtil.showNotification = isChecked
            if (isChecked && !isNotificationGranted()) {
                requestNotificationPermission()
            }
        }

        showWindow.setOnClickListener {
            if (!showWindow.isChecked) {
                PopupManager.dismiss()
                return@setOnClickListener
            }

            if (isSystemOverlayGranted() && isCommonPermissionsGranted()) {
                PopupManager.show(this, packageName, this::class.java.name)
                startPackageMonitoringService()
            } else {
                showWindow.isChecked = false
                requestSystemOverlayPermission()
                requestCommonPermissions()
            }
        }
    }

    private fun setupToolbarMenu() {
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.github -> {
                    openLink(REPO_URL)
                    true
                }
                R.id.check_update -> {
                    showToast(R.string.checking_for_update)
                    checkForUpdate(silent = false)
                    true
                }
                R.id.auto_update -> {
                    item.isChecked = !item.isChecked
                    DatabaseUtil.autoUpdate = item.isChecked
                    true
                }
                R.id.use_system_font -> {
                    item.isChecked = !item.isChecked
                    DatabaseUtil.useSystemFont = item.isChecked
                    showRestartAppDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupAutostartSection(
        layout: LinearLayout, divider: MaterialDivider, button: Button
    ) {
        if (BuildConfig.FLAVOR != "global") return

        if (AutostartUtil.isAutoStartPermissionAvailable(this)) {
            layout.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
            button.setOnClickListener { AutostartUtil.requestAutoStartPermission(this) }
        } else {
            layout.visibility = View.GONE
            divider.visibility = View.GONE
        }
    }

    // -- State queries --

    private fun handleQsTileIntent(intent: Intent): Boolean {
        if (!intent.getBooleanExtra(EXTRA_FROM_QS_TILE, false)) return false
        showWindow.isChecked = true
        showWindow.callOnClick()
        return PopupManager.isActive
    }

    private fun isCommonPermissionsGranted(): Boolean =
        !isAccessibilityNotStarted() && isUsageStatsGranted()

    private fun isSystemOverlayGranted(): Boolean =
        Settings.canDrawOverlays(this)

    private fun isNotificationGranted(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                checkSelfPermission(POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun isUsageStatsGranted(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName
        )
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            checkCallingOrSelfPermission(PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

    private fun isAccessibilityNotStarted(): Boolean =
        BuildConfig.FLAVOR == "global" &&
                DatabaseUtil.useAccessibility &&
                AccessibilityMonitoringService.instance == null

    // -- UI updates --

    private fun applyTheme() {
        setTheme(
            if (DatabaseUtil.useSystemFont) R.style.AppTheme_SystemFont
            else R.style.AppTheme
        )
    }

    private fun refreshMenu() {
        val customMenu = toolbar.menu.findItem(R.id.menu)?.subMenu ?: return
        customMenu.findItem(R.id.auto_update)?.isChecked = DatabaseUtil.autoUpdate
        customMenu.findItem(R.id.use_system_font)?.isChecked = DatabaseUtil.useSystemFont
    }

    private fun refreshWindowSwitch() {
        showWindow.isChecked = PopupManager.isActive
    }

    private fun refreshNotificationSwitch() {
        if (!isNotificationGranted()) {
            DatabaseUtil.showNotification = false
            showNotification.isChecked = false
            return
        }
        showNotification.isChecked = DatabaseUtil.showNotification
    }

    private fun refreshAccessibilitySwitch() {
        useAccessibility.isChecked = DatabaseUtil.useAccessibility
    }

    // -- Screen measurement --

    private fun getScreenWidth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = windowManager.currentWindowMetrics
            val insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    // -- Service management --

    private fun startPackageMonitoringService() {
        val intent = Intent(this, PackageMonitoringService::class.java)
        applicationContext.startService(intent)
        applicationContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    // -- Permission requests --

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(POST_NOTIFICATIONS)
        }
    }

    private fun requestSystemOverlayPermission() {
        if (Settings.canDrawOverlays(this)) return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.system_overlay_title)
            .setMessage(getString(R.string.system_overlay_description, getString(R.string.app_name)))
            .setPositiveButton(R.string.settings) { dialog, _ ->
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                    .setData(Uri.parse("package:$packageName"))
                startActivity(intent)
                dialog.dismiss()
            }
            .setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun requestCommonPermissions() {
        requestAccessibilityIfNeeded()
        requestUsageStatsIfNeeded()
    }

    private fun requestAccessibilityIfNeeded() {
        if (!isAccessibilityNotStarted()) return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.accessibility_permission_title)
            .setMessage(getString(R.string.accessibility_permission_description, getString(R.string.app_name)))
            .setPositiveButton(R.string.settings) { dialog, _ ->
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                dialog.dismiss()
            }
            .setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun requestUsageStatsIfNeeded() {
        if (isUsageStatsGranted()) return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.usage_access_title)
            .setMessage(getString(R.string.usage_access_description, getString(R.string.app_name)))
            .setPositiveButton(R.string.settings) { dialog, _ ->
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                dialog.dismiss()
            }
            .setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // -- Dialogs --

    private fun checkForUpdateIfAllowed() {
        if (DatabaseUtil.autoUpdate) {
            checkForUpdate(silent = true)
        }
    }

    private fun checkForUpdate(silent: Boolean) {
        try {
            Volley.newRequestQueue(this).add(buildVersionCheckRequest(silent))
        } catch (_: Exception) {
            handleUpdateError(silent)
        }
    }

    private fun buildVersionCheckRequest(silent: Boolean): JsonObjectRequest {
        return JsonObjectRequest(
            GET, "$API_URL/releases/latest", null,
            { response ->
                try {
                    processUpdateResponse(response, silent)
                } catch (_: Exception) {
                    handleUpdateError(silent)
                }
            },
            { handleUpdateError(silent) }
        ).apply {
            setShouldRetryConnectionErrors(true)
            setShouldCache(false)
        }
    }

    private fun handleUpdateError(silent: Boolean) {
        if (!silent) {
            showToast(R.string.update_check_failed)
            openLink("$REPO_URL/releases/latest")
        }
    }

    private fun processUpdateResponse(response: JSONObject, silent: Boolean) {
        val tag = response.getString("tag_name")
        val serverVersion = tag.replace(Regex("[^0-9]"), "").toInt()
        val currentVersion = BuildConfig.VERSION_NAME.replace(Regex("[^0-9]"), "").toInt()

        if (serverVersion > currentVersion) {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.update_available)
                .setMessage(getString(R.string.new_version_available, tag))
                .setPositiveButton(R.string.download) { dialog, _ ->
                    openLink("$REPO_URL/releases/tag/$tag")
                    dialog.dismiss()
                }
                .setNeutralButton(R.string.later) { dialog, _ -> dialog.dismiss() }
                .show()
        } else if (!silent) {
            showToast(R.string.already_using_latest)
        }
    }

    private fun showAutoUpdatePolicyDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.auto_update_check)
            .setMessage(R.string.auto_update_desc)
            .setPositiveButton(R.string.enable) { dialog, _ ->
                dialog.dismiss()
                DatabaseUtil.autoUpdate = true
                refreshMenu()
            }
            .setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setOnDismissListener { DatabaseUtil.isFirstRun = false }
            .show()
    }

    private fun showRestartAppDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.restart_required)
            .setMessage(R.string.restart_app)
            .setPositiveButton(R.string.restart) { dialog, _ ->
                dialog.dismiss()
                if (PopupManager.isActive) {
                    PopupManager.dismiss()
                }
                finishAndRemoveTask()
            }
            .setNeutralButton(R.string.later) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showGlobalVersionDownloadDialog() {
        val message = HtmlCompat.fromHtml(
            getString(R.string.global_version_description),
            HtmlCompat.FROM_HTML_MODE_COMPACT
        )
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.global_version)
            .setMessage(message)
            .setPositiveButton(R.string.download) { dialog, _ ->
                dialog.dismiss()
                openLink("$REPO_URL/releases/latest")
            }
            .setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showConfigureWidthDialog() {
        val dialogView = layoutInflater.inflate(R.layout.layout_configure_width, null)
        val widthInput = dialogView.findViewById<TextInputLayout>(R.id.width)
        val helperText = dialogView.findViewById<TextView>(R.id.helper)

        val minWidth = 500
        val screenWidth = getScreenWidth()
        val userWidth = DatabaseUtil.userWidth

        if (userWidth != -1) {
            widthInput.editText?.setText(userWidth.toString())
        }
        helperText.text = getString(R.string.configure_width_help, minWidth, screenWidth)

        val alertDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.configure_width_title)
            .setView(dialogView)
            .setNeutralButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setPositiveButton(R.string.save, null)
            .create()

        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                handleWidthSave(widthInput, minWidth, screenWidth, alertDialog)
            }
        }

        alertDialog.show()
    }

    private fun handleWidthSave(
        widthInput: TextInputLayout, minWidth: Int, screenWidth: Int, dialog: AlertDialog
    ) {
        val input = widthInput.editText?.text?.toString()?.trim().orEmpty()

        if (input.isEmpty()) {
            DatabaseUtil.userWidth = -1
            dialog.dismiss()
            showToast(R.string.saved)
            return
        }

        val width = input.toIntOrNull() ?: return
        when {
            width < minWidth -> {
                widthInput.error = getString(R.string.low_width_error_msg, minWidth)
            }
            width > screenWidth -> {
                widthInput.error = getString(R.string.high_width_error_msg, screenWidth)
            }
            else -> {
                DatabaseUtil.userWidth = width
                dialog.dismiss()
                showToast(R.string.saved)
            }
        }
    }

    // -- Utilities --

    private fun openLink(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    private fun showToast(@StringRes message: Int) {
        Snackbar.make(baseView, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        const val EXTRA_FROM_QS_TILE = "from_qs_tile"
    }
}

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
package io.github.ratul.topactivity.extensions

import android.Manifest.permission.PACKAGE_USAGE_STATS
import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.android.material.snackbar.Snackbar
import io.github.ratul.topactivity.R
import io.github.ratul.topactivity.services.AccessibilityMonitoringService
import io.github.ratul.topactivity.utils.DatabaseUtil

fun AppCompatActivity.isSystemOverlayGranted() = Settings.canDrawOverlays(this)

fun AppCompatActivity.isNotificationGranted() =
    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            checkSelfPermission(POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

fun AppCompatActivity.isUsageStatsGranted(): Boolean {
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

fun AppCompatActivity.isAccessibilityNotStarted() =
    resources.getBoolean(R.bool.global_version) &&
            DatabaseUtil.useAccessibility &&
            AccessibilityMonitoringService.instance == null

fun AppCompatActivity.openLink(url: String) = startActivity(
    Intent(Intent.ACTION_VIEW, url.toUri())
)

fun AppCompatActivity.showMessage(@StringRes message: Int) = Snackbar.make(
    window.decorView.rootView, message,
    Snackbar.LENGTH_SHORT
).show()
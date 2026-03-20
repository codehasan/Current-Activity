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

fun AppCompatActivity.isCommonPermissionsGranted() =
    !isAccessibilityNotStarted() && isUsageStatsGranted() && isNotificationGranted()

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
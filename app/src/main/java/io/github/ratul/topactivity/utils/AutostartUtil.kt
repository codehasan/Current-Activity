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

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import java.util.Locale

object AutostartUtil {

    fun isAutoStartPermissionAvailable(context: Context): Boolean {
        val packageManager = context.packageManager
        return getAutostartIntents(context).any {
            packageManager.resolveActivity(it, PackageManager.MATCH_DEFAULT_ONLY) != null
        }
    }

    fun requestAutoStartPermission(context: Context) {
        val packageManager = context.packageManager
        for (intent in getAutostartIntents(context)) {
            if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return
                } catch (_: Exception) {
                    // Ignore crashes, move to next intent
                }
            }
        }
    }

    private fun getAutostartIntents(context: Context): Array<Intent> {
        val brand = "${Build.BRAND} ${Build.MANUFACTURER}".lowercase(Locale.ROOT)

        return when {
            brand.containsAny("oppo", "realme", "oneplus") -> oppoRealmeOnePlusIntents()
            brand.containsAny("xiaomi", "redmi", "poco", "blackshark") -> xiaomiIntents()
            brand.containsAny("vivo", "iqoo") -> vivoIntents()
            brand.containsAny("huawei", "honor") -> huaweiHonorIntents()
            brand.containsAny("transsion", "tecno", "infinix", "itel") -> tecnoInfinixIntents()
            brand.contains("samsung") -> samsungIntents()
            else -> arrayOf(
                classIntent("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"),
                Intent(Intent.ACTION_MAIN)
                    .setComponent(ComponentName("com.android.settings", "com.android.settings.Settings\$AccessLockSummaryActivity"))
                    .putExtra("packageName", context.packageName)
            )
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean =
        keywords.any { contains(it) }

    private fun xiaomiIntents() = arrayOf(
        classIntent("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
        actionIntent("miui.intent.action.OP_AUTO_START"),
    )

    private fun tecnoInfinixIntents() = arrayOf(
        classIntent("com.transsion.phonemaster", "com.cyin.himgr.autostart.AutoStartActivity"),
        actionIntent("com.cyin.himgr.applicationmanager.view.activities.AUTO_START_ACTIVITY"),
    )

    private fun oppoRealmeOnePlusIntents() = arrayOf(
        classIntent("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
        classIntent("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
        classIntent("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
        classIntent("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.FakeActivity"),
        classIntent("com.coloros.safecenter", "com.coloros.safecenter.permission.startupapp.StartupAppListActivity"),
        classIntent("com.coloros.safecenter", "com.coloros.safecenter.permission.startupmanager.StartupAppListActivity"),
        classIntent("com.coloros.safe", "com.coloros.safe.permission.startup.StartupAppListActivity"),
        classIntent("com.coloros.safe", "com.coloros.safe.permission.startupapp.StartupAppListActivity"),
        classIntent("com.coloros.safe", "com.coloros.safe.permission.startupmanager.StartupAppListActivity"),
        classIntent("com.coloros.safecenter", "com.coloros.safecenter.permission.startsettings"),
        classIntent("com.coloros.safecenter", "com.coloros.safecenter.permission.startupapp.startupmanager"),
        classIntent("com.coloros.safecenter", "com.coloros.safecenter.permission.startupmanager.startupActivity"),
        classIntent("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.startupapp.startupmanager"),
        classIntent("com.coloros.safecenter", "com.coloros.privacypermissionsentry.PermissionTopActivity.Startupmanager"),
        classIntent("com.coloros.safecenter", "com.coloros.privacypermissionsentry.PermissionTopActivity"),
        classIntent("com.coloros.safecenter", "com.coloros.safecenter.FakeActivity"),
    )

    private fun vivoIntents() = arrayOf(
        classIntent("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
        classIntent("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"),
        classIntent("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
        actionIntent("com.iqoo.secure.BGSTARTUPMANAGER"),
    )

    private fun huaweiHonorIntents() = arrayOf(
        classIntent("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
        classIntent("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"),
        classIntent("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.bootstart.BootStartActivity"),
        actionIntent("huawei.intent.action.HSM_STARTUPAPP_MANAGER"),
        actionIntent("huawei.intent.action.HSM_BOOTAPP_MANAGER"),
    )

    private fun samsungIntents() = arrayOf(
        classIntent("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"),
        classIntent("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity"),
        classIntent("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.ram.AutoRunActivity"),
        actionIntent("com.samsung.android.sm.ACTION_BATTERY"),
    )

    private fun classIntent(pkg: String, cls: String) =
        Intent().setComponent(ComponentName(pkg, cls))

    private fun actionIntent(action: String) =
        Intent(action).addCategory(Intent.CATEGORY_DEFAULT)
}

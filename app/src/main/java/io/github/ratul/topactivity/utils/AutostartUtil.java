package io.github.ratul.topactivity.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.Locale;

public class AutostartUtil {
    private static Intent[] getXiaomiIntents() {
        return new Intent[]{
                classIntent("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
                actionIntent("miui.intent.action.OP_AUTO_START"),
        };
    }

    private static Intent[] getTechnoInfinixIntents() {
        return new Intent[]{
                classIntent("com.transsion.phonemaster", "com.cyin.himgr.autostart.AutoStartActivity"),
                actionIntent("com.cyin.himgr.applicationmanager.view.activities.AUTO_START_ACTIVITY"),
        };
    }

    private static Intent[] getOppoRealmeOnePlusIntents() {
        return new Intent[]{
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
        };
    }

    private static Intent[] getVivoIntents() {
        return new Intent[]{
                classIntent("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
                classIntent("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"),
                classIntent("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
                actionIntent("com.iqoo.secure.BGSTARTUPMANAGER"),
        };
    }

    private static Intent[] getHuaweiHonorIntents() {
        return new Intent[]{
                classIntent("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
                classIntent("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"),
                classIntent("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.bootstart.BootStartActivity"),
                actionIntent("huawei.intent.action.HSM_STARTUPAPP_MANAGER"),
                actionIntent("huawei.intent.action.HSM_BOOTAPP_MANAGER"),
        };
    }

    private static Intent[] getSamsungIntents() {
        return new Intent[]{
                classIntent("com.samsung.android.lool", "com.samsung.android.sm.ui.battery.BatteryActivity"),
                classIntent("com.samsung.android.sm", "com.samsung.android.sm.ui.battery.BatteryActivity"),
                classIntent("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.ram.AutoRunActivity"),
                actionIntent("com.samsung.android.sm.ACTION_BATTERY"),
        };
    }

    private static Intent[] getAutostartIntentForBrands(Context context) {
        String brand = (Build.BRAND + " " + Build.MANUFACTURER).toLowerCase(Locale.ROOT);

        if (brand.contains("oppo") ||
                brand.contains("realme") ||
                brand.contains("oneplus")) {
            return getOppoRealmeOnePlusIntents();
        }

        if (brand.contains("xiaomi") ||
                brand.contains("redmi") ||
                brand.contains("poco") ||
                brand.contains("blackshark")) {
            return getXiaomiIntents();
        }

        if (brand.contains("vivo") ||
                brand.contains("iqoo")) {
            return getVivoIntents();
        }

        if (brand.contains("huawei") ||
                brand.contains("honor")) {
            return getHuaweiHonorIntents();
        }

        if (brand.contains("transsion") ||
                brand.contains("tecno") ||
                brand.contains("infinix") ||
                brand.contains("itel")) {
            return getTechnoInfinixIntents();
        }

        if (brand.contains("samsung")) {
            return getSamsungIntents();
        }

        // LetV and LG
        return new Intent[]{
                classIntent("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"),
                new Intent(Intent.ACTION_MAIN)
                        .setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity"))
                        .putExtra("packageName", context.getPackageName())
        };
    }

    public static boolean isAutoStartPermissionAvailable(Context context) {
        PackageManager packageManager = context.getPackageManager();

        for (Intent intent : getAutostartIntentForBrands(context)) {
            if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                return true;
            }
        }

        return false;
    }

    public static void requestAutoStartPermission(Context context) {
        PackageManager packageManager = context.getPackageManager();

        for (Intent intent : getAutostartIntentForBrands(context)) {
            if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                try {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                    return;
                } catch (Exception ignored) {
                    // Ignore crashes, move to next intent
                }
            }
        }
    }

    private static Intent classIntent(String pkg, String cls) {
        return new Intent().setComponent(new ComponentName(pkg, cls));
    }

    private static Intent actionIntent(String action) {
        return new Intent(action).addCategory(Intent.CATEGORY_DEFAULT);
    }
}
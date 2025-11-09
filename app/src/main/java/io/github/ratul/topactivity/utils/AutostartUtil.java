package io.github.ratul.topactivity.utils;

import static io.github.ratul.topactivity.App.showToast;
import static io.github.ratul.topactivity.utils.NullSafety.isNullOrEmpty;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

public class AutostartUtil {
    private static final ComponentName[] AUTOSTART_COMPONENTS = {
            // Xiaomi
            new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"),
            // LeTV
            new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity"),
            // Huawei, Honor
            new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"),
            new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity"),
            // Oppo
            new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"),
            new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"),
            new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"),
            // Vivo
            new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"),
            new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"),
            new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"),
            // Samsung
            new ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.ram.AutoRunActivity"),
            // HTC
            new ComponentName("com.htc.pitroad", "com.htc.pitroad.landingpage.activity.LandingPageActivity"),
            // Asus
            new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"),
    };

    public static boolean isAutoStartPermissionAvailable(Context context) {
        PackageManager packageManager = context.getPackageManager();

        for (ComponentName component : AUTOSTART_COMPONENTS) {
            Intent intent = new Intent()
                    .setComponent(component)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                return true;
            }
        }

        return !isNullOrEmpty(Build.BRAND) && Build.BRAND.equalsIgnoreCase("LG");
    }

    public static void requestAutoStartPermission(Context context) {
        PackageManager packageManager = context.getPackageManager();

        for (ComponentName component : AUTOSTART_COMPONENTS) {
            Intent intent = new Intent()
                    .setComponent(component)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                context.startActivity(intent);
                return;
            }
        }

        if (!isNullOrEmpty(Build.BRAND) && Build.BRAND.equalsIgnoreCase("LG")) {
            LG(context);
        }
    }

    private static void LG(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .putExtra("packageName", context.getPackageName())
                    .setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity"));
            context.startActivity(intent);
        } catch (Throwable ignored) {
            showToast(context, "Failed to open Autostart");
        }
    }
}
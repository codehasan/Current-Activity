package com.ratul.topactivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class SharedPrefsUtil {
    public static boolean isShowWindow(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("is_show_window", true);
    }
    
    public static boolean hasBattery(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("hasBattery", false);
    }

    public static boolean setHasBattery(Context context, boolean bool) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.edit().putBoolean("hasBattery", bool).commit();
    }

    public static void setIsShowWindow(Context context, boolean isShow) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean("is_show_window", isShow).commit();
    }
    
    public static boolean setWatchingService(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("watching_service", false);
    }

    public static void setWatchingService(Context context, boolean added) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean("watching_service", added).commit();
    }
    
    public static boolean appInitiated(Context context) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    return sp.getBoolean("app_init", false);
        }

    public static void setAppInitiated(Context context, boolean added) {
    SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    sp.edit().putBoolean("app_init", added).commit();
        }
    
    public static boolean hasAccess(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("has_access", true);
    }

    public static void setHasAccess(Context context, boolean added) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean("has_access", added).commit();
    }

    public static boolean hasQSTileAdded(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("has_qs_tile_added", false);
    }

    public static void setQSTileAdded(Context context, boolean added) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean("has_qs_tile_added", added).commit();
    }

    public static boolean isNotificationToggleEnabled(Context context) {
        if (!hasQSTileAdded(context)) {
            return true;
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("is_noti_toggle_enabled", false);
    }

    public static void setNotificationToggleEnabled(Context context, boolean isEnabled) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        sp.edit().putBoolean("is_noti_toggle_enabled", isEnabled).commit();
    }
}

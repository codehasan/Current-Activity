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
package com.ratul.topactivity.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.ratul.topactivity.App;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class DatabaseUtil {
    private static SharedPreferences sp = App.getApp().getSharedPreferences("com.ratul.topactivity", 0);

    public static boolean isShowWindow(Context context) {
        return sp.getBoolean("is_show_window", false);
    }
    
    public static boolean hasBattery(Context context) {
        return sp.getBoolean("hasBattery", false);
    }

    public static boolean setHasBattery(Context context, boolean bool) {
        return sp.edit().putBoolean("hasBattery", bool).commit();
    }

    public static void setIsShowWindow(Context context, boolean isShow) {
        sp.edit().putBoolean("is_show_window", isShow).commit();
    }
    
    public static boolean setWatchingService(Context context) {
        return sp.getBoolean("watching_service", false);
    }

    public static void setWatchingService(Context context, boolean added) {
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
        return sp.getBoolean("has_access", true);
    }

    public static void setHasAccess(Context context, boolean added) {
        sp.edit().putBoolean("has_access", added).commit();
    }

    public static boolean hasQSTileAdded(Context context) {
        return sp.getBoolean("has_qs_tile_added", false);
    }

    public static void setQSTileAdded(Context context, boolean added) {
        sp.edit().putBoolean("has_qs_tile_added", added).commit();
    }

    public static boolean isNotificationToggleEnabled(Context context) {
        if (!hasQSTileAdded(context)) {
            return true;
        }
        return sp.getBoolean("is_noti_toggle_enabled", true);
    }

    public static void setNotificationToggleEnabled(Context context, boolean isEnabled) {
        sp.edit().putBoolean("is_noti_toggle_enabled", isEnabled).commit();
    }
}

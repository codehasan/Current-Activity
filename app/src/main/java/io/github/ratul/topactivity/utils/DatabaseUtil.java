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
package io.github.ratul.topactivity.utils;

import io.github.ratul.topactivity.App;

/**
 * Created by Wen on 16/02/2017.
 * Refactored by Ratul on 04/05/2022.
 */
public class DatabaseUtil {
    public static int getDisplayWidth() {
        return App.getInstance().getSharedPreferences()
                .getInt("width", 720);
    }

    public static void setDisplayWidth(int width) {
        App.getInstance().getSharedPreferences().edit()
                .putInt("width", width)
                .apply();
    }

    public static int getUserWidth() {
        return App.getInstance().getSharedPreferences()
                .getInt("user_width", -1);
    }

    public static void setUserWidth(int width) {
        App.getInstance().getSharedPreferences().edit()
                .putInt("user_width", width)
                .apply();
    }

    public static boolean isShowingWindow() {
        return App.getInstance().getSharedPreferences()
                .getBoolean("is_show_window", false);
    }

    public static void setShowingWindow(boolean bool) {
        App.getInstance().getSharedPreferences().edit()
                .putBoolean("is_show_window", bool).apply();
    }

    public static boolean useAccessibility() {
        return App.getInstance().getSharedPreferences()
                .getBoolean("has_access", false);
    }

    public static void setUseAccessibility(boolean bool) {
        App.getInstance().getSharedPreferences().edit()
                .putBoolean("has_access", bool).apply();
    }

    public static boolean isShowNotification() {
        return App.getInstance().getSharedPreferences()
                .getBoolean("show_notification", false);
    }

    public static void setShowNotification(boolean bool) {
        App.getInstance().getSharedPreferences().edit()
                .putBoolean("show_notification", bool).apply();
    }
}

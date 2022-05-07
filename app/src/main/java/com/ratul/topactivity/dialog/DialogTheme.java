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
package com.ratul.topactivity.dialog;

import android.content.*;
import android.content.res.*;

/**
 * Created by Ratul on 04/05/2022.
 */
public class DialogTheme {
    public static int background = 0xFFFFFFFF;
	public static int titleColor = 0xFF212121;
    public static int messageColor = 0xFF616161;
	public static int negativeButtonColor = 0xFFF5F5F5;
	public static int negativeTextColor = 0xFF616161;
	public static int positiveButtonColor = 0xFF6C63FF;
	public static int positiveTextColor = 0xFFFFFFFF;
	public static int strokeColor = 0xFFE0E0E0;
	public static int pressedColor = 0xFFE0E0E0;
    public static double round = 15;

	public static void setupColors(Context context, int theme) {
        switch (theme) {
            case FancyDialog.DARK_THEME :
                DialogTheme.setupDarkColors(); break;
            case FancyDialog.LIGHT_THEME :
                DialogTheme.setupLightColors(); break;
            case FancyDialog.DRACULA_THEME :
                DialogTheme.setupDraculaColors(); break;
            case FancyDialog.SUCCESS_THEME :
                DialogTheme.setupSuccessColors(); break;
            case FancyDialog.INFO_THEME :
                DialogTheme.setupInfoColors(); break;
            case FancyDialog.WARNING_THEME :
                DialogTheme.setupWarningColors(); break;
            case FancyDialog.ERROR_THMEE :
                DialogTheme.setupErrorColors(); break;
            case FancyDialog.HOLO_THEME :
                DialogTheme.setupHoloColors(); break;
            default :
                if (isDark(context))
                    setupDraculaColors();
                else
                    setupLightColors();
        }
	}

	public static void setupLightColors() {
		background = 0xFFFFFFFF;
		titleColor = 0xFF212121;
		messageColor = 0xFF616161;
		negativeButtonColor = 0xFFF5F5F5;
		negativeTextColor = 0xFF616161;
		positiveTextColor = 0xFFFAFAFA;
		positiveButtonColor = 0xFF6C63FF;
		strokeColor = 0xFFE0E0E0;
        round = 15;
	}

	public static void setupDarkColors() {
		background = 0xFF2E3132;
		titleColor = 0xFFFFFFFF;
		messageColor = 0xFFFAFAFA;
		negativeButtonColor = 0xFF8F9296;
		negativeTextColor = 0xFFFAFAFA;
        positiveButtonColor = 0xFF6C63FF;
		positiveTextColor = 0xFFFAFAFA;
		strokeColor = 0xFF8F9999;
        round = 15;
	}
    
    public static void setupErrorColors() {
        background = 0xFFF6655A;
        titleColor = 0xFFFEF6F5;
        messageColor = 0xFFFEEBE9;
        negativeButtonColor = 0xFFF44336;
        negativeTextColor = 0xFFFDE4E3;
        positiveTextColor = 0xFFC4291E;
        positiveButtonColor = 0xFFC4271D;
        strokeColor = 0x11A3B0C9;
        round = 35;
	}
    
    public static void setupHoloColors() {
        background = 0xFF787885;
        titleColor = 0xFFF8F8FB;
        messageColor = 0xFFECECF1;
        negativeButtonColor = 0xFF5A5B6A;
        negativeTextColor = 0xFFEDEEF2;
        positiveTextColor = 0xFF393A47;
        positiveButtonColor = 0xFFF7F7FA;
        strokeColor = 0x11A3B0C9;
        round = 35;
	}
    
    public static void setupSuccessColors() {
        background = 0xFF65B168;
        titleColor = 0xFFFAFAFA;
        messageColor = 0xFFF1F8F2;
        negativeButtonColor = 0xFF43A047;
        negativeTextColor = 0xFFF8FBF8;
        positiveTextColor = 0xFF2C7C31;
        positiveButtonColor = 0xFFF5FAF5;
        strokeColor = 0x11A3B0C9;
        round = 35;
	}
    
    public static void setupWarningColors() {
        background = 0xFFDB9E35;
        titleColor = 0xFFFAFAFA;
        messageColor = 0xFFFEFEDC;
        negativeButtonColor = 0xFFCF8401;
        negativeTextColor = 0xFFFFFFFF;
        positiveTextColor = 0xFFCF8401;
        positiveButtonColor = 0xFFF5FAF5;
        strokeColor = 0x11C2C9D5;
        round = 35;
	}
    
    public static void setupDraculaColors() {
        background = 0xFF30303D;
        titleColor = 0xFFF9F9F9;
        messageColor = 0xFFB6B6B6;
        negativeButtonColor = 0xFF494954;
        negativeTextColor = 0xFFE9E9EB;
        positiveTextColor = 0xFF2E2E45;
        positiveButtonColor = 0xFFEAEAF0;
        strokeColor = 0x1144475f;
        round = 20;
	}
    
    public static void setupInfoColors() {
        background = 0xFF4F91FF;
        titleColor = 0xFFFAFAFA;
        messageColor = 0xFFEBF1FF;
        negativeButtonColor = 0xFF2979FF;
        negativeTextColor = 0xFFE1EBFF;
        positiveTextColor = 0xFF2E6CD4;
        positiveButtonColor = 0xFFF7F9FF;
        strokeColor = 0x11E0E0E0;
        round = 35;
	}

	private static boolean isDark(Context context) {
		int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
		switch (nightModeFlags) {
			case Configuration.UI_MODE_NIGHT_YES:
				return true;
			case Configuration.UI_MODE_NIGHT_NO:
				return false;
			case Configuration.UI_MODE_NIGHT_UNDEFINED:
				return false;
			default:
			    return true;
		}
	}
}

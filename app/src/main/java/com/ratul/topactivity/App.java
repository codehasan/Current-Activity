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
package com.ratul.topactivity;

import android.app.Application;
import com.ratul.topactivity.R;
import com.ratul.topactivity.model.CrashHandler;
import android.content.Context;
import java.io.File;
import android.app.Activity;
import com.ratul.topactivity.ui.MainActivity;
import android.content.Intent;
import com.ratul.topactivity.ui.CrashActivity;
import android.widget.Toast;

public class App extends Application {
    private static App sApp;
    private Thread.UncaughtExceptionHandler getHandler = Thread.getDefaultUncaughtExceptionHandler();
    
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        CrashHandler handleCrash = new CrashHandler(this, getHandler);
        handleCrash.init(this.getFilesDir());
    }
    
    public void gotoCrashActivity(Exception ex) {
        if (ex == null) {
            return;
        }
        Intent intent = new Intent(this, CrashActivity.class);
        intent.putExtra(CrashActivity.EXTRA_CRASH_INFO, ex.toString());
        startActivity(intent);
    }
    
    public void setSafeContentView(Activity activity, int layout) {
        try {
            activity.setContentView(layout);
        } catch (Exception e) {
            Toast.makeText(this, "Saving crash log", 0).show();
            gotoCrashActivity(e);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
    }

    public static App getApp() {
        return sApp;
    }

}

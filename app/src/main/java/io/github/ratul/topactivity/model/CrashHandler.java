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
package io.github.ratul.topactivity.model;

import io.github.ratul.topactivity.ui.*;
import android.app.*;
import java.text.*;
import java.io.*;
import android.content.pm.*;
import android.text.*;
import android.os.*;
import android.content.*;
import java.util.*;
import java.lang.Thread.UncaughtExceptionHandler;
import android.widget.Toast;

/**
 * Created by Ratul on 04/05/2022.
 */

public class CrashHandler implements UncaughtExceptionHandler {
    private UncaughtExceptionHandler DEFAULT;
    private Application mApp;
    private File crashDirectory;
    private String fullStackTrace, versionName;
    private long versionCode;

    public CrashHandler(Application app, UncaughtExceptionHandler defaultExceptionHandler) {
        mApp = app;
        DEFAULT = defaultExceptionHandler;

        try { 
            PackageInfo packageInfo = mApp.getPackageManager().getPackageInfo(mApp.getPackageName(), 0);
            versionName = packageInfo.versionName;
            versionCode = Build.VERSION.SDK_INT >= 28 ? packageInfo.getLongVersionCode()
                : packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException ignored) {
            ignored.printStackTrace();
        }
    }

    public void init(File crashDir) {
        crashDirectory = crashDir;
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread main, Throwable mThrowable) {
        if (tryUncaughtException(main, mThrowable) || DEFAULT == null) {
        
        try {
            Thread.sleep(1000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
        } else {
            DEFAULT.uncaughtException(main, mThrowable);
        }
    }

    private void showToast(String str, int length) {
        Toast.makeText(mApp, str, length).show();
    }

    private boolean tryUncaughtException(Thread thread, Throwable throwable) {
        if (throwable == null) {
            return false;
        } else {
            new Thread() {
                @Override
                public void run() {
                    Looper.prepare();
                    showToast("Saving Crash Log", 0);
                    Looper.loop();
                }
            }.start();
        }
        File crashFile = new File(crashDirectory, "crash.txt");
        long timestamp = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String time = format.format(new Date(timestamp));

        StringWriter sw = new StringWriter(); 
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        fullStackTrace = sw.toString();
        pw.close();

        StringBuilder sb = new StringBuilder();
        sb.append("*********************** Crash Head ***********************\n");
        sb.append("Time Of Crash : ").append(time).append("\n");
        sb.append("Device Manufacturer : ").append(Build.MANUFACTURER).append("\n");
        sb.append("Device Model : ").append(Build.MODEL).append("\n");
        sb.append("Android Version : ").append(Build.VERSION.RELEASE).append("\n");
        sb.append("Android SDK : ").append(Build.VERSION.SDK_INT).append("\n");
        sb.append("App VersionName : ").append(versionName).append("\n");
        sb.append("App VersionCode : ").append(versionCode).append("\n");
        sb.append("\n*********************** Crash Log ***********************");
        sb.append("\n").append(fullStackTrace);

        String errorLog = sb.toString();

        try {
            writeFile(crashFile, errorLog);
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        
        gotoCrashActiviy: {
            Intent intent = new Intent(mApp, CrashActivity.class);
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
            );
            intent.putExtra(CrashActivity.EXTRA_CRASH_INFO, errorLog);
            mApp.startActivity(intent);
        }
        
        return errorLog != null;
    }

    private void writeFile(File file, String content) throws IOException {
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            parentFile.mkdirs();
        }
        file.createNewFile();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(content.getBytes());
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



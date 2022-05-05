package crash;

import android.app.Application;
import com.willme.topactivity.R;

public class App extends Application {
    private static App sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        CrashHandler.init(this);
    }

    public static boolean isNightMode() {
        return getApp().getResources().getBoolean(R.bool.night_mode);
    }

    public static App getApp() {
        return sApp;
    }

}

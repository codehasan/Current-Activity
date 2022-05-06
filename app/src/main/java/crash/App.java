package crash;

import android.app.Application;
import com.ratul.topactivity.R;

public class App extends Application {
    private static App sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        CrashHandler.init(this);
    }

    public static App getApp() {
        return sApp;
    }

}

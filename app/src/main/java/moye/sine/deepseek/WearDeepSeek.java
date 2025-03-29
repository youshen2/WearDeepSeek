package moye.sine.deepseek;

import android.app.Application;
import android.content.Context;

public class WearDeepSeek extends Application {
    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        if (context == null && getApplicationContext() != null) {
            context = getApplicationContext();
        }
    }
}

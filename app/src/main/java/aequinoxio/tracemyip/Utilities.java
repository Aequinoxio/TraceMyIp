package aequinoxio.tracemyip;

import android.app.ActivityManager;
import android.content.Context;

class Utilities {
    private static final Utilities ourInstance = new Utilities();

    public boolean isMyServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    static Utilities getInstance() {
        return ourInstance;
    }

    private Utilities() {
    }
}

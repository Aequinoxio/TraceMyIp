package aequinoxio.tracemyip;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;


public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.

        if (!Utilities.getInstance().isMyServiceRunning(context,LogIp.class)) {
            Intent intentService = new Intent(context, LogIp.class);
            context.startService(intentService);
        }
    }
}

package aequinoxio.tracemyip;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import static aequinoxio.tracemyip.Constants.LOAD_IP_JOB_ID;


public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.

        /* Utilizzo il jobservice
        if (!Utilities.getInstance().isMyServiceRunning(context,LogIp.class)) {
            Intent intentService = new Intent(context, LogIp.class);
            context.startService(intentService);
        }
        */

        // Check se il jobService è già in esecuzione
        if (!Utilities.getInstance().isMyJobServiceRunning(context,Constants.LOAD_IP_JOB_ID)) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            ComponentName componentName = new ComponentName(context, LogIp.class);
            JobInfo.Builder jBuilder = new JobInfo.Builder(LOAD_IP_JOB_ID, componentName);
            jBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
            jBuilder.setOverrideDeadline(5000);

            jobScheduler.schedule(jBuilder.build());
        }

        NetworkState networkState = NetworkState.getInstance(context);
        networkState.updateState();

        Utilities.getInstance().generateNotification(context);
    }
}

package aequinoxio.tracemyip;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

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

    public boolean isMyJobServiceRunning (Context context, int JOB_ID){
        JobScheduler scheduler = (JobScheduler) context.getSystemService( Context.JOB_SCHEDULER_SERVICE ) ;

        boolean hasBeenScheduled = false ;

        for ( JobInfo jobInfo : scheduler.getAllPendingJobs() ) {
            if ( jobInfo.getId() == JOB_ID ) {
                hasBeenScheduled = true ;
                break ;
            }
        }

        return hasBeenScheduled ;
    }


    static Utilities getInstance() {
        return ourInstance;
    }

    public Notification generateNotification(Context context){
        //Log.v("LogIP","GenerateNotification");


//Create a PendingIntent to do something when the user clicks on the Notification
//Normally this would be something in your own app
        Intent intent = new Intent(context,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.setData(Uri.parse("http://www.stackoverflow.com"));
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

//Create the Notification
        String testo=NetworkState.getInstance(context).getLastestExternalIP();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, context.getString(R.string.channel_ID));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Constants.getInstance().createNotificationChannel(context);

            // notificationBuilder = new Notification.Builder(this, "");
        } else{
            //notificationBuilder = new Notification.Builder(this);
            //notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        //Notification notification = new Notification.Builder(this)
        Notification notification = notificationBuilder.setContentTitle("Trace My Ip")
                //.setTicker("Traccia il mio IP")   // Accessibilità
                .setShowWhen(true)
                //.setSubText("----")               // Linea di testo in basso oltre il separatore
                .setContentIntent(pi)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentText(testo)
                .setTicker(testo)
                .setSmallIcon(R.drawable.ic_notification)
                // .setStyle(new Notification.BigTextStyle().bigText(testo))
                .build();
        //.setLargeIcon(aBitmap)

//Display the Notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        //NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // nm.notify(Constants.NOTIFICATION_ID, notification);  //ID_HELLO_WORLD is a int ID
        //NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        notificationManager.notify(Constants.NOTIFICATION_ID, notification);
        return notification;
    }


    private Utilities() {
    }
}

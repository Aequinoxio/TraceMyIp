package aequinoxio.tracemyip;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

// Servizio per gestire le notifiche ed i cambi di stati della rete
public class LogIp extends JobService {
    private BroadcastReceiver broadcastReceiver;
    private static final String TAG = LogIp.class.getSimpleName();


    public LogIp() {
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //generateNotification();
                Utilities.getInstance().generateNotification(LogIp.this);
                Log.d(TAG ,"OnBroadcastReceiver");
                jobFinished(params,true);
            }
        };

        // Ogni qualvolta c'è un cambio di stato nelle interfacce aggiorno la notifica
        IntentFilter intentFilter = new IntentFilter(Constants.NETWORK_AVAILABLE_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        NetworkState networkState = NetworkState.getInstance(this);
        networkState.updateState();

        Log.d(TAG ,"OnStartJob");

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
        Log.d(TAG ,"OnStopJob");
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    /* Vecchio codice pre-JobService
    @Override
    public void onCreate() {
        super.onCreate();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                generateNotification();
            }
        };

        // Ogni qualvolta c'è un cambio di stato nelle interfacce aggiorno la notifica
        IntentFilter intentFilter = new IntentFilter(Constants.NETWORK_AVAILABLE_INTENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter);

        //startForeground(Constants.NOTIFICATION_ID,generateNotification());
    }
    */
/*
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //generateNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }
*/
//    @Override
//    public IBinder onBind(Intent intent) {
//        // Return the communication channel to the service if needed
//        return null; // Nessun binding
//    }

   /*
   // Sostituito con un metodo analogo nella classe Utilities
    private Notification generateNotification(){
        //Log.v("LogIP","GenerateNotification");


//Create a PendingIntent to do something when the user clicks on the Notification
//Normally this would be something in your own app
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.setData(Uri.parse("http://www.stackoverflow.com"));
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

//Create the Notification
        String testo=NetworkState.getInstance(this).getLastestExternalIP();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.channel_ID));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Constants.getInstance().createNotificationChannel(this);

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
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        //NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
       // nm.notify(Constants.NOTIFICATION_ID, notification);  //ID_HELLO_WORLD is a int ID
        //NotificationManagerCompat nm = NotificationManagerCompat.from(this);
        notificationManager.notify(Constants.NOTIFICATION_ID, notification);
        return notification;
    }
*/

}

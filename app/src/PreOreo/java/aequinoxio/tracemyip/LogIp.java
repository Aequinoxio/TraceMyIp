package aequinoxio.tracemyip;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

// Servizio per gestire le notifiche
public class LogIp extends Service {
    BroadcastReceiver broadcastReceiver;

    public LogIp() {
    }

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

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        generateNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // Return the communication channel to the service if needed
        return null; // Nessun binding
    }

    private void generateNotification(){
        //Log.v("LogIP","GenerateNotification");


//Create a PendingIntent to do something when the user clicks on the Notification
//Normally this would be something in your own app
        Intent intent = new Intent(this,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.setData(Uri.parse("http://www.stackoverflow.com"));
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

//Create the Notification
        String testo="Latest external ip: "+NetworkState.getInstance(this).getLastestExternalIP();

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
    }


}

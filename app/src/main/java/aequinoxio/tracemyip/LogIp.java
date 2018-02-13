package aequinoxio.tracemyip;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

public class LogIp extends Service {
    public LogIp() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        generateNotification();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
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
        Notification notification = new Notification.Builder(this)
                .setContentTitle("Trace My Ip")
                .setContentIntent(pi)
                .setAutoCancel(false)
                .setOngoing(true)
                //.setContentText(subject)
                .setSmallIcon(R.drawable.ic_notification)
                .build();
        //.setLargeIcon(aBitmap)

//Display the Notification
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(Constants.NOTIFICATION_ID, notification);  //ID_HELLO_WORLD is a int ID

    }
}

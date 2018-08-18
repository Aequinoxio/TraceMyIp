package aequinoxio.tracemyip;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

/**
 * Created by utente on 11/02/2018.
 */

public class Constants {
    public static final String NETWORK_AVAILABLE_INTENT = "aequinoxio.network.state";
    public static final String IS_NETWORK_AVAILABLE_DETAIL_INTENT = "aequinoxio.network.netavail";
    public static final String NETWORK_INTERFACE_NAME_DETAIL_INTENT = "aequinoxio.network.ifname";
    public static final String NETWORK_INTERFACE_IP_DETAIL_INTENT = "aequinoxio.network.ifip";
    public static final String INTENT_OBJECTS_LENGTH = "aequinoxio.network.iobl";
    public static final String INTENT_TIMESTAMP = "aequinoxio.network.ts";

    public static final String PREFERENCES_NAME="aequinoxio.prefs";
    public static final String PREFERENCES_PREF_KEY_EXTERNAL_IP ="EXTERNAL_IP";
    public static final String PREFERENCES_PREF_KEY_ONLY_IP = "ONLY_IP";
    public static final String PREFERENCES_PREF_KEY_DISTRO_IP = "DISTRO_IP_X_DATA";

    public static final String DIALOG_PARAM_DATA = "dialog.param.data";
    public static final String DIALOG_PARAM_IP = "dialog.param.ip";

    public static final String LOG_FILENAME="log.txt";
    public static final String FILENAME_GIORNALIERO = "export.csv";
    public static final String EXTERNAL_SD_SAVEPATH = Environment.getExternalStorageDirectory() + "/TraceMyIp";

    // File di destinazione dove verrà copiato il DB
    public static final File DB_destination = new File(Constants.EXTERNAL_SD_SAVEPATH, Constants.DBNAME);

    public static final int NOTIFICATION_ID = 123443;
    public static final int DIRECTORY_CHOOSER_INTENT_ID = 1238;

    public final static String DBNAME = "logIp.db";

    public final static int APP_WRITE_STORAGE_PERMISSION = 10011;
    public final static File DOWNLOAD_FOLDER = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);


    /////////////////////////////////////////////////////////////////////
    private static final Constants ourInstance = new Constants();
    static Constants getInstance() {
        return ourInstance;
    }

    private Constants() {
    }

    // Per compatibilità con OREO (8.0 - API 26)
    // From https://developer.android.com/training/notify-user/build-notification
    public void createNotificationChannel(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String CHANNEL_ID = context.getString(R.string.channel_ID);
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}

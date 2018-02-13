package aequinoxio.tracemyip;

/**
 * Created by utente on 11/02/2018.
 */

class Constants {
    public static final String NETWORK_AVAILABLE_INTENT = "aequinoxio.network.state";
    public static final String IS_NETWORK_AVAILABLE_DETAIL_INTENT = "aequinoxio.network.netavail";
    public static final String NETWORK_INTERFACE_NAME_DETAIL_INTENT = "aequinoxio.network.ifname";
    public static final String NETWORK_INTERFACE_IP_DETAIL_INTENT = "aequinoxio.network.ifip";
    public static final String INTENT_OBJECTS_LENGTH = "aequinoxio.network.iobl";
    public static final String INTENT_TIMESTAMP = "aequinoxio.network.ts";

    public static final String PREFERENCES_NAME="aequinoxio.prefs";
    public static final String PREFERENCES_PREF_KEY_EXTERNAL_IP ="EXTERNAL_IP";

    public static final String LOG_FILENAME="log.txt";

    public static final int NOTIFICATION_ID = 123443;

    private static final Constants ourInstance = new Constants();

    static Constants getInstance() {
        return ourInstance;
    }

    private Constants() {
    }
}

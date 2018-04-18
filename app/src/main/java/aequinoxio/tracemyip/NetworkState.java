package aequinoxio.tracemyip;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.Formatter;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import aequinoxio.tracemyip.DatabaseUtils.DataAdapter;
import aequinoxio.tracemyip.DatabaseUtils.DataRow;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by utente on 11/02/2018.
 */

class NetworkState implements NetworkUpdateCallback {
    private static final NetworkState ourInstance = new NetworkState();
    private static Timestamp timestamp;
    private boolean isConnected = false;      // Internet connection
    private boolean isAirplaneMode = false;
    private Context context = null;
    DataAdapter dataAdapter;

    static final String INSERT_VALUES = "INSERT INTO IP (ip,interface) values (?,?)";

    // Imposto il timestamp in ora locale
    static final String SELECT_VALUES_EXTERNAL = "SELECT datetime(timestamp, 'localtime') as timestamp,ip, interface FROM IP WHERE interface='External' ORDER BY timestamp DESC";
    static final String SELECT_VALUES_ALL = "SELECT datetime(timestamp, 'localtime') as timestamp ,ip, interface FROM IP ORDER BY timestamp DESC";
    static final String SELECT_VALUES_COLUMN_NAME = "timestamp, ip, interface";

    // Query per selezionare solo gli IP
    static final String SELECT_ONLYIP_EXTERNAL = "SELECT distinct ip FROM IP WHERE interface='External' ORDER BY ip ASC";
    static final String SELECT_ONLYIP_ALL = "SELECT distinct ip FROM IP ORDER BY ip ASC";
    static final String SELECT_ONLYIP_COLUMN_NAME = "ip";

    static final String SELECT_LASTEST_EXTERNAL = "SELECT datetime(timestamp, 'localtime') as timestamp,ip, interface FROM IP WHERE interface='External' ORDER BY timestamp DESC LIMIT 1";

    // Query da decidere...
    static final String SELECT_EXTERNAL_IP_BY_DAY_COUNT = "SELECT date(timestamp) AS data_semplice , ip, interface, COUNT( ip) AS counter FROM ip WHERE interface='External' GROUP BY data_semplice, ip, interface ORDER BY data_semplice DESC, ip ASC";
    static final String SELECT_ALL_IP_BY_DAY_COUNT = "SELECT date(timestamp) AS data_semplice , ip, interface, COUNT( ip) AS counter FROM ip GROUP BY data_semplice, ip, interface ORDER BY timestamp DESC, ip ASC";
    static final String SELECT_EXTERNAL_IP_BY_DAY_COUNT_COLUMN_NAME = "data_semplice, ip, interface, counter";

    static final String SELECT_ALL_IP_COUNT = "SELECT ip, interface, COUNT( ip) AS counter FROM ip GROUP BY ip, interface ORDER BY  ip ASC";
    static final String SELECT_EXTERNAL_IP_COUNT = "SELECT ip, interface, COUNT( ip) AS counter FROM ip WHERE interface='External' GROUP BY ip, interface ORDER BY  ip ASC";
    static final String SELECT_IP_COUNT_COLUMN_NAME = "ip, interface, counter";

    // Query per la dialog di dettaglio
    static final String SELECT_IP_FROM_DAY = "SELECT DISTINCT ip from IP where date(timestamp)=? ORDER BY ip ASC";
    static final String SELECT_EXTERNAL_IP_FROM_DAY = "SELECT DISTINCT ip from IP where date(timestamp)=? and interface='External'ORDER BY ip ASC";
    static final String SELECT_DAYS_FROM_IP = "SELECT DISTINCT date(timestamp) as data_semplice from IP where ip=? ORDER BY data_semplice ASC";


    private List<NetworkInterface> networkInterfaces;
    private String ExternalIP;

    static NetworkState getInstance(Context context) {
        if (ourInstance.context == null) {
            ourInstance.context = context;
        }
        // Aggiorno il DB creandolo se necessario
        ourInstance.dataAdapter = new DataAdapter(context);
        ourInstance.dataAdapter.createDatabase();

        return ourInstance;
    }

    private NetworkState() {
        try {
            networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public List<String> getDataIpInDay(String day, boolean onlyExternal) {
        dataAdapter.open();
        String query;
        if (onlyExternal) {
            query = SELECT_EXTERNAL_IP_FROM_DAY;
        } else {
            query = SELECT_IP_FROM_DAY;
        }
        List<String> temp = dataAdapter.getValues(query, new String[]{day});

        dataAdapter.close();
        return temp;
    }

    public List<String> getDataDayForIp(String ip) {
        dataAdapter.open();
        List<String> temp = dataAdapter.getValues(SELECT_DAYS_FROM_IP, new String[]{ip});

        dataAdapter.close();
        return temp;

    }


    public List<DataRow> getData() {
        dataAdapter.open();
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean selectType = sharedPreferences.getBoolean(Constants.PREFERENCES_PREF_KEY_EXTERNAL_IP, true);
        boolean selectQuery = sharedPreferences.getBoolean(Constants.PREFERENCES_PREF_KEY_ONLY_IP, true);
        boolean selectDistroQuery = sharedPreferences.getBoolean(Constants.PREFERENCES_PREF_KEY_DISTRO_IP, true);

        String query;
        String columns;

        // TODO: In futuro va implementata una ottimizzazione per rendere più flessibile la generazione degli oggetti
        // e rivedere anche come l'oggetto DataRow deve funzionare
        if (selectDistroQuery) {
            if (selectQuery){
                query = selectType ? SELECT_EXTERNAL_IP_COUNT:SELECT_ALL_IP_COUNT ;
                columns = SELECT_IP_COUNT_COLUMN_NAME;

            }else {
                query = selectType ? SELECT_EXTERNAL_IP_BY_DAY_COUNT : SELECT_ALL_IP_BY_DAY_COUNT;
                columns = SELECT_EXTERNAL_IP_BY_DAY_COUNT_COLUMN_NAME;
            }
        } else {
            if (selectType) {
                if (selectQuery) {
                    query = SELECT_ONLYIP_EXTERNAL;
                    columns = SELECT_ONLYIP_COLUMN_NAME;
                } else {
                    query = SELECT_VALUES_EXTERNAL;
                    columns = SELECT_VALUES_COLUMN_NAME;
                }
            } else {
                if (selectQuery) {
                    query = SELECT_ONLYIP_ALL;
                    columns = SELECT_ONLYIP_COLUMN_NAME;
                } else {
                    query = SELECT_VALUES_ALL;
                    columns = SELECT_VALUES_COLUMN_NAME;
                }
            }
        }

        List<DataRow> temp = dataAdapter.getValues(query, columns);
        dataAdapter.close();
        return temp;
    }

    public String getLastestExternalIP() {
        String temp;
        dataAdapter.open();
        Cursor cursor = dataAdapter.getCursor(SELECT_LASTEST_EXTERNAL);
        if (cursor.getCount() > 0) {
            temp = String.format("%s (%s)", cursor.getString(cursor.getColumnIndex("ip")), cursor.getString(cursor.getColumnIndex("timestamp")));
        } else {
            temp = "";
        }
        dataAdapter.close();
        return temp;
    }

    /**
     * Aggiorna lo stato interrogando le interfacce e cercando l'ip esterno
     */
    public void updateState() {
        try {

            ExternalIpAsyncTask externalIpAsyncTask = new ExternalIpAsyncTask(this);
            externalIpAsyncTask.execute();
            // String ip = externalIpAsyncTask.get();  // Bloccante, commentato per rendere l'app più responsiva
            //callback(ip);

            //Log.e("IP:", ip); // Eliminare dopo il debug

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void callback(Object o) {
        ExternalIP = o.toString();
        timestamp = new Timestamp(System.currentTimeMillis());

        ArrayList<String> ifaces = new ArrayList<>();
        ArrayList<String> ifacesIP = new ArrayList<>();
        Intent networkStateIntent = new Intent(Constants.NETWORK_AVAILABLE_INTENT);

        networkStateIntent.putExtra(Constants.INTENT_OBJECTS_LENGTH, 1); //
        networkStateIntent.putExtra(Constants.IS_NETWORK_AVAILABLE_DETAIL_INTENT, true);
        networkStateIntent.putExtra(Constants.INTENT_TIMESTAMP, timestamp.toString());
        for (NetworkInterface networkInterface : networkInterfaces) {
            // Ogni interfaccia ha una lista di indirizzi. filtrare gli IPV4
            ifaces.add(networkInterface.getDisplayName());
            Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses();
            InetAddress inetAddress;
            String sAddr = "";
            try {
                if (networkInterface.isUp()) {
                    while (addressEnumeration.hasMoreElements()) {
                        inetAddress = addressEnumeration.nextElement();
                        sAddr = inetAddress.getHostAddress();
                        if (sAddr.indexOf(':') < 0) {
                            break;// Mi fermo al primno indirizzo IPv4 che trovo
                        }
                    }
                } else {
                    sAddr = "Unconnected";
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            ifacesIP.add(sAddr); // Comunque aggiungo un indirizzo anche vuoto
        }

        ifaces.add("External");
        ifacesIP.add(ExternalIP);

        dataAdapter.open();
        // Scrivo tutto sul DB. Il timestamp è quello della scrittura e non quello dell'avio del recupero dell'external ip
        for (int i = 0; i < ifaces.size(); i++) {
            dataAdapter.insertValues(INSERT_VALUES, ifacesIP.get(i), ifaces.get(i));
        }
        dataAdapter.close();

        networkStateIntent.putStringArrayListExtra(Constants.NETWORK_INTERFACE_NAME_DETAIL_INTENT, ifaces);
        networkStateIntent.putStringArrayListExtra(Constants.NETWORK_INTERFACE_IP_DETAIL_INTENT, ifacesIP);
        LocalBroadcastManager.getInstance(context).sendBroadcast(networkStateIntent);

    }


    public String getNetworkState() {
        String connType;
        // EditText edtLog = (EditText) context.findViewById(R.id.edtLog);
        String ip2 = "";
        WifiManager wm;

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        if (!isConnected) {
            connType = "No connection";
        } else {
            switch (activeNetwork.getType()) {
                case ConnectivityManager.TYPE_BLUETOOTH:
                    connType = "BlueTooth";
                    break;
                case ConnectivityManager.TYPE_ETHERNET:
                    connType = "Ethernet";
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    connType = "Mobile";
                    break;
                case ConnectivityManager.TYPE_MOBILE_DUN:
                    connType = "Mobile DUN";
                    break;
                case ConnectivityManager.TYPE_VPN:
                    connType = "VPN";
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    connType = "WIFI";
                    wm = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
                    if (wm != null && wm.isWifiEnabled()) {
                        int ipAddress = wm.getConnectionInfo().getIpAddress();
                        //   String ip = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
                        ip2 = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
                    } else {
                        ip2 = "Disabled";
                    }
                    break;
                case ConnectivityManager.TYPE_WIMAX:
                    connType = "WIMAX";
                    break;
                case ConnectivityManager.TYPE_DUMMY:
                    connType = "Dummy";
                    break;
                default:
                    connType = "Unknown";
            }
        }

        return connType;
    }
}
